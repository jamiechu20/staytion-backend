package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.HotelDetailDTO;
import com.example.demo.dto.HotelSearchRequest;
import com.example.demo.dto.HotelSearchResult;
import com.example.demo.entity.Hotel;
import com.example.demo.service.HotelService;

@RestController
@RequestMapping("/api/hotels")
@CrossOrigin(origins = "http://127.0.0.1:5500")
public class HotelController {

    @Autowired
    private HotelService hotelService;

    @Operation(
            summary = "查詢飯店列表",
            description = "依照城市、區域、入住/退房、入住人數、設施、價格、評分等條件查詢飯店列表" +
                    "內容包含:飯店ID、名稱、城市、區域、封面圖、經緯度、地圖連結、房型名稱、最低價格、平均評分、住宿晚數與入住人數、分頁資訊等"
    )
    @GetMapping
    public Map<String, Object> searchHotels(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String checkin,
            @RequestParam(required = false) String checkout,
            @RequestParam(required = false) Integer adult,
            @RequestParam(required = false) Integer child,
            @RequestParam(required = false) Integer room,
            @RequestParam(required = false) String price,
            @RequestParam(required = false) List<Long> amenity,
            @RequestParam(required = false) Integer score,
            @RequestParam(required = false) String sort,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        HotelSearchResult resultDTO = new HotelSearchResult();
        resultDTO.setCity(city);
        resultDTO.setArea(area);
        resultDTO.setCheckin(checkin);
        resultDTO.setCheckout(checkout);
        resultDTO.setAdult(adult);
        resultDTO.setChild(child);
        resultDTO.setRoom(room);
        resultDTO.setPrice(price);
        resultDTO.setAmenity(amenity);
        resultDTO.setScore(score);
        resultDTO.setSort(sort);

        Page<HotelSearchRequest> hotelsPage = hotelService.searchHotels(resultDTO, page, size);
        Map<String, Object> rs = new HashMap<>();
        rs.put("hotels", hotelsPage.getContent());
        rs.put("total", hotelsPage.getTotalElements());
        rs.put("page", hotelsPage.getNumber() + 1);
        rs.put("size", hotelsPage.getSize());
        return rs;
    }

    @Operation(
            summary = "查詢符合條件的飯店總數",
            description = "依照查詢條件(城市、區域、入住/退房、設施、價格、評分等)統計符合條件的飯店數量,內容包含:篩選過的count(數量)"
    )
    @GetMapping("/count")
    public Map<String, Long> getHotelCount(HotelSearchResult resultDTO) {
        Long count = hotelService.searchHotels(resultDTO, 1, Integer.MAX_VALUE).getTotalElements();
        Map<String, Long> result = new HashMap<>();
        result.put("count", count);
        return result;
    }

    @Operation(
            summary = "查詢飯店詳情",
            description = "依飯店ID查詢該飯店的完整詳細資料,內容包含:飯店ID、名稱、地址、描述、平均評分、所有圖片、各房型資訊(名稱、描述、價格、床型、大小、設施、各房型圖片)"
    )
    @GetMapping("/{hotelId}")
    public HotelDetailDTO getHotelDetail(@PathVariable Long hotelId) {
        return hotelService.getHotelDetail(hotelId);
    }
    
    
    @GetMapping("/search")
    public List<HotelSearchRequest> searchHotels(
        @RequestParam("keyword") String keyword,
        @RequestParam(value = "highlight_hotel_id", required = false) Long highlightHotelId
    ) {
    	return hotelService.searchHotelsByName(keyword, highlightHotelId);
    }
    
}
