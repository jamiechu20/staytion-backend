package com.example.demo.dto.response;

import com.example.demo.entity.User;

public record GoogleLoginResult(User user, String token, Boolean isNewUser) {
}
