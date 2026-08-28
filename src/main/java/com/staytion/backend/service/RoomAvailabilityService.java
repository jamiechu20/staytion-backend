package com.staytion.backend.service;

import com.staytion.backend.dto.RoomAvailabilityCheckResponse;
import com.staytion.backend.dto.request.AvailableRoomQueryDto;
import com.staytion.backend.dto.response.AvailableRoomResponseDto;
import com.staytion.backend.entity.OrderItem;
import com.staytion.backend.entity.RoomAvailability;
import com.staytion.backend.entity.RoomType;
import com.staytion.backend.repository.OrderItemRepository;
import com.staytion.backend.repository.RoomAvailabilityRepository;
import com.staytion.backend.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomAvailabilityService {

    private final RoomAvailabilityRepository roomAvailabilityRepository;

    private final RoomTypeRepository roomTypeRepository;

    private final OrderItemRepository orderItemRepository;


    public Optional<RoomAvailability>
    findByRoomTypeAndDate(Long roomTypeId, LocalDate date) {
        return roomAvailabilityRepository.findByRoomType_IdAndDate(roomTypeId, date);
    }

    public List<RoomAvailabilityCheckResponse>
    findByRoomTypeAndDateBetween(Long roomTypeId, LocalDate start, LocalDate end) {
        List<RoomAvailability> entities = roomAvailabilityRepository.findByRoomType_IdAndDateBetween(roomTypeId, start, end);

        return entities.stream()
                .map(e -> new RoomAvailabilityCheckResponse(
                        e.getId(),
                        e.getRoomType().getId(),
                        e.getDate(),
                        e.getAvailableQuantity()
                ))
                .collect(java.util.stream.Collectors.toList());
    }

    public int getAvailableCount(Long roomTypeId, LocalDate checkInDate, LocalDate checkOutDate) {
        List<RoomAvailability> availList = roomAvailabilityRepository.findByRoomType_IdAndDateBetween(
                roomTypeId, checkInDate, checkOutDate.minusDays(1)); // checkout 不含最後一天

        if (availList.isEmpty()) {
            return 0;
        }
        return availList.stream()
                .mapToInt(RoomAvailability::getAvailableQuantity)
                .min()
                .orElse(0);
    }

    // 更新訂單
    public int getAvailableCount(Long roomTypeId, LocalDate checkIn, LocalDate checkOut, Long excludeOrderId) {
        int totalRooms = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new RuntimeException("找不到房型"))
                .getQuantity(); // 假設你有這個欄位表示總房間數

        // 查找所有其他訂單中已訂的數量（不包含此訂單）
        List<OrderItem> overlappingItems = orderItemRepository
                .findOverlappingItems(roomTypeId, checkIn, checkOut, excludeOrderId);

        int booked = overlappingItems.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();

        return totalRooms - booked;
    }

    //查詢訂單更新房型可選的房型選項
    public List<AvailableRoomResponseDto> findAvailableRooms(AvailableRoomQueryDto queryDto) {

        Long hotelId = queryDto.getHotelId();
        LocalDate checkInDate = queryDto.getCheckInDate();
        LocalDate checkOutDate = queryDto.getCheckOutDate();

        // 查出該飯店所有房型
        List<RoomType> roomTypes = roomTypeRepository.findByHotelId(hotelId);
        List<AvailableRoomResponseDto> result = new ArrayList<>();

        for (RoomType roomType : roomTypes) {
            Long roomTypeId = roomType.getId();

            // 查該房型，該日期區間內已被預訂幾間房
            Integer bookedCount = orderItemRepository.countBookedRooms(
                    hotelId,
                    roomTypeId,
                    checkInDate,
                    checkOutDate
            );

            // 該房型總房間數
            Integer totalRooms = roomTypeRepository.countByRoomTypeId(roomTypeId);

            int availableCount = totalRooms - bookedCount;

            if (availableCount > 0) {
                result.add(new AvailableRoomResponseDto(
                        roomType.getId(),
                        roomType.getRname(),
                        roomType.getCapacity(),
                        roomType.getPrice(),
                        availableCount,
                        roomType.getImgUrl()
                ));
            }
        }

        return result;
    }

}
