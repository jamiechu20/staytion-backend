package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.request.RoomTypeRequestDto;
import com.example.demo.dto.response.RoomTypeResponseDto;
import com.example.demo.service.ArvinRoomTypeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
@RestController
@RequestMapping("/api/admin/roomTypes")
@CrossOrigin(origins = "http://127.0.0.1:5500")
@Tag(name = "Admin RoomType Management", description = "後台房型管理 API")
public class ArvinRoomTypeController {

    @Autowired
    private ArvinRoomTypeService arvinRoomTypeService;

    @Operation(
    	    summary = "取得單一房型",
    	    description = "根據房型 ID 回傳房型詳細資料",
    	    operationId = "getRoomTypeById"
    	)
    	@GetMapping("/{id}")
    	public RoomTypeResponseDto getById(@PathVariable Long id) {
    	    return arvinRoomTypeService.getRoomTypeById(id);
    	}


    @Operation(
        summary = "取得指定飯店的房型清單",
        description = "根據飯店 ID，回傳該飯店所擁有的所有房型",
        operationId = "getRoomTypesByHotelId"
    )
    @GetMapping("/hotel/{hotelId}")
    public List<RoomTypeResponseDto> getByHotel(@PathVariable Long hotelId) {
        return arvinRoomTypeService.getRoomTypesByHotel(hotelId);
    }

    @Operation(
        summary = "搜尋房型（關鍵字 + 分頁）",
        description = "可根據關鍵字搜尋房型名稱，並支援分頁查詢功能",
        operationId = "searchRoomTypes"
    )
    @GetMapping("/search")
    public Page<RoomTypeResponseDto> searchRoomTypes(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return arvinRoomTypeService.search(keyword, page, size);
    }

    @Operation(
        summary = "新增房型",
        description = "建立一筆新的房型資料，需提供飯店 ID、房型名稱、價格、床型等必要欄位",
        operationId = "createRoomType"
    )
    @PostMapping
    public RoomTypeResponseDto createOrUpdate(@RequestBody RoomTypeRequestDto dto) {
        return arvinRoomTypeService.saveRoomType(dto);
    }

    @Operation(
        summary = "更新房型",
        description = "根據房型 ID 修改現有房型的資訊，如價格、床型、數量等",
        operationId = "updateRoomType"
    )
    @PutMapping("/{id}")
    public RoomTypeResponseDto update(@PathVariable Long id, @RequestBody RoomTypeRequestDto dto) {
        return arvinRoomTypeService.updateRoomType(id, dto);
    }

    @Operation(
        summary = "刪除房型",
        description = "根據房型 ID 刪除對應的房型資料，資料將從資料庫中移除",
        operationId = "deleteRoomTypeById"
    )
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        arvinRoomTypeService.deleteRoomType(id);
    }

    /*
    @Operation(
        summary = "取得指定使用者的房型統計",
        description = "根據擁有者 ID 以及統計類型，回傳房型的統計資訊",
        operationId = "getRoomTypeSummaryByOwner"
    )
    @GetMapping("/summary/owner/{ownerId}")
    public List<RoomTypeSummaryDto> getSummaryByOwner(
        @PathVariable Long ownerId,
        @RequestParam String type
    ) {
        return roomTypeService.getOwnerRoomTypeSummary(ownerId, type);
    }

    @Operation(
        summary = "取得指定使用者的綜合房型統計",
        description = "根據擁有者 ID 以及統計類型，回傳統整的房型統計資訊",
        operationId = "getCombinedRoomTypeSummaryByOwner"
    )
    @GetMapping("/summary/owner/{ownerId}/combined")
    public RoomTypeSummaryDto getCombinedSummary(
        @PathVariable Long ownerId,
        @RequestParam String type
    ) {
        return roomTypeService.getCombinedRoomTypeSummary(ownerId, type);
    }
    */
}
     