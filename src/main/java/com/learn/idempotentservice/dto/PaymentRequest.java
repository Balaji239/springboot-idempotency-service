package com.learn.idempotentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentRequest(
        @NotBlank
        String customerId,
        @NotBlank
        String fromAccountId,
        @NotBlank
        String toAccountId,
        @NotNull @Positive
        Double amount
) {}
