package com.example.auth0springbootstarter.persistence.dto.user.signup;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    @NotBlank(message = "El campo email es obligatorio")
    private String email;

    @NotBlank(message = "El campo password es obligatorio")
    private String password;

    @NotBlank(message = "El campo name es obligatorio")
    private String name;

    private String roleName;
}