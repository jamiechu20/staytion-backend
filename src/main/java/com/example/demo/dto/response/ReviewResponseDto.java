package com.example.demo.dto.response;

import java.time.LocalDateTime;

public class ReviewResponseDto {
    private Long orderId;
    private Long hotelId;
    private String hotelName;
    private Integer score;
    private String comment;
    private String reply;
    private String firstName;
    private LocalDateTime createdAt;
    private Long userId;
    private String imgUrl;

    public ReviewResponseDto() {
    }

    public ReviewResponseDto(Long orderId, Long hotelId, Integer score, String comment, String reply,
                             String firstName, LocalDateTime createdAt, Long userId, String hotelName, String imgUrl) {
        this.orderId = orderId;
        this.hotelId = hotelId;
        this.score = score;
        this.comment = comment;
        this.reply = reply;
        this.firstName = firstName;
        this.createdAt = createdAt;
        this.userId = userId;
        this.hotelName = hotelName;
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

    public Long getHotelId() {
        return hotelId;
    }

    public void setHotelId(Long hotelId) {
        this.hotelId = hotelId;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
