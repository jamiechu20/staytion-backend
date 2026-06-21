package com.example.demo.mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.demo.dto.request.OrderItemRequestDto;
import com.example.demo.dto.request.OrderRequestDto;
import com.example.demo.dto.response.OrderItemResponseDto;
import com.example.demo.dto.response.OrderResponseDto;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.Payment;
import com.example.demo.entity.RoomType;
import com.example.demo.entity.User;

public class OrderMapper {

	public static Order toEntity(OrderRequestDto dto, User user, List<RoomType> roomTypes) {
		Order order = new Order();
		order.setUser(user);
		order.setCheckInDate(dto.getCheckInDate());
		order.setCheckOutDate(dto.getCheckOutDate());
		order.setCreatedAt(LocalDateTime.now());
		order.setStatus(Order.OrderStatus.CONFIRMED);

		Payment payment = new Payment();
		payment.setStatus(Payment.PaymentStatus.UNPAID);
		payment.setMethod(Payment.PaymentMethod.CASH);
		payment.setCreatedAt(LocalDateTime.now());
		order.setPayment(payment);

		List<OrderItem> items = new ArrayList<>();
		BigDecimal total = BigDecimal.ZERO;

		for (OrderItemRequestDto itemDto : dto.getItems()) {
			RoomType room = roomTypes.stream().filter(r -> r.getId().equals(itemDto.getRoomTypeId())).findFirst()
					.orElseThrow(() -> new RuntimeException("RoomType not found: " + itemDto.getRoomTypeId()));

			BigDecimal pricePerRoom = room.getPrice();

			OrderItem item = new OrderItem();
			item.setRoomType(room);
			item.setQuantity(itemDto.getQuantity());
			item.setPricePerRoom(pricePerRoom);
			item.setSubtotal(pricePerRoom.multiply(BigDecimal.valueOf(itemDto.getQuantity())));
			item.setOrder(order);

			total = total.add(item.getSubtotal());
			items.add(item);
		}

		order.setTotalPrice(total);
		order.setOrderItems(items);
		return order;
	}

	public static OrderResponseDto toDto(Order order) {
		OrderResponseDto dto = new OrderResponseDto();
		dto.setId(order.getId());
		dto.setUserId(order.getUser().getId());
		dto.setUserName(order.getUser().getFirstName() + " " + order.getUser().getLastName());
		dto.setUserEmail(order.getUser().getEmail());
		dto.setUserTel(order.getUser().getTel());
		dto.setCheckInDate(order.getCheckInDate());
		dto.setCheckOutDate(order.getCheckOutDate());
		dto.setCreatedAt(order.getCreatedAt());
		dto.setTotalPrice(order.getTotalPrice());
		dto.setStatus(order.getStatus().name());

		List<OrderItemResponseDto> itemDtos = order.getOrderItems().stream().map(item -> {
			OrderItemResponseDto itemDto = new OrderItemResponseDto();
			itemDto.setRoomTypeName(item.getRoomType().getRname());
			itemDto.setQuantity(item.getQuantity());
			itemDto.setPricePerRoom(item.getPricePerRoom());
			itemDto.setSubtotal(item.getSubtotal());
			return itemDto;
		}).collect(Collectors.toList());

		dto.setItems(itemDtos);

		if (!order.getOrderItems().isEmpty()) {
			String hotelName = order.getOrderItems().get(0).getRoomType().getHotel().getHname();
			dto.setHotelName(hotelName);
		} else {
			dto.setHotelName(null);
		}

		if (order.getPayment() != null) {
			dto.setPaymentMethod(order.getPayment().getMethod().name());
			dto.setPaymentStatus(order.getPayment().getStatus().name());
		}

		return dto;
	}
}
