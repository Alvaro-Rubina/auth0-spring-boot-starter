package com.example.auth0springbootstarter.persistence.repository;

import com.example.auth0springbootstarter.persistence.entity.Role;
import com.example.auth0springbootstarter.persistence.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByAuth0Id(String auth0Id);
    boolean existsByEmail(String email);
    List<User> findByRole(Role role);
}
