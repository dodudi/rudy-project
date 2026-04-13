# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 참고 자료

Spring Authorization Server 관련 클래스·패키지를 조회할 때는 아래 공식 API 문서를 참고한다.

- **Spring Authorization Server API Javadoc**: https://docs.spring.io/spring-authorization-server/docs/current/api/org/springframework

> 패키지 경로나 클래스 시그니처가 불확실할 때 반드시 위 문서를 먼저 확인한다.  
> 특히 Spring Boot 4.x / Spring Security 7.x 환경에서는 패키지 위치가 이전 버전과 다를 수 있다.

---

## Commands

```bash
# Build
./gradlew build

# Run the application
./gradlew bootRun

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.auth.SomeTest"

# Run a single test method
./gradlew test --tests "com.auth.SomeTest.methodName"

# Clean build
./gradlew clean build
```

## Architecture Overview

This is a **Spring Boot 4.0.5 / Java 21** OAuth2 Authorization Server. The root package is `com.auth`.

### Key technology choices

| Concern | Technology |
|---|---|
| Auth | Spring Security OAuth2 Authorization Server |
| Persistence | Spring Data JPA + Flyway migrations |
| DB (prod) | PostgreSQL |
| DB (dev/test) | H2 in-memory |
| Caching/sessions | Redis |
| API docs | SpringDoc OpenAPI (`/swagger-ui.html`) |
| Metrics | Micrometer + Prometheus |
| Boilerplate | Lombok |

### Library Versions

| Library | Version | Note |
|---|---|---|
| Java | 21 | LTS, toolchain 설정 |
| Spring Boot | 4.0.5 | Gradle plugin |
| Spring Dependency Management | 1.1.7 | Gradle plugin |
| SpringDoc OpenAPI | 3.0.2 | 명시적 버전 (BOM 미포함) |
| Spring Security OAuth2 Authorization Server | Spring Boot BOM 관리 | |
| Spring Data JPA | Spring Boot BOM 관리 | |
| Spring Data Redis | Spring Boot BOM 관리 | |
| Flyway | Spring Boot BOM 관리 | `flyway-database-postgresql` 포함 |
| H2 | Spring Boot BOM 관리 | 테스트/개발 전용 |
| PostgreSQL JDBC | Spring Boot BOM 관리 | 프로덕션 전용 |
| Micrometer Prometheus | Spring Boot BOM 관리 | |
| Lombok | Spring Boot BOM 관리 | |

> Spring Boot BOM 관리 항목은 `io.spring.dependency-management` 플러그인이 버전을 자동으로 결정한다.
> 버전 확인: `./gradlew dependencies`

### 설정 파일 구조 (Spring Profile)

| 파일 | 활성 프로파일 | 용도 |
|---|---|---|
| `application.yml` | 공통 (항상 로드) | local·prod 공통 설정 |
| `application-local.yml` | `local` | 개발 환경 설정 (H2, 로컬 Redis 등) |
| `application-prod.yml` | `prod` | 배포 환경 설정 (PostgreSQL, 운영 Redis 등) |

프로파일 활성화 방법:

```bash
# 로컬 개발
./gradlew bootRun --args='--spring.profiles.active=local'

# 운영 배포
java -jar app.jar --spring.profiles.active=prod
```

> `application-local.yml`은 `.gitignore`에 추가해 민감한 로컬 설정이 커밋되지 않도록 한다.

### Database migrations

Flyway manages schema changes. Migration scripts go in `src/main/resources/db/migration/` following the naming convention `V{version}__{description}.sql`. The `flyway-database-postgresql` dependency means scripts should be PostgreSQL-compatible; H2 compatibility mode is typically needed for tests.

### Testing

`@SpringBootTest`로 전체 컨텍스트를 로드하는 통합 테스트를 기본으로 사용한다. H2가 테스트 JPA 런타임이며, Redis 테스트는 embedded Redis 지원을 사용한다.

## Project Rules

### 응답 언어

Claude는 이 프로젝트에서 항상 **한국어**로 응답한다. 코드와 주석은 영어로 작성한다.

### 패키지 구조 (도메인 기반)

기술 계층이 아닌 비즈니스 도메인을 기준으로 패키지를 구성한다.

```
com.auth
├── user/          # 사용자 관리
├── client/        # OAuth2 클라이언트 등록
├── token/         # 토큰 발급·검증·회전
└── config/        # 보안·인프라 설정 (공통)
```

각 도메인 패키지 내부는 다음 레이어 서브패키지로 구분한다 (`user/` 기준):

```
user/
├── api/                    # Controller (HTTP 인터페이스)
├── application/            # Service (비즈니스 로직)
├── domain/                 # Entity + Repository
└── dto/                    # Request / Response record
```

`client/`, `token/` 도 동일한 구조를 따른다. `config/`는 서브패키지 없이 설정 클래스를 직접 배치한다.

### OAuth2 설계 원칙

이 서버가 지원하는 흐름과 정책:

- **Authorization Code + PKCE** — 프론트엔드 SPA / 모바일 앱 대상. `code_challenge_method=S256` 필수.
- **Client Credentials** — 서비스 간 M2M 통신 대상. scope 기반 접근 제어.
- **Refresh Token 회전** — Refresh Token 사용 시 매번 새 토큰을 발급하고 이전 토큰은 즉시 무효화한다.

새로운 흐름이나 예외를 추가할 때는 위 원칙과 충돌하지 않는지 확인한다.

### 코딩 컨벤션

- Lombok: `@RequiredArgsConstructor` + `final` 필드로 생성자 주입. `@Data` 사용 금지(엔티티에 특히).
- 엔티티: `@Entity` 클래스는 `domain/` 서브패키지에 위치하며, ID 타입은 `Long` 또는 `UUID`로 통일. 기본 애노테이션 구성은 아래와 같다.

```java
@Entity
@Getter
@Table(name = "...")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SomeEntity {

    @Builder
    private SomeEntity(...) {
        ...
    }
}
```
- DTO: 요청/응답 DTO는 `record` 타입을 우선 사용. Bean Validation 애노테이션은 DTO에만 적용.
- API 응답: 모든 엔드포인트는 `ResponseEntity<ApiResponse<T>>`를 반환한다. HTTP 상태 코드는 `ResponseEntity`로, 응답 body 형식은 `ApiResponse<T>`로 통일한다.

```java
public record ApiResponse<T>(
    String code,
    String message,
    T data
) {
    public static <T> ApiResponse<T> ok(T data) { ... }
    public static ApiResponse<Void> fail(ErrorCode code) { ... }
}

// 컨트롤러 사용 예
return ResponseEntity.ok(ApiResponse.ok(user));
return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(ErrorCode.USER_NOT_FOUND));
```
- 예외: 커스텀 예외는 `RuntimeException` 상속, 예외 메시지와 에러 코드는 enum으로 관리한다. 모든 예외는 `GlobalExceptionHandler`(`@RestControllerAdvice`)에서 일괄 처리하며, `ResponseEntity<ApiResponse<Void>>`로 응답한다.

```java
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND("U001", "사용자를 찾을 수 없습니다."),
    ...;

    private final String code;
    private final String message;
}

public class UserNotFoundException extends RuntimeException {
    private final ErrorCode errorCode;

    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND.getMessage());
        this.errorCode = ErrorCode.USER_NOT_FOUND;
    }
}
```
