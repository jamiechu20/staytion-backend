//package com.example.demo.controller;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Map;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.DeleteMapping;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.PutMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.example.demo.dto.request.OrderRequestDto;
//import com.example.demo.dto.response.OrderResponseDto;
//import com.example.demo.entity.Order;
//import com.example.demo.entity.Payment;
//import com.example.demo.service.OrderService;
//
//@RestController
//@RequestMapping("/api/admin/orders")
//@CrossOrigin
//public class OrderController {
//
//	@Autowired
//	private OrderService orderService;
//
//	// 建立訂單
//	@PostMapping
//	public OrderResponseDto createOrder(@RequestBody OrderRequestDto dto) {
//		return orderService.createOrder(dto);
//	}
//
//	// 查單筆訂單
//	@GetMapping("/{id}")
//	public OrderResponseDto getOrder(@PathVariable Long id) {
//		return orderService.getOrderById(id);
//	}
//
//	// 查全部訂單（後台總覽）
//	@GetMapping
//	public List<OrderResponseDto> getAllOrders() {
//		return orderService.getAllOrders();
//	}
//
//	// 查某使用者的訂單（我的訂單）
//	@GetMapping("/user/{userId}")
//	public List<OrderResponseDto> getByUser(@PathVariable Long userId) {
//		return orderService.getOrdersByUserId(userId);
//	}
//
//	// 查某飯店的所有訂單（後台管理）
//	@GetMapping("/hotel/{hotelId}")
//	public List<OrderResponseDto> getByHotel(@PathVariable Long hotelId) {
//		return orderService.getOrdersByHotelId(hotelId);
//	}
//
//	// 狀態 + 日期 + 關鍵字篩選 + 分頁查詢（後台篩選）
//	@GetMapping("/filter")
//	public Page<OrderResponseDto> filterOrders(@RequestParam(required = false) Order.OrderStatus status,
//			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
//			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
//			@RequestParam(required = false) String keyword,
//			@RequestParam(required = false) Payment.PaymentMethod paymentMethod,
//			@RequestParam(required = false) Payment.PaymentStatus paymentStatus,
//			@RequestParam(required = false) Long currentUserId, // 暫時用來模擬登入
//			Pageable pageable) {
//		return orderService.searchOrders(currentUserId, status, start, end, keyword, paymentMethod, paymentStatus,
//				pageable);
//	}
//
//	// 修改訂單狀態（ex: CONFIRMED → CANCELED）
//	@PutMapping("/{id}/status")
//	public OrderResponseDto updateStatus(@PathVariable Long id, @RequestParam String status) {
//		return orderService.updateStatus(id, status);
//	}
//
//	// 刪除訂單（後台刪除）
//	@DeleteMapping("/{id}")
//	public void deleteOrder(@PathVariable Long id) {
//		orderService.deleteOrder(id);
//	}
//
//	// 月營收統計（Chart.js: Bar Chart）
//	@GetMapping("/summary/monthly-revenue")
//	public List<Map<String, Object>> getMonthlyRevenue(@RequestParam int year) {
//		return orderService.getMonthlyRevenue(year);
//	}
//
//	// 訂單趨勢圖（Chart.js: Line Chart）
//	@GetMapping("/summary/order-trend")
//	public List<Map<String, Object>> getOrderTrend(@RequestParam LocalDate start, @RequestParam LocalDate end) {
//		return orderService.getOrderTrend(start, end);
//	}
//
//	// （預留）匯出功能
//	// @GetMapping("/export")
//	// public ResponseEntity<Resource> exportOrders(...) { ... }
//
//	// （預留）Email 通知可串接在 createOrder / updateStatus 時機點觸發
//}
