# Observability Lab

OpenTelemetry를 활용한 마이크로서비스 분산 추적 학습 프로젝트

## 구현 화면

## metric
<img width="1914" height="1005" alt="metric" src="https://github.com/user-attachments/assets/b6232c9c-677a-4a6e-a3ef-d9e1c11b9514" />

## trace
<img width="1912" height="1008" alt="trace" src="https://github.com/user-attachments/assets/2dc4e845-98c7-4cb4-8220-894c70ad4070" />

## log
<img width="1914" height="959" alt="logs" src="https://github.com/user-attachments/assets/a8e999fd-7d3e-449b-8764-869ad0a43499" />

## 프로젝트 개요

이 프로젝트는 Spring Boot와 Kotlin을 기반으로 한 마이크로서비스 아키텍처에서 OpenTelemetry를 사용하여 분산 추적(Distributed Tracing), 메트릭(Metrics), 로그(Logs)를 구현한 학습용 프로젝트입니다.

## 아키텍처

### 마이크로서비스 구성

- **order-service** (포트: 8081): 주문 생성 및 관리
- **inventory-service**: 재고 예약 및 관리
- **payment-service**: 결제 처리
- **shipping-service**: 배송 처리
- **common-observability**: 공통 observability 설정 모듈

### 이벤트 기반 아키텍처

각 서비스는 Kafka를 통해 이벤트를 발행하고 구독하여 비동기적으로 통신합니다:

1. `order.created` → 주문 생성 이벤트
2. `inventory.reserved` → 재고 예약 완료 이벤트
3. `payment.completed` → 결제 완료 이벤트
4. `shipping.created` → 배송 생성 이벤트

## 기술 스택

### Core
- Kotlin 2.2.21
- Spring Boot 4.0.3
- Java 17

### Observability
- OpenTelemetry (Traces, Metrics, Logs)
- Spring Boot Actuator
- Micrometer
- OpenTelemetry Logback Appender

### Messaging
- Apache Kafka
- Spring Kafka

### Build Tool
- Gradle (Kotlin DSL)

## 주요 기능

### 분산 추적 (Distributed Tracing)
- OpenTelemetry를 사용한 서비스 간 요청 추적
- Kafka 메시지 전파를 통한 트레이스 컨텍스트 전달
- OTLP(OpenTelemetry Protocol)를 통한 트레이스 데이터 수집

### 메트릭 (Metrics)
- Micrometer를 통한 애플리케이션 메트릭 수집
- Prometheus 엔드포인트 노출
- 공통 태그 설정 (application name)
- High cardinality 메트릭 필터링

### 로그 (Logs)
- OpenTelemetry Logback Appender를 통한 구조화된 로그
- TraceId, SpanId를 포함한 상관관계 로그
- OTLP를 통한 로그 수집

### Observability 어노테이션
`@Observed` 어노테이션을 사용하여 비즈니스 로직의 관찰 가능성 향상:
```kotlin
@Observed(
    name = "order.create",
    contextualName = "create-order"
)
@Transactional
fun createOrder(productId: Long, quantity: Int): Long
```

## 프로젝트 구조

```
tracing-lab/
├── order-service/          # 주문 서비스
├── inventory-service/      # 재고 서비스
├── payment-service/        # 결제 서비스
├── shipping-service/       # 배송 서비스
├── common-observability/   # 공통 observability 설정
│   ├── event/             # 이벤트 정의
│   └── config/            # Kafka, OpenTelemetry 설정
├── build.gradle.kts
└── settings.gradle.kts
```

## 설정

### OpenTelemetry 설정 (application.yml)

```yaml
management:
  tracing:
    sampling:
      probability: 1.0  # 모든 트레이스 샘플링
  opentelemetry:
    tracing:
      export:
        otlp:
          endpoint: http://localhost:4318/v1/traces
    logging:
      export:
        otlp:
          endpoint: http://localhost:4318/v1/logs
  otlp:
    metrics:
      export:
        url: http://localhost:4318/v1/metrics
```

### Kafka 설정

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    template:
      observation-enabled: true  # Producer 관찰 활성화
    listener:
      observation-enabled: true  # Consumer 관찰 활성화
```

## 실행 방법

### 사전 요구사항
- Java 17
- Kafka (localhost:9092)
- OpenTelemetry Collector (localhost:4318)

### 빌드
```bash
./gradlew build
```

### 각 서비스 실행
```bash
# Order Service
./gradlew :order-service:bootRun

# Inventory Service
./gradlew :inventory-service:bootRun

# Payment Service
./gradlew :payment-service:bootRun

# Shipping Service
./gradlew :shipping-service:bootRun
```

## 모니터링

### Actuator 엔드포인트
각 서비스는 다음 엔드포인트를 제공합니다:
- `/actuator/health` - 헬스 체크
- `/actuator/metrics` - 메트릭 조회
- `/actuator/prometheus` - Prometheus 형식 메트릭

### 로그 상관관계
모든 로그는 다음 형식으로 트레이스 정보를 포함합니다:
```
[app=order-service, traceId=xxx, spanId=yyy] 로그 메시지
```

## 학습 포인트

1. **분산 추적의 이해**: 마이크로서비스 환경에서 요청이 여러 서비스를 거치며 처리되는 과정을 추적
2. **이벤트 기반 통신 추적**: Kafka를 통한 비동기 메시지 전파 시 트레이스 컨텍스트 유지
3. **관찰 가능성(Observability) 구현**: 메트릭, 로그, 트레이스를 통합하여 시스템 가시성 확보
4. **OpenTelemetry 표준**: 벤더 중립적인 관찰 가능성 구현

## 라이선스

이 프로젝트는 학습 목적으로 작성되었습니다.
