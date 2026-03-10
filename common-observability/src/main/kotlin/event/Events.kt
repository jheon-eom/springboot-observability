package event

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

interface Event {
    val eventId: String
    val occurredAt: LocalDateTime
}

data class OrderCreatedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: LocalDateTime,
    val orderId: Long,
    val productId: Long,
    val quantity: Int,
): Event

data class InventoryReservedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: LocalDateTime,
    val orderId: Long,
    val productId: Long,
    val quantity: Int,
): Event

data class PaymentCompletedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: LocalDateTime,
    val orderId: Long,
    val amount: BigDecimal,
): Event

data class ShippingCreatedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: LocalDateTime,
    val orderId: Long,
    val address: String,
): Event