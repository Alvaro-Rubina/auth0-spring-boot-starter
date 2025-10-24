package com.example.auth0springbootstarter.service.auth0;

import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.roles.Role;
import com.auth0.json.mgmt.roles.RolesPage;
import com.auth0.net.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class Auth0RoleService {

    private final ManagementAPI managementAPI;

    /**
     * Crea un nuevo rol en Auth0 si no existe previamente.
     * Si el rol ya existe, lo retorna sin crear uno nuevo.
     *
     * @param name Nombre del rol a crear.
     * @param description Descripción del rol.
     * @return El rol creado o el existente si ya estaba en Auth0.
     * @throws Auth0Exception Si ocurre un error al comunicarse con Auth0.
     */
    public Role createRol(String name, String description) throws Auth0Exception {
        Role existing = getRoleByName(name);
        if (existing != null) {
            log.info("El rol '{}' ya existe en Auth0.", name);
            return existing;
        }

        Role rolAuth0 = new Role();
        rolAuth0.setName(name);
        rolAuth0.setDescription(description);

        Response<Role> response = managementAPI.roles().create(rolAuth0).execute();
        Role createdRole = response.getBody();
        log.info("Rol '{}' creado exitosamente en Auth0 con ID '{}'.", name, createdRole.getId());
        return createdRole;
    }

    /**
     * Actualiza el nombre y la descripción de un rol existente en Auth0.
     *
     * @param auth0Id ID del rol en Auth0.
     * @param name Nuevo nombre del rol.
     * @param description Nueva descripción del rol.
     * @throws Auth0Exception Si ocurre un error al comunicarse con Auth0.
     */
    public void updateRol(String auth0Id, String name, String description) throws Auth0Exception {
        Role update = new Role();
        update.setName(name);
        update.setDescription(description);

        managementAPI.roles().update(auth0Id, update).execute();
        log.info("Rol '{}' actualizado exitosamente en Auth0.", auth0Id);
    }

    /**
     * Busca y retorna un rol de Auth0 por su nombre.
     *
     * @param name Nombre del rol a buscar.
     * @return El rol encontrado, o null si no existe.
     * @throws Auth0Exception Si ocurre un error al comunicarse con Auth0.
     */
    public Role getRoleByName(String name) throws Auth0Exception {
        Response<RolesPage> response = managementAPI.roles().list(null).execute();
        List<Role> roles = response.getBody().getItems();

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

    /**
     * Verifica si existe un rol en Auth0 con el nombre especificado.
     *
     * @param name Nombre del rol a verificar.
     * @return true si el rol existe, false en caso contrario.
     * @throws Auth0Exception Si ocurre un error al comunicarse con Auth0.
     */
    public boolean existsRoleByName(String name) throws Auth0Exception {
        return getRoleByName(name) != null;
    }
}
