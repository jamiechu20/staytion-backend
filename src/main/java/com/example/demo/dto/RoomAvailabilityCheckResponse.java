package com.example.demo.dto;

import java.time.LocalDate;

public class RoomAvailabilityCheckResponse {
    private Long id;
    private Long roomTypeId;
    private LocalDate date;
    private Integer availableQuantity;

    public RoomAvailabilityCheckResponse() {}

    public RoomAvailabilityCheckResponse(Long id, Long roomTypeId, LocalDate date, Integer availableQuantity) {
        this.id = id;
        this.roomTypeId = roomTypeId;
        this.date = date;
        this.availableQuantity = availableQuantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(Long roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }
}
