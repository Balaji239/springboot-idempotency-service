package com.learn.idempotentservice.controller;

import com.learn.idempotentservice.annotations.Idempotent;
import com.learn.idempotentservice.dto.PaymentRequest;
import com.learn.idempotentservice.dto.PaymentSuccessResponse;
import com.learn.idempotentservice.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymeService) {
        this.paymentService = paymeService;
    }

    @PostMapping
    @Idempotent
    public ResponseEntity<PaymentSuccessResponse> save(@RequestBody @Valid PaymentRequest paymentRequest) {
        var response = paymentService.processPayment(paymentRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
