package com.staytion.backend.controller;

import com.staytion.backend.dto.MessageDto;
import com.staytion.backend.entity.User;
import com.staytion.backend.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/messages")
public class MessageController {
    private final MessageService messageService;


    @GetMapping("/{chatRoomId}/messages")
    @Operation(summary = "查看歷史訊息")
    public List<MessageDto> getMessages(@PathVariable Long chatRoomId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        return messageService.getMessages(chatRoomId, userId);
    }
}
