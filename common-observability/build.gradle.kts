plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

// 라이브러리 모듈이므로 bootJar 비활성화
tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-actuator")
    api("org.springframework.boot:spring-boot-starter-opentelemetry")
    api("org.springframework.kafka:spring-kafka")
    api("org.springframework.boot:spring-boot-starter-json")
    // OpenTelemetry Logback Appender
    implementation("io.opentelemetry.instrumentation:opentelemetry-logback-appender-1.0:2.25.0-alpha")
}