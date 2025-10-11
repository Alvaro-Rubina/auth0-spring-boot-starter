package com.example.auth0springbootstarter.persistence.repository;

import com.example.auth0springbootstarter.persistence.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByAuth0Id(String auth0Id);

    Optional<User> findByEmail(String email);
}
