package com.security.keycloak.service.impl;

import java.util.List;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.security.keycloak.controller.exception.UserException;
import com.security.keycloak.dtos.UserDTO;
import com.security.keycloak.service.IUserKeycloakService;
import com.security.keycloak.util.KeycloakProvider;
import jakarta.ws.rs.core.Response;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserKeycloakServiceImpl implements IUserKeycloakService {

    @Autowired
    private KeycloakProvider keycloakProvider;

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
    public UserDTO createUser(@NonNull UserDTO userDTO) {
        int status = 0;
        UsersResource usersResource = keycloakProvider.getUserResource();

        UserRepresentation user = new UserRepresentation();

        user.setUsername(userDTO.getUsername());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setEnabled(true);
        user.setEmailVerified(true);

        Response response = usersResource.create(user);
        status = response.getStatus();

        if(status == 201) {
            String path = response.getLocation().getPath();
            String userId = path.substring(path.lastIndexOf('/') + 1);
            
            CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
            credentialRepresentation.setTemporary(false);
            credentialRepresentation.setType(OAuth2Constants.PASSWORD);
            credentialRepresentation.setValue(userDTO.getPassword());

            usersResource.get(userId).resetPassword(credentialRepresentation);

            RealmResource realmResource = keycloakProvider.getRealmResource();

            List<RoleRepresentation> roles = null;

            if(userDTO.getRoles() == null || userDTO.getRoles().isEmpty()) {
                roles = List.of(realmResource.roles().get("user_realm").toRepresentation());       
            }else{
                roles = realmResource
                    .roles()
                    .list()
                    .stream()
                    .filter(role -> userDTO.getRoles()
                        .stream()
                        .anyMatch(roleName -> roleName.equalsIgnoreCase(role.getName())))
                    .toList();
            }

            realmResource.users()
                .get(userId)
                .roles()
                .realmLevel()
                .add(roles);

            return userDTO;

        } else if(status == 409) {
            log.error("User with username: {} already exists.", user.getUsername());
            throw new UserException("User with username already exists", status);
        } else {
            log.error("Error creating user with username: {}. Status code: {}", user.getUsername(), status);
            throw new UserException("Error creating user. Status code: " + status, status);
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
    
}
