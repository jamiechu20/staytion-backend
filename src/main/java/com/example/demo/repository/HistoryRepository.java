package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.entity.History;
import com.example.demo.projection.HistoryProjection;

public interface HistoryRepository extends JpaRepository<History, Long> {
	@Query(value = """
			    SELECT
			        CASE
			            WHEN h.city_id IS NOT NULL THEN c.cname
			            WHEN h.district_id IS NOT NULL THEN d.dname
			        END AS locationName,
			        CASE
			            WHEN h.city_id IS NOT NULL THEN c.img_url
			            WHEN h.district_id IS NOT NULL THEN city_for_district.img_url
			        END AS imgUrl,
			        h.check_in_date AS checkInDate,
			        h.check_out_date AS checkOutDate,
			        (h.adults + h.kids) AS total
			    FROM histories h
			    LEFT JOIN cities c ON h.city_id = c.id
			    LEFT JOIN districts d ON h.district_id = d.id
			    LEFT JOIN cities city_for_district ON d.city_id = city_for_district.id
			    WHERE h.user_id = :userId
			    ORDER BY h.search_time DESC
			""", nativeQuery = true)
	List<HistoryProjection> findHistoriesByUserId(Long userId);
}
