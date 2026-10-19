package com.staytion.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.staytion.backend.entity.Amenity;

public interface AmenityRepository extends JpaRepository<Amenity, Long> {
}