package com.example.demo.service;

import java.util.List;

import com.example.demo.dto.HotelDetailDTO;
import com.example.demo.dto.HotelSearchRequest;
import com.example.demo.dto.HotelSearchResult;
import com.example.demo.entity.Hotel;

import org.springframework.data.domain.Page;import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.projection.HotelProjection;
import com.example.demo.repository.HotelRepository;

public interface HotelService {
    List<HotelProjection> getTopHotels();

    Page<HotelSearchRequest> searchHotels(HotelSearchResult dto, int page, int size);

    HotelDetailDTO getHotelDetail(Long hotelId);
    
    List<HotelSearchRequest> searchHotelsByName(String keyword, Long highlightHotelId);
}
