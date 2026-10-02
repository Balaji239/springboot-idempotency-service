package com.learn.idempotentservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "payments")
@Data
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String paymentId;

    private String customerId;

    private String fromAccountId;

    private String toAccountId;

    private Double amount;

    private String status;

}
