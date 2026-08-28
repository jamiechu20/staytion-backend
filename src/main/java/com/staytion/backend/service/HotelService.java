package com.staytion.backend.service;

import java.util.List;

import com.staytion.backend.dto.HotelDetailDTO;
import com.staytion.backend.dto.HotelSearchRequest;
import com.staytion.backend.dto.HotelSearchResult;
import com.staytion.backend.entity.Hotel;

import org.springframework.data.domain.Page;import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.staytion.backend.projection.HotelProjection;
import com.staytion.backend.repository.HotelRepository;

public interface HotelService {
    List<HotelProjection> getTopHotels();

    Page<HotelSearchRequest> searchHotels(HotelSearchResult dto, int page, int size);

    HotelDetailDTO getHotelDetail(Long hotelId);
    
    List<HotelSearchRequest> searchHotelsByName(String keyword, Long highlightHotelId);
}
