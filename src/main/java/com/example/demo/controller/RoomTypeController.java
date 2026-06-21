package com.example.demo.controller;

import java.util.List;

import com.example.demo.entity.RoomType;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.RoomTypeDTO;
import com.example.demo.service.RoomTypeService;

@RestController
@RequestMapping("/api")
public class RoomTypeController {

    @Autowired
    private RoomTypeService roomTypeService;

    @Operation(
            summary = "查詢所有房型",
            description = "取得所有房型資料,內容包含:房型ID、飯店ID、房型名稱、價格、描述、大小、景觀、主圖、可訂房數、床型、床數、容量以及該房型設施"
    )
    @GetMapping("/room-types")
    public List<RoomTypeDTO> getAllRoomTypes() {
        return roomTypeService.getAllRoomTypeDtos();
    }

    @Operation(
            summary = "查詢單一飯店的所有房型",
            description = "依飯店ID查詢該飯店的所有房型,內容包含:房型ID、飯店ID、房型名稱、價格、描述、大小、景觀、主圖、可訂房數、床型、床數、容量以及該房型設施"
    )
    @GetMapping("/hotels/{hotelId}/room-types")
    public List<RoomTypeDTO> getRoomTypesByHotel(@PathVariable Long hotelId) {
        return roomTypeService.findRoomTypesByHotel(hotelId);
    }

    @Operation(
            summary = "查詢單一飯店的所有可容納指定人數的房型",
            description = "依飯店ID及入住人數，查詢該飯店所有可容納指定人數的房型,內容包含:房型ID、飯店ID、房型名稱、價格、描述、大小、景觀、主圖、可訂房數、床型、床數、容量該房型設施"
    )
    @GetMapping("/hotels/{hotelId}/room-types/filter")
    public List<RoomTypeDTO> getRoomTypesByHotelAndCapacity(
            @PathVariable Long hotelId,
            @RequestParam Integer adults,
            @RequestParam(required = false, defaultValue = "0") Integer children
    ) {
        int people = adults + children;
        return roomTypeService.findRoomTypesByHotelAndCapacity(hotelId, people);
    }

    @Operation(
            summary = "查詢單一房型詳細資料",
            description = "依房型ID查詢該房型的詳細資訊,內容包含:房型ID、飯店ID、名稱、價格、描述、大小、景觀、主圖、床型、容量、設施"
    )
    @GetMapping("/room_types/{id}")
    public ResponseEntity<RoomType> getRoomTypeById(@PathVariable Long id) {
        RoomType roomType = roomTypeService.getRoomTypeById(id);
        if (roomType !=null) {
            return ResponseEntity.ok(roomType);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}