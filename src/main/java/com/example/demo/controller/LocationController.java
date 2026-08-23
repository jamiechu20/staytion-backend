package com.example.demo.controller;

import java.util.List;
import com.example.demo.dto.response.LocationOption;
import com.example.demo.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LocationController {
    private final LocationService locationService;

    @Operation(
            summary = "查詢所有城市",
            description = "取得所有城市清單,內容包含:label(城市名稱)、value(城市名稱)"
    )    @GetMapping("/cities")
    public List<LocationOption> getCities() {
       return locationService.getAllCities();
    }

    @Operation(
            summary = "查詢指定城市的所有區域",
            description = "根據城市名稱查詢該城市的所有區域,內容包含:label(區域名稱)、value(區域名稱)"
    )
    @GetMapping("/areas")
    public List<LocationOption> getAreaByCity(@RequestParam("city") String cityName) {
        return locationService.getDistrictsByCity(cityName);
    }

    @Operation(
            summary = "取得所有區域清單",
            description = "查詢所有區域,內容包含:label(區域名稱),value(區域名稱),city(所屬城市名稱),常用於前端關鍵字搜尋、自動補齊功能"
    )
    @GetMapping("/areas/all")
    public List<LocationOption> getAllAreas() {
        return locationService.getAllDistricts();
    }

    @Operation(
            summary = "關鍵字模糊搜尋城市或區域",
            description = "根據輸入的關鍵字，模糊搜尋所有城市與區域，回傳名稱與類型"
    )
    @GetMapping("/locations/search")
    public List<LocationOption> searchLocations(@RequestParam("keyword") String keyword) {
        return locationService.searchLocations(keyword);
    }

}
