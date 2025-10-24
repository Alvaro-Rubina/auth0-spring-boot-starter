package com.example.auth0springbootstarter.service.auth0;

import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.roles.Role;
import com.auth0.json.mgmt.roles.RolesPage;
import com.auth0.json.mgmt.users.User;
import com.auth0.net.Response;
import com.example.auth0springbootstarter.persistence.dto.role.RoleResponse;
import com.example.auth0springbootstarter.persistence.dto.user.signup.SignupRequest;
import com.example.auth0springbootstarter.persistence.dto.user.signup.SignupResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class Auth0UserService {

    private final ManagementAPI managementAPI;

    /**
     * Registra un nuevo usuario en Auth0 a partir de un DTO de registro.
     *
     * @param dto DTO con los datos del usuario a registrar.
     * @return SignupResponse con los datos del usuario creado en Auth0.
     * @throws Auth0Exception Si ocurre un error al comunicarse con Auth0.
     */
    @Retryable(
            value = Auth0Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public SignupResponse registerUserFromDTO(SignupRequest dto) throws Auth0Exception {
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword().toCharArray());
        user.setName((dto.getName() != null && !dto.getName().isBlank())
                ? dto.getName()
                : dto.getEmail());

        log.info("Creando usuario '{}' en Auth0 vía Management API", dto.getEmail());
        Response<User> response = managementAPI.users().create(user).execute();
        User createdUser = response.getBody();

        return SignupResponse.builder()
                .auth0Id(createdUser.getId())
                .email(createdUser.getEmail())
                .name(createdUser.getName())
                .build();
    }

    /**
     * Activa un usuario en Auth0.
     *
     * @param auth0Id ID del usuario en Auth0.
     * @throws Auth0Exception Si ocurre un error al comunicarse con Auth0.
     */
    @Retryable(
            value = Auth0Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void activateUser(String auth0Id) throws Auth0Exception {
        log.info("Activando usuario con id '{}' en Auth0", auth0Id);

        User userUpdate = new User();
        userUpdate.setBlocked(false);

        managementAPI.users().update(auth0Id, userUpdate).execute();

        log.info("Usuario con id '{}' en Auth0 activado exitosamente", auth0Id);
    }

    /**
     * Desactiva (bloquea) un usuario en Auth0.
     *
     * @param auth0Id ID del usuario en Auth0.
     * @throws Auth0Exception Si ocurre un error al comunicarse con Auth0.
     */
    @Retryable(
            value = Auth0Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void deactivateUser(String auth0Id) throws Auth0Exception {
        log.info("Desactivando usuario con id '{}' en Auth0", auth0Id);

        User userUpdate = new User();
        userUpdate.setBlocked(true);

        managementAPI.users().update(auth0Id, userUpdate).execute();

        log.info("Usuario con id '{}' en Auth0 desactivado exitosamente", auth0Id);
    }


    /**
     * Asigna un rol a un usuario en Auth0.
     *
     * @param auth0Id ID del usuario en Auth0.
     * @param auth0RoleId ID del rol en Auth0.
     * @throws Auth0Exception Si ocurre un error al comunicarse con Auth0.
     */
    @Retryable(
            value = Auth0Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void setUserRole(String auth0Id, String auth0RoleId) throws Auth0Exception {
        log.info("Asignando rol con id en Auth0 '{}' al usuario con id en Auth0 '{}'", auth0RoleId, auth0Id);
        managementAPI.users().addRoles(auth0Id, Collections.singletonList(auth0RoleId)).execute();
        log.info("Rol asignado exitosamente al usuario con id en Auth0 '{}'", auth0Id);
    }

    /**
     * Establece el nombre de un usuario en Auth0.
     *
     * @param auth0Id ID del usuario en Auth0.
     * @param name Nuevo nombre para el usuario.
     * @throws Auth0Exception Si ocurre un error al comunicarse con Auth0.
     */
    @Retryable(
            value = Auth0Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void setUserName(String auth0Id, String name) throws Auth0Exception {
        log.info("Estableciendo nombre del usuario con id en Auth0 '{}'", auth0Id);
        User userUpdate = new User();
        userUpdate.setName(name);
        managementAPI.users().update(auth0Id, userUpdate).execute();
        log.info("Nombre establecido exitosamente para el usuario con id en Auth0 '{}'", auth0Id);
    }
    /**
     * Establece la contraseña de un usuario en Auth0.
     *
     * @param auth0Id ID del usuario en Auth0.
     * @param password Nueva contraseña para el usuario.
     * @throws Auth0Exception Si ocurre un error al comunicarse con Auth0.
     */
    @Retryable(
            value = Auth0Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void setUserPassword(String auth0Id, String password) throws Auth0Exception {
        log.info("Estableciendo contraseña del usuario con id en Auth0 '{}'", auth0Id);
        User userUpdate = new User();
        userUpdate.setPassword(password.toCharArray());
        managementAPI.users().update(auth0Id, userUpdate).execute();
        log.info("Contraseña establecida exitosamente para el usuario con id en Auth0 '{}'", auth0Id);
    }

    /**
     * Obtiene el rol principal asignado a un usuario en Auth0.
     *
     * @param auth0Id ID del usuario en Auth0.
     * @return RoleResponse con los datos del rol, o null si no tiene roles.
     */
    public RoleResponse getUserRole(String auth0Id) {
        try {
            log.info("Obteniendo roles del usuario con id en Auth0 '{}'", auth0Id);

            Response<RolesPage> response = managementAPI.users().listRoles(auth0Id, null).execute();
            List<Role> roles = response.getBody().getItems();

            if (roles != null && !roles.isEmpty()) {
                Role role = roles.get(0);
                log.info("El Usuario con id en Auth0 '{}' tiene el siguiente rol asignado: '{}'", auth0Id, role.getName());
                return RoleResponse.builder()
                        .name(role.getName())
                        .description(role.getDescription())
                        .build();

            } else {
                log.warn("El usuario con id en Auth0 '{}' no tiene roles asignados", auth0Id);
                return null;
            }

        } catch (Auth0Exception e) {
            log.error("Error obteniendo roles del usuario con id en Auth0 '{}'", auth0Id, e);
            return null;
        }
    }

    /**
     * Obtiene la URL de la foto de perfil de un usuario en Auth0.
     *
     * @param auth0Id ID del usuario en Auth0.
     * @return URL de la foto de perfil, o null si no está disponible.
     */
    public String getUserPicture(String auth0Id) {
        try {
            log.info("Obteniendo foto de perfil del usuario con id en Auth0 '{}'", auth0Id);
            Response<User> response = managementAPI.users().get(auth0Id, null).execute();
            User user = response.getBody();
            return user.getPicture();

        } catch (Auth0Exception e) {
            log.error("Error obteniendo foto de perfil del usuario con id en Auth0 '{}'", auth0Id, e);
            return null;
        }
    }

    /**
     * Elimina un usuario de Auth0 por su ID.
     *
     * @param auth0Id ID del usuario en Auth0.
     * @throws Auth0Exception Si ocurre un error al comunicarse con Auth0.
     */
    @Retryable(
            value = Auth0Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void deleteUser(String auth0Id) throws Auth0Exception {
        log.info("Eliminando usuario con id en Auth0 '{}'", auth0Id);
        managementAPI.users().delete(auth0Id).execute();
        log.info("Usuario con id en Auth0 '{}' eliminado exitosamente", auth0Id);
    }
}
