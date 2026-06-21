package com.example.demo.repository;

import com.example.demo.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    void deleteByUserIdAndHotelId(Long userId, Long hotelId);

    boolean existsByUserIdAndHotelId(Long userId, Long hotelId);

    @Query("""
                SELECT h.id
                FROM Favorite f
                JOIN f.hotel h
                JOIN h.district d
                JOIN d.city c
                WHERE f.user.id = :userId
                  AND (:city IS NULL OR c.cname LIKE concat('%', :city, '%'))
            """)
    List<Long> findHotelIdsByUserIdAndCity(@Param("userId") Long userId, @Param("city") String city);
}
