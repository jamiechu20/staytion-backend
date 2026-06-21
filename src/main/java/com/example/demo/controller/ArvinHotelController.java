package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.request.HotelRequestDto;
import com.example.demo.dto.response.HotelResponseDto;
import com.example.demo.service.ArvinHotelService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin/hotels")
@CrossOrigin(origins = "http://127.0.0.1:5500")
@Tag(name = "Admin Hotel Management", description = "後台飯店管理 API")
public class ArvinHotelController {

	@Autowired
	private ArvinHotelService arvinHotelService;

	@Operation(summary = "取得單一飯店", description = "根據指定的飯店 ID，查詢並回傳該筆飯店的詳細資訊", operationId = "getHotelById")
	@GetMapping("/{id}")
	public HotelResponseDto getById(@PathVariable Long id) {
		return arvinHotelService.getHotelById(id);
	}

	@Operation(summary = "取得指定擁有者的飯店列表", description = "根據擁有者 ID 查詢該使用者所擁有的所有飯店列表資料", operationId = "getHotelsByOwnerId")
	@GetMapping("/owner/{ownerId}")
	public List<HotelResponseDto> getByHotel(@PathVariable Long ownerId) {
		return arvinHotelService.getHotelsByOwner(ownerId);
	}

	@Operation(summary = "新增飯店", description = "建立一筆新的飯店資料，並自動轉換地址為經緯度儲存，若資料正確將回傳建立成功的飯店資料", operationId = "createHotel")
	@PostMapping
	public HotelResponseDto create(@RequestBody HotelRequestDto dto) {
		return arvinHotelService.saveHotel(dto);
	}

	@Operation(summary = "更新飯店", description = "根據指定的飯店 ID，更新該飯店的資訊內容。若地址有變更，將自動更新經緯度", operationId = "updateHotel")
	@PutMapping("/{id}")
	public HotelResponseDto update(@PathVariable Long id, @RequestBody HotelRequestDto dto) {
		return arvinHotelService.updateHotel(id, dto);
	}

	@Operation(summary = "刪除飯店", description = "根據指定的飯店 ID，刪除該筆飯店資料，資料將從資料庫移除", operationId = "deleteHotelById")
	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		arvinHotelService.deleteHotel(id);
	}
}
