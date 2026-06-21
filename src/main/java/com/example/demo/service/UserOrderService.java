package com.example.demo.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.request.OrderItemRequestDto;
import com.example.demo.dto.request.OrderRequestDto;
import com.example.demo.dto.response.OrderResponseDto;
import com.example.demo.entity.Order;
import com.example.demo.entity.RoomType;
import com.example.demo.entity.User;
import com.example.demo.mapper.OrderMapper;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.RoomTypeRepository;
import com.example.demo.repository.UserRepository;

@Service
public class UserOrderService {

	@Autowired
	private OrderRepository orderRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private RoomTypeRepository roomTypeRepository;

	// 建立訂單
	@Transactional
	public OrderResponseDto createOrder(OrderRequestDto dto) {
		User user = userRepository.findById(dto.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));

		LocalDate checkIn = dto.getCheckInDate();
		LocalDate checkOut = dto.getCheckOutDate();

		List<RoomType> roomTypes = roomTypeRepository
				.findAllByIdWithLock(dto.getItems().stream().map(OrderItemRequestDto::getRoomTypeId).toList());

		// 初始化總價
		BigDecimal totalPrice = BigDecimal.ZERO;

		for (OrderItemRequestDto itemDto : dto.getItems()) {
			RoomType room = roomTypes.stream().filter(rt -> rt.getId().equals(itemDto.getRoomTypeId())).findFirst()
					.orElseThrow(() -> new RuntimeException("RoomType not found: " + itemDto.getRoomTypeId()));

			// 累計總價（room.price * 數量 * 入住天數）
			int night = (int) ChronoUnit.DAYS.between(checkIn, checkOut);
			BigDecimal price = room.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()))
					.multiply(BigDecimal.valueOf(night));
			totalPrice = totalPrice.add(price);

		}

		// 建立訂單 entity，傳入總價
		Order order = OrderMapper.toEntity(dto, user, roomTypes);
		order.setTotalPrice(totalPrice); // 確保從後端計算

		return OrderMapper.toDto(orderRepository.save(order));
	}

	// 查某使用者的訂單（前台）
//	public List<OrderResponseDto> getOrdersByUserId(Long userId) {
//		return orderRepository.findByUserId(userId).stream().map(OrderMapper::toDto).toList();
//	}
	public Page<OrderResponseDto> getOrdersByUserId(Long userId, Pageable pageable) {
		return orderRepository.findByUserId(userId, pageable).map(OrderMapper::toDto);
	}

	// 查詢單筆訂單（個人訂單）
	public OrderResponseDto getOrderById(Long id) {
		return orderRepository.findById(id).map(OrderMapper::toDto)
				.orElseThrow(() -> new RuntimeException("Order not found"));
	}

	// 更新單筆訂單（個人訂單）
	@Transactional
	public OrderResponseDto updateOrder(Long id, OrderRequestDto dto) {
		Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));

		// 可選擇更新的欄位，例如入住、退房、房型數量等
		order.setCheckInDate(dto.getCheckInDate());
		order.setCheckOutDate(dto.getCheckOutDate());

		// 你可以補一個清除舊 item、重新建立 item 的邏輯
		// 也可以限制只修改部分欄位（像 checkIn/checkOut）

		return OrderMapper.toDto(order);
	}

//    // 庫存檢查
//    private void validateStock(RoomType room, int quantity) {
//        if (room.getStock() < quantity) {
//            throw new RuntimeException("庫存不足：" + room.getRname());
//        }
//    }
//
//    // 日期檢查
//	  private void validateOverlap(User user, RoomType room, LocalDate checkIn, LocalDate checkOut) {
//		  boolean exists = orderRepository.existsOverlappingOrder(user.getId(), room.getId(), checkIn, checkOut);
//		  if (exists) {
//			  throw new RuntimeException("該日期已有相同房型訂單");
//		  }
//	  }
//    validateStock(room, itemDto.getQuantity());
//    validateOverlap(user, room, dto.getCheckInDate(), dto.getCheckOutDate());

}
