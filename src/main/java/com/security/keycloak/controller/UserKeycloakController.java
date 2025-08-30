package com.security.keycloak.controller;

import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.security.keycloak.controller.exception.UserException;
import com.security.keycloak.dtos.UserDTO;
import com.security.keycloak.service.IAuthKeycloakService;
import com.security.keycloak.service.IUserKeycloakService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@PreAuthorize("hasRole('admin_client')")
@RequestMapping("/api/keycloak")
public class UserKeycloakController {

    @Autowired
    private IUserKeycloakService userKeycloakService;

    @Autowired
    private IAuthKeycloakService authKeycloakService;

    @Operation(summary = "Obtener todos los usuarios", description = "Recupera una lista de todos los usuarios registrados en el sistema.", responses = {
            @ApiResponse(responseCode = "200", description = "Lista de usuarios recuperada con éxito", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Error interno al recuperar los usuarios", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/users")
    public ResponseEntity<?> findAllUsers() {
        return ResponseEntity.ok(userKeycloakService.findAllUsers());
    }

    @Operation(summary = "Obtener un usuario por ID", description = "Recupera los detalles de un usuario específico utilizando su ID.", responses = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado con éxito", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Error interno al buscar el usuario", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> findUserById(@PathVariable String userId) {
        return ResponseEntity.ok(userKeycloakService.findUserById(userId));
    }

    @Operation(summary = "Buscar un usuario por nombre de usuario", description = "Recupera los detalles de un usuario específico utilizando su nombre de usuario.", responses = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado con éxito", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Error interno al buscar el usuario", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/users/{username}")
    public ResponseEntity<?> findUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userKeycloakService.findUserByUsername(username));
    }

    @Operation(summary = "Crear un nuevo usuario", description = "Crea un usuario en el sistema utilizando la información proporcionada en el cuerpo de la solicitud.", responses = {
            @ApiResponse(responseCode = "200", description = "Usuario creado con éxito", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "Usuario ya existente", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Error interno al crear el usuario", content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO) throws URISyntaxException {
        try {
            UserDTO response = userKeycloakService.createUser(userDTO);
            return ResponseEntity.ok(response);
        } catch (UserException e) {
            if (e.getStatus() == 409) {
                return ResponseEntity.status(409).body(e.getMessage());
            } else {
                return ResponseEntity.status(500).body(e.getMessage());
            }
        }
    }

    @Operation(summary = "Actualizar un usuario", description = "Actualiza la información de un usuario existente identificado por su ID.", responses = {
            @ApiResponse(responseCode = "200", description = "Usuario actualizado con éxito", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json"))
    })
    @PutMapping("/update/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable String userId, @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userKeycloakService.updateUser(userId, userDTO));
    }

    @Operation(summary = "Eliminar un usuario", description = "Elimina un usuario del sistema identificado por su ID.", responses = {
            @ApiResponse(responseCode = "200", description = "Usuario eliminado con éxito", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json"))
    })
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        userKeycloakService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully");
    }

    @Operation(summary = "Obtener información del usuario actual", description = "Recupera los detalles del usuario autenticado utilizando el token de autorización proporcionado en el encabezado.", responses = {
            @ApiResponse(responseCode = "200", description = "Información del usuario recuperada con éxito", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "No autorizado o token inválido", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Error interno al recuperar la información del usuario", content = @Content(mediaType = "application/json"))
    })
    @PreAuthorize("hasRole('user_client') or hasRole('admin_client')")
    @GetMapping("/getCurrentUser")
    public UserDTO obtenerUsername(@RequestHeader("Authorization") String authorizationHeader)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        return authKeycloakService.getCurrentUser(authorizationHeader);
    }

}
