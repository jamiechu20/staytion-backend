package com.example.demo.controller;

import java.time.LocalDate;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.response.OrderResponseDto;
import com.example.demo.entity.Order;
import com.example.demo.entity.Payment;
import com.example.demo.service.AdminOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admins/orders")
@Tag(name = "Admin Order Management", description = "後台訂單管理 API")
public class AdminOrderController {

    private final AdminOrderService service;

	@GetMapping
	@Operation(summary = "取得自己飯店的訂單", description = "根據登入的飯店擁有者（ownerId），以分頁形式取得所有所屬飯店的訂單資料", operationId = "getPagedOrdersByOwner")
	public Page<OrderResponseDto> getOrdersByOwnerPaged(@RequestParam Long ownerId,
			@ParameterObject Pageable pageable) {
		return service.getOrdersByHotelOwner(ownerId, pageable);
	}

    @PutMapping("/{id}/status")
    @Operation(summary = "更新訂單狀態", description = "根據訂單 ID 修改其狀態（例如：處理中、已完成、已取消等）", operationId = "updateOrderStatus")
    public OrderResponseDto updateStatus(@PathVariable Long id, @RequestParam Order.OrderStatus status) {
        return service.updateStatus(id, status);
    }

    @PutMapping("/{id}/payment-status")
    @Operation(summary = "更新訂單付款狀態", description = "根據訂單 ID 更新其付款狀態（PAID / UNPAID / CANCELED）", operationId = "updateOrderPaymentStatus")
    public OrderResponseDto updatePaymentStatus(@PathVariable Long id,
                                                @RequestParam Payment.PaymentStatus paymentStatus) {
        return service.updatePaymentStatus(id, paymentStatus);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "刪除訂單", description = "根據訂單 ID 刪除對應的訂單紀錄", operationId = "deleteOrderById")
    public void delete(@PathVariable Long id) {
        service.deleteOrder(id);
    }

	@GetMapping("/filter")
	@Operation(summary = "篩選訂單（狀態、日期、關鍵字、付款）", description = "可依據訂單狀態、建立時間區間、關鍵字（姓名/電話）、付款方式與付款狀態等條件進行訂單查詢（支援分頁）", operationId = "filterOrdersForAdmin")
	public Page<OrderResponseDto> filterOrders(@RequestParam(required = false) Order.OrderStatus status,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
			@RequestParam(required = false) String keyword, @RequestParam(required = false) Long hotelId,
			@RequestParam(required = false) Payment.PaymentMethod paymentMethod,
			@RequestParam(required = false) Payment.PaymentStatus paymentStatus,
			@RequestParam(required = true) Long currentUserId, @ParameterObject Pageable pageable) {
		return service.searchOrders(currentUserId, hotelId, status, start, end, keyword, paymentMethod, paymentStatus,
				pageable);
	}
}
