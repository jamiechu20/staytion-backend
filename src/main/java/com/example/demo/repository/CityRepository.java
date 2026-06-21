package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.City;
import com.example.demo.projection.CityProjection;

public interface CityRepository extends JpaRepository<City, Long> {

	@Query(value = """
			SELECT c.cname, c.img_url, COUNT(h.id) AS hotelCount
			FROM cities c
			JOIN districts d ON c.id = d.city_id
			JOIN hotels h ON d.id = h.district_id
			WHERE c.id = :cid
			GROUP BY c.id, c.cname, c.img_url
			""", nativeQuery = true)

	CityProjection findHotelCountByCityId(@Param("cid") Long cid);

	Optional<City> findByCname(String cname);

	List<City> findByCnameContainingIgnoreCase(String cname);

}
