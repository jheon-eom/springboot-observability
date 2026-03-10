package com.example.orderservice.infrastructure

import com.example.orderservice.domain.Order
import org.springframework.stereotype.Repository

@Repository
class OrderRepository {
    private val storage = mutableMapOf<Long, Order>()

    fun save(newOrder: Order): Order {
        val newId = (storage.keys.maxOrNull() ?: 0L) + 1

        val order = Order(
            id = newId,
            productId = newOrder.productId,
            quantity = newOrder.quantity
        )

        storage[newId] = order

        return order
    }
}