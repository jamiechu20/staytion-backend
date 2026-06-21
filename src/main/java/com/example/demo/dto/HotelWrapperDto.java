package com.example.demo.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HotelWrapperDto {
	@JsonProperty("Hotels")
	private List<HotelJsonDto> Hotels;
	public List<HotelJsonDto> getHotels(){
		return Hotels;
	}
	public void setHotels(List<HotelJsonDto>hotels) {
		this.Hotels = hotels;
	}
}
