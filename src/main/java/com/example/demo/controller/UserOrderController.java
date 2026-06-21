package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.request.OrderRequestDto;
import com.example.demo.dto.response.OrderResponseDto;
import com.example.demo.service.UserOrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users/orders")
@Tag(name = "User Order Management", description = "前台使用者訂單管理 API (應該會拿掉)")
public class UserOrderController {

	@Autowired
	private UserOrderService service;

	@Operation(summary = "建立訂單", description = "使用者建立一筆新訂單，需提供入住日期、房型、付款方式等資訊", operationId = "createUserOrder")
	@PostMapping
	public OrderResponseDto create(@RequestBody OrderRequestDto dto) {
		return service.createOrder(dto);
	}

	@Operation(summary = "取得使用者的訂單清單", description = "根據使用者 ID，取得他所有的訂單，支援分頁查詢", operationId = "getOrdersByUserId")
	@GetMapping("/{userId}")
	public Page<OrderResponseDto> getUserOrders(@PathVariable Long userId, Pageable pageable) {
		return service.getOrdersByUserId(userId, pageable);
	}

	@Operation(summary = "更新訂單", description = "根據訂單 ID 修改訂單內容，如更改付款方式或狀態（例如取消）", operationId = "updateUserOrder")
	@PutMapping("/{id}")
	public OrderResponseDto updateOrder(@PathVariable Long id, @RequestBody OrderRequestDto dto) {
		return service.updateOrder(id, dto);
	}
}
