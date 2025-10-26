package com.security.keycloak.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.security.keycloak.controller.exception.ConflictException;
import com.security.keycloak.controller.exception.ResourceNotFoundException;
import com.security.keycloak.controller.exception.UserException;
import com.security.keycloak.dtos.UserDTO;
import com.security.keycloak.service.IUserKeycloakService;
import com.security.keycloak.util.KeycloakProvider;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserKeycloakServiceImpl implements IUserKeycloakService {

    @Autowired
    private KeycloakProvider keycloakProvider;

    private static final Pattern STRONG_PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$");

    @Override
    public List<UserDTO> findUserByEmail(String email) {
        log.info("Buscando usuario por email exacto: {}", email);
        
        // Usamos searchByEmail(email, true) para una búsqueda exacta
        return keycloakProvider.getRealmResource()
                .users()
                .searchByEmail(email, true) 
                .stream()
                .map(user -> {
                    List<RoleRepresentation> roles = keycloakProvider.getRealmResource()
                            .users()
                            .get(user.getId())
                            .roles()
                            .realmLevel()
                            .listEffective();

                    return UserDTO.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .roles(roles.stream().map(RoleRepresentation::getName).toList())
                            .build();
                })
                .toList();
    }


    @Override
    public List<UserDTO> findAllUsers() {
        return keycloakProvider.getRealmResource()
                .users()
                .list()
                .stream()
                .map(user -> {
                    List<RoleRepresentation> roles = keycloakProvider.getRealmResource()
                            .users()
                            .get(user.getId())
                            .roles()
                            .realmLevel()
                            .listEffective();

                    return UserDTO.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .roles(roles.stream().map(RoleRepresentation::getName).toList())
                            .build();
                })
                .toList();
    }
    
    @Override
    public List<UserDTO> findUserByUsername(String username) {
        return keycloakProvider.getRealmResource()
                .users()
                .search(username)
                .stream()
                .map(user -> {
                    List<RoleRepresentation> roles = keycloakProvider.getRealmResource()
                            .users()
                            .get(user.getId())
                            .roles()
                            .realmLevel()
                            .listEffective();

                    return UserDTO.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .roles(roles.stream().map(RoleRepresentation::getName).toList())
                            .build();
                })
                .toList();
    }

    @Override
    public UserDTO createUser(UserDTO userDTO, String role) {
        RealmResource realm = keycloakProvider.getRealmResource();
        UsersResource usersResource = realm.users();

        String password = userDTO.getPassword();
        if (password == null || password.isBlank()) {
            throw new UserException("La contraseña es obligatoria al crear un usuario", 400); // O usa ConstraintViolationException si prefieres
        }
        if (password.length() < 8 || password.length() > 20) {
             throw new UserException("La contraseña debe tener entre 8 y 20 caracteres", 400);
        }
        if (!STRONG_PASSWORD_PATTERN.matcher(password).matches()) {
             throw new UserException("La contraseña debe contener al menos una mayúscula, una minúscula, un número y un carácter especial", 400);
        }

        // Validar si ya existe un usuario con ese username o email
        List<UserRepresentation> existingUsers = usersResource.search(userDTO.getUsername(), true);
        if (!existingUsers.isEmpty()) {
            log.error("El usuario '{}' ya existe en Keycloak", userDTO.getUsername());
            throw new ConflictException("El usuario ya existe");
        }

        // Crear el objeto UserRepresentation
        UserRepresentation user = new UserRepresentation();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEnabled(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(userDTO.getPassword());

        user.setCredentials(Collections.singletonList(credential));

        try {
            Response response = usersResource.create(user);
            int status = response.getStatus();

            if (status == 201) {
                // Obtener el ID del nuevo usuario desde el header "Location"
                String location = response.getHeaderString("Location");
                String userId = location != null ? location.replaceAll(".*/(.*)$", "$1") : null;

                if (userId == null) {
                    log.warn("Usuario '{}' creado, pero no se pudo extraer el ID", userDTO.getUsername());
                    throw new RuntimeException("Error interno: no se pudo obtener el ID del usuario creado");
                }

                UserRepresentation createdUser = usersResource.get(userId).toRepresentation();

                // Asignar rol 
                List<String> rolesToAssign = new ArrayList<>();
                if (role != null && !role.isBlank()) {
                    rolesToAssign.add(role);
                } else if (userDTO.getRoles() != null && !userDTO.getRoles().isEmpty()) {
                    rolesToAssign.addAll(userDTO.getRoles());
                } else {
                    rolesToAssign.add("user_realm");
                }

                List<RoleRepresentation> rolesRep = realm.roles().list().stream()
                    .filter(r -> rolesToAssign.contains(r.getName()))
                    .toList();

                if (!rolesRep.isEmpty()) {
                    realm.users().get(userId).roles().realmLevel().add(rolesRep);
                    log.info("Roles asignados al usuario '{}': {}", createdUser.getUsername(), rolesToAssign);
                } else {
                    log.warn("Roles no encontrados, usuario creado sin roles");
                }

                // Obtener roles efectivos del usuario
                List<RoleRepresentation> assignedRoles = realm.users().get(userId).roles().realmLevel().listEffective();

                log.info("Usuario '{}' creado correctamente en Keycloak (id={})", createdUser.getUsername(), userId);

                return UserDTO.builder()
                        .id(userId)
                        .username(createdUser.getUsername())
                        .email(createdUser.getEmail())
                        .firstName(createdUser.getFirstName())
                        .lastName(createdUser.getLastName())
                        .roles(assignedRoles.stream().map(RoleRepresentation::getName).toList())
                        .build();

            } else if (status == 409) {
                throw new ConflictException("El usuario ya existe");
            } else if (status == 404) {
                throw new ResourceNotFoundException("Recurso de Keycloak no encontrado");
            } else {
                throw new RuntimeException("Error al crear usuario en Keycloak (HTTP " + status + ")");
            }

        } catch (ClientErrorException cee) {
            int status = cee.getResponse() != null ? cee.getResponse().getStatus() : 500;
            log.error("Error de Keycloak al crear usuario '{}'. Status={} - {}", userDTO.getUsername(), status, cee.getMessage(), cee);
            if (status == 409) throw new ConflictException("El usuario ya existe");
            if (status == 404) throw new ResourceNotFoundException("Recurso de Keycloak no encontrado");
            throw new RuntimeException("Error de Keycloak (" + status + "): " + cee.getMessage(), cee);
        } catch (Exception e) {
            log.error("Error inesperado al crear usuario '{}': {}", userDTO.getUsername(), e.getMessage(), e);
            throw new RuntimeException("Error interno del servidor al crear usuario", e);
        }
    }


    @Override
    public void deleteUser(String userId) {
        keycloakProvider.getUserResource()
            .get(userId)
            .remove();
    }

    @Override
    public UserDTO updateUser(String userId,@NonNull UserDTO userDTO) {

        UserRepresentation user = new UserRepresentation();
        user.setUsername(userDTO.getUsername());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setEnabled(true);
        user.setEmailVerified(true);

        if (userDTO.getPassword() != null) {

            String password = userDTO.getPassword();
              if (password.length() < 8 || password.length() > 20) {
                 throw new UserException("La contraseña debe tener entre 8 y 20 caracteres", 400);
             }
             if (!STRONG_PASSWORD_PATTERN.matcher(password).matches()) {
                 throw new UserException("La contraseña debe contener...", 400);
             }
            CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
            credentialRepresentation.setTemporary(false);
            credentialRepresentation.setType(OAuth2Constants.PASSWORD);
            credentialRepresentation.setValue(userDTO.getPassword());
            user.setCredentials(List.of(credentialRepresentation));
        }

        RealmResource realmResource = keycloakProvider.getRealmResource();
        List<RoleRepresentation> roles = realmResource
            .roles()
            .list()
            .stream()
            .filter(role -> userDTO.getRoles()
                .stream()
                .anyMatch(roleName -> roleName.equalsIgnoreCase(role.getName())))
            .toList();
        
        realmResource.users()
            .get(userId)
            .roles()
            .realmLevel()
            .remove(realmResource.roles().list());
        
        realmResource.users()
            .get(userId)
            .roles()
            .realmLevel()
            .add(roles);

        UserResource userResource = keycloakProvider.getUserResource().get(userId);
        userResource.update(user);

        return userDTO;
    }

    @Override
    public UserDTO findUserById(String userId) {
        UserRepresentation user = keycloakProvider.getUserResource().get(userId).toRepresentation();

        List<RoleRepresentation> roles = keycloakProvider.getRealmResource()
            .users()
            .get(user.getId())
            .roles()
            .realmLevel()
            .listEffective();

        return UserDTO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .roles(roles.stream().map(RoleRepresentation::getName).toList())
            .build();
    }

    @Override
    public List<String> getRoles() {
        return keycloakProvider.getRealmResource()
            .roles()
            .list()
            .stream()
            .map(RoleRepresentation::getName)
            .filter(role -> !role.startsWith("default-roles") && !role.equals("offline_access") && !role.equals("uma_protection"))
            .toList();
    }

}
