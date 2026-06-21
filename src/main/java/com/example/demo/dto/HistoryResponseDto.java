package com.example.demo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class HistoryResponseDto {
	private String cityName;
	private String cityImageUrl;
	private LocalDate checkInDate;
	private LocalDate checkOutDate;
	private Integer total;
	private LocalDateTime searchTime;

	public String getCityName() {
		return cityName;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	public String getCityImageUrl() {
		return cityImageUrl;
	}
	public void setCityImageUrl(String cityImageUrl) {
		this.cityImageUrl = cityImageUrl;
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
	public Integer getTotal() {
		return total;
	}
	public void setTotal(Integer total) {
		this.total = total;
	}
	public LocalDateTime getSearchTime() {
		return searchTime;
	}
	public void setSearchTime(LocalDateTime searchTime) {
		this.searchTime = searchTime;
	}
	
	
	
	
}
