package com.staytion.backend.repository;

import com.staytion.backend.dto.FavHotelDto;
import com.staytion.backend.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    void deleteByUserIdAndHotelId(Long userId, Long hotelId);

    boolean existsByUserIdAndHotelId(Long userId, Long hotelId);

    // 收藏清單：評分、最低價、評論數、封面圖用相關子查詢一次算完，整個列表只需要一條 SQL
    // CAST(:city AS String)：city 為 null 時 PostgreSQL 推不出參數型別會當成 bytea，LIKE 會報錯，需明確轉型
    @Query("""
                SELECT new com.staytion.backend.dto.FavHotelDto(
                    h.id,
                    h.hname,
                    c.cname,
                    d.dname,
                    COALESCE((SELECT ROUND(AVG(r.score), 1) FROM Review r WHERE r.hotel = h), 0.0),
                    COALESCE((SELECT CAST(MIN(rt.price) AS Double) FROM RoomType rt WHERE rt.hotel = h), 0.0),
                    (SELECT COUNT(r2) FROM Review r2 WHERE r2.hotel = h),
                    (SELECT MIN(img.imgUrl) FROM Image img WHERE img.hotel = h AND img.isCover = true)
                )
                FROM Favorite f
                JOIN f.hotel h
                JOIN h.district d
                JOIN d.city c
                WHERE f.user.id = :userId
                  AND (:city IS NULL OR c.cname LIKE concat('%', CAST(:city AS String), '%'))
            """)
    List<FavHotelDto> findFavoriteHotelsByUserIdAndCity(@Param("userId") Long userId, @Param("city") String city);
}
