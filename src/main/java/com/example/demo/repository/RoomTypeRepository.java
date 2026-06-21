package com.example.demo.repository;

import com.example.demo.entity.RoomType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
    List<RoomType> findByHotelId(Long hotelId);

    Page<RoomType> findByRnameContaining(String keyword, Pageable pageable);

    //	資料庫層級鎖，避免多個交易同時搶同一房型
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RoomType r WHERE r.id IN :ids")
    List<RoomType> findAllByIdWithLock(@Param("ids") List<Long> ids);


//	查詢房型總和
/*
	@Query("""
		    SELECT new com.example.demo.dto.response.RoomTypeSummaryDto(
		        h.id,
		        h.hname,
		        r.rname,
		        COUNT(r),
		        SUM(CASE WHEN r.isCanceled = false THEN r.quantity ELSE 0 END),
		        false,
		        AVG(r.price)
		    )
		    FROM RoomType r
		    JOIN r.hotel h
		    WHERE h.owner.id = :ownerId AND r.rname = :rname
		    GROUP BY h.id, h.hname, r.rname
		""")
		List<RoomTypeSummaryDto> summarizeByOwnerAndRoomType(@Param("ownerId") Long ownerId, @Param("rname") String rname);

	@Query("""
		    SELECT new com.example.demo.dto.response.RoomTypeSummaryDto(
		        r.rname,
		        COUNT(r),
		        SUM(CASE WHEN r.isCanceled = false THEN r.quantity ELSE 0 END),
		        AVG(r.price)
		    )
		    FROM RoomType r
		    WHERE r.hotel.owner.id = :ownerId AND r.rname = :rname
		    GROUP BY r.rname
		""")
		RoomTypeSummaryDto summarizeTotalByOwnerAndRoomType(
		    @Param("ownerId") Long ownerId,
		    @Param("rname") String rname
		);
*/

    List<RoomType> findByHotelIdAndCapacityGreaterThanEqual(Long hotelId, Integer capacity);

    //查詢飯店最低價
    @Query("""
                SELECT MIN(rt.price)
                FROM RoomType rt
                WHERE rt.hotel.id = :hotelId
            """)
    Integer findMinPriceByHotelId(@Param("hotelId") Long hotelId);

    // 查詢業主底下所有飯店房型的總房數
    @Query("""
                SELECT SUM(rt.quantity)
                FROM RoomType rt
                JOIN rt.hotel h
                WHERE h.owner.id = :ownerId
            """)
    Integer sumTotalRoomsByOwner(@Param("ownerId") Long ownerId);


    // 取得指定房型的總房間數 (quantity 欄位)。
    @Query("SELECT r.quantity FROM RoomType r WHERE r.id = :id")
    Integer countByRoomTypeId(@Param("id") Long id);
}
