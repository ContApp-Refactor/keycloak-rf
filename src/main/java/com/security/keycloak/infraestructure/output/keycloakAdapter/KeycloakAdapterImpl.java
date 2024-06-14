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
import com.security.keycloak.infraestructure.output.keycloakAdapter.Exception.UserException;

import jakarta.ws.rs.core.Response;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * KeycloakAdapterImpl es un servicio que implementa la interfaz IKeycloakOutputPort,
 * proporcionando métodos para interactuar con Keycloak como crear, actualizar, eliminar y obtener usuarios.
 */
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
     * @param  username nombre de usuario
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
    public User createUser(@NonNull User userDTO) {
        int status = 0;
        //Obtencion de los recursos de usuario
        UsersResource usersResource = realmResourceInputPort.getUserResource();

        //Creacion de la representacion del usuario
        UserRepresentation user = new UserRepresentation();

        user.setUsername(userDTO.getUsername());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setEnabled(true);
        user.setEmailVerified(true);

        // Crear el usuario en Keycloak
        Response response = usersResource.create(user);
        status = response.getStatus();

        if(status == 201) {
            // Obtener el ID del usuario a partir de la respuesta
            String path = response.getLocation().getPath();
            String userId = path.substring(path.lastIndexOf('/') + 1);
            
            // Configurar la representación de las credenciales del usuario
            CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
            credentialRepresentation.setTemporary(false);
            credentialRepresentation.setType(OAuth2Constants.PASSWORD);
            credentialRepresentation.setValue(userDTO.getPassword());

            usersResource.get(userId).resetPassword(credentialRepresentation);

            // Obtener el recurso del realm de Keycloak
            RealmResource realmResource = realmResourceInputPort.getRealmResource();

            List<RoleRepresentation> roles = null;

             // Si no se especifican roles, asignar el rol por defecto 'user_realm'
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

            // Asignar los roles al usuario en Keycloak
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
    public User updateUser(String userId,@NonNull User userDTO) {

        //Creacion de la representacion del usuario
        UserRepresentation user = new UserRepresentation();
        user.setUsername(userDTO.getUsername());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setEnabled(true);
        user.setEmailVerified(true);

        //Si la contraseña es difrente de null no se actualiza y se mantiene la misma contraseña
        if (userDTO.getPassword() != null) {
            //Creacion de la representacion de las credenciales
            CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
            credentialRepresentation.setTemporary(false);
            credentialRepresentation.setType(OAuth2Constants.PASSWORD);
            credentialRepresentation.setValue(userDTO.getPassword());

            user.setCredentials(List.of(credentialRepresentation));
        }

        //Actualizacion de los roles
        RealmResource realmResource = realmResourceInputPort.getRealmResource();
        List<RoleRepresentation> roles = realmResource
            .roles()
            .list()
            .stream()
            .filter(role -> userDTO.getRoles()
                .stream()
                .anyMatch(roleName -> roleName.equalsIgnoreCase(role.getName())))
            .toList();
        
        //Eliminacion de los roles actuales
        realmResource.users()
            .get(userId)
            .roles()
            .realmLevel()
            .remove(realmResource.roles().list());
        
        //Adicion de los nuevos roles
        realmResource.users()
            .get(userId)
            .roles()
            .realmLevel()
            .add(roles);

        //Actualizacion del usuario
        UserResource userResource = realmResourceInputPort.getUserResource().get(userId);
        userResource.update(user);

        return userDTO;
    }

    /**
     * Metodo para obtener un usuario por su id
     * @param userId id del usuario
     * @return UserResponse
     */
    @Override
    public UserResponse findUserById(String userId) {
        UserRepresentation user = realmResourceInputPort.getUserResource().get(userId).toRepresentation();

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
    }
}
