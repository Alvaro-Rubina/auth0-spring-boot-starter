package com.example.auth0springbootstarter.persistence.repository;

import com.example.auth0springbootstarter.persistence.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByAuth0Id(String auth0Id);
    Optional<Role> findByNameIgnoreCase(String name);
}
