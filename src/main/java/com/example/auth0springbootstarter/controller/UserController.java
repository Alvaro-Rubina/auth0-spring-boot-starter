package com.example.auth0springbootstarter.controller;

import com.auth0.exception.Auth0Exception;
import com.example.auth0springbootstarter.persistence.dto.user.UserResponse;
import com.example.auth0springbootstarter.persistence.dto.user.UserUpdateRequest;
import com.example.auth0springbootstarter.persistence.dto.user.signup.SignupRequest;
import com.example.auth0springbootstarter.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.auth0springbootstarter.utils.Constants.ADMIN_ROLE_NAME;
import static com.example.auth0springbootstarter.utils.Constants.USER_ROLE_NAME;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Value("${auth0.audience}")
    private String audience;

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> registerFromJwt(@AuthenticationPrincipal Jwt jwt) throws Auth0Exception {
        String auth0UserId = jwt.getSubject();
        String email = jwt.getClaim(audience + "/email");
        String name = jwt.getClaim(audience + "/name");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.registerFromJwt(auth0UserId, email, name));
    }

    @PostMapping("/owner/signup")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<UserResponse> registerFromDto(@RequestBody @Valid SignupRequest dto) throws Auth0Exception {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.registerFromDto(dto));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        String auth0Id = jwt.getSubject();
        String email = jwt.getClaim(audience + "/email");
        String name = jwt.getClaim(audience + "/name");
        return ResponseEntity.ok(userService.getCurrent(auth0Id, email, name));
    }

    @GetMapping("/admin/id/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @GetMapping("/admin/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.findAllByRole(USER_ROLE_NAME));
    }

    @GetMapping("/admin/admins")
    public ResponseEntity<List<UserResponse>> getAllAdmins() {
        return ResponseEntity.ok(userService.findAllByRole(ADMIN_ROLE_NAME));
    }

    @PatchMapping
    public ResponseEntity<UserResponse> updateUser(@AuthenticationPrincipal Jwt jwt,
                                                   @RequestBody @Valid UserUpdateRequest dto) throws Auth0Exception {
        String auth0Id = jwt.getSubject();
        return ResponseEntity.ok(userService.update(auth0Id, dto));
    }

    @PatchMapping("/admin/{id}/activate")
    public ResponseEntity<UserResponse> activateUser(@PathVariable Long id) throws Auth0Exception {
        return ResponseEntity.ok(userService.activate(id));
    }

    @PatchMapping("/admin/{id}/deactivate")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable Long id) throws Auth0Exception {
        return ResponseEntity.ok(userService.deactivate(id));
    }

}
