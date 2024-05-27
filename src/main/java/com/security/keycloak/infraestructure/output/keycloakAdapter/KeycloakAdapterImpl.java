package com.security.keycloak.infraestructure.output.keycloakAdapter;

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

import com.security.keycloak.application.input.IRealmResourceInputPort;
import com.security.keycloak.application.output.IKeycloakOutputPort;
import com.security.keycloak.domain.models.User;
import com.security.keycloak.infraestructure.input.rest.data.response.UserResponse;

import jakarta.ws.rs.core.Response;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KeycloakAdapterImpl implements IKeycloakOutputPort{

    @Autowired
    private IRealmResourceInputPort realmResourceInputPort;

    /**
     * Metodo para obtener todos los usuarios de Keycloak
     * @return List<UserResponse>
     */
    @Override
    public List<UserResponse> findAllUsers() {
        return realmResourceInputPort.getRealmResource()
            .users()
            .list()
            .stream()
            .map(user -> {
                List<RoleRepresentation> roles = realmResourceInputPort.getRealmResource()
                    .users()
                    .get(user.getId())
                    .roles()
                    .realmLevel()
                    .listEffective();

                return UserResponse.builder()
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


    /**
     * Metodo para obtener un usuario por su id
     * @param userId id del usuario
     * @return List<UserResponse>
     */
    @Override
    public List<UserResponse> findUserByUsername(String username) {

        return realmResourceInputPort.getRealmResource()
            .users()
            .search(username)
            .stream()
            .map(user -> {
                List<RoleRepresentation> roles = realmResourceInputPort.getRealmResource()
                    .users()
                    .get(user.getId())
                    .roles()
                    .realmLevel()
                    .listEffective();

                return UserResponse.builder()
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

    /**
     * Metodo para crear un usuario
     * @param userDTO datos del usuario
     * @return String
     */
    @Override
    public String createUser(@NonNull User userDTO) {
        int status = 0;
        UsersResource usersResource = realmResourceInputPort.getUserResource();

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

            RealmResource realmResource = realmResourceInputPort.getRealmResource();

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

            return "User created with username: " + user.getUsername() + " successfully.";

        } else if(status == 409) {
            log.error("User with username: {} already exists.", user.getUsername());
            return "User with username: " + user.getUsername() + " already exists.";
        } else {
            log.error("Error creating user with username: {}. Status code: {}", user.getUsername(), status);
            return "Error creating user with username: " + user.getUsername() + ". Status code: " + status;    
        }
    }

    /**
     * Metodo para eliminar un usuario
     * @param userId id del usuario
     */
    @Override
    public void deleteUser(String userId) {
        realmResourceInputPort.getUserResource()
            .get(userId)
            .remove();
    }

    /**
     * Metodo para actualizar un usuario
     * @param userId id del usuario
     * @param userDTO datos del usuario
     */
    @Override
    public void updateUser(String userId,@NonNull User userDTO) {
        
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType(OAuth2Constants.PASSWORD);
        credentialRepresentation.setValue(userDTO.getPassword());

        UserRepresentation user = new UserRepresentation();

        user.setUsername(userDTO.getUsername());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setCredentials(List.of(credentialRepresentation));

        UserResource userResource = realmResourceInputPort.getUserResource().get(userId);
        userResource.update(user);
    }
}
