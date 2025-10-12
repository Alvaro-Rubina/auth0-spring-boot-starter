package com.example.auth0springbootstarter.persistence.dto.user.signup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponse {

    private String auth0Id;

    private String email;

    private String name;
}
