package com.example.auth0springbootstarter.persistence.dto.user;

import com.example.auth0springbootstarter.persistence.dto.role.RoleResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;

    private String name;

    private String email;

    private Boolean active;

    private RoleResponse role;

}
