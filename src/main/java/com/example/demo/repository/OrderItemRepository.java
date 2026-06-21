package com.example.demo.repository;

import com.example.demo.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query("""
              SELECT DISTINCT ri.roomType.hotel.hname 
                FROM OrderItem ri 
               WHERE ri.order.id = :orderId
            """)
    List<String> findDistinctHotelNamesByOrderId(@Param("orderId") Long orderId);

    // 更新訂單
    @Query("""
                SELECT oi FROM OrderItem oi
                JOIN oi.order o
                WHERE oi.roomType.id = :roomTypeId
                  AND o.status = 'CONFIRMED'
                  AND o.id <> :excludeOrderId
                  AND o.checkOutDate > :checkIn
                  AND o.checkInDate < :checkOut
            """)
    List<OrderItem> findOverlappingItems(
            @Param("roomTypeId") Long roomTypeId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("excludeOrderId") Long excludeOrderId
    );

    //    計算在指定飯店與房型中，所有與查詢日期區間重疊的訂單所訂房間總數。
    @Query("""
                SELECT COALESCE(SUM(oi.quantity), 0)
                FROM OrderItem oi
                JOIN oi.order o
                WHERE oi.roomType.hotel.id = :hotelId
                  AND oi.roomType.id = :roomTypeId
                  AND o.checkInDate < :checkOutDate
                  AND o.checkOutDate > :checkInDate
            """)
    Integer countBookedRooms(
            @Param("hotelId") Long hotelId,
            @Param("roomTypeId") Long roomTypeId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );

}