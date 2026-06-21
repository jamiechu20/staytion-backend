package com.example.demo.repository;

import com.example.demo.entity.ChatRoom;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 以 id 查找雙方和飯店組合
    Optional<ChatRoom> findByUser1IdAndUser2IdAndHotel_Id(Long user1Id, Long user2Id, Long hotelId);

    Optional<ChatRoom> findByUser2IdAndUser1IdAndHotel_Id(Long user2Id, Long user1Id, Long hotelId);

    List<ChatRoom> findByUser1_IdOrUser2_Id(Long user1Id, Long user2Id);

    List<ChatRoom> findByUser1OrUser2(User user1, User user2);

    List<ChatRoom> findByHotel_Id(Long hotelId);
}
