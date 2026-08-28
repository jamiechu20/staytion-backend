package com.staytion.backend.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.staytion.backend.dto.AmenityDTO;
import com.staytion.backend.service.AmenityService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/amenities")
public class AmenityController {

    private final AmenityService amenityService;

    @Operation(
            summary = "取得所有設施資料",
            description = "查詢所有設施資訊,內容包含:設施ID、名稱"
    )
    @GetMapping
    public List<AmenityDTO> getAllAmenities() {
        return amenityService.getAllAmenities();
    }
}