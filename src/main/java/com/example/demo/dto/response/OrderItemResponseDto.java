package com.example.demo.dto.response;

import java.math.BigDecimal;

public class OrderItemResponseDto {
	private String roomTypeName;
	private Integer quantity;
	private BigDecimal pricePerRoom;
	private BigDecimal subtotal;

	public String getRoomTypeName() {
		return roomTypeName;
	}

	public void setRoomTypeName(String roomTypeName) {
		this.roomTypeName = roomTypeName;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getPricePerRoom() {
		return pricePerRoom;
	}

	public void setPricePerRoom(BigDecimal pricePerRoom) {
		this.pricePerRoom = pricePerRoom;
	}

	public BigDecimal getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(BigDecimal subtotal) {
		this.subtotal = subtotal;
	}

}
