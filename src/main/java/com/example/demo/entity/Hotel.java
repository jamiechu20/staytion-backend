package com.example.demo.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "hotels")
public class Hotel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "hname")
	private String hname;

	@Column(name = "address")
	private String address;

	@Column(name = "tel")
	private String tel;

	@Column(name = "description")
	private String description;

	@Column(name = "latitude")
	private Double latitude;

	@Column(name = "longitude")
	private Double longitude;

	// Constructor
	public Hotel() {

	}

	public Hotel(String hname, String address, String tel, String description, Double latitude, Double longitude) {
		this.hname = hname;
		this.address = address;
		this.tel = tel;
		this.description = description;
		this.latitude = latitude;
		this.longitude = longitude;

	}

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getHname() {
		return hname;
	}

	public void setHname(String hname) {
		this.hname = hname;
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

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	// ------------------------------
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "district_id")
	@JsonBackReference
	private District district;

	// -------------------------------
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_id")
	@JsonBackReference
	private User owner;

	// ------------------
	@OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JsonBackReference
	private List<RoomType> roomTypes  = new ArrayList<>();

	@OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JsonBackReference
	private List<Image> images = new ArrayList<>();

	@OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JsonBackReference
	private List<Review> reviews;

	public District getDistrict() {
		return district;
	}

	public void setDistrict(District district) {
		this.district = district;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public List<RoomType> getRoomTypes() {
		return roomTypes;
	}

	public void setRoomTypes(List<RoomType> roomTypes) {
		this.roomTypes = roomTypes;
	}

	public List<Image> getImages() {
		return images;
	}

	public void setImages(List<Image> images) {
		this.images = images;
	}
	// -------------------------------------
	// // 要再討論看看要不要加
	// private Double rating;
	//
	// public Double getRating() {
	// return rating;
	// }
	//
	// public void setRating(Double rating) {
	// this.rating = rating;
	// }


	// -------------------------------------
	// @Override
	// public String toString() {
	// return "Hotel{" + "id=" + id + ", hname='" + hname + '\'' + ", address='" +
	// address + '\'' + ", tel='" + tel
	// + '\'' + ", districtId=" + district.getId() + ", latitude=" + latitude + ",
	// longitude=" + longitude
	// + ", ownerId=" + owner.getId() + '}';
	// }


	public List<Review> getReviews() {
		return reviews;
	}

	public void setReviews(List<Review> reviews) {
		this.reviews = reviews;
	}

	//----------------
	@ManyToMany
	@JoinTable(
			name = "hotel_amenities",
			joinColumns = @JoinColumn(name = "hotel_id"),
			inverseJoinColumns = @JoinColumn(name = "amenities_id")
	)
	private List<Amenity> amenities = new ArrayList<>();

	public List<Amenity> getAmenities() {
		return amenities;
	}

	public void setAmenities(List<Amenity> amenities) {
		this.amenities = amenities;
	}
}