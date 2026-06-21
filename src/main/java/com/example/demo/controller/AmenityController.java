package com.example.demo.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.AmenityDTO;
import com.example.demo.service.AmenityService;

@RestController
@RequestMapping("/api/amenities")
@CrossOrigin(origins = "http://127.0.0.1:5500")
public class AmenityController {
    @Autowired
    private AmenityService amenityService;

    @Operation(
            summary = "取得所有設施資料",
            description = "查詢所有設施資訊,內容包含:設施ID、名稱"
    )
    @GetMapping
    public List<AmenityDTO> getAllAmenities() {
        return amenityService.getAllAmenities();
    }
}