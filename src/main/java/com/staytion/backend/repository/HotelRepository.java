package com.staytion.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.staytion.backend.entity.Hotel;
import com.staytion.backend.projection.HotelProjection;

public interface HotelRepository extends JpaRepository<Hotel, Long>, JpaSpecificationExecutor<Hotel> {
    List<Hotel> findByOwnerId(Long ownerId);

    List<Hotel> findByHnameContaining(String keyword);

    Optional<Hotel> findByHname(String hname);

	@EntityGraph(attributePaths = {"district", "district.city"})
	List<Hotel> findByHnameContainingIgnoreCase(String keyword);

	// 搜尋飯店：ToOne 關聯（district、city）直接 fetch join，分頁不受影響；
	// 一對多的 roomTypes/reviews/images 不能 fetch join（會變成記憶體分頁），交給 default_batch_fetch_size 批次載入
	@Override
	@EntityGraph(attributePaths = {"district", "district.city"})
	Page<Hotel> findAll(Specification<Hotel> spec, Pageable pageable);

	@Query(value = """
			SELECT
			    h.id AS hotelId,
			    h.hname AS hotelName,
			    c.cname AS cityName,
			    d.dname AS districtName,
			    COUNT(DISTINCT r.user_id) AS reviewCount,
			    MIN(ro.price) AS lowestPrice,
			    ROUND(AVG(r.score), 1) AS averageRating,
			    i.img_url AS coverImageUrl
			FROM hotels h
			JOIN districts d ON h.district_id = d.id
			JOIN cities c ON c.id = d.city_id
			LEFT JOIN images i ON i.hotel_id = h.id AND i.is_cover = true
			LEFT JOIN reviews r ON r.hotel_id = h.id
			LEFT JOIN room_types ro ON ro.hotel_id = h.id
			GROUP BY h.id, h.hname, c.cname, d.dname, i.img_url
			ORDER BY averageRating DESC
			LIMIT 10

			""", nativeQuery = true)

	List<HotelProjection> findTopHotels();

    //查詢飯店縣市和區域
    @Query("""
                SELECT h
                FROM Hotel h
                JOIN FETCH h.district d
                JOIN FETCH d.city c
                WHERE h.id IN :ids
            """)
    List<Hotel> findHotelsWithDistrictAndCity(@Param("ids") List<Long> hotelIds);


}
