package com.example.demo.dto;

import java.time.LocalDate;

public class UnreviewedOrderDto {
    private Long orderId;
    private String hotelName;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String imgUrl;

    public UnreviewedOrderDto(Long orderId, String hotelName, LocalDate checkInDate, LocalDate checkOutDate, String imgUrl) {
        this.orderId = orderId;
        this.hotelName = hotelName;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.imgUrl = imgUrl;
    }


    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }
}
