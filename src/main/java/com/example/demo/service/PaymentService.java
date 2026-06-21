package com.example.demo.service;

import com.example.demo.entity.Order;
import com.example.demo.entity.Payment;
import com.example.demo.entity.Payment.PaymentMethod;
import com.example.demo.entity.Payment.PaymentStatus;
import com.example.demo.repository.PaymentRepository; // 導入 PaymentRepository

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 支付服務類別。
 * 結合了介面定義和實作，直接作為 Spring 服務元件。
 */
@Service // 將 @Service 註解直接加在類別上
public class PaymentService {


    @Autowired
    private PaymentRepository paymentRepository;
    

    /**
     * 根據訂單和支付方式創建新的支付記錄。
     *
     * @param order           關聯的訂單實體。
     * @param paymentMethod   支付方式 (例如 ECPAY)。
     * @return 新創建的 Payment 實體。
     */
    @Transactional
    public Payment createPayment(Order order, PaymentMethod paymentMethod) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setMethod(paymentMethod);
        payment.setStatus(PaymentStatus.UNPAID);
        payment.setCreatedAt(null); // 初始為空，待支付成功後更新

        return paymentRepository.save(payment);
    }

    /**
     * 根據支付記錄 ID 查找支付記錄。
     *
     * @param paymentId 支付記錄的唯一 ID。
     * @return 包含 Payment 實體的 Optional，如果找不到則為 Optional.empty()。
     */
    public Optional<Payment> findPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId);
    }

    /**
     * 更新支付記錄的狀態。
     *
     * @param paymentId 支付記錄的唯一 ID。
     * @param status    要更新為的支付狀態 (例如 PAID, CANCELED)。
     * @return 更新後的 Payment 實體，如果找不到則為 null。
     */
    @Transactional
    public Payment updatePaymentStatus(Long paymentId, PaymentStatus status) {
        Optional<Payment> optionalPayment = paymentRepository.findById(paymentId);
        if (optionalPayment.isPresent()) {
            Payment payment = optionalPayment.get();
            payment.setStatus(status);
            if (status == PaymentStatus.PAID) {
                payment.setCreatedAt(LocalDateTime.now());
            }
            return paymentRepository.save(payment);
        }
        System.err.println("無法更新 Payment 狀態：找不到 ID 為 " + paymentId + " 的支付記錄。");
        return null;
    }
    
    @Transactional
    public Payment updatePaymentMethod(Long paymentId, PaymentMethod method) {
    	Optional<Payment> optionalPayment = paymentRepository.findById(paymentId);
    	if (optionalPayment.isPresent()) {
    		Payment payment = optionalPayment.get();
    		payment.setMethod(method);
    		
    		return paymentRepository.save(payment);
    	}
    	System.err.println("無法更新 Payment 記錄：找不到 ID 為 " + paymentId + " 的付款方式。");
    	return null;
    }
}
