package config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer
import org.springframework.kafka.support.serializer.JacksonJsonSerializer
import org.springframework.util.backoff.FixedBackOff

/**
 * 공통 Kafka 설정
 *
 * 주요 특징:
 * - JSON 기반 직렬화/역직렬화
 * - Observability 활성화 (분산 추적)
 * - 멱등성 Producer (중복 메시지 방지)
 * - 에러 핸들링 및 재시도
 * - 단일 Factory로 모든 이벤트 타입 처리
 *
 * 토픽 관리:
 * - Kafka Broker의 auto.create.topics.enable=true로 자동 생성
 * - 또는 Terraform/Helm 등 인프라 도구로 관리 권장
 */
@EnableKafka
@Configuration
class KafkaConfig(
    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
) {

    /**
     * Producer Factory 설정
     * - 멱등성 활성화로 정확히 한 번(exactly-once) 전송 보장
     * - JSON 직렬화로 타입 안정성 확보
     */
    @Bean
    fun producerFactory(): ProducerFactory<String, Any> {
        val props = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JacksonJsonSerializer::class.java,

            // 멱등성 설정 (실무 필수)
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to true,
            ProducerConfig.ACKS_CONFIG to "all",

            // 타임아웃 및 재시도
            ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG to 5000,
            ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG to 10000,
            ProducerConfig.RETRIES_CONFIG to 3,
            ProducerConfig.RETRY_BACKOFF_MS_CONFIG to 1000,

            // 성능 최적화
            ProducerConfig.BATCH_SIZE_CONFIG to 16384,
            ProducerConfig.LINGER_MS_CONFIG to 10,
            ProducerConfig.COMPRESSION_TYPE_CONFIG to "snappy",
        )
        return DefaultKafkaProducerFactory(props)
    }

    /**
     * KafkaTemplate 설정
     * - Observability 활성화로 분산 추적 지원
     */
    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, Any>): KafkaTemplate<String, Any> {
        return KafkaTemplate(producerFactory).apply {
            setObservationEnabled(true)  // 분산 추적 활성화
        }
    }

    /**
     * Consumer Factory 설정
     * - 제네릭 타입으로 모든 이벤트 타입 지원
     * - ErrorHandlingDeserializer로 역직렬화 오류 처리
     * - TYPE_MAPPINGS로 토픽별 이벤트 타입 매핑
     */
    @Bean
    fun consumerFactory(): ConsumerFactory<String, Any> {
        val jsonDeserializer = JacksonJsonDeserializer<Any>().apply {
            addTrustedPackages("*")
            setUseTypeHeaders(true)  // Producer가 보낸 타입 정보 사용
        }

        val props = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",

            // 타임아웃 설정
            ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG to 5000,
            ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG to 5000,
            ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG to 10000,
            ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG to 3000,

            // Consumer 안정성
            ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG to 300000,  // 5분
            ConsumerConfig.MAX_POLL_RECORDS_CONFIG to 500,
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,  // 수동 커밋 (정확성)
        )

        return DefaultKafkaConsumerFactory(
            props,
            StringDeserializer(),
            ErrorHandlingDeserializer(jsonDeserializer)  // 에러 핸들링
        )
    }

    /**
     * Kafka Listener Container Factory 설정
     * - 모든 Consumer가 공통으로 사용
     * - @KafkaListener의 groupId로 Consumer Group 구분
     */
    @Bean
    fun kafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<String, Any>
    ): ConcurrentKafkaListenerContainerFactory<String, Any> {
        return ConcurrentKafkaListenerContainerFactory<String, Any>().apply {
            setConsumerFactory(consumerFactory)

            // Observability 활성화
            containerProperties.isObservationEnabled = true

            // 에러 핸들링: 최대 3회 재시도, 1초 간격
            setCommonErrorHandler(
                DefaultErrorHandler(
                    FixedBackOff(1000L, 3L)
                )
            )

            // ACK 모드: 수동 (트랜잭션과 함께 사용)
            containerProperties.ackMode = ContainerProperties.AckMode.RECORD
        }
    }
}