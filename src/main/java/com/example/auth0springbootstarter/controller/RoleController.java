package com.example.auth0springbootstarter.controller;

import com.auth0.exception.Auth0Exception;
import com.example.auth0springbootstarter.persistence.dto.role.RoleRequest;
import com.example.auth0springbootstarter.persistence.dto.role.RoleResponse;
import com.example.auth0springbootstarter.persistence.dto.role.RoleUpdateRequest;
import com.example.auth0springbootstarter.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles/admin")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    private ResponseEntity<RoleResponse> createRole(@RequestBody @Valid RoleRequest dto) throws Auth0Exception {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roleService.save(dto));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<RoleResponse> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.findById(id));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<RoleResponse> getRoleByName(@PathVariable String name) {
        return ResponseEntity.ok(roleService.findByName(name));
    }

    @GetMapping
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        return ResponseEntity.ok(roleService.findAll());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<RoleResponse> updateRole(@PathVariable Long id,
                                                   @RequestBody @Valid RoleUpdateRequest dto) throws Auth0Exception {
        return ResponseEntity.ok(roleService.update(id, dto));
    }
}
