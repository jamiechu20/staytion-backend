package com.example.demo.dto.response;

import java.math.BigDecimal;

public class AvailableRoomResponseDto {
    private Long roomTypeId;
    private String roomTypeName;
    private Integer capacity;
    private BigDecimal price;
    private Integer availableCount;
    private String imgUrl;

    public AvailableRoomResponseDto(Long roomTypeId, String roomTypeName, Integer capacity, BigDecimal price, Integer availableCount, String imgUrl) {
        this.roomTypeId = roomTypeId;
        this.roomTypeName = roomTypeName;
        this.capacity = capacity;
        this.price = price;
        this.availableCount = availableCount;
        this.imgUrl = imgUrl;
    }


    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public Long getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(Long roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public String getRoomTypeName() {
        return roomTypeName;
    }

    public void setRoomTypeName(String roomTypeName) {
        this.roomTypeName = roomTypeName;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getAvailableCount() {
        return availableCount;
    }

    public void setAvailableCount(Integer availableCount) {
        this.availableCount = availableCount;
    }
}
