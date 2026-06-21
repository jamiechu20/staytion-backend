package com.example.demo.dto;

public class FavoriteDto {
    private Long userId;
    private Long hotelId;

    public FavoriteDto() {}

    public FavoriteDto(Long userId, Long hotelId) {
        this.userId = userId;
        this.hotelId = hotelId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getHotelId() {
        return hotelId;
    }

    public void setHotelId(Long hotelId) {
        this.hotelId = hotelId;
    }
}
