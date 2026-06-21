package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.AmenityDTO;
import com.example.demo.dto.RoomTypeDTO;
import com.example.demo.entity.RoomType;
import com.example.demo.repository.RoomTypeRepository;

@Service
public class RoomTypeService {

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    public List<RoomTypeDTO> findRoomTypesByHotel(Long hotelId) {
        List<RoomType> entities = roomTypeRepository.findByHotelId(hotelId);
        List<RoomTypeDTO> dtos = new ArrayList<>();
        for (RoomType room : entities) {
            RoomTypeDTO dto = new RoomTypeDTO();
            dto.setId(room.getId());
            dto.setHotelId(room.getHotel().getId());
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

            List<AmenityDTO> amenityDTOs = room.getAmenities().stream()
                    .map(a -> {
                        AmenityDTO adto = new AmenityDTO();
                        adto.setId(a.getId());
                        adto.setAname(a.getAname());
                        return adto;
                    }).collect(Collectors.toList());
            dto.setAmenities(amenityDTOs);

            dtos.add(dto);
        }
        return dtos;
    }

    // 其他不動
    public List<RoomType> getAllRoomTypes() {
        return roomTypeRepository.findAll();
    }

    public RoomType getRoomTypeById(Long id) {
        return roomTypeRepository.findById(id).orElse(null);
    }

    public List<RoomTypeDTO> getAllRoomTypeDtos() {
        List<RoomType> entities = roomTypeRepository.findAll();
        List<RoomTypeDTO> dtos = new ArrayList<>();
        for (RoomType room : entities) {
            RoomTypeDTO dto = new RoomTypeDTO();
            dto.setId(room.getId());
            dto.setHotelId(room.getHotel().getId());
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

            List<AmenityDTO> amenityDTOs = room.getAmenities().stream()
                    .map(a -> {
                        AmenityDTO adto = new AmenityDTO();
                        adto.setId(a.getId());
                        adto.setAname(a.getAname());
                        return adto;
                    }).collect(Collectors.toList());
            dto.setAmenities(amenityDTOs);

            dtos.add(dto);
        }
        return dtos;
    }
    public List<RoomTypeDTO> findRoomTypesByHotelAndCapacity(Long hotelId, int people) {
        List<RoomType> roomTypes = roomTypeRepository.findByHotelIdAndCapacityGreaterThanEqual(hotelId, people);
        return roomTypes.stream().map(RoomTypeDTO::from).collect(Collectors.toList());
    }
}