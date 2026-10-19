package com.staytion.backend.repository;

import com.staytion.backend.entity.ChatRoom;
import com.staytion.backend.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 以 id 查找雙方和飯店組合
    Optional<ChatRoom> findByUser1IdAndUser2IdAndHotel_Id(Long user1Id, Long user2Id, Long hotelId);

    Optional<ChatRoom> findByUser2IdAndUser1IdAndHotel_Id(Long user2Id, Long user1Id, Long hotelId);

    // 聊天室列表會用到雙方使用者與飯店業主，一次 fetch join 載入
    @EntityGraph(attributePaths = {"user1", "user2", "hotel", "hotel.owner"})
    List<ChatRoom> findByUser1_IdOrUser2_Id(Long user1Id, Long user2Id);

    List<ChatRoom> findByUser1OrUser2(User user1, User user2);

    @EntityGraph(attributePaths = {"user1", "user2", "hotel", "hotel.owner"})
    List<ChatRoom> findByHotel_Id(Long hotelId);
}
