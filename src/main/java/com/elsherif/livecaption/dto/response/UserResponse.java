package com.elsherif.livecaption.dto.response;

import com.elsherif.livecaption.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String avatarUrl;
    private Role role;
    private LocalDateTime createdAt;
}
