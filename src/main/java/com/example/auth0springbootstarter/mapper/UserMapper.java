package com.example.auth0springbootstarter.mapper;

import com.example.auth0springbootstarter.persistence.dto.user.UserResponse;
import com.example.auth0springbootstarter.persistence.dto.user.signup.SignupResponse;
import com.example.auth0springbootstarter.persistence.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public interface UserMapper {

    User toEntity(SignupResponse dto);

    UserResponse toResponse(User user);
}
