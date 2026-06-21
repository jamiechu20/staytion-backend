package com.example.demo.dto.response;

public class RoomTypeSummaryDto {
    private Long hotelId;
    private String hotelName;
    private String rname;
    private Long roomCount;
    private Long totalAvailable;
    private Boolean isCanceled;
    private Double averagePrice;

    public RoomTypeSummaryDto() {
		// TODO Auto-generated constructor stub
	}
    
    public RoomTypeSummaryDto(Long hotelId, String hotelName, String rname, Long roomCount, Long totalAvailable, Boolean isCanceled, Double averagePrice) {
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.rname = rname;
        this.roomCount = roomCount;
        this.totalAvailable = totalAvailable;
        this.isCanceled = isCanceled;
        this.averagePrice = averagePrice;
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

	public String getRname() {
		return rname;
	}

	public void setRname(String rname) {
		this.rname = rname;
	}

	public Long getRoomCount() {
		return roomCount;
	}

	public void setRoomCount(Long roomCount) {
		this.roomCount = roomCount;
	}

	public Long getTotalAvailable() {
		return totalAvailable;
	}

	public void setTotalAvailable(Long totalAvailable) {
		this.totalAvailable = totalAvailable;
	}

	public Boolean getIsCanceled() {
		return isCanceled;
	}

	public void setIsCanceled(Boolean isCanceled) {
		this.isCanceled = isCanceled;
	}

	public Double getAveragePrice() {
		return averagePrice;
	}

	public void setAveragePrice(Double averagePrice) {
		this.averagePrice = averagePrice;
	}

    // getters / setters
    
    
}
