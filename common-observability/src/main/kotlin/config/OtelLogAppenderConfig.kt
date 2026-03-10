package config

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration

@Configuration
class OtelLogAppenderConfig(
    private val openTelemetry: OpenTelemetry
) {
    @PostConstruct
    fun installAppender() {
        OpenTelemetryAppender.install(openTelemetry)
    }
}