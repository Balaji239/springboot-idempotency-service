package com.learn.idempotentservice.service;

import com.learn.idempotentservice.dto.PaymentRequest;
import com.learn.idempotentservice.dto.PaymentSuccessResponse;
import com.learn.idempotentservice.entity.Payment;
import com.learn.idempotentservice.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public PaymentSuccessResponse processPayment(PaymentRequest request) {
        // validation and custom logic...
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        String paymentId = UUID.randomUUID().toString();

        Payment payment = new Payment();
        payment.setAmount(request.amount());
        payment.setCustomerId(request.customerId());
        payment.setFromAccountId(request.fromAccountId());
        payment.setToAccountId(request.toAccountId());
        payment.setPaymentId(paymentId);
        payment.setStatus("COMPLETED");

        paymentRepository.save(payment);

        return new PaymentSuccessResponse(
                paymentId,
                request.customerId(),
                request.fromAccountId(),
                request.toAccountId(),
                request.amount(),
                "COMPLETED"
        );
    }
}
