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
import io.swagger.v3.oas.annotations.Operation;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ChatController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private ChatRoomRepository chatRoomRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HotelRepository hotelRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private ChatRoomService chatRoomService;
    @Autowired
    private JwtService jwtService;

    @MessageMapping("/send")
    @Transactional
    public void sendMessage(MessageDto dto, @Header("Authorization") String authHeader) {
        try {
            System.out.println("收到 WebSocket 訊息: " + dto);

            if (dto.getChatRoomId() == null) {
                System.out.println("缺少 chatRoomId，請先透過 REST API 建立聊天室");
                return;
            }

            ChatRoom chatRoom = chatRoomRepository.findById(dto.getChatRoomId())
                    .orElse(null);

            if (chatRoom == null) {
                System.out.println("查不到 chatRoomId=" + dto.getChatRoomId());
                return;
            }
            String token = authHeader.replace("Bearer ", "");
            Long senderId = jwtService.getUserIdFromToken(token);
            User sender = userRepository.findById(senderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "使用者不存在"));
            User receiver = userRepository.findById(dto.getReceiverId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到接收者"));

            if (sender == null || receiver == null) {
                System.out.println("查不到 sender 或 receiver");
                return;
            }

            Message message = new Message();
            message.setSender(sender);
            message.setReceiver(receiver);
            message.setHotel(chatRoom.getHotel());
            message.setChatRoom(chatRoom);
            message.setContent(dto.getContent());
            message.setSentAt(LocalDateTime.now());
            Message savedMessage = messageRepository.save(message);

            chatRoom.setLastMessage(savedMessage.getContent());
            chatRoom.setUpdatedAt(LocalDateTime.now());
            chatRoomRepository.save(chatRoom);

            MessageDto outDto = new MessageDto();
            outDto.setId(savedMessage.getId());
            outDto.setSenderId(sender.getId());
            outDto.setReceiverId(receiver.getId());
            outDto.setHotelId(chatRoom.getHotel().getId());
            outDto.setChatRoomId(chatRoom.getId());
            outDto.setContent(savedMessage.getContent());
            outDto.setSentAt(savedMessage.getSentAt());

            messagingTemplate.convertAndSend("/topic/chat/" + chatRoom.getId(), outDto);
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
