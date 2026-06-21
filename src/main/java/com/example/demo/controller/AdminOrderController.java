package com.example.demo.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.response.OrderResponseDto;
import com.example.demo.entity.Order;
import com.example.demo.entity.Payment;
import com.example.demo.service.AdminOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admins/orders")
@CrossOrigin(origins = "http://127.0.0.1:5500")
@Tag(name = "Admin Order Management", description = "後台訂單管理 API")
public class AdminOrderController {

    @Autowired
    private AdminOrderService service;

//	@GetMapping
//	@Operation(summary = "取得所有訂單", operationId = "getAllOrdersForAdmin")
//	public List<OrderResponseDto> getAll() {
//		return service.getAllOrders();
//	}

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

//	@GetMapping("/summary/monthly-revenue")
//	@Operation(summary = "月營收統計", description = "根據指定年份，回傳各月份的訂單總營收資料（給圖表使用）", operationId = "summaryMonthRevenue")
//	public List<Map<String, Object>> getMonthlyRevenue(@RequestParam int year) {
//		return service.getMonthlyRevenue(year);
//	}
//
//	@GetMapping("/summary/order-trend")
//	@Operation(summary = "訂單趨勢圖", description = "根據日期區間，回傳每天的訂單數量統計資料（給折線圖使用）", operationId = "summaryOrderTrend")
//	public List<Map<String, Object>> getOrderTrend(
//			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
//			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
//		return service.getOrderTrend(start, end);
//	}
}
