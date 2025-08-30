package com.security.keycloak.service.impl;

import java.util.Arrays;
import java.util.List;

import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import com.security.keycloak.controller.exception.ConflictException;
import com.security.keycloak.controller.exception.ResourceNotFoundException;
import com.security.keycloak.dtos.ProfileDTO;
import com.security.keycloak.service.IProfileKeycloakService;
import com.security.keycloak.util.KeycloakProvider;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProfileKeycloakServiceImpl implements IProfileKeycloakService {

    private final KeycloakProvider keycloakProvider;

    @Override
    public List<ProfileDTO> findAllProfiles() {
        List<String> excludedRoles = Arrays.asList(
                "uma_authorization",
                "offline_access",
                "default-roles-spring-boot-realm-dev",
                "default-roles-oauth2-realm"
        );

        return keycloakProvider.getRealmResource()
                .roles()
                .list()
                .stream()
                .filter(role -> !excludedRoles.contains(role.getName()))
                .map(role -> ProfileDTO.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .description(role.getDescription())
                        .build())
                .toList();
    }

    @Override
    public ProfileDTO findProfileById(String profileId) {
        return keycloakProvider.getRealmResource()
                .roles()
                .list()
                .stream()
                .filter(role -> role.getId().equals(profileId))
                .map(role -> ProfileDTO.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .description(role.getDescription())
                        .build())
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el perfil con ID " + profileId));
    }

    @Override
    public ProfileDTO createProfile(ProfileDTO profileDTO) {
        RealmResource realm = keycloakProvider.getRealmResource();
        RolesResource roles = realm.roles();
        String roleName = profileDTO.getName();

        // Verificar existencia por nombre → 409 si ya existe
        try {
            roles.get(roleName).toRepresentation();
            log.error("El nombre de perfil '{}' ya existe", roleName);
            throw new ConflictException("El nombre del perfil ya existe");
        } catch (NotFoundException notExists) {
            // OK, no existe
        }

        // Crear rol
        RoleRepresentation rep = new RoleRepresentation();
        rep.setName(roleName);
        rep.setDescription(profileDTO.getDescription());

        try {
            roles.create(rep);
            RoleRepresentation created = roles.get(roleName).toRepresentation();
            log.info("Perfil '{}' creado correctamente (id={})", roleName, created.getId());
            return ProfileDTO.builder()
                    .id(created.getId())
                    .name(created.getName())
                    .description(created.getDescription())
                    .build();
        } catch (ClientErrorException cee) {
            int status = cee.getResponse() != null ? cee.getResponse().getStatus() : 500;
            log.error("Error de Keycloak al crear perfil ({}). Status={} - {}", roleName, status, cee.getMessage(), cee);
            if (status == 409) throw new ConflictException("El nombre del perfil ya existe");
            if (status == 404) throw new ResourceNotFoundException("Recurso de Keycloak no encontrado");
            throw new RuntimeException("Error de Keycloak (" + status + "): " + cee.getMessage(), cee);
        }
    }

    @Override
    public void deleteProfile(String profileId) {
        RealmResource realm = keycloakProvider.getRealmResource();

        RoleRepresentation roleToDelete = realm.roles().list().stream()
                .filter(role -> profileId.equals(role.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el perfil con ID " + profileId));

        String roleName = roleToDelete.getName();
        RoleResource roleResource = realm.roles().get(roleName);

        List<UserRepresentation> users = roleResource.getUserMembers();
        if (users != null && !users.isEmpty()) {
            // coherente con Swagger: 409
            throw new ConflictException("El perfil está asignado a uno o más usuarios");
        }

        try {
            roleResource.remove();
            log.info("Perfil '{}' eliminado correctamente", roleName);
        } catch (ClientErrorException cee) {
            int status = cee.getResponse() != null ? cee.getResponse().getStatus() : 500;
            log.error("Error de Keycloak al eliminar perfil ({}). Status={} - {}", roleName, status, cee.getMessage(), cee);
            if (status == 404) throw new ResourceNotFoundException("No se encontró el perfil");
            throw new RuntimeException("Error de Keycloak (" + status + "): " + cee.getMessage(), cee);
        }
    }

    @Override
    public ProfileDTO updateProfile(String profileId, ProfileDTO profileDTO) {
        RealmResource realm = keycloakProvider.getRealmResource();

        RoleRepresentation existingRole = realm.roles().list().stream()
                .filter(role -> profileId.equals(role.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el perfil con ID " + profileId));

        String currentRoleName = existingRole.getName();
        String newRoleName = profileDTO.getName();

        // Si cambia el nombre, verificar duplicado → 409
        if (!currentRoleName.equals(newRoleName)) {
            try {
                realm.roles().get(newRoleName).toRepresentation();
                throw new ConflictException("El nombre del perfil ya existe");
            } catch (NotFoundException notExists) {
                // OK: no existe, se puede renombrar
            }
        }

        RoleRepresentation updated = new RoleRepresentation();
        updated.setName(newRoleName);
        updated.setDescription(profileDTO.getDescription());

        try {
            realm.roles().get(currentRoleName).update(updated);
            RoleRepresentation finalRep = realm.roles().get(newRoleName).toRepresentation();
            log.info("Perfil '{}' actualizado correctamente a '{}'", currentRoleName, newRoleName);
            return ProfileDTO.builder()
                    .id(finalRep.getId())
                    .name(finalRep.getName())
                    .description(finalRep.getDescription())
                    .build();
        } catch (ClientErrorException cee) {
            int status = cee.getResponse() != null ? cee.getResponse().getStatus() : 500;
            log.error("Error de Keycloak al actualizar perfil ({} -> {}). Status={} - {}", currentRoleName, newRoleName, status, cee.getMessage(), cee);
            if (status == 404) throw new ResourceNotFoundException("No se encontró el perfil");
            if (status == 409) throw new ConflictException("El nombre del perfil ya existe");
            throw new RuntimeException("Error de Keycloak (" + status + "): " + cee.getMessage(), cee);
        }
    }

    @Override
    public List<ProfileDTO> findProfilesByName(String name) {
        return keycloakProvider.getRealmResource()
                .roles()
                .list()
                .stream()
                .filter(role -> role.getName().equalsIgnoreCase(name))
                .map(role -> ProfileDTO.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .description(role.getDescription())
                        .build())
                .toList();
    }
}
