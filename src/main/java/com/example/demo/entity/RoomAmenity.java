package com.example.demo.entity;

// import java.io.Serializable;

import jakarta.persistence.Table;

// @Entity
@Table(name = "room_amenities")

// @IdClass(RoomAmenity.RoomAmenityId.class)
public class RoomAmenity {

    // @Id
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "room_type_id", nullable = false)
    // private RoomType roomType;

    // public RoomType getRoomType() {
    // return roomType;
    // }

    // public void setRoomType(RoomType roomType) {
    // this.roomType = roomType;
    // }

    // public Amenity getAmenity() {
    // return amenity;
    // }

    // public void setAmenity(Amenity amenity) {
    // this.amenity = amenity;
    // }

    // @Id
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "amenity_id", nullable = false)
    // private Amenity amenity;

    // public static class RoomAmenityId implements Serializable {
    // private Long roomType;
    // private Long amenity;
    // }
}