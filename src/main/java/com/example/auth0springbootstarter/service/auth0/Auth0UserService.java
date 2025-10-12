package com.example.auth0springbootstarter.service.auth0;

import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.Role;
import com.auth0.json.mgmt.users.User;
import com.auth0.net.Request;
import com.example.auth0springbootstarter.persistence.dto.role.RoleResponse;
import com.example.auth0springbootstarter.persistence.dto.user.signup.SignupRequest;
import com.example.auth0springbootstarter.persistence.dto.user.signup.SignupResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class Auth0UserService {

    /*@Value("${auth0.connection}")
    private String connection; *//*Username-Password-Authentication*/

    private final ManagementAPI managementAPI;

    public SignupResponse registerUserFromDTO(SignupRequest dto) throws Auth0Exception {
        User user = new User();

        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword().toCharArray());
        user.setName((dto.getName() != null && !dto.getName().isBlank())
                ? dto.getName()
                : dto.getEmail());

        try {
            log.info("Creando usuario '{}' en Auth0 vía Management API", dto.getEmail());
            User createdUser = managementAPI.users().create(user).execute();

            return SignupResponse.builder()
                    .auth0Id(createdUser.getId())
                    .email(createdUser.getEmail())
                    .name(createdUser.getName())
                    .build();

        } catch (Auth0Exception e) {
            log.error("Error creando usuario '{}' en Auth0", dto.getEmail(), e);
            throw e;
        }
    }

    public void toggleUserActiveStatus(String auth0Id, boolean active) throws Auth0Exception {
        try {
            User userUpdate = new User();
            userUpdate.setBlocked(!active); // blocked = true -> desactivado

            log.info("Actualizando estado activo del usuario con id en Auth0 '{}'", auth0Id);
            Request<User> request = managementAPI.users().update(auth0Id, userUpdate);
            request.execute();
            log.info("Usuario con id en Auth0 '{}' actualizado exitosamente", auth0Id);

        } catch (Auth0Exception e) {
            log.error("Error actualizando estado activo del usuario con id en Auth0 '{}'", auth0Id, e);
            throw e;
        }
    }

    public void setUserRole(String auth0Id, String auth0RoleId) throws Auth0Exception {
        try {
            log.info("Asignando rol con id en Auth0 '{}' al usuario con id en Auth0 '{}'", auth0RoleId, auth0Id);
            managementAPI.users().addRoles(auth0Id, Collections.singletonList(auth0RoleId)).execute();
            log.info("Rol asignado exitosamente al usuario con id en Auth0 '{}'", auth0Id);

        } catch (Auth0Exception e) {
            log.error("Error asignando rol al usuario con id en Auth0 '{}'", auth0Id, e);
            throw e;
        }
    }

    public void setUserName(String auth0Id, String name) throws Auth0Exception {
        try {
            User userUpdate = new User();
            userUpdate.setName(name);
            log.info("Estableciendo nombre del usuario con id en Auth0 '{}'", auth0Id);
            managementAPI.users().update(auth0Id, userUpdate).execute();

        } catch (Auth0Exception e) {
            log.error("Error estableciendo el nombre del usuario con id en Auth0 '{}'", auth0Id);
            throw e;
        }
    }

    public void setUserPassword(String auth0Id, String password) throws Auth0Exception {
        try {
            User userUpdate = new User();
            userUpdate.setPassword(password.toCharArray());
            log.info("Estableciendo contraseña del usuario con id en Auth0 '{}'", auth0Id);
            managementAPI.users().update(auth0Id, userUpdate).execute();
            log.info("Contraseña establecida exitosamente para el usuario con id en Auth0 '{}'", auth0Id);

        } catch (Auth0Exception e) {
            log.error("Error estableciendo la contraseña del usuario con id en Auth0 '{}'", auth0Id, e);
            throw e;
        }
    }

    public void setUserPicture(String auth0Id, String pictureUrl) throws Auth0Exception {
        try {
            User userUpdate = new User();
            userUpdate.setPicture(pictureUrl);
            log.info("Estableciendo foto de perfil al usuario con id en Auth0 '{}'", auth0Id);
            managementAPI.users().update(auth0Id, userUpdate).execute();

        } catch (Auth0Exception e) {
            log.error("Error estableciendo la foto de perfil del usuario con id en Auth0 '{}'", auth0Id, e);
            throw e;
        }
    }

    public RoleResponse getUserRole(String auth0Id) {
        try {
            log.info("Obteniendo roles del usuario con id en Auth0 '{}'", auth0Id);

            List<Role> roles = managementAPI.users().listRoles(auth0Id, null).execute().getItems();
            if (roles != null && !roles.isEmpty()) {
                Role rol = roles.getFirst();
                return RoleResponse.builder()
                        .name(rol.getName())
                        .description(rol.getDescription())
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

    public String getUserPicture(String auth0Id) {
        try {
            log.info("Obteniendo foto de perfil del usuario con id en Auth0 '{}'", auth0Id);
            User user = managementAPI.users().get(auth0Id, null).execute();
            return user.getPicture();

        } catch (Auth0Exception e) {
            log.error("Error obteniendo foto de perfil del usuario con id en Auth0 '{}'", auth0Id, e);
            return null;
        }
    }

    public void deleteUser(String auth0Id) throws Auth0Exception {
        try {
            log.info("Eliminando usuario con id en Auth0 '{}'", auth0Id);
            managementAPI.users().delete(auth0Id).execute();
            log.info("Usuario con id en Auth0 '{}' eliminado exitosamente", auth0Id);

        } catch (Auth0Exception e) {
            log.error("Error eliminando usuario con id en Auth0 '{}'", auth0Id, e);
            throw e;
        }
    }

}
