package com.example.demo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

public class HistoryRequestDto {

	@Schema(hidden = true)
	private Long userId;
	private String locationName;
	private String locationType;
	private LocalDate checkInDate;
	private LocalDate checkOutDate;
	private Integer Adults;
	private Integer Kids;
	private LocalDateTime searchTime;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public String getLocationType() {
		return locationType;
	}

	public void setLocationType(String locationType) {
		this.locationType = locationType;
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

	public Integer getAdults() {
		return Adults;
	}

	public void setAdults(Integer adults) {
		Adults = adults;
	}

	public Integer getKids() {
		return Kids;
	}

	public void setKids(Integer kids) {
		Kids = kids;
	}

	public LocalDateTime getSearchTime() {
		return searchTime;
	}

	public void setSearchTime(LocalDateTime searchTime) {
		this.searchTime = searchTime;
	}

}
