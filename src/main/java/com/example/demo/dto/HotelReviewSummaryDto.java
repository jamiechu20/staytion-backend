package com.example.demo.dto;

import com.example.demo.dto.response.ReviewResponseDto;

import java.util.List;

public class HotelReviewSummaryDto {
    private Double averageScore;
    private Long reviewCount;
    private List<ReviewResponseDto> reviews;

    public HotelReviewSummaryDto(Double averageScore, Long reviewCount, List<ReviewResponseDto> reviews) {
        this.averageScore = averageScore;
        this.reviewCount = reviewCount;
        this.reviews = reviews;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

    public Long getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Long reviewCount) {
        this.reviewCount = reviewCount;
    }

    public List<ReviewResponseDto> getReviews() {
        return reviews;
    }

    public void setReviews(List<ReviewResponseDto> reviews) {
        this.reviews = reviews;
    }
}
