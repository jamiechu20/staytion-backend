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
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;

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

    public List<MessageDto> getMessages(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(()-> new ChatRoomNotFoundException("找不到聊天室，chatRoomId=" + chatRoomId));
        if (!userId.equals(chatRoom.getUser1().getId()) && !userId.equals(chatRoom.getUser2().getId())) {
            throw new ChatRoomAccessDeniedException("使用者不屬於此聊天室，userId=" + userId + ", chatRoomId=" + chatRoomId);
        }

        List<Message> messages = messageRepository.findByChatRoomIdOrderBySentAt(chatRoomId);
        messages.stream()
                .filter(m -> !m.getSender().getId().equals(userId) && !m.getIsRead())
                .forEach(m -> {
                    m.setIsRead(true);
                    messageRepository.save(m);
                });
        return messages.stream()
                .map(message -> toMessageDto(message, userId))
                .toList();

    }
    public MessageDto toMessageDto(Message message, Long userId) {
        MessageDto messageDto = new MessageDto();
        messageDto.setId(message.getId());
        messageDto.setSenderId(message.getSender().getId());
        messageDto.setReceiverId(message.getReceiver().getId());
        messageDto.setContent(message.getContent());
        messageDto.setSentAt(message.getSentAt());
        messageDto.setChatRoomId(message.getChatRoom().getId());
        messageDto.setHotelId(message.getHotel().getId());
        messageDto.setRead(message.getIsRead());
        messageDto.setDisplayName(resolveDisplayName(message, userId));
        return messageDto;
    }
    public String resolveDisplayName(Message message, Long userId) {
        String displayName;
        if (message.getSender().getId().equals(userId)) {
            if (message.getReceiver().getId().equals(message.getHotel().getOwner().getId())) {
                displayName = message.getHotel().getHname();
            } else {
                displayName = message.getReceiver().getFirstName();
            }
        } else {
            if (message.getSender().getId().equals(message.getHotel().getOwner().getId())) {
                displayName = message.getHotel().getHname();
            }  else {
                displayName = message.getSender().getFirstName();
            }
        }
        return displayName;
    }

}
