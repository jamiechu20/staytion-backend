package com.example.demo.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.projection.CityProjection;
import com.example.demo.repository.CityRepository;


@Service
public class CityService {

	@Autowired
	private CityRepository cityRepository;

	public Map<String, Object> getCityHotelCount(Long cityId) {
		CityProjection data = cityRepository.findHotelCountByCityId(cityId);

		Map<String, Object> result = new HashMap<>();
		result.put("city", data.getCname());
		result.put("img_url", data.getImgUrl());
		result.put("hotelCount", data.getHotelCount());
		return result;
	}
}
