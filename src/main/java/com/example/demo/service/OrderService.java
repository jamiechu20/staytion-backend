//package com.example.demo.service;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import com.example.demo.dto.request.OrderItemRequestDto;
//import com.example.demo.dto.request.OrderRequestDto;
//import com.example.demo.dto.response.OrderItemResponseDto;
//import com.example.demo.dto.response.OrderResponseDto;
//import com.example.demo.entity.Order;
//import com.example.demo.entity.OrderItem;
//import com.example.demo.entity.Payment;
//import com.example.demo.entity.RoomType;
//import com.example.demo.entity.User;
//import com.example.demo.repository.OrderItemRepository;
//import com.example.demo.repository.OrderRepository;
//import com.example.demo.repository.RoomTypeRepository;
//import com.example.demo.repository.UserRepository;
//
//@Service
//public class OrderService {
//
//	@Autowired
//	private OrderRepository orderRepository;
//	@Autowired
//	private OrderItemRepository orderItemRepository;
//	@Autowired
//	private UserRepository userRepository;
//	@Autowired
//	private RoomTypeRepository roomTypeRepository;
////	@Autowired
////	private EmailService emailService;
//
////	// 建立訂單V1
////	public OrderResponseDto createOrder(OrderRequestDto dto) {
////		User user = userRepository.findById(dto.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));
////		
////		List<RoomType> roomTypes = roomTypeRepository.findAllById(
////				dto.getItems().stream().map(OrderItemRequestDto::getRoomTypeId).collect(Collectors.toList()));
////		
////		Order order = toEntity(dto, user, roomTypes);
////		return toDto(orderRepository.save(order));
//
//	// 建立訂單V2
//	public OrderResponseDto createOrder(OrderRequestDto dto) {
//		User user = userRepository.findById(dto.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));
//
//		List<RoomType> roomTypes = roomTypeRepository.findAllByIdWithLock(
//				dto.getItems().stream().map(OrderItemRequestDto::getRoomTypeId).collect(Collectors.toList()));
//
//		Order order = toEntity(dto, user, roomTypes);
//		return toDto(orderRepository.save(order));
//
////		確認信
////		Order saved = orderRepository.save(order);
////		String content = buildOrderConfirmationHtml(saved);
////		emailService.sendOrderConfirmation(saved.getUser().getEmail(), "Staytion 訂單已確認", content);
////		return toDto(saved);
//
//	}
//
//	// 查詢單筆訂單
//	public OrderResponseDto getOrderById(Long id) {
//		return orderRepository.findById(id).map(this::toDto).orElseThrow(() -> new RuntimeException("Order not found"));
//	}
//
//	// 查詢全部訂單
//	public List<OrderResponseDto> getAllOrders() {
//		return orderRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
//	}
//
//	// 查某使用者訂單
//	public List<OrderResponseDto> getOrdersByUserId(Long userId) {
//		return orderRepository.findByUserId(userId).stream().map(this::toDto).collect(Collectors.toList());
//	}
//
//	// 查某飯店的所有訂單（透過 orderItems → roomType → hotel）
//	public List<OrderResponseDto> getOrdersByHotelId(Long hotelId) {
//		return orderRepository.findByRoomTypeHotelId(hotelId).stream().map(this::toDto).collect(Collectors.toList());
//	}
//
//	// 分頁 + 狀態/時間/關鍵字篩選
//	public Page<OrderResponseDto> searchOrders(Long currentUserId, Order.OrderStatus status, LocalDate start,
//			LocalDate end, String keyword,Payment.PaymentMethod paymentMethod,Payment.PaymentStatus paymentStatus,Pageable pageable) {
//		LocalDateTime startDateTime = (start != null) ? start.atStartOfDay() : null;
//		LocalDateTime endDateTime = (end != null) ? end.plusDays(1).atStartOfDay() : null;
//
//		return orderRepository
//				.searchAccessibleOrdersWithKeyword(currentUserId, status, startDateTime, endDateTime, keyword,paymentMethod,paymentStatus, pageable)
//				.map(this::toDto);
//	}
//
//	// 更新訂單狀態
//	@Transactional
//	public OrderResponseDto updateStatus(Long id, String status) {
//		Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
//		order.setStatus(Order.OrderStatus.valueOf(status));
//		return toDto(order);
//	}
//
//	// 刪除訂單（可搭配後台使用）
//	@Transactional
//	public void deleteOrder(Long id) {
//		if (!orderRepository.existsById(id)) {
//			throw new RuntimeException("Order not found");
//		}
//		orderRepository.deleteById(id);
//	}
//
//	// 月營收統計
//	public List<Map<String, Object>> getMonthlyRevenue(int year) {
//		return orderRepository.getMonthlyRevenue(year);
//	}
//
//	// 每日訂單趨勢
//	public List<Map<String, Object>> getOrderTrend(LocalDate start, LocalDate end) {
//		LocalDateTime startDateTime = start.atStartOfDay(); // 當天 00:00:00
//		LocalDateTime endDateTime = end.plusDays(1).atStartOfDay(); // 隔天 00:00:00
//		return orderRepository.getOrderTrend(startDateTime, endDateTime);
//	}
//
//	// DTO -> Entity
//	public Order toEntity(OrderRequestDto dto, User user, List<RoomType> roomTypes) {
//		Order order = new Order();
//		order.setUser(user);
//		order.setCheckInDate(dto.getCheckInDate());
//		order.setCheckOutDate(dto.getCheckOutDate());
//		order.setCreatedAt(LocalDateTime.now());
//		order.setStatus(Order.OrderStatus.CONFIRMED);
//
//		List<OrderItem> items = new ArrayList<>();
//		BigDecimal total = BigDecimal.ZERO;
//
//		for (OrderItemRequestDto itemDto : dto.getItems()) {
//			RoomType room = roomTypes.stream().filter(r -> r.getId().equals(itemDto.getRoomTypeId())).findFirst()
//					.orElseThrow(() -> new RuntimeException("RoomType not found: " + itemDto.getRoomTypeId()));
//
//			OrderItem item = new OrderItem();
//			item.setRoomType(room);
//			item.setQuantity(itemDto.getQuantity());
////			item.setPricePerRoom(itemDto.getPricePerRoom());
////			item.setSubtotal(itemDto.getPricePerRoom().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
//			item.setOrder(order);
//
//			total = total.add(item.getSubtotal());
//			items.add(item);
//		}
//
//		order.setTotalPrice(total);
//		order.setOrderItems(items);
//		return order;
//	}
//
//	// Entity -> DTO
//	public OrderResponseDto toDto(Order order) {
//		OrderResponseDto dto = new OrderResponseDto();
//		dto.setId(order.getId());
//		dto.setUserName(order.getUser().getFirstName() + " " + order.getUser().getLastName());
//		dto.setCheckInDate(order.getCheckInDate());
//		dto.setCheckOutDate(order.getCheckOutDate());
//		dto.setCreatedAt(order.getCreatedAt());
//		dto.setTotalPrice(order.getTotalPrice());
//		dto.setStatus(order.getStatus().name());
//
//		List<OrderItemResponseDto> itemDtos = order.getOrderItems().stream().map(item -> {
//			OrderItemResponseDto itemDto = new OrderItemResponseDto();
//			itemDto.setRoomTypeName(item.getRoomType().getRname());
//			itemDto.setQuantity(item.getQuantity());
//			itemDto.setPricePerRoom(item.getPricePerRoom());
//			itemDto.setSubtotal(item.getSubtotal());
//			return itemDto;
//		}).collect(Collectors.toList());
//
//		dto.setItems(itemDtos);
//		return dto;
//	}
//
////	==========
////	訂單檢查邏輯
////	 // ✅ 1. 檢查庫存
////    if (room.getStock() < itemDto.getQuantity()) {
////        throw new RuntimeException("房型庫存不足: " + room.getRname());
////    }
////
////    // ✅ 2. 檢查是否已有相同用戶同房型、日期重疊的訂單（你需補上這查詢邏輯）
////    if (hasOverlapOrder(user, room, dto.getCheckInDate(), dto.getCheckOutDate())) {
////        throw new RuntimeException("您在該日期已預訂此房型");
////    }
////
////    // ✅ 3. 使用房型價格
////    BigDecimal price = room.getPrice();
////
////    // ✅ 4. 建立訂單項目並計算小計
////    OrderItem item = new OrderItem();
////    item.setRoomType(room);
////    item.setQuantity(itemDto.getQuantity());
////    item.setPricePerRoom(price);
////    item.setSubtotal(price.multiply(BigDecimal.valueOf(itemDto.getQuantity())));
////    item.setOrder(order);
////
////    // 加總
////    total = total.add(item.getSubtotal());
////    items.add(item);
////
////    // ✅ 5. 扣除庫存（這邊先記錄，實際扣庫存可交由庫存模組或額外邏輯完成）
////    room.setStock(room.getStock() - itemDto.getQuantity());
////}
//
////	==========
////	確認信內容
////	public String buildOrderConfirmationHtml(Order order) {
////	    return String.format("""
////	        <html>
////	        <body>
////	            <h2>感謝您的訂單！</h2>
////	            <p>親愛的 %s，您的訂單已確認：</p>
////	            <ul>
////	                <li>訂單編號：%d</li>
////	                <li>入住日期：%s</li>
////	                <li>退房日期：%s</li>
////	                <li>總金額：NT$%s</li>
////	                <li>狀態：%s</li>
////	            </ul>
////	            <p>祝您有美好的一天！</p>
////	        </body>
////	        </html>
////	        """,
////	        (order.getUser().getFirstName()+ " " + order.getUser().getLastName()),
////	        order.getId(),
////	        order.getCheckInDate(),
////	        order.getCheckOutDate(),
////	        order.getTotalPrice(),
////	        order.getStatus().name()
////	    );
////	}
////
////	
//
//}
