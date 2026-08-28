package com.staytion.backend.controller;

import com.staytion.backend.dto.ChatRoomDto;
import com.staytion.backend.dto.request.ChatRoomRequestDto;
import com.staytion.backend.entity.ChatRoom;
import com.staytion.backend.service.ChatRoomService;
import com.staytion.backend.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatrooms")
public class ChatRoomController {
    private static final Logger log = LoggerFactory.getLogger(ChatRoomController.class);

    private final ChatRoomService chatRoomService;
    private final JwtService jwtService;

    @PostMapping("/find-or-create")
    @Operation(summary = "尋找或建立聊天室", description = "根據 senderId（由JWT解析userId）、receiverId 和 hotelId，尋找或建立聊天室並回傳 chatRoomId")
    public Map<String, Long> findOrCreateChatRoom(
            @RequestBody ChatRoomRequestDto requestDto,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        Long senderId = jwtService.getUserIdFromToken(token); // 從 JWT 解析 userId
        Long receiverId = requestDto.getReceiverId();
        Long hotelId = requestDto.getHotelId();

        ChatRoom chatRoom = chatRoomService.findOrCreateChatRoom(senderId, receiverId, hotelId);
        log.debug("建立聊天室成功 ID: {}", chatRoom.getId());
        Map<String, Long> response = new HashMap<>();
        response.put("chatRoomId", chatRoom.getId());
        log.debug("即將回傳 JSON: {}", response);
        return response;

    }

    @GetMapping("/my")
    @Operation(summary = "載入使用者聊天室列表")
    public List<ChatRoomDto> getChatRoomsByUser(@RequestHeader("Authorization") String authHeader) {
        log.debug("getMyChatRooms 執行了！");
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtService.getUserIdFromToken(token);
        return chatRoomService.getChatRoomsForUser(userId);
    }

    @GetMapping("/hotel/{hotelId}")
    @Operation(summary = "某飯店的所有聊天室")
    public List<ChatRoomDto> getChatRoomsByHotel(@PathVariable Long hotelId) {
        return chatRoomService.getChatRoomsForHotel(hotelId);
    }
}
