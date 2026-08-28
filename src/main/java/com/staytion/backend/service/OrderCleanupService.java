package com.staytion.backend.service;

import com.staytion.backend.entity.Order;
import com.staytion.backend.entity.Order.OrderStatus;
import com.staytion.backend.entity.Payment.PaymentStatus;
import com.staytion.backend.repository.OrderRepository;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderCleanupService {

    private static final Logger log = LoggerFactory.getLogger(OrderCleanupService.class);

    private final OrderRepository orderRepository;

    private final PaymentService paymentService;

    @Scheduled(fixedRate = 30 * 60 * 1000) // 每 30 分鐘執行一次
    @Transactional // 確保整個操作（訂單和支付狀態更新）在一個事務中

    public void cancelExpiredOrders() {
        LocalDateTime expiredBefore = LocalDateTime.now().minusMinutes(15);

        List<Order> expiredOrders = orderRepository
                .findByStatusAndCreatedAtBefore(OrderStatus.PENDING, expiredBefore);

        if (expiredOrders.isEmpty()) {
            log.debug("沒有找到過期訂單需要取消。");
            return;
        }

        log.info("正在自動取消過期訂單，共 {} 筆...", expiredOrders.size());

        for (Order order : expiredOrders) {
            // 1. 將訂單狀態設定為 CANCELED
            order.setStatus(OrderStatus.CANCELED);
            log.debug("訂單 ID {} 狀態設定為 CANCELED。", order.getId());

            // 2. 如果訂單有關聯的 Payment，也將其狀態設定為 CANCELED
            if (order.getPayment() != null && order.getPayment().getId() != null) {
                Long paymentId = order.getPayment().getId();
                try {
                    paymentService.updatePaymentStatus(paymentId, PaymentStatus.CANCELED);
                    log.debug("支付記錄 ID {} 狀態已更新為 CANCELED。", paymentId);
                } catch (Exception e) {
                    // 記錄錯誤，但不阻止訂單本身的更新
                    log.warn("更新支付記錄 ID {} 狀態失敗", paymentId, e);
                }
            } else {
                log.debug("訂單 ID {} 沒有關聯的支付記錄或支付 ID 為空，跳過支付狀態更新。", order.getId());
            }

            // TODO: 可加上釋放房間庫存、清除鎖房資料等邏輯
        }

        // 批量保存更新後的訂單
        orderRepository.saveAll(expiredOrders);

        log.info("已自動取消過期訂單：{} 筆。", expiredOrders.size());
    }
}
