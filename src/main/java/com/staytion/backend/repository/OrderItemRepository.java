package com.staytion.backend.repository;

import com.staytion.backend.entity.OrderItem;
import com.staytion.backend.projection.RoomTypeBookedCountProjection;
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

    //    計算指定飯店中，各房型在查詢日期區間內（與訂單日期重疊）已被訂走的房間總數。
    //    以 GROUP BY 一次算出所有房型，沒有任何訂單的房型不會出現在結果中。
    @Query("""
                SELECT oi.roomType.id AS roomTypeId, SUM(oi.quantity) AS bookedCount
                FROM OrderItem oi
                JOIN oi.order o
                WHERE oi.roomType.hotel.id = :hotelId
                  AND o.checkInDate < :checkOutDate
                  AND o.checkOutDate > :checkInDate
                GROUP BY oi.roomType.id
            """)
    List<RoomTypeBookedCountProjection> countBookedRoomsByHotel(
            @Param("hotelId") Long hotelId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );

}