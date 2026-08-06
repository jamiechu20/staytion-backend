package com.example.demo.exception;

public class ChatRoomAccessDeniedException extends RuntimeException {
    public ChatRoomAccessDeniedException(String message) {
        super(message);
    }
}
