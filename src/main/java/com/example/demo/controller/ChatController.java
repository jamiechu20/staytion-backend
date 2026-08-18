package com.example.demo.controller;

import com.example.demo.dto.MessageDto;
import com.example.demo.entity.ChatRoom;
import com.example.demo.entity.Message;
import com.example.demo.entity.User;
import com.example.demo.repository.ChatRoomRepository;
import com.example.demo.repository.HotelRepository;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ChatRoomService;
import com.example.demo.service.JwtService;
import com.example.demo.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final MessageRepository messageRepository;
    private final ChatRoomService chatRoomService;
    private final JwtService jwtService;
    private final MessageService messageService;

    @MessageMapping("/send")
    @Transactional
    public void sendMessage(MessageDto dto, @Header("Authorization") String authHeader) {
        try {
            System.out.println("收到 WebSocket 訊息: " + dto);

            Long chatRoomId = dto.getChatRoomId();
            String content = dto.getContent();
            String token = authHeader.replace("Bearer ", "");
            Long senderId = jwtService.getUserIdFromToken(token);
            MessageDto outDto = messageService.sendMessage(chatRoomId, senderId, content);

            messagingTemplate.convertAndSend("/topic/chat/" + chatRoomId, outDto);
        } catch (Exception e) {
            System.err.println("處理訊息時發生錯誤：" + e.getMessage());
            e.printStackTrace();
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "某使用者的所有聊天室")
    public List<ChatRoom> getChatRoomsByUser(@PathVariable Long userId) {
        User user = new User();
        user.setId(userId);
        return chatRoomRepository.findByUser1OrUser2(user, user);
    }

    @GetMapping("/hotel/{hotelId}")
    @Operation(summary = "某飯店的所有聊天室")
    public List<ChatRoom> getChatRoomsByHotel(@PathVariable Long hotelId) {
        return chatRoomRepository.findByHotel_Id(hotelId);
    }
}
