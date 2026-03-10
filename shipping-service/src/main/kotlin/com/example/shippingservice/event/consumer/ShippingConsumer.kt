package com.example.shippingservice.event.consumer

import event.PaymentCompletedEvent
import event.ShippingCreatedEvent
import io.micrometer.observation.annotation.Observed
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime.now

@Component
class ShippingConsumer(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {
    private val logger = LoggerFactory.getLogger(ShippingConsumer::class.java)

    @Observed(
        name = "order.created",
        contextualName = "shipping-created"
    )
    @KafkaListener(
        topics = ["payment.completed"],
        groupId = "shipping-service"
    )
    fun onPaymentCompleted(event: PaymentCompletedEvent) {
        logger.info("배송 시작 = orderId={}", event.orderId)

        val next = ShippingCreatedEvent(
            orderId = event.orderId,
            address = "경기도 오산시 운암로 117-1",
            occurredAt = now()
        )

        kafkaTemplate.send("shipping.created", event.orderId.toString(), next)

        logger.info("배송 완료, 이벤트 발행 = orderId={} address={}", event.orderId, next.address)
    }
}