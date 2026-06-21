package com.example.demo.service;

import com.example.demo.dto.FavHotelDto;
import com.example.demo.dto.FavoriteDto;
import com.example.demo.entity.Favorite;
import com.example.demo.entity.Hotel;
import com.example.demo.repository.FavoriteRepository;
import com.example.demo.repository.HotelRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.RoomTypeRepository;
import com.example.demo.repository.ImageRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class FavoriteServiceImpl implements FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final HotelRepository hotelRepository;
    private final ReviewRepository reviewRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final ImageRepository imageRepository;

    public FavoriteServiceImpl(
            FavoriteRepository favoriteRepository,
            HotelRepository hotelRepository,
            ReviewRepository reviewRepository,
            RoomTypeRepository roomTypeRepository,
            ImageRepository imageRepository
    ) {
        this.favoriteRepository = favoriteRepository;
        this.hotelRepository = hotelRepository;
        this.reviewRepository = reviewRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.imageRepository = imageRepository;
    }

    //加入收藏清單
    @Override
    public Favorite addFavorite(FavoriteDto dto) {
        Favorite favorite = new Favorite(dto.getUserId(), dto.getHotelId());
        return favoriteRepository.save(favorite);
    }

    //移除收藏清單
    @Override
    @Transactional
    public void removeFavorite(Long userId, Long hotelId) {
        favoriteRepository.deleteByUserIdAndHotelId(userId, hotelId);

    }

    //是否是收藏飯店（判斷愛心）
    @Override
    public boolean isFavorite(Long userId, Long hotelId) {
        return favoriteRepository.existsByUserIdAndHotelId(userId, hotelId);
    }

    //用城市查詢篩選收藏清單
    @Override
    public List<FavHotelDto> getFavoriteHotels(Long userId, String city) {
        // 1. 取得該用戶在指定城市的所有收藏 hotelId
        List<Long> hotelIds = favoriteRepository.findHotelIdsByUserIdAndCity(userId, city);
        List<FavHotelDto> result = new ArrayList<>();
        for (Long hotelId : hotelIds) {
            Optional<Hotel> hotelOpt = hotelRepository.findById(hotelId);
            if (hotelOpt.isEmpty()) continue;
            Hotel hotel = hotelOpt.get();
            String hname = hotel.getHname();
            String cityName = hotel.getDistrict() != null && hotel.getDistrict().getCity() != null
                    ? hotel.getDistrict().getCity().getCname() : null;
            String districtName = hotel.getDistrict() != null ? hotel.getDistrict().getDname() : null;
            Double avgScore = reviewRepository.findAverageScoreByHotelId(hotelId);
            if (avgScore == null) avgScore = 0.0;
            Long reviewCount = reviewRepository.countByHotelId(hotelId);
            if (reviewCount == null) reviewCount = 0L;
            Integer minPrice = roomTypeRepository.findMinPriceByHotelId(hotelId);
            if (minPrice == null) minPrice = 0;
            List<String> covers = imageRepository.findCoverImageByHotelId(hotelId);
            String coverImageUrl = covers.isEmpty() ? null : covers.get(0);
            FavHotelDto dto = new FavHotelDto(
                    hotelId,
                    hname,
                    cityName,
                    districtName,
                    avgScore,
                    minPrice != null ? minPrice.doubleValue() : 0.0,
                    reviewCount,
                    coverImageUrl
            );
            result.add(dto);
        }
        return result;
    }
}
