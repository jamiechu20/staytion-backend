package com.staytion.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.staytion.backend.dto.HistoryRequestDto;
import com.staytion.backend.dto.HistoryResponseDto;
import com.staytion.backend.entity.History;
import com.staytion.backend.projection.HistoryProjection;
import com.staytion.backend.projection.HotelProjection;
import com.staytion.backend.service.CityService;
import com.staytion.backend.service.HistoryService;
import com.staytion.backend.service.HotelService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class HomeController {

	private final HistoryService historyService;
	private final HotelService hotelService;
	private final CityService cityService;

	// 查詢搜尋紀錄
	@Operation(summary = "查詢用戶的搜尋紀錄", description = "根據用戶 ID 查詢該用戶的所有搜尋紀錄")
	@GetMapping("/histories/{userId}")
	public List<HistoryProjection> getHistoriesByUserId(@PathVariable Long userId) {
		return historyService.getHistoriesByUserId(userId);
	}

	// 增加搜尋紀錄
	@Operation(summary = "新增用戶的搜尋紀錄", description = "根據用戶 ID 和搜尋條件新增一條搜尋紀錄")
	@PostMapping("/histories/{userId}")
	public ResponseEntity<HistoryResponseDto> createHistory(@PathVariable Long userId,
			@RequestBody HistoryRequestDto dto) {

		dto.setUserId(userId);
		HistoryResponseDto response = historyService.saveHistory(dto);
		return ResponseEntity.ok(response);
	}

	// 查詢精選飯店
	@Operation(summary = "查詢精選飯店", description = "查詢排名前 10 的精選飯店，根據評分排序")
	@GetMapping("/top-hotels")
	public ResponseEntity<List<HotelProjection>> getTopHotels() {
		List<HotelProjection> hotels = hotelService.getTopHotels();
		return ResponseEntity.ok(hotels);
	}

	// 查詢熱門城市資料
	@Operation(summary = "查詢熱門城市的飯店數量", description = "根據城市 ID 查詢該城市的飯店數量和其他相關資料")
	@GetMapping("/city/{id}/hotel-count")
	public ResponseEntity<Map<String, Object>> getCityHotelCount(@PathVariable Long id) {
		return ResponseEntity.ok(cityService.getCityHotelCount(id));
	}

}
