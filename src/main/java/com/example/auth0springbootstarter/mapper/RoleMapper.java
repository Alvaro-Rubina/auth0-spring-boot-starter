package com.example.auth0springbootstarter.mapper;

import com.example.auth0springbootstarter.persistence.dto.role.RoleRequest;
import com.example.auth0springbootstarter.persistence.dto.role.RoleResponse;
import com.example.auth0springbootstarter.persistence.entity.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    Role toEntity(RoleRequest dto);

    RoleResponse toResponse(Role entity);

}
