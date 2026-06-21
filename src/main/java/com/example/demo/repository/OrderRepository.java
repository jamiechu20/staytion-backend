package com.example.demo.repository;

import com.example.demo.entity.Order;
import com.example.demo.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);

	Page<Order> findByUserId(Long userId, Pageable pageable);

	List<Order> findByStatusAndCreatedAtBefore(Order.OrderStatus status, LocalDateTime createdAt);

    // 取得擁有飯店的所有訂單
    @Query("""
                SELECT DISTINCT o FROM Order o
                JOIN o.orderItems i
                JOIN i.roomType rt
                JOIN rt.hotel h
                WHERE h.owner.id = :ownerId
            """)
    Page<Order> findOrdersByHotelOwner(@Param("ownerId") Long ownerId, Pageable pageable);

    // 取得某飯店所有訂單
    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems i WHERE i.roomType.hotel.id = :hotelId")
    List<Order> findByRoomTypeHotelId(@Param("hotelId") Long hotelId);

    // 狀態日期關鍵字篩選
    @Query("""
                SELECT DISTINCT o FROM Order o
                JOIN o.orderItems i
                JOIN i.roomType rt
                JOIN rt.hotel h
                JOIN o.payment p
                WHERE (
                    o.user.id = :userId OR
                    h.owner.id = :userId
                )
                AND (:hotelId IS NULL OR h.id = :hotelId)
                AND (:status IS NULL OR o.status = :status)
                AND (:start IS NULL OR o.createdAt >= :start)
                AND (:end IS NULL OR o.createdAt <= :end)
                AND (:paymentMethod IS NULL OR p.method = :paymentMethod)
                AND (:paymentStatus IS NULL OR p.status = :paymentStatus)
                AND (
                    :keyword IS NULL OR
                    LOWER(h.hname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                    LOWER(rt.rname) LIKE LOWER(CONCAT('%', :keyword, '%'))
                )
            """)
    Page<Order> searchAccessibleOrdersWithKeyword(@Param("userId") Long userId, @Param("hotelId") Long hotelId,
                                                  @Param("status") Order.OrderStatus status, @Param("start") LocalDateTime start,
                                                  @Param("end") LocalDateTime end, @Param("keyword") String keyword,
                                                  @Param("paymentMethod") Payment.PaymentMethod paymentMethod,
                                                  @Param("paymentStatus") Payment.PaymentStatus paymentStatus, Pageable pageable);

    // 是否已經訂過「同一房型」在「某個入住期間內」的訂單
    @Query("""
                SELECT COUNT(o) > 0
                FROM Order o
                JOIN o.orderItems i
                WHERE o.user.id = :userId
                  AND i.roomType.id = :roomTypeId
                  AND o.checkInDate < :checkOut
                  AND o.checkOutDate > :checkIn
                  AND o.status = com.example.demo.entity.Order$OrderStatus.CONFIRMED
            """)
    boolean existsOverlappingOrder(@Param("userId") Long userId, @Param("roomTypeId") Long roomTypeId,
                                   @Param("checkIn") LocalDate checkIn, @Param("checkOut") LocalDate checkOut);

    // 取得存在訂單的年份（依據 checkInDate）— 限某業主
    @Query("""
                SELECT DISTINCT YEAR(o.checkInDate)
                FROM Order o
                JOIN o.orderItems oi
                JOIN oi.roomType rt
                JOIN rt.hotel h
                WHERE h.owner.id = :ownerId
                ORDER BY YEAR(o.checkInDate)
            """)
    List<Integer> findAvailableYears(@Param("ownerId") Long ownerId);

    // 指定年份後取得有訂單的月份 — 限某業主（依照 checkInDate）
    @Query("""
                SELECT DISTINCT MONTH(o.checkInDate)
                FROM Order o
                JOIN o.orderItems oi
                JOIN oi.roomType rt
                JOIN rt.hotel h
                WHERE h.owner.id = :ownerId
                  AND YEAR(o.checkInDate) = :year
                ORDER BY MONTH(o.checkInDate)
            """)
    List<Integer> findAvailableMonths(@Param("ownerId") Long ownerId, @Param("year") int year);

    // 計算某年月總收入 — 限業主（依照 checkInDate）
    @Query("""
                SELECT SUM(o.totalPrice)
                FROM Order o
                JOIN o.orderItems oi
                JOIN oi.roomType rt
                JOIN rt.hotel h
                WHERE h.owner.id = :ownerId
                  AND YEAR(o.checkInDate) = :year
                  AND MONTH(o.checkInDate) = :month
            """)
    int sumRevenue(@Param("ownerId") Long ownerId, @Param("year") int year, @Param("month") int month);

    // 計算某年月訂單數量 — 限業主（依照 checkInDate）
    @Query("""
                SELECT COUNT(DISTINCT o)
                FROM Order o
                JOIN o.orderItems oi
                JOIN oi.roomType rt
                JOIN rt.hotel h
                WHERE h.owner.id = :ownerId
                  AND YEAR(o.checkInDate) = :year
                  AND MONTH(o.checkInDate) = :month
            """)
    int countOrders(@Param("ownerId") Long ownerId, @Param("year") int year, @Param("month") int month);

    // 各房型訂單數統計 — 原生查詢，限業主（依照 checkInDate）
    @Query(nativeQuery = true, value = """
                SELECT rt.rname AS label, COUNT(*) AS value
                FROM orders o
                JOIN order_items oi ON o.id = oi.order_id
                JOIN room_types rt ON oi.room_type_id = rt.id
                JOIN hotels h ON rt.hotel_id = h.id
                WHERE h.owner_id = :ownerId
                  AND YEAR(o.check_in_date) = :year
                  AND MONTH(o.check_in_date) = :month
                GROUP BY rt.rname
            """)
    List<Map<String, Object>> getRoomTypeChart(@Param("ownerId") Long ownerId, @Param("year") int year,
                                               @Param("month") int month);

    // 每日收入趨勢 — 原生查詢，限業主（依照 checkInDate）
    @Query(nativeQuery = true, value = """
                SELECT DAY(o.check_in_date) AS label, SUM(o.total_price) AS value
                FROM orders o
                JOIN order_items oi ON o.id = oi.order_id
                JOIN room_types rt ON oi.room_type_id = rt.id
                JOIN hotels h ON rt.hotel_id = h.id
                WHERE h.owner_id = :ownerId
                  AND YEAR(o.check_in_date) = :year
                  AND MONTH(o.check_in_date) = :month
                GROUP BY DAY(o.check_in_date)
                ORDER BY DAY(o.check_in_date)
            """)
    List<Map<String, Object>> getTrendChart(@Param("ownerId") Long ownerId, @Param("year") int year,
                                            @Param("month") int month);

    // 月報表：某業主某年每月收入統計（依照 createdAt）
    @Query("""
                SELECT FUNCTION('DATE_FORMAT', o.createdAt, '%Y-%m') as month,
                       SUM(o.totalPrice) as totalRevenue
                FROM Order o
                JOIN o.orderItems oi
                JOIN oi.roomType rt
                JOIN rt.hotel h
                WHERE h.owner.id = :ownerId
                  AND FUNCTION('YEAR', o.createdAt) = :year
                GROUP BY FUNCTION('DATE_FORMAT', o.createdAt, '%Y-%m')
                ORDER BY month
            """)
    List<Map<String, Object>> getMonthlyRevenue(@Param("ownerId") Long ownerId, @Param("year") int year);

    // 訂單趨勢：業主指定區間內每日訂單數（依照 createdAt）
    @Query("""
                SELECT FUNCTION('DATE', o.createdAt) as date,
                       COUNT(DISTINCT o.id) as orderCount
                FROM Order o
                JOIN o.orderItems oi
                JOIN oi.roomType rt
                JOIN rt.hotel h
                WHERE h.owner.id = :ownerId
                  AND o.createdAt BETWEEN :start AND :end
                GROUP BY FUNCTION('DATE', o.createdAt)
                ORDER BY date
            """)
    List<Map<String, Object>> getOrderTrend(@Param("ownerId") Long ownerId, @Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);

    // 年度總營收
    @Query("""
                SELECT COALESCE(SUM(o.totalPrice), 0)
                FROM Order o
                JOIN o.orderItems oi
                JOIN oi.roomType rt
                JOIN rt.hotel h
                WHERE h.owner.id = :ownerId
                  AND YEAR(o.checkInDate) = :year
            """)
    Integer sumYearRevenue(@Param("ownerId") Long ownerId, @Param("year") int year);

    // 累積總營收
    @Query("""
                SELECT COALESCE(SUM(o.totalPrice), 0)
                FROM Order o
                JOIN o.orderItems oi
                JOIN oi.roomType rt
                JOIN rt.hotel h
                WHERE h.owner.id = :ownerId
            """)
    Integer sumTotalRevenue(@Param("ownerId") Long ownerId);

    // 每日訂單
    @Query("""
                SELECT o.checkInDate AS date, SUM(oi.quantity) AS bookedRooms
                FROM Order o
                JOIN o.orderItems oi
                JOIN oi.roomType rt
                JOIN rt.hotel h
                WHERE h.owner.id = :ownerId
                  AND o.checkInDate BETWEEN :start AND :end
                GROUP BY o.checkInDate
                ORDER BY o.checkInDate
            """)
    List<Map<String, Object>> findDailyBookedRooms(@Param("ownerId") Long ownerId, @Param("start") LocalDate start,
                                                   @Param("end") LocalDate end);

}