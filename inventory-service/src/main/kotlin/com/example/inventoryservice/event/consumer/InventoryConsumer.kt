package com.example.inventoryservice.event.consumer

import event.InventoryReservedEvent
import event.OrderCreatedEvent
import io.micrometer.observation.annotation.Observed
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime.now

@Component
class InventoryConsumer(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    ) {
    private val logger = LoggerFactory.getLogger(InventoryConsumer::class.java)

    @Observed(
        name = "order.created",
        contextualName = "inventory-reserve",
    )
    @Transactional
    @KafkaListener(
        topics = ["order.created"],
        groupId = "inventory-service"
    )
    fun onOrderCreated(event: OrderCreatedEvent) {
        logger.info("재고 예약 시작 = orderId={} productId={}", event.orderId, event.productId)

        val next = InventoryReservedEvent(
            orderId = event.orderId,
            productId = event.productId,
            quantity = event.quantity,
            occurredAt = now()
        )

        kafkaTemplate.send("inventory.reserved", event.orderId.toString(), next)

        logger.info("재교 예약 완료, 이벤트 발행 = orderId={}", event.orderId)
    }
}