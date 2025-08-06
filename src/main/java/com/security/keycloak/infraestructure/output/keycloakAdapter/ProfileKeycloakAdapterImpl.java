package com.security.keycloak.infraestructure.output.keycloakAdapter;

import java.util.Arrays;
import java.util.List;

import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.security.keycloak.application.input.IRealmResourceInputPort;
import com.security.keycloak.application.output.IProfileKeycloakOutputPort;
import com.security.keycloak.domain.models.Profile;
import com.security.keycloak.infraestructure.input.rest.data.response.ProfileResponse;
import com.security.keycloak.infraestructure.output.keycloakAdapter.Exception.ProfileException;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProfileKeycloakAdapterImpl implements IProfileKeycloakOutputPort {
    
    @Autowired
    private IRealmResourceInputPort realmResourceInputPort;

    /**
     * Metodo para obtener todos los perfiles de Keycloak.´
     * @return List<ProfileResponse> Lista de perfiles.
     */
    @Override
    public List<ProfileResponse> findAllProfiles() {
        List<String> excludedRoles = Arrays.asList(
                "uma_authorization",
                "offline_access",
                "default-roles-spring-boot-realm-dev",
                "default-roles-oauth2-realm");

        return realmResourceInputPort.getRealmResource()
                .roles()
                .list()
                .stream()
                .filter(role -> !excludedRoles.contains(role.getName()))
                .map(role -> ProfileResponse.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .description(role.getDescription())
                        .build())
                .toList();

    }

    /**
     * Metodo para obtener un perfil por su nombre
     * @param name nombre del perfil
     * @return List<ProfileResponse>
     */
    @Override
    public List<ProfileResponse> findProfileByName(String name) {
        return realmResourceInputPort.getRealmResource()
                .roles()
                .list()
                .stream()
                .filter(role -> role.getName().equalsIgnoreCase(name))
                .map(role -> ProfileResponse.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .description(role.getDescription())
                        .build())
                .toList();
    }

    /**
     * Metodo para obtener un perfil por su ID
     * @param profileId ID del perfil
     * @return ProfileResponse
     */
    @Override
    public ProfileResponse findProfileById(String profileId) {
        return realmResourceInputPort.getRealmResource()
            .roles()
            .list()
            .stream()
            .filter(role -> profileId.equals(role.getId()))
            .findFirst()
            .map(role -> ProfileResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .build())
            .orElseThrow(() -> new ProfileException(
                "Role with ID " + profileId + " not found", 
                Response.Status.NOT_FOUND.getStatusCode()
            ));
    }

    /**
     * Metodo para crear un perfil en Keycloak
     * @param profileDTO Objeto Profile con los datos del perfil a crear
     * @return Profile Objeto Profile creado
     */
    @Override
    public Profile createProfile(Profile profileDTO) {
        RealmResource realmResource = realmResourceInputPort.getRealmResource();
        RolesResource rolesResource = realmResource.roles();

        String roleName = profileDTO.getName();

        try {
            try {
                rolesResource.get(roleName).toRepresentation();
                // Si llega aquí, el rol existe
                log.error("Role with name: {} already exists", roleName);
                throw new ProfileException("Role name already exists", Response.Status.CONFLICT.getStatusCode());
            } catch (NotFoundException e) {
                // El rol no existe, proceder a crearlo
                RoleRepresentation roleRep = new RoleRepresentation();
                roleRep.setName(roleName);
                roleRep.setDescription(profileDTO.getDescription());

                rolesResource.create(roleRep);
                log.info("Role {} created successfully", roleName);
                return profileDTO;
            }
        } catch (ClientErrorException e) {
            int status = e.getResponse().getStatus();
            log.error("Client error creating role: {}. Status: {}", roleName, status);
            throw new ProfileException("Client error creating role: " + e.getMessage(), status);
        } catch (Exception e) {
            // Manejar otros errores genéricos
            log.error("Unexpected error creating role: {}", roleName, e);
            throw new ProfileException("Unexpected error: " + e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
    }

    /**
     * Metodo para eliminar un perfil en Keycloak
     * @param profileId ID del perfil a eliminar
     * @throws ProfileException Si el rol está asignado a algún usuario o no existe
     */
    @Override
    public void deleteProfile(String profileId) {
        RealmResource realmResource = realmResourceInputPort.getRealmResource();
        
        // 1. Buscar el rol por ID para obtener su nombre
        RoleRepresentation roleToDelete = realmResource.roles().list().stream()
            .filter(role -> profileId.equals(role.getId()))
            .findFirst()
            .orElseThrow(() -> new ProfileException(
                "Role with ID " + profileId + " not found", 
                Response.Status.NOT_FOUND.getStatusCode()
            ));
        
        String roleName = roleToDelete.getName();
        RoleResource roleResource = realmResource.roles().get(roleName);
        
        // 2. Verificar asignaciones a usuarios
        List<UserRepresentation> users = roleResource.getUserMembers();
        if (users != null && !users.isEmpty()) {
            throw new ProfileException(
                "Role '" + roleName + "' is assigned to " + users.size() + " user(s)", 
                Response.Status.CONFLICT.getStatusCode()
            );
        }
        
        // 3. Eliminar usando el NOMBRE
        roleResource.remove();
    }

    /**
     * Metodo para actualizar un perfil en Keycloak
     * @param profileId ID del perfil a actualizar
     * @param profile Objeto Profile con los datos a actualizar
     * @return Profile Objeto Profile actualizado
     */
    @Override
    public Profile updateProfile(String profileId, Profile profileDTO) {
        RealmResource realmResource = realmResourceInputPort.getRealmResource();
        
        // 1. Buscar el rol existente por ID
        RoleRepresentation existingRole = realmResource.roles().list().stream()
            .filter(role -> profileId.equals(role.getId()))
            .findFirst()
            .orElseThrow(() -> new ProfileException(
                "Role with ID " + profileId + " not found", 
                Response.Status.NOT_FOUND.getStatusCode()
            ));
        
        String currentRoleName = existingRole.getName();
        String newRoleName = profileDTO.getName();
        
        // 2. Verificar si el nuevo nombre ya existe (solo si cambió)
        if (!currentRoleName.equals(newRoleName)) {
            try {
                realmResource.roles().get(newRoleName).toRepresentation();
                throw new ProfileException(
                    "Role name '" + newRoleName + "' already exists", 
                    Response.Status.CONFLICT.getStatusCode()
                );
            } catch (NotFoundException e) {
                // Nombre disponible, continuar
            }
        }
        
        // 3. Actualizar el rol usando su NOMBRE actual
        RoleRepresentation updatedRole = new RoleRepresentation();
        updatedRole.setName(newRoleName);
        updatedRole.setDescription(profileDTO.getDescription());
        
        realmResource.roles().get(currentRoleName).update(updatedRole);
        
        return profileDTO;
    }

    
}
