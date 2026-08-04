package com.elsherif.livecaption.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message="Email is required")
    @Email(message="invalid email format")
    private String email;
    @NotBlank(message="password is required")
    private String password;

}
