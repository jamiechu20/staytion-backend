package com.example.demo.dto.request;

import java.math.BigDecimal;
import java.util.Set;

public class RoomTypeRequestDto {
	private Long hotelId;
	private String rname;
	private BigDecimal price;
	private String description;
	private Integer size;
	private String view;
	private String imgUrl;
	private Boolean isCanceled;
	private Integer quantity;
	private Integer bedCount;
	private String bedType;
	private Integer capacity;
	private Set<Long> amenityIds; // 只送 amenity ID，不是整個物件

	// getter / setter
	
	public String getRname() {
		return rname;
	}

	public Long getHotelId() {
		return hotelId;
	}

	public void setHotelId(Long hotelId) {
		this.hotelId = hotelId;
	}

	public void setRname(String rname) {
		this.rname = rname;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public String getView() {
		return view;
	}

	public void setView(String view) {
		this.view = view;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	public Boolean getIsCanceled() {
		return isCanceled;
	}

	public void setIsCanceled(Boolean isCanceled) {
		this.isCanceled = isCanceled;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Integer getBedCount() {
		return bedCount;
	}

	public void setBedCount(Integer bedCount) {
		this.bedCount = bedCount;
	}

	public String getBedType() {
		return bedType;
	}

	public void setBedType(String bedType) {
		this.bedType = bedType;
	}

	public Integer getCapacity() {
		return capacity;
	}

	public void setCapacity(Integer capacity) {
		this.capacity = capacity;
	}

	public Set<Long> getAmenityIds() {
		return amenityIds;
	}

	public void setAmenityIds(Set<Long> amenityIds) {
		this.amenityIds = amenityIds;
	}

}
