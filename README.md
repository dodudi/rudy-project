# Commerce Spring

마이크로서비스 기반 이커머스 플랫폼 — 백엔드 프로젝트

## 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.3, Spring Cloud 2025.0.0 |
| Web | Spring MVC, Spring WebFlux |
| Data | JPA, R2DBC, PostgreSQL, Redis |
| Messaging | Apache Kafka |
| Infrastructure | Docker Compose, Netflix Eureka, Spring Cloud Gateway |
| Build | Gradle |

---

## 서비스 구성

| 서비스 | 포트 | 역할 | 주요 기술 |
|---|---|---|---|
| gateway | 8080 | API 진입점, 로드밸런싱 라우팅 | Spring Cloud Gateway (WebFlux) |
| eureka-server | 8761 | 서비스 레지스트리 & 디스커버리 | Netflix Eureka Server |
| commerce | 8081 | 회원·상품·주문 도메인, Kafka 프로듀서/컨슈머 | Spring MVC, JPA |
| payment | 8082 | 결제 처리, 멱등성 제어, 결제 조회 | WebFlux, R2DBC, Redis |
| settlement | 8083 | 결제 결과 집계, 정산 배치 (구조 설계 단계) | Spring Batch, JPA, Redis |

---

## 시스템 아키텍처

```
Client (HTTP)
    │
    ▼
┌──────────────┐       ┌─────────────────────┐
│   Gateway    │──────▶│    Eureka Server     │
│   :8080      │       │       :8761          │
└──────┬───────┘       └─────────────────────┘
       │ lb://commerce
       ▼
┌──────────────┐
│   Commerce   │  회원 / 상품 / 주문
│   :8081      │  JPA · Outbox 패턴
└──────┬───────┘
       │ Kafka: payment.request
       ▼
┌──────────────┐
│   Payment    │  결제 처리 (WebFlux)
│   :8082      │  Redis 멱등성 · R2DBC
└──────┬───────┘
  ┌────┴────┐
  │  Kafka: payment.result
  ▼         ▼
Commerce  Settlement  정산 배치
(결과수신)  :8083       Spring Batch · Redis
```

### Kafka 토픽

| 토픽 | Producer | Consumer |
|---|---|---|
| `payment.request` | Commerce | Payment |
| `payment.result` | Payment | Commerce, Settlement |

---

## 구현 기능

### Commerce 서비스
- 회원 CRUD — 아이디·닉네임 기반 동적 필터링 조회 (JPA Specification)
- 상품 CRUD — 가격·재고 범위 필터링, 오버셀 방지 비관적 락(Pessimistic Lock)
- 주문 생성 — 복수 상품 동시 주문 시 productId 정렬로 데드락 방지
- Transactional Outbox 패턴 — 주문 저장과 Kafka 이벤트 발행의 원자성 보장
- Kafka 컨슈머 — `payment.result` 수신 후 주문 상태 업데이트

### Payment 서비스
- Kafka 컨슈머 — `payment.request` 수신 후 리액티브 파이프라인으로 결제 처리
- Redis 멱등성 처리 — `orderId` 키로 중복 결제 요청 차단 (TTL 24h)
- 결제 결과 발행 — `payment.result` 토픽에 성공/실패 이벤트 프로듀싱
- 결제 목록 조회 API — `GET /payments` (Flux 스트리밍 응답)
- WebFlux + R2DBC 전 구간 논블로킹 처리

---

## 기술적 의사결정

### 1. Transactional Outbox 패턴
주문 저장(DB)과 Kafka 이벤트 발행은 서로 다른 시스템이라 하나의 트랜잭션으로 묶는 것이 불가능합니다.
`outbox` 테이블을 도입해 주문 저장과 아웃박스 레코드 삽입을 **동일 DB 트랜잭션**으로 처리하고, 별도 스케줄러(5초 주기)가 미발행 레코드를 폴링해 Kafka에 발행합니다.

