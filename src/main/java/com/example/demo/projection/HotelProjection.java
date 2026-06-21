package com.example.demo.projection;

public interface HotelProjection {
	Long getHotelId();

	String getHotelName();

	String getCityName();

	String getDistrictName();

	Integer getReviewCount();

	Double getLowestPrice();

	Double getAverageRating();
	
	String getCoverImageUrl();
}
