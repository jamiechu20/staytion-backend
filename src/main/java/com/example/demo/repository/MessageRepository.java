package com.example.demo.repository;

import com.example.demo.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long>{
    @Query("""
    SELECT m FROM Message m
    WHERE 
        (m.sender.id = :user1 AND m.receiver.id = :user2 
        OR m.sender.id = :user2 AND m.receiver.id = :user1)
        AND m.hotel.id = :hotelId
    ORDER BY m.sentAt
""")
    List<Message> findChatHistory(@Param("user1") Long user1,
                                  @Param("user2") Long user2,
                                  @Param("hotelId") Long hotelId);

    @Query("SELECT m FROM Message m WHERE m.chatRoom.id = :chatRoomId ORDER BY m.sentAt")
    List<Message> findByChatRoomIdOrderBySentAt(@Param("chatRoomId") Long chatRoomId);
}
