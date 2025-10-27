package com.example.auth0springbootstarter.persistence.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequest {

    @NotBlank(message = "El campo name es obligatorio")
    @Size(max = 50, message = "El campo name no puede tener mas de 50 caracteres")
    private String name;

    @NotBlank(message = "El campo description es obligatorio")
    @Size(max = 500, message = "El campo description no puede tener mas de 500 caracteres")
    private String description;
}
