package com.example.demo.service;

import com.example.demo.entity.Order;
import com.example.demo.entity.Order.OrderStatus;
import com.example.demo.entity.Payment.PaymentStatus; // 導入 PaymentStatus
import com.example.demo.repository.OrderRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderCleanupService {

    @Autowired
    private OrderRepository orderRepository;


    @Autowired
    private PaymentService paymentService; // 注入 PaymentService

    @Scheduled(fixedRate = 30 * 60 * 1000) // 每 30 分鐘執行一次
    @Transactional // 確保整個操作（訂單和支付狀態更新）在一個事務中

    public void cancelExpiredOrders() {
        LocalDateTime expiredBefore = LocalDateTime.now().minusMinutes(15);

        List<Order> expiredOrders = orderRepository
                .findByStatusAndCreatedAtBefore(OrderStatus.PENDING, expiredBefore);

        if (expiredOrders.isEmpty()) {
            System.out.println("沒有找到過期訂單需要取消。");
            return;
        }

        System.out.println("正在自動取消過期訂單，共 " + expiredOrders.size() + " 筆...");

        for (Order order : expiredOrders) {
            // 1. 將訂單狀態設定為 CANCELED
            order.setStatus(OrderStatus.CANCELED);
            System.out.println("   - 訂單 ID " + order.getId() + " 狀態設定為 CANCELED。");

            // 2. 如果訂單有關聯的 Payment，也將其狀態設定為 CANCELED
            if (order.getPayment() != null && order.getPayment().getId() != null) {
                Long paymentId = order.getPayment().getId();
                try {
                    paymentService.updatePaymentStatus(paymentId, PaymentStatus.CANCELED);
                    System.out.println("     - 支付記錄 ID " + paymentId + " 狀態已更新為 CANCELED。");
                } catch (Exception e) {
                    // 記錄錯誤，但不阻止訂單本身的更新
                    System.err.println("     - ❌ 警告：更新支付記錄 ID " + paymentId + " 狀態失敗: " + e.getMessage());
                }
            } else {
                System.out.println("     - 訂單 ID " + order.getId() + " 沒有關聯的支付記錄或支付 ID 為空，跳過支付狀態更新。");
            }

            // TODO: 可加上釋放房間庫存、清除鎖房資料等邏輯
        }

        // 批量保存更新後的訂單
        orderRepository.saveAll(expiredOrders);

        System.out.println("✅ 已自動取消過期訂單：" + expiredOrders.size() + " 筆。");
    }
}
