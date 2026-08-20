package com.example.demo.controller;

import com.example.demo.dto.ErrorMessage;
import com.example.demo.dto.MessageDto;
import com.example.demo.entity.ChatRoom;
import com.example.demo.entity.User;
import com.example.demo.exception.ChatRoomAccessDeniedException;
import com.example.demo.exception.ChatRoomNotFoundException;
import com.example.demo.repository.ChatRoomRepository;
import com.example.demo.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomRepository chatRoomRepository;
    private final MessageService messageService;

    @MessageMapping("/send")
    @Transactional
    public void sendMessage(MessageDto dto, Principal principal) {
        Long chatRoomId = dto.getChatRoomId();
        String content = dto.getContent();
        Long senderId = Long.parseLong(principal.getName());
        MessageDto outDto = messageService.sendMessage(chatRoomId, senderId, content);
        messagingTemplate.convertAndSend("/topic/chat/" + chatRoomId, outDto);
    }

    @MessageExceptionHandler(ChatRoomNotFoundException.class)
    @SendToUser("/queue/errors")
    public ErrorMessage handleChatRoomNotFoundException(ChatRoomNotFoundException ex) {
        return new ErrorMessage(ex.getMessage());
    }

    @MessageExceptionHandler(ChatRoomAccessDeniedException.class)
    @SendToUser("/queue/errors")
    public ErrorMessage handleChatRoomAccessDeniedException(ChatRoomAccessDeniedException ex) {
        return new ErrorMessage(ex.getMessage());
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
