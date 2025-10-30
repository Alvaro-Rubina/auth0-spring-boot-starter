package com.example.auth0springbootstarter.service;

import com.auth0.exception.Auth0Exception;
import com.example.auth0springbootstarter.exception.ExistingResourceException;
import com.example.auth0springbootstarter.exception.ResourceNotFoundException;
import com.example.auth0springbootstarter.exception.UserRegistrationException;
import com.example.auth0springbootstarter.mapper.UserMapper;
import com.example.auth0springbootstarter.persistence.dto.role.RoleResponse;
import com.example.auth0springbootstarter.persistence.dto.user.UserResponse;
import com.example.auth0springbootstarter.persistence.dto.user.UserUpdateRequest;
import com.example.auth0springbootstarter.persistence.dto.user.signup.SignupRequest;
import com.example.auth0springbootstarter.persistence.dto.user.signup.SignupResponse;
import com.example.auth0springbootstarter.persistence.entity.Role;
import com.example.auth0springbootstarter.persistence.entity.User;
import com.example.auth0springbootstarter.persistence.repository.UserRepository;
import com.example.auth0springbootstarter.service.auth0.Auth0UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.auth0springbootstarter.utils.Constants.USER_ROLE_NAME;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final Auth0UserService auth0UserService;
    private final RoleService roleService;

    @Transactional
    public UserResponse registerFromDto(SignupRequest dto) throws Auth0Exception {

        Role role = new Role();
        String roleName = dto.getRoleName();
        try {
            role = (roleName != null && !roleName.isEmpty())
                    ? roleService.getRoleByNameOrThrow(roleName, true)
                    : roleService.getRoleByNameOrThrow(USER_ROLE_NAME, true);
        } catch (ResourceNotFoundException ex) {
            log.warn("Rol '{}' no encontrado. Asignando rol por defecto '{}'", roleName, USER_ROLE_NAME);
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ExistingResourceException("El email proporcionado ya está registrado en la base de datos");
        }

        SignupResponse signupResponse = auth0UserService.registerUserFromDTO(dto);

        User user = userMapper.toEntity(signupResponse);
        user.setRole(role);

        try {
            log.info("Creando usuario '{}' con el rol '{}'", user.getEmail(), role.getName());

            auth0UserService.setUserRole(user.getAuth0Id(), role.getAuth0Id());
            userRepository.save(user);

            log.info("Usuario '{}' creado exitosamente", user.getEmail());

        } catch (DataAccessException ex) {
            log.error("Error guardando usuario '{}' en BD, eliminando usuario en Auth0", user.getEmail(), ex);
            auth0UserService.deleteUser(user.getAuth0Id());
            throw new UserRegistrationException("Error guardando usuario en la base de datos", ex);

        } catch (Auth0Exception ex) {
            log.error("Error asignando rol, eliminando usuario {}", user.getAuth0Id(), ex);
            auth0UserService.deleteUser(user.getAuth0Id());
            throw new UserRegistrationException("Error asignando rol en Auth0", ex);
        }
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse registerFromJwt(String auth0Id, String email, String name) throws Auth0Exception {
        Role role = roleService.getRoleByNameOrThrow(USER_ROLE_NAME, true);

        if (userRepository.existsByEmail(email)) {
            throw new ExistingResourceException("El email proporcionado ya está registrado en la base de datos");
        }

        User user = User.builder()
                .name(name != null ? name : email)
                .auth0Id(auth0Id)
                .email(email)
                .role(role)
                .build();

        try {
            log.info("Creando usuario '{}' con el rol '{}'", user.getEmail(), role.getName());

            auth0UserService.setUserRole(user.getAuth0Id(), role.getAuth0Id());
            userRepository.save(user);

            log.info("Usuario '{}' creado exitosamente", user.getEmail());

        } catch (DataAccessException ex){
            log.error("Error guardando usuario '{}' en BD, eliminando usuario en Auth0", user.getEmail(), ex);
            auth0UserService.deleteUser(user.getAuth0Id());
            throw new UserRegistrationException("Error guardando usuario en la base de datos", ex);

        } catch (Auth0Exception ex) {
            log.error("Error asignando rol, eliminando usuario {}", user.getAuth0Id(), ex);
            auth0UserService.deleteUser(user.getAuth0Id());
            throw new UserRegistrationException("Error asignando rol en Auth0", ex);
        }
        return userMapper.toResponse(user);
    }

    // TODO: Mejorar este método
    @Transactional
    public UserResponse getCurrent(String auth0Id, String email, String name) {
        return userRepository.findByAuth0Id(auth0Id)
                .map(userMapper::toResponse)
                .orElseGet(() -> {
                    if (userRepository.existsByEmail(email)) {
                        throw new ExistingResourceException("El email '" + email + "' ya está registrado con otro método de autenticación");
                    }

                    RoleResponse userRole = auth0UserService.getUserRole(auth0Id);
                    String roleName = (userRole != null) ? userRole.getName() : USER_ROLE_NAME;

                    Role role = roleService.getRoleByNameOrThrow(roleName, true);

                    User user = User.builder()
                            .auth0Id(auth0Id)
                            .name(name)
                            .email(email)
                            .role(role)
                            .build();

                    try {
                        log.info("Asignando rol en Auth0 al usuario '{}'", email);
                        auth0UserService.setUserRole(auth0Id, role.getAuth0Id());

                        log.info("Guardando usuario '{}' en la base de datos", email);
                        User saved = userRepository.save(user);
                        return userMapper.toResponse(saved);

                    } catch (Auth0Exception e) {
                        log.error("Error asignando rol en Auth0 al usuario '{}'", auth0Id, e);
                        throw new UserRegistrationException("Error asignando rol en Auth0", e);

                    } catch (DataAccessException e) {
                        log.error("Error guardando el usuario '{}' en la base de datos", auth0Id, e);
                        throw new UserRegistrationException("Error guardando usuario en la base de datos", e);
                    }
                });
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        User user = getUserByIdOrThrow(id, false);
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toResponse);
    }

    @Transactional
    public Page<UserResponse> findAllByRole(Pageable pageable, String roleName) {
        Role role = roleService.getRoleByNameOrThrow(roleName, true);

        return userRepository.findByRole(pageable, role)
                .map(userMapper::toResponse);
    }

    @Transactional
    public UserResponse activate(Long id) throws Auth0Exception {
        User user = getUserByIdOrThrow(id, false);

        if (user.getActive()) {
            log.info("Usuario con id '{}' ({}) ya está activo, no es necesario realizar otra acción", id, user.getEmail());
            return userMapper.toResponse(user);
        }

        auth0UserService.activateUser(user.getAuth0Id());
        user.setActive(true);

        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse deactivate(Long id) throws Auth0Exception {
        User user = getUserByIdOrThrow(id, false);

        if (!user.getActive()) {
            log.info("Usuario con id '{}' ({}) ya está desactivado, no es necesario realizar otra acción", id, user.getEmail());
            return userMapper.toResponse(user);
        }

        auth0UserService.deactivateUser(user.getAuth0Id());
        user.setActive(false);

        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse update(String auth0Id, UserUpdateRequest dto) throws Auth0Exception {
        User user = getUserByAuth0IdOrThrow(auth0Id, true);

        if ((dto.getName() != null && !dto.getName().isBlank()) && (!user.getName().equals(dto.getName()))) {
            auth0UserService.setUserName(user.getAuth0Id(), dto.getName());
            user.setName(dto.getName());
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            auth0UserService.setUserPassword(user.getAuth0Id(), dto.getPassword());
        }

        return userMapper.toResponse(user);
    }

    // Métodos auxiliares
    public User getUserByIdOrThrow(Long id, boolean verifyActive) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con el id '" + id + "' no encontrado"));

        if (verifyActive && !user.getActive()) {
            throw new ResourceNotFoundException("Usuario con el id '" + id + "' inactivo");
        }

        return user;
    }

    public User getUserByAuth0IdOrThrow(String auth0UserId, boolean verifyActive) {
        User usuario = userRepository.findByAuth0Id(auth0UserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con el auth0Id proporcionado no encontrado"));

        if (verifyActive && !usuario.getActive()) {
            throw new ResourceNotFoundException("Usuario con el auth0Id proporcionado inactivo");
        }

        return usuario;
    }
}
