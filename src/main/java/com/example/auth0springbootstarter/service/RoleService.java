package com.example.auth0springbootstarter.service;

import com.auth0.exception.Auth0Exception;
import com.example.auth0springbootstarter.exception.ForbiddenOperationException;
import com.example.auth0springbootstarter.exception.ResourceNotFoundException;
import com.example.auth0springbootstarter.mapper.RoleMapper;
import com.example.auth0springbootstarter.persistence.dto.role.RoleRequest;
import com.example.auth0springbootstarter.persistence.dto.role.RoleResponse;
import com.example.auth0springbootstarter.persistence.dto.role.RoleUpdateRequest;
import com.example.auth0springbootstarter.persistence.entity.Role;
import com.example.auth0springbootstarter.persistence.repository.RoleRepository;
import com.example.auth0springbootstarter.service.auth0.Auth0RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.auth0springbootstarter.utils.Constants.ADMIN_ROL_NAME;
import static com.example.auth0springbootstarter.utils.Constants.USER_ROL_NAME;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final Auth0RoleService auth0RoleService;

    @Transactional
    public RoleResponse save(RoleRequest dto) throws Auth0Exception {
        return createRoleIfNotExists(dto.getName(), dto.getDescription());
    }

    @Transactional(readOnly = true)
    public RoleResponse findById(Long id) {
        Role role = getRoleByIdOrThrow(id, false);
        return roleMapper.toResponse(role);
    }

    @Transactional(readOnly = true)
    public RoleResponse findByName(String name) {
        Role role = getRoleByNameOrThrow(name, false);
        return roleMapper.toResponse(role);
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> findAll() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toResponse)
                .toList();
    }

    @Transactional
    public RoleResponse update(Long id, RoleUpdateRequest dto) throws Auth0Exception {
        Role role = getRoleByIdOrThrow(id, false);

        boolean nameChanged = dto.getName() != null && !dto.getName().equalsIgnoreCase(role.getName());
        boolean descriptionChanged = dto.getDescription() != null && !dto.getDescription().equals(role.getDescription());
        boolean activeChanged = dto.getActive() != null && !dto.getActive().equals(role.getActive());

        if (nameChanged) {

            if ((role.getName().equals(USER_ROL_NAME)) || (role.getName().equals(ADMIN_ROL_NAME))) {
                throw new ForbiddenOperationException("No es posible editar el nombre del rol '" + role.getName() + "' porque es un rol por defecto");
            }

            roleRepository.findByNameIgnoreCase(dto.getName()).ifPresent(existing -> {
                if (!existing.getId().equals(role.getId())) {
                    throw new IllegalArgumentException("Ya existe un rol con el nombre '" + dto.getName() + "'.");
                }
            });
            role.setName(dto.getName());
        }

        if (descriptionChanged) {
            role.setDescription(dto.getDescription());
        }

        if (activeChanged) {
            role.setActive(dto.getActive());
        }

        if (nameChanged || descriptionChanged) {
            auth0RoleService.updateRol(role.getAuth0Id(), role.getName(), role.getDescription());
        }

        return roleMapper.toResponse(role);
    }

    // Métodos auxiliares
    @Transactional
    public Role getRoleByIdOrThrow(Long id, boolean verifyActive) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol con el id '" + id + "' no encontrado"));

        if (verifyActive && !role.getActive()) {
            throw new ResourceNotFoundException("Rol el id '" + id + "' inactivo");
        }

        return role;
    }

    @Transactional
    public Role getRoleByNameOrThrow(String name, boolean verifyActive) {
        Role role = roleRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("Rol con el nombre '" + "' no encontrado"));

        if (verifyActive && !role.getActive()) {
            throw new ResourceNotFoundException("Rol el nombre '" + name + "' inactivo");
        }

        return role;
    }

    private RoleResponse createRoleIfNotExists(String name, String description) throws Auth0Exception {
        Role existingRole = roleRepository.findByNameIgnoreCase(name).orElse(null);

        if (existingRole != null) {
            log.info("El rol '{}' ya existe en la base de datos. Verificando existencia en Auth0.", name);

            if (auth0RoleService.existsRoleByName(name)) {
                log.info("El rol '{}' ya existe en Auth0.", name);
                return roleMapper.toResponse(existingRole);
            } else {
                log.warn("El rol '{}' existe en la base de datos pero no en Auth0. Creando en Auth0.", name);
                com.auth0.json.mgmt.roles.Role auth0Role = auth0RoleService.createRol(name, description);

                existingRole.setAuth0Id(auth0Role.getId());
                existingRole.setDescription(auth0Role.getDescription());

                log.info("Rol '{}' creado en Auth0 y actualizado en la base de datos.", name);
                return roleMapper.toResponse(roleRepository.save(existingRole));
            }
        }

        log.info("El rol '{}' no existe en la base de datos. Creando en Auth0 y guardando en la base de datos.", name);
        com.auth0.json.mgmt.roles.Role auth0Role = auth0RoleService.createRol(name, description);

        Role role = Role.builder()
                .name(auth0Role.getName())
                .description(auth0Role.getDescription())
                .auth0Id(auth0Role.getId())
                .build();

        log.info("Rol '{}' creado exitosamente en Auth0 y base de datos", name);
        return roleMapper.toResponse(roleRepository.save(role));
    }
}
