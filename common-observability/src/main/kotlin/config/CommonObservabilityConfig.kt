package config

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.config.MeterFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.micrometer.metrics.autoconfigure.MeterRegistryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CommonObservabilityConfig {
    @Bean
    fun commonTagsCustomizer(
        @Value("\${spring.application.name}") appName: String
    ): MeterRegistryCustomizer<MeterRegistry> {
        return MeterRegistryCustomizer {
            it.config().commonTags("application", appName)
        }
    }

    @Bean
    fun denyHighCardinalityNoise(): MeterFilter {
        return MeterFilter.denyNameStartsWith("process.files")
    }
}