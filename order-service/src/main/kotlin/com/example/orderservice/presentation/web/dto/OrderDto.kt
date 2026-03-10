package com.example.orderservice.presentation.web.dto

data class OrderCreateRequest(
    val productId: Long,
    val quantity: Int
)

data class OrderCreateResponse(
    val orderId: Long
)