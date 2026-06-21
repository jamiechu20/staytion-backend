package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.response.OrderResponseDto;
import com.example.demo.entity.Order;
import com.example.demo.entity.Payment;
import com.example.demo.mapper.OrderMapper;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.PaymentRepository;

@Service
public class AdminOrderService {

	@Autowired
	private OrderRepository orderRepository;

	// 查全部訂單
	public List<OrderResponseDto> getAllOrders() {
		return orderRepository.findAll().stream().map(OrderMapper::toDto).toList();
	}

	// 查某使用者的訂單（後台）
	public Page<OrderResponseDto> getOrdersByHotelOwner(Long ownerId, Pageable pageable) {
		return orderRepository.findOrdersByHotelOwner(ownerId, pageable).map(OrderMapper::toDto);
	}

	// 查某飯店所有訂單（後台）
	public List<OrderResponseDto> getOrdersByHotelId(Long hotelId) {
		return orderRepository.findByRoomTypeHotelId(hotelId).stream().map(OrderMapper::toDto).toList();
	}

	// 狀態 + 日期篩選 + 分頁
	public Page<OrderResponseDto> searchOrders(Long currentUserId, Long hotelId, Order.OrderStatus status,
			LocalDate start, LocalDate end, String keyword, Payment.PaymentMethod paymentMethod,
			Payment.PaymentStatus paymentStatus, Pageable pageable) {
		LocalDateTime startDateTime = (start != null) ? start.atStartOfDay() : null;
		LocalDateTime endDateTime = (end != null) ? end.plusDays(1).atStartOfDay() : null;

		return orderRepository.searchAccessibleOrdersWithKeyword(currentUserId, hotelId, status, startDateTime,
				endDateTime, keyword, paymentMethod, paymentStatus, pageable).map(OrderMapper::toDto);
	}

	// 修改訂單狀態
	@Transactional
	public OrderResponseDto updateStatus(Long id, Order.OrderStatus status) {
		Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));

		order.setStatus(status);
		return OrderMapper.toDto(order);
	}

	// 修改訂單付款狀態
	@Transactional
	public OrderResponseDto updatePaymentStatus(Long orderId, Payment.PaymentStatus status) {
		Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));

		Payment payment = order.getPayment();
		if (payment == null) {
			throw new RuntimeException("No payment found for this order");
		}

		payment.setStatus(status);

		return OrderMapper.toDto(order);
	}

	// 刪除訂單
	@Transactional
	public void deleteOrder(Long id) {
		if (!orderRepository.existsById(id)) {
			throw new RuntimeException("Order not found");
		}
		orderRepository.deleteById(id);
	}

//	// 月營收
//	public List<Map<String, Object>> getMonthlyRevenue(int year) {
//		return orderRepository.getMonthlyRevenue(year);
//	}
//
//	// 訂單趨勢
//	public List<Map<String, Object>> getOrderTrend(LocalDate start, LocalDate end) {
//		LocalDateTime startDateTime = start.atStartOfDay();
//		LocalDateTime endDateTime = end.plusDays(1).atStartOfDay(); // 包含整天
//
//		List<Map<String, Object>> rawData = orderRepository.getOrderTrend(startDateTime, endDateTime);
//
//		// 將資料轉為 Map<LocalDate, Long>
//		Map<LocalDate, Long> trendMap = new HashMap<>();
//		for (Map<String, Object> row : rawData) {
//			LocalDate date = ((java.sql.Date) row.get("date")).toLocalDate();
//			Long count = (Long) row.get("orderCount");
//			trendMap.put(date, count);
//		}
//
//		// 建立完整日期範圍，補 0
//		List<Map<String, Object>> result = new ArrayList<>();
//		LocalDate current = start;
//		while (!current.isAfter(end)) {
//			Long count = trendMap.getOrDefault(current, 0L);
//			Map<String, Object> entry = new HashMap<>();
//			entry.put("date", current);
//			entry.put("orderCount", count);
//			result.add(entry);
//			current = current.plusDays(1);
//		}
//
//		return result;
//	}

}
