# ---------- build stage ----------
FROM gradle:8.14.0-jdk21 AS builder
WORKDIR /app

# 1. 의존성 캐싱
COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon

# 2. 소스 복사
COPY . .

# 3. 빌드
RUN gradle clean build -x test --no-daemon

# ---------- runtime stage ----------
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]