package com.staytion.backend.service;

import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.staytion.backend.projection.CityProjection;
import com.staytion.backend.repository.CityRepository;


@Service
@RequiredArgsConstructor
public class CityService {

	private final CityRepository cityRepository;

	public Map<String, Object> getCityHotelCount(Long cityId) {
		CityProjection data = cityRepository.findHotelCountByCityId(cityId);

		Map<String, Object> result = new HashMap<>();
		result.put("city", data.getCname());
		result.put("img_url", data.getImgUrl());
		result.put("hotelCount", data.getHotelCount());
		return result;
	}
}
