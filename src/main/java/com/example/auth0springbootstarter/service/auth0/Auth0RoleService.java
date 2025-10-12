package com.example.auth0springbootstarter.service.auth0;

import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.Role;
import com.auth0.json.mgmt.RolesPage;
import com.auth0.net.Request;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class Auth0RoleService {

    private final ManagementAPI managementAPI;

    public Role createRol(String name, String description) throws Auth0Exception {
        Role existing = getRoleByName(name);
        if (existing != null) {
            log.info("El rol '{}' ya existe en Auth0.", name);
            return existing;
        }

        Role rolAuth0 = new Role();
        rolAuth0.setName(name);
        rolAuth0.setDescription(description);

        Role createdRole = managementAPI.roles().create(rolAuth0).execute();
        log.info("Rol '{}' creado exitosamente en Auth0 con ID '{}'.", name, createdRole.getId());
        return createdRole;
    }

    public void updateRol(String auth0Id, String name, String description) throws Auth0Exception {
        Role update = new Role();
        update.setName(name);
        update.setDescription(description);

        managementAPI.roles().update(auth0Id, update).execute();
        log.info("Rol '{}' actualizado exitosamente en Auth0.", auth0Id);
    }

    public Role getRoleByName(String name) throws Auth0Exception {
        Request<RolesPage> request = managementAPI.roles().list(null);
        List<Role> roles = request.execute().getItems();

        Role foundRole = roles.stream()
                .filter(r -> r.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);

        if (foundRole != null) {
            log.info("Rol '{}' encontrado en Auth0 con ID '{}'.", name, foundRole.getId());
        } else {
            log.info("Rol '{}' no encontrado en Auth0.", name);
        }

        return foundRole;
    }

}
