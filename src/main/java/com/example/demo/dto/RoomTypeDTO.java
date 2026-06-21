package com.example.demo.dto;

import com.example.demo.entity.RoomType;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class RoomTypeDTO {
	private Long id;
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
	private List<ImageDTO> images;
	private List<AmenityDTO> amenities;

	public static RoomTypeDTO from(RoomType entity) {
		RoomTypeDTO dto = new RoomTypeDTO();
		dto.setId(entity.getId());
		dto.setHotelId(entity.getHotel() != null ? entity.getHotel().getId() : null);
		dto.setRname(entity.getRname());
		dto.setPrice(entity.getPrice());
		dto.setDescription(entity.getDescription());
		dto.setSize(entity.getSize());
		dto.setView(entity.getView());
		dto.setImgUrl(entity.getImgUrl());
		dto.setIsCanceled(entity.getIsCanceled());
		dto.setQuantity(entity.getQuantity());
		dto.setBedCount(entity.getBedCount());
		dto.setBedType(entity.getBedType());
		dto.setCapacity(entity.getCapacity());

		if (entity.getAmenities() != null) {
			dto.setAmenities(
					entity.getAmenities().stream()
							.map(AmenityDTO::from)
							.collect(java.util.stream.Collectors.toList())
			);
		} else {
			dto.setAmenities(null);
		}
		//只有 imgUrl（一張主圖）
		return dto;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getHotelId() {
		return hotelId;
	}

	public void setHotelId(Long hotelId) {
		this.hotelId = hotelId;
	}

	public String getRname() {
		return rname;
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

}
