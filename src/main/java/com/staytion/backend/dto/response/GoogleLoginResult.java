package com.staytion.backend.dto.response;

import com.staytion.backend.entity.User;

public record GoogleLoginResult(User user, String token, Boolean isNewUser) {
}
