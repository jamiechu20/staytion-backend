package com.staytion.backend.dto.response;

import com.staytion.backend.entity.User;

public record UserResponse(Long id, String firstName, String lastName, String email, String tel) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getTel());
    }
}
