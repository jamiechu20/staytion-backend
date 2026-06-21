package com.example.demo.service;

import com.example.demo.dto.FavHotelDto;
import com.example.demo.dto.FavoriteDto;
import com.example.demo.entity.Favorite;

import java.util.List;

public interface FavoriteService {
    Favorite addFavorite(FavoriteDto dto);

    void removeFavorite(Long userId, Long hotelId);

    boolean isFavorite(Long userId, Long hotelId);

    List<FavHotelDto> getFavoriteHotels(Long userId, String city);
    
}
