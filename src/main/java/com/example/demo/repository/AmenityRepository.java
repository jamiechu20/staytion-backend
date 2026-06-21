package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Amenity;

public interface AmenityRepository extends JpaRepository<Amenity, Long> {
    @Query(value = "SELECT a.* FROM amenities a JOIN room_amenities ra ON a.id = ra.amenity_id WHERE ra.room_type_id = :roomTypeId", nativeQuery = true)
    List<Amenity> findByRoomTypeId(@Param("roomTypeId") Long roomTypeId);
}