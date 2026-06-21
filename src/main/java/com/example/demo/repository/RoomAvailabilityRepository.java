package com.example.demo.repository;

import com.example.demo.entity.RoomAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomAvailabilityRepository extends JpaRepository<RoomAvailability, Long> {
    Optional<RoomAvailability>
        findByRoomType_IdAndDate(Long roomTypeId, LocalDate date);

    List<RoomAvailability>
    findByRoomType_IdAndDateBetween(Long roomTypeId, LocalDate start, LocalDate end);
}
