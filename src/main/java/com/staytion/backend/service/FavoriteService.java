package com.staytion.backend.service;

import com.staytion.backend.dto.FavHotelDto;
import com.staytion.backend.dto.FavoriteDto;
import com.staytion.backend.entity.Favorite;

import java.util.List;

public interface FavoriteService {
    Favorite addFavorite(FavoriteDto dto);

    void removeFavorite(Long userId, Long hotelId);

    boolean isFavorite(Long userId, Long hotelId);

    List<FavHotelDto> getFavoriteHotels(Long userId, String city);
    
}
