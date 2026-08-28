package com.staytion.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.staytion.backend.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
	Payment findByOrderId(Long orderId);
}
