package com.example.demo.dto;

import java.util.List;

public class HotelDetailDTO {
    private Long id;
    private String hname;
    private String address;
    private String description;
    private Double score;
    private List<ImageDTO> images;
    private List<AmenityDTO> amenities;
    private List<RoomTypeDTO> roomTypes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHname() {
        return hname;
    }

    public void setHname(String hname) {
        this.hname = hname;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public List<ImageDTO> getImages() {
        return images;
    }

    public void setImages(List<ImageDTO> images) {
        this.images = images;
    }

    public List<AmenityDTO> getAmenities() {
        return amenities;
    }

    public void setAmenities(List<AmenityDTO> amenities) {
        this.amenities = amenities;
    }

    public List<RoomTypeDTO> getRoomTypes() {
        return roomTypes;
    }

    public void setRoomTypes(List<RoomTypeDTO> roomTypes) {
        this.roomTypes = roomTypes;
    }

}
