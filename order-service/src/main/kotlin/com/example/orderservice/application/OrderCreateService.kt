package com.example.orderservice.application

import com.example.orderservice.domain.Order
import com.example.orderservice.infrastructure.OrderRepository
import event.OrderCreatedEvent
import io.micrometer.observation.annotation.Observed
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class OrderCreateService(
    private val orderRepository: OrderRepository,
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {
    private val logger = LoggerFactory.getLogger(OrderCreateService::class.java)

    @Observed(
        name = "order.create",
        contextualName = "create-order"
    )
    @Transactional
    fun createOrder(productId: Long, quantity: Int): Long {
        logger.info("주문 생성 시작 = productId={} quantity={}", productId, quantity)

        val order = Order(
            productId = productId,
            quantity = quantity
        )

        val savedOrder = orderRepository.save(order)

        val event = OrderCreatedEvent(
            occurredAt = LocalDateTime.now(),
            orderId = savedOrder.id!!,
            productId = productId,
            quantity = quantity
        )

        kafkaTemplate.send("order.created", savedOrder.id.toString(), event)

        logger.info("주문 생성 완료, 이벤트 발행 = orderId={}", savedOrder.id)

        return savedOrder.id
    }
}