package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class AdminOrderService {

	private final OrderRepository orderRepository;

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

}
