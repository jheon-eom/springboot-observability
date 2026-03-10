package com.example.orderservice.domain

class Order(
    val id: Long? = null,
    val productId: Long,
    val quantity: Int
)