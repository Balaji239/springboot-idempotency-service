package com.learn.idempotentservice.dto;

public record PaymentSuccessResponse(
        String paymentId,
        String customerId,
        String fromAccountId,
        String toAccountId,
        Double amount,
        String status
) {}
