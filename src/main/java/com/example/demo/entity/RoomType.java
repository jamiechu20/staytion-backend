package com.example.demo.entity;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "room_types")
public class RoomType {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    @Column(name = "rname")
    private String rname;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "description")
    private String description;

	@Column(name = "size")
	private Integer size; // 平方公尺

    @Column(name = "view")
    private String view;

    @Column(name = "img_url")
    private String imgUrl;

    @Column(name = "is_canceled")
    private Boolean isCanceled;

	@Column(name = "quantity")
	private Integer quantity; // 該房型總數量

    @Column(name = "bed_count")
    private Integer bedCount;

    @Column(name = "bed_type")
    private String bedType;

	@Column(name = "capacity")
	private Integer capacity; // 容納人數

	// Constructor
	public RoomType() {

	}

	public RoomType(String rname, BigDecimal price, String description, Integer size, String view, String imgUrl,
			Boolean isCanceled, Integer quantity, Integer bedCount, String bedType, Integer capacity) {
		this.rname = rname;
		this.price = price;
		this.description = description;
		this.size = size;
		this.view = view;
		this.imgUrl = imgUrl;
		this.isCanceled = isCanceled;
		this.quantity = quantity;
		this.bedCount = bedCount;
		this.bedType = bedType;
		this.capacity = capacity;
	}

	// -------------------------------------
//	@ManyToOne
//	@JoinColumn(name = "hotel_id")
//	private Hotel hotel;

	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinTable(name = "room_amenities", // 中間表的名稱
			joinColumns = @JoinColumn(name = "room_type_id"), // 本實體 (RoomType) 在中間表的外鍵
			inverseJoinColumns = @JoinColumn(name = "amenity_id") // 另一實體 (Amenity) 在中間表的外鍵
	)
	private Set<Amenity> amenities = new HashSet<>(); // 使用 Set 避免重複

	@OneToMany(mappedBy = "roomType", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<RoomAvailability> availabilities;

	// Getters and Setters
	public Long getId() {
		return id;
	}

    public void setId(Long id) {
        this.id = id;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
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

    public Set<Amenity> getAmenities() {
        return amenities;
    }

    public void setAmenities(Set<Amenity> amenities) {
        this.amenities = amenities;
    }


	public List<RoomAvailability> getAvailabilities() {
		return availabilities;
	}
	public void setAvailabilities(List<RoomAvailability> availabilities) {
		this.availabilities = availabilities;
	}


	// -------------------------------------
	// @Override
	// public String toString() {
	// return "RoomType{" + "id=" + id + ", hotelId=" + hotel + ", rname='" + rname
	// + '\'' + ", price=" + price
	// + ", size=" + size + ", quantity=" + quantity + '}';
	// }
}
