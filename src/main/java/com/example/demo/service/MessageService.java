package com.example.demo.service;

import com.example.demo.dto.MessageDto;
import com.example.demo.entity.ChatRoom;
import com.example.demo.entity.Message;
import com.example.demo.entity.User;
import com.example.demo.exception.ChatRoomAccessDeniedException;
import com.example.demo.exception.ChatRoomNotFoundException;
import com.example.demo.repository.ChatRoomRepository;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final MessageDto messageDto;

    @Transactional
    public MessageDto sendMessage(Long chatRoomId,
                                  Long senderId,
                                  String content) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(()-> new ChatRoomNotFoundException("找不到聊天室，chatRoomId=" + chatRoomId));
        User sender;
        User receiver;
        if (senderId.equals(chatRoom.getUser1().getId())) {
            sender = chatRoom.getUser1();
            receiver = chatRoom.getUser2();
        } else if (senderId.equals(chatRoom.getUser2().getId())) {
            sender = chatRoom.getUser2();
            receiver = chatRoom.getUser1();
        }  else {
            throw new ChatRoomAccessDeniedException("使用者不屬於此聊天室，senderId=" + senderId + ", chatRoomId=" + chatRoomId);
        }
        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setHotel(chatRoom.getHotel());
        message.setContent(content);
        message.setChatRoom(chatRoom);
        message.setSentAt(LocalDateTime.now());
        Message savedMessage = messageRepository.save(message);
        chatRoom.setLastMessage(savedMessage.getContent());
        chatRoom.setUpdatedAt(LocalDateTime.now());

        MessageDto messageDto = new MessageDto();
        messageDto.setId(savedMessage.getId());
        messageDto.setSenderId(senderId);
        messageDto.setReceiverId(receiver.getId());
        messageDto.setHotelId(chatRoom.getHotel().getId());
        messageDto.setChatRoomId(chatRoomId);
        messageDto.setContent(content);
        messageDto.setSentAt(savedMessage.getSentAt());
        return messageDto;
    }

}
