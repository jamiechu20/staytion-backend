package com.staytion.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.staytion.backend.dto.HistoryRequestDto;
import com.staytion.backend.dto.HistoryResponseDto;
import com.staytion.backend.entity.City;
import com.staytion.backend.entity.District;
import com.staytion.backend.entity.History;
import com.staytion.backend.entity.User;
import com.staytion.backend.projection.HistoryProjection;
import com.staytion.backend.repository.CityRepository;
import com.staytion.backend.repository.DistrictRepository;
import com.staytion.backend.repository.HistoryRepository;
import com.staytion.backend.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class HistoryService {

	private final HistoryRepository historyRepository;

	private final UserRepository userRepository;

	private final CityRepository cityRepository;

	private final DistrictRepository districtRepository;

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
