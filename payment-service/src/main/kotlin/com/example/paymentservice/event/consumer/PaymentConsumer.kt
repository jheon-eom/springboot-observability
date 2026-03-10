package com.example.paymentservice.event.consumer

import event.InventoryReservedEvent
import event.PaymentCompletedEvent
import io.micrometer.observation.annotation.Observed
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDateTime.now

@Component
class PaymentConsumer(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {
    private val logger = LoggerFactory.getLogger(PaymentConsumer::class.java)

    @Observed(
        name = "order.created",
        contextualName = "payment-completed"
    )
    @KafkaListener(
        topics = ["inventory.reserved"],
        groupId = "payment-service"
    )
    fun onInventoryReserved(event: InventoryReservedEvent) {
        logger.info("결제 시작 = orderId={} ", event.orderId)

        val next = PaymentCompletedEvent(
            orderId = event.orderId,
            amount = BigDecimal.ZERO,
            occurredAt = now()
        )

        kafkaTemplate.send("payment.completed", event.orderId.toString(), next)

        logger.info("결제 완료, 이벤트 발행 = orderId={}", event.orderId)
    }
}