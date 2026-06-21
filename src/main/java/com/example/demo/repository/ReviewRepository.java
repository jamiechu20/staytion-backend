package com.example.demo.repository;

import com.example.demo.dto.UnreviewedOrderDto;
import com.example.demo.entity.Review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> {
    List<Review> findByHotelId(Long hotelId);
    List<Review> findByUserId(Long userId);
    boolean existsByOrderId(Long orderId);
    Optional<Review> findByOrderId(Long orderId);

    //飯店平均分數（用在房間詳情頁）
    @Query(value = "SELECT ROUND(AVG(score), 1) FROM reviews WHERE hotel_id = :hotelId", nativeQuery = true)
    Double findAverageScoreByHotelId(@Param("hotelId") Long hotelId);

    //統計飯店評論則數
    @Query("SELECT COUNT(r) FROM Review r WHERE r.hotel.id = :hotelId")
    Long countByHotelId(@Param("hotelId") Long hotelId);

    //查詢尚未評論
    @Query("""
                 SELECT DISTINCT new com.example.demo.dto.UnreviewedOrderDto(
                   o.id,
                   h.hname,
                   o.checkInDate,
                   o.checkOutDate,
                   COALESCE(i.imgUrl, '/images/default-hotel.jpg')
                   )
                   FROM Order o
                   JOIN o.orderItems oi
                   JOIN oi.roomType rt
                   JOIN rt.hotel h
                   LEFT JOIN h.images i ON i.isCover = true
                   LEFT JOIN Review r ON r.order.id = o.id
                   WHERE o.user.id = :userId
                   AND o.checkOutDate < CURRENT_DATE
                   AND r.id IS NULL
            """)
    List<UnreviewedOrderDto> findUnreviewedOrdersByUserId(@Param("userId") Long userId);
}
