package com.example.demo.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", columnDefinition = "ENUM('CREDIT_CARD','CASH','ECPAY') DEFAULT 'CASH'")
    private PaymentMethod method = PaymentMethod.CASH;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "ENUM('PAID','UNPAID','CANCELED') DEFAULT 'UNPAID'")
    private PaymentStatus status = PaymentStatus.UNPAID;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Constructor
    public Payment() {
    }

    public Payment(PaymentMethod method, PaymentStatus status, LocalDateTime createdAt) {
        this.method = method;
        this.status = status;
        this.createdAt = createdAt;
    }

    // -------------------------------------
    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    // -------------------------------------
    public enum PaymentMethod {
        CREDIT_CARD, CASH, ECPAY
    }

    public enum PaymentStatus {
        PAID, UNPAID, CANCELED
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

}