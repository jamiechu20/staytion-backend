package com.staytion.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.staytion.backend.entity.Hotel;
import com.staytion.backend.entity.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByHotelId(Long hotelId);

    List<Image> findByHotelIdAndIsCover(Long hotelId, Boolean isCover);

    void deleteByHotel(Hotel hotel);
}