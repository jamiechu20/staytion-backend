package com.example.demo.dto;

public class FavHotelDto {
    private Long id;
    private String name;
    private String city;
    private String district;
    private Double score;
    private Double price;          // min price
    private Long reviewCount;
    private String imgUrl;

    public FavHotelDto(Long id, String name, String city, String district,
                       Double score, Double price, Long reviewCount, String imgUrl) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.district = district;
        this.score = score;
        this.price = price;
        this.reviewCount = reviewCount;
        this.imgUrl = imgUrl;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Long getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Long reviewCount) {
        this.reviewCount = reviewCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
