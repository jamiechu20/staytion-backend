package com.example.demo.dto.request;

import java.util.List;

import com.example.demo.dto.AmenityDTO;
import com.example.demo.dto.ImageDTO;

import io.swagger.v3.oas.annotations.media.Schema;

public class HotelRequestDto {
	private String hotelname;

	@Schema(description = "地址（後端自動轉換為經緯度）")
	private String address;

	private String tel;
	private String description;
	private Long ownerId;
	private List<ImageDTO> images;
	private List<Long> amenities;

	public String getHotelname() {
		return hotelname;
	}

	public void setHotelname(String hotelname) {
		this.hotelname = hotelname;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(Long ownerId) {
		this.ownerId = ownerId;
	}

	public List<ImageDTO> getImages() {
		return images;
	}

	public void setImages(List<ImageDTO> images) {
		this.images = images;
	}

	public List<Long> getAmenities() {
		return amenities;
	}
	public void setAmenities(List<Long> amenities) {
		this.amenities = amenities;
	}
	
}
