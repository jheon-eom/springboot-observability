package com.example.orderservice.presentation.web

import com.example.orderservice.application.OrderCreateService
import com.example.orderservice.presentation.web.dto.OrderCreateRequest
import com.example.orderservice.presentation.web.dto.OrderCreateResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/orders")
class OrderCreateController(
    private val orderCreateService: OrderCreateService
) {
    @PostMapping
    fun createOrder(
        @RequestBody request: OrderCreateRequest
    ): OrderCreateResponse {
        val orderId = orderCreateService.createOrder(request.productId, request.quantity)
        return OrderCreateResponse(orderId)
    }
}