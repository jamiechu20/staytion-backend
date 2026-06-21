package com.example.demo.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.demo.dto.request.RoomTypeRequestDto;
import com.example.demo.dto.response.RoomTypeResponseDto;
import com.example.demo.entity.Amenity;
import com.example.demo.entity.Hotel;
import com.example.demo.entity.RoomType;
import com.example.demo.repository.AmenityRepository;
import com.example.demo.repository.HotelRepository;
import com.example.demo.repository.RoomTypeRepository;

@Service
public class ArvinRoomTypeService {

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private AmenityRepository amenityRepository;

    // 查詢全部房型
    public List<RoomTypeResponseDto> getAllRoomTypes() {
        return roomTypeRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    // 查詢指定房型
    public RoomTypeResponseDto getRoomTypeById(Long id) {
        RoomType roomType = roomTypeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("RoomType not found"));

        return toDto(roomType); 
    }

    // 查詢指定飯店的房型
    public List<RoomTypeResponseDto> getRoomTypesByHotel(Long hotelId) {
        return roomTypeRepository.findByHotelId(hotelId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // 關鍵字搜尋 + 分頁
    public Page<RoomTypeResponseDto> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RoomType> roomPage = roomTypeRepository.findByRnameContaining(keyword, pageable);
        return roomPage.map(this::toDto);
    }

    // 新增
    public RoomTypeResponseDto saveRoomType(RoomTypeRequestDto dto) {
        Hotel hotel = hotelRepository.findById(dto.getHotelId())
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + dto.getHotelId()));

        Set<Amenity> amenities = dto.getAmenityIds() == null
                ? new HashSet<>()
                : new HashSet<>(amenityRepository.findAllById(dto.getAmenityIds()));

        RoomType room = toEntity(dto, hotel, amenities);
        RoomType saved = roomTypeRepository.save(room);
        return toDto(saved);
    }

    // 更新
    public RoomTypeResponseDto updateRoomType(Long id, RoomTypeRequestDto dto) {
        RoomType room = roomTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RoomType not found with id: " + id));

        Hotel hotel = hotelRepository.findById(dto.getHotelId())
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + dto.getHotelId()));

        Set<Amenity> amenities = dto.getAmenityIds() == null
                ? new HashSet<>()
                : new HashSet<>(amenityRepository.findAllById(dto.getAmenityIds()));

        System.out.println("amenityIds: " + dto.getAmenityIds());
        System.out.println("查到的Amenity: " + amenities.size());

        room.setRname(dto.getRname());
        room.setPrice(dto.getPrice());
        room.setDescription(dto.getDescription());
        room.setSize(dto.getSize());
        room.setView(dto.getView());
        room.setImgUrl(dto.getImgUrl());
        room.setIsCanceled(dto.getIsCanceled());
        room.setQuantity(dto.getQuantity());
        room.setBedCount(dto.getBedCount());
        room.setBedType(dto.getBedType());
        room.setCapacity(dto.getCapacity());
        room.setHotel(hotel);
        room.setAmenities(amenities);

        RoomType saved = roomTypeRepository.save(room);
        System.out.println("儲存後 amenities: " + saved.getAmenities().size());
        return toDto(saved);
    }

    // 刪除
    public void deleteRoomType(Long id) {
        if (!roomTypeRepository.existsById(id)) {
            throw new RuntimeException("RoomType not found with id: " + id);
        }
        roomTypeRepository.deleteById(id);
    }

    // 查詢房型總覽
    /*
    public List<RoomTypeSummaryDto> getOwnerRoomTypeSummary(Long ownerId, String rname) {
        return roomTypeRepository.summarizeByOwnerAndRoomType(ownerId, rname);
    }
    public RoomTypeSummaryDto getCombinedRoomTypeSummary(Long ownerId, String rname) {
        return roomTypeRepository.summarizeTotalByOwnerAndRoomType(ownerId, rname);
    }
	*/

    
    
    // 將 RequestDto 轉換成 Entity
    private RoomType toEntity(RoomTypeRequestDto dto, Hotel hotel, Set<Amenity> amenities) {
        RoomType room = new RoomType();
        room.setRname(dto.getRname());
        room.setPrice(dto.getPrice());
        room.setDescription(dto.getDescription());
        room.setSize(dto.getSize());
        room.setView(dto.getView());
        room.setImgUrl(dto.getImgUrl());
        room.setIsCanceled(dto.getIsCanceled());
        room.setQuantity(dto.getQuantity());
        room.setBedCount(dto.getBedCount());
        room.setBedType(dto.getBedType());
        room.setCapacity(dto.getCapacity());
        room.setHotel(hotel);
        room.setAmenities(amenities);
        return room;
    }

    // 將 Entity 轉換為 ResponseDto
    private RoomTypeResponseDto toDto(RoomType room) {
        RoomTypeResponseDto dto = new RoomTypeResponseDto();
        dto.setId(room.getId());
        dto.setHotelId(room.getHotel().getId());
        dto.setHotelName(room.getHotel().getHname());
        dto.setRname(room.getRname());
        dto.setPrice(room.getPrice());
        dto.setDescription(room.getDescription());
        dto.setSize(room.getSize());
        dto.setView(room.getView());
        dto.setImgUrl(room.getImgUrl());
        dto.setIsCanceled(room.getIsCanceled());
        dto.setQuantity(room.getQuantity());
        dto.setBedCount(room.getBedCount());
        dto.setBedType(room.getBedType());
        dto.setCapacity(room.getCapacity());

        Set<String> amenityNames = room.getAmenities()
                .stream()
                .map(Amenity::getAname)
                .collect(Collectors.toSet());
        dto.setAmenities(amenityNames);

        return dto;
    }
}
