package com.security.keycloak.application.input;

import java.util.List;

import com.security.keycloak.domain.models.User;
import com.security.keycloak.infraestructure.input.rest.data.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;  
import io.swagger.v3.oas.annotations.responses.ApiResponse; 
import io.swagger.v3.oas.annotations.responses.ApiResponses;
public interface IKeycloakInputPort {
    @Operation(summary = "Obtener todos los usuarios", description = "Devuelve una lista de todos los usuarios") 
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Usuarios obtenidos exitosamente"), 
        @ApiResponse(responseCode = "500", description = "Error interno del servidor") 
    })
    List<UserResponse> findAllUsers();
    @Operation(summary = "Buscar usuario por nombre de usuario", description = "Devuelve una lista de usuarios que coinciden con el nombre de usuario proporcionado")
    List<UserResponse> findUserByUsername(String username);
    @Operation(summary = "Buscar usuario por ID", description = "Devuelve un usuario que coincide con el ID proporcionado")
    UserResponse findUserById(String userId);
    @Operation(summary = "Crear usuario", description = "Crea un nuevo usuario")
    User createUser(User user);
    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario basado en el ID proporcionado")
    void deleteUser(String userId);
    @Operation(summary = "Actualizar usuario", description = "Actualiza un usuario basado en el ID proporcionado")
    User updateUser(String userId, User user);
}
