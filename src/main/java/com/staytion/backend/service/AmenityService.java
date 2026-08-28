package com.staytion.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.staytion.backend.dto.AmenityDTO;
import com.staytion.backend.repository.AmenityRepository;

@Service
@RequiredArgsConstructor
public class AmenityService {

    private final AmenityRepository amenityRepository;

    public List<AmenityDTO> getAllAmenities() {
        return amenityRepository.findAll().stream()
            .map(a -> new AmenityDTO(a.getId(), a.getAname()))
            .collect(Collectors.toList());
    }
}