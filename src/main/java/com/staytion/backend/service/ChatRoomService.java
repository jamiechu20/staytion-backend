package com.staytion.backend.service;

import com.staytion.backend.dto.ChatRoomDto;
import com.staytion.backend.entity.ChatRoom;
import com.staytion.backend.entity.User;
import com.staytion.backend.repository.ChatRoomRepository;
import com.staytion.backend.repository.HotelRepository;
import com.staytion.backend.repository.MessageRepository;
import com.staytion.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private static final Logger log = LoggerFactory.getLogger(ChatRoomService.class);

    private final ChatRoomRepository chatRoomRepository;

    private final UserRepository userRepository;

    private final HotelRepository hotelRepository;

    private final MessageRepository messageRepository;

    // 新增：根據兩個用戶和飯店查找或創建聊天室

    /**
     * senderId 對應 user1，receiverId 對應 user2
     * 若反過來查不到，會嘗試以 receiverId 作為 user1 查一次
     */
    public ChatRoom findOrCreateChatRoom(Long user1Id, Long user2Id, Long hotelId) {
        // 先查詢 user1Id-user2Id
        Optional<ChatRoom> optionalRoom = chatRoomRepository.findByUser1IdAndUser2IdAndHotel_Id(user1Id, user2Id, hotelId);
        if (optionalRoom.isEmpty()) {
            // 再查詢 user2Id-user1Id
            optionalRoom = chatRoomRepository.findByUser2IdAndUser1IdAndHotel_Id(user2Id, user1Id, hotelId);
        }
        if (optionalRoom.isPresent()) {
            return optionalRoom.get();
        }
        // 創建新聊天室
        ChatRoom newRoom = new ChatRoom();
        newRoom.setUser1(userRepository.findById(user1Id).orElseThrow());
        newRoom.setUser2(userRepository.findById(user2Id).orElseThrow());
        newRoom.setHotel(hotelRepository.findById(hotelId).orElseThrow());
        newRoom.setCreatedAt(LocalDateTime.now());
        newRoom.setUpdatedAt(LocalDateTime.now());
        return chatRoomRepository.save(newRoom);
    }

    // 取得某用戶相關的聊天室列表
    public List<ChatRoomDto> getChatRoomsForUser(Long userId) {
        log.debug("取得聊天室列表，使用者 userId = {}", userId);

        List<ChatRoom> chatRooms = chatRoomRepository.findByUser1_IdOrUser2_Id(userId, userId);
        log.debug("查到聊天室筆數: {}", chatRooms.size());
        return chatRooms.stream().map(room -> {
            ChatRoomDto dto = new ChatRoomDto();
            dto.setChatRoomId(room.getId());
            dto.setUpdatedAt(room.getUpdatedAt());
            dto.setLastMessage(room.getLastMessage());
            dto.setHotelId(room.getHotel().getId());
            dto.setHotelName(room.getHotel().getHname());

            Long ownerId = room.getHotel().getOwner().getId();
            if (userId.equals(ownerId)) {
                //房東身份，顯示房客名字＋飯店名
                User guest = room.getUser1().getId().equals(ownerId) ? room.getUser2() : room.getUser1();
                dto.setReceiverId(guest.getId());
                String guestName = guest.getFirstName() != null ? guest.getFirstName() : "未知房客";
                dto.setDisplayName(guestName + "｜" + room.getHotel().getHname());

            } else {
                //房客身份，顯示飯店名
                Long owner = room.getHotel().getOwner().getId();
                dto.setReceiverId(owner);
                dto.setDisplayName(room.getHotel().getHname());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    public List<ChatRoomDto> getChatRoomsForHotel(Long hotelId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findByHotel_Id(hotelId);
        return chatRooms.stream().map(room -> {
            ChatRoomDto dto = new ChatRoomDto();
            dto.setChatRoomId(room.getId());
            dto.setUpdatedAt(room.getUpdatedAt());
            dto.setLastMessage(room.getLastMessage());
            dto.setHotelId(room.getHotel().getId());
            dto.setHotelName(room.getHotel().getHname());
            Long ownerId = room.getHotel().getOwner().getId();
            User guest = room.getUser1().getId().equals(ownerId) ? room.getUser2() : room.getUser1();
            dto.setReceiverId(guest.getId());
            String guestName = guest.getFirstName() != null ? guest.getFirstName() : "未知房客";
            dto.setDisplayName(guestName);
            return dto;
        }).toList();

    }
}