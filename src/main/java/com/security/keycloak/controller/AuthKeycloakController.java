package com.security.keycloak.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.security.keycloak.dtos.AuthDTO;
import com.security.keycloak.dtos.ForgotPasswordDTO;
import com.security.keycloak.dtos.RefreshTokenDTO;
import com.security.keycloak.service.IAuthKeycloakService;
import com.security.keycloak.service.impl.AuthKeycloakServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/keycloak/token")
public class AuthKeycloakController {

    private final AuthKeycloakServiceImpl authKeycloakServiceImpl;

    @Autowired
    private IAuthKeycloakService authKeycloakService;


    AuthKeycloakController(AuthKeycloakServiceImpl authKeycloakServiceImpl) {
        this.authKeycloakServiceImpl = authKeycloakServiceImpl;
    }

    @Operation(
        summary = "Obtener token de autenticación",
        description = "Genera un access token y refresh token a partir de credenciales válidas",
        responses = {
            @ApiResponse(responseCode = "200", description = "Token generado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de autenticación inválidos"),
            @ApiResponse(responseCode = "401", description = "Error de autenticación. Por favor verifica tus credenciales."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso"),
            @ApiResponse(responseCode = "409", description = "Conflicto en el estado de autenticación"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        }
    )
    @PostMapping("/")
    public ResponseEntity<?> getToken(@Valid @RequestBody AuthDTO authDTO, HttpServletRequest request) throws JsonMappingException, JsonProcessingException {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(authKeycloakService.getToken(authDTO, request));
    }

    @Operation(
        summary = "Cerrar sesión",
        description = "Invalida el token actual y lo agrega a la blacklist",
        responses = {
            @ApiResponse(responseCode = "204", description = "Sesión cerrada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Token inválido"),
            @ApiResponse(responseCode = "401", description = "Error de autenticación"),
            @ApiResponse(responseCode = "409", description = "Conflicto en el estado de la sesión"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        }
    )
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {

        authKeycloakService.logoutAndBlacklist(request);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Refrescar token",
        description = "Genera un nuevo access token usando un refresh token válido",
        responses = {
            @ApiResponse(responseCode = "200", description = "Token refrescado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Refresh token inválido"),
            @ApiResponse(responseCode = "401", description = "Error de autenticación"),
            @ApiResponse(responseCode = "409", description = "Conflicto en el estado del token"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        }
    )
    @PostMapping("/refresh")
    public String refresh(@Valid @RequestBody RefreshTokenDTO dto)
            throws JsonProcessingException {

        return authKeycloakService.refreshToken(dto.getRefreshToken());
    }

    @Operation(
        summary = "Recuperar contraseña",
        description = "Envía un correo con el enlace para restablecer la contraseña",
        responses = {
            @ApiResponse(responseCode = "204", description = "Correo enviado exitosamente"),
            @ApiResponse(responseCode = "401", description = "Error de autenticación con Keycloak"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        }
    )
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(
            @Valid @RequestBody ForgotPasswordDTO dto) {

        authKeycloakService.sendPasswordReset(dto.getEmail());
        return ResponseEntity.noContent().build();
    }


}
