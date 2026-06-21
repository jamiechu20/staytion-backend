package com.example.demo.controller;

import com.example.demo.dto.MessageDto;
import com.example.demo.entity.ChatRoom;
import com.example.demo.entity.Message;
import com.example.demo.repository.ChatRoomRepository;
import com.example.demo.repository.HotelRepository;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ChatRoomService;
import com.example.demo.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private JwtService jwtService;

    @GetMapping
    public List<Message> getChatHistory(@RequestParam Long receiverId,
                                        @RequestParam Long hotelId,
                                        HttpServletRequest request) {
        Long senderId = (Long) request.getAttribute("userId");
        if (senderId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT 驗證失敗");
        }
        return messageRepository.findChatHistory(senderId, receiverId, hotelId);
    }

    @Transactional
    @PostMapping
    @Operation(summary = "保留測試用，正式用WebSocket")
    public Message sendMessage(@RequestBody MessageDto dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT 驗證失敗");
        }

        ChatRoom chatRoom = chatRoomService.findOrCreateChatRoom(userId, dto.getReceiverId(), dto.getHotelId());

        Message message = new Message();
        message.setSender(userRepository.findById(userId).orElseThrow());
        message.setReceiver(userRepository.findById(dto.getReceiverId()).orElseThrow());
        message.setChatRoom(chatRoom);
        message.setContent(dto.getContent());
        message.setSentAt(LocalDateTime.now());
        message.setIsRead(false);

        chatRoom.setLastMessage(dto.getContent());
        chatRoom.setUpdatedAt(LocalDateTime.now());

        chatRoomRepository.save(chatRoom);
        return messageRepository.save(message);
    }

    @GetMapping("/{chatRoomId}/messages")
    @Operation(summary = "查看歷史訊息")
    public List<MessageDto> getMessages(@PathVariable Long chatRoomId, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT 驗證失敗");
        }
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtService.getUserIdFromToken(token);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT 驗證失敗");
        }
        List<Message> messages = messageRepository.findByChatRoomIdOrderBySentAt(chatRoomId);

        // 標記為已讀
        messages.stream()
                .filter(m -> !m.getSender().getId().equals(userId) && !m.getIsRead())
                .forEach(m -> {
                    m.setIsRead(true);
                    messageRepository.save(m);
                });

        return messages.stream().map(m -> {
            MessageDto dto = new MessageDto();
            dto.setId(m.getId());
            dto.setSenderId(m.getSender().getId());
            dto.setReceiverId(m.getReceiver().getId());
            dto.setHotelId(m.getHotel().getId());
            dto.setChatRoomId(m.getChatRoom().getId());
            dto.setContent(m.getContent());
            dto.setSentAt(m.getSentAt());
            dto.setRead(m.getIsRead());

            //displayName邏輯
            Long currentUserId = userId;
            String displayName;
            if (m.getSender().getId().equals(currentUserId)) {
                // 我是發訊者 → 顯示對方資訊
                if (m.getReceiver().getId().equals(m.getHotel().getOwner().getId())) {
                    displayName = m.getHotel().getHname();
                } else {
                    displayName = m.getReceiver().getFirstName();
                }
            } else {
                // 我是接收者 → 顯示發送者資訊
                if (m.getSender().getId().equals(m.getHotel().getOwner().getId())) {
                    displayName = m.getHotel().getHname();
                } else {
                    displayName = m.getSender().getFirstName();
                }
            }

            dto.setDisplayName(displayName);
            return dto;
        }).toList();
    }
}