### 2. Payment 서비스 WebFlux + R2DBC
결제 처리는 DB I/O, Redis 조회가 중첩되는 I/O 집약 작업입니다. WebFlux + R2DBC로 전 구간 논블로킹 파이프라인을 구성해, 적은 수의 이벤트 루프 스레드로 높은 동시성을 처리합니다.

### 3. Pessimistic Lock + productId 정렬로 데드락 방지
복수 상품 동시 주문 시 트랜잭션 간 역방향 락 획득으로 데드락이 발생합니다. `@Lock(PESSIMISTIC_WRITE)`으로 재고 정합성을 보장하고, **productId 오름차순 정렬 후 락 획득**을 강제해 순환 대기 조건을 제거했습니다.

### 4. Redis 멱등성 처리로 중복 결제 방지
Kafka의 at-least-once 보장 특성상 동일 메시지가 두 번 이상 소비될 수 있습니다. `orderId`를 키로 Redis에 처리 여부를 저장하고, 이미 처리된 요청은 즉시 스킵해 exactly-once 처리를 달성합니다.

---

## 트러블슈팅

### Kafka `__TypeId__` 헤더로 인한 ClassNotFoundException
`JsonSerializer`가 발신 서비스의 전체 클래스명을 `__TypeId__` 헤더에 삽입해, 수신 서비스에서 클래스를 찾지 못하는 문제가 발생했습니다.

```yaml
# Producer
spring.kafka.producer.properties:
  spring.json.add.type.headers: false

# Consumer
spring.kafka.consumer.properties:
  spring.json.use.type.headers: false
  spring.json.value.default.type: com.payment.dto.event.PaymentRequestEvent
```

### Kafka 이중 직렬화 문제
`JsonSerializer`를 value-serializer로 설정한 상태에서 `objectMapper.writeValueAsString()`으로 미리 직렬화한 문자열을 전달해 이중 직렬화가 발생했습니다.
value-serializer를 `StringSerializer`로 교체하고, 프로듀서/컨슈머 모두 `ObjectMapper`로 수동 직렬화·역직렬화하도록 전 서비스 통일했습니다.

```java
// Producer
String json = objectMapper.writeValueAsString(event);
kafkaTemplate.send(topic, json);

// Consumer
PaymentRequestEvent event = objectMapper.readValue(message, PaymentRequestEvent.class);
```

### Kafka 오프셋 누적 — 잘못된 메시지 무한 재시도
개발 중 잘못된 형식의 메시지가 오프셋에 남아 컨슈머 재시작 시 역직렬화 실패 → 재시도를 반복했습니다.
개발 환경에서는 토픽 삭제 후 재생성으로 해결했으며, 운영 환경 대응 방안으로 `ErrorHandlingDeserializer` + Dead Letter Topic(DLT) 패턴을 정립했습니다.

### MemberSpecification 잘못된 fetch join
`OrderSpecification`에서 코드를 복붙하면서 `Member` 엔티티에 존재하지 않는 `root.fetch("member")`, `root.fetch("orderItems")` 블록이 포함되어 런타임 오류가 발생했습니다.
불필요한 fetch join 블록을 제거하고, 이후 Specification 작성 시 대상 엔티티의 연관관계를 먼저 확인하는 습관을 정립했습니다.

---

## 실행 방법

### 인프라 실행 (Docker Compose)
```bash
cd docker
docker-compose up -d
```

인프라 구성: Redis (6379), PostgreSQL (5432), Kafka (9092/9094)

### 서비스 빌드 및 실행
```bash
# 각 서비스 빌드
./gradlew :eureka-server:bootJar
./gradlew :gateway:bootJar
./gradlew :commerce:bootJar
./gradlew :payment:bootJar
./gradlew :settlement:bootJar

# 실행 순서: eureka → gateway → commerce/payment/settlement
java -jar eureka-server/build/libs/eureka-server.jar
java -jar gateway/build/libs/gateway.jar
java -jar commerce/build/libs/commerce.jar
java -jar payment/build/libs/payment.jar
java -jar settlement/build/libs/settlement.jar
```

---

## 패키지 구조

```
com.{service}
├── controller
├── service
├── repository
├── domain
└── dto
    ├── request
    ├── response
    └── event
```
