package com.elsherif.livecaption.mapper;

import com.elsherif.livecaption.dto.response.UserResponse;
import com.elsherif.livecaption.entity.User;

public class UserMapper {
    public UserResponse toResponse(User user) {
        return UserResponse.builder().id(user.getId()).email(user.getEmail())
                .name(user.getName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
