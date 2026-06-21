package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.HistoryRequestDto;
import com.example.demo.dto.HistoryResponseDto;
import com.example.demo.entity.City;
import com.example.demo.entity.District;
import com.example.demo.entity.History;
import com.example.demo.entity.User;
import com.example.demo.projection.HistoryProjection;
import com.example.demo.repository.CityRepository;
import com.example.demo.repository.DistrictRepository;
import com.example.demo.repository.HistoryRepository;
import com.example.demo.repository.UserRepository;

@Service
public class HistoryService {

	@Autowired
	private HistoryRepository historyRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CityRepository cityRepository;

	@Autowired
	private DistrictRepository districtRepository;

	public List<HistoryProjection> getHistoriesByUserId(Long userId) {
		return historyRepository.findHistoriesByUserId(userId);
	}

	public HistoryResponseDto saveHistory(HistoryRequestDto dto) {
		User user = userRepository.findById(dto.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));

		History history = new History();
		history.setCheckInDate(dto.getCheckInDate());
		history.setCheckOutDate(dto.getCheckOutDate());
		history.setAdults(dto.getAdults());
		history.setKids(dto.getKids());
		history.setSearchTime(dto.getSearchTime() != null ? dto.getSearchTime() : LocalDateTime.now());
		history.setUser(user);

		// 檢查是否為城市或區域
		if (dto.getLocationType().equals("city")) {
			City city = cityRepository.findByCname(dto.getLocationName())
					.orElseThrow(() -> new RuntimeException("City not found"));
			history.setCity(city); // 設定 city_id
		} else if (dto.getLocationType().equals("district")) {
			District district = districtRepository.findByDname(dto.getLocationName())
					.orElseThrow(() -> new RuntimeException("District not found"));
			history.setDistrict(district); // 設定 district_id
		} else {
			throw new RuntimeException("Invalid location type");
		}

		History saved = historyRepository.save(history);

		HistoryResponseDto response = new HistoryResponseDto();
		if (history.getCity() != null) {
			response.setCityName(history.getCity().getCname());
			response.setCityImageUrl(history.getCity().getImgUrl());
		} else if (history.getDistrict() != null) {
			response.setCityName(
					history.getDistrict().getDname() + "（" + history.getDistrict().getCity().getCname() + "）");
			response.setCityImageUrl(history.getDistrict().getCity().getImgUrl());
		}
	
		response.setCheckInDate(saved.getCheckInDate());
		response.setCheckOutDate(saved.getCheckOutDate());
		response.setTotal(saved.getAdults() + saved.getKids());
		response.setSearchTime(saved.getSearchTime());

		return response;
	}
}
