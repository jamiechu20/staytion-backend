package com.example.demo.dto;

public class OrderItemRequest {
    private Long roomTypeId;
    private Integer quantity;
//	private BigDecimal pricePerRoom;

    public Long getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(Long roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

//	public BigDecimal getPricePerRoom() {
//		return pricePerRoom;
//	}
//
//	public void setPricePerRoom(BigDecimal pricePerRoom) {
//		this.pricePerRoom = pricePerRoom;
//	}

}