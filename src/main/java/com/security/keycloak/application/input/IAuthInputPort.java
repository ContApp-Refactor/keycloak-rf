package com.security.keycloak.application.input;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import com.security.keycloak.domain.models.User;
import io.swagger.v3.oas.annotations.Operation; 
import io.swagger.v3.oas.annotations.Parameter; 
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

public interface IAuthInputPort {
    @Operation(summary = "Obtener el usuario actual", description = "Devuelve el usuario actual basado en el encabezado de autorización proporcionado") 
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Usuario obtenido exitosamente"), 
        @ApiResponse(responseCode = "401", description = "No autorizado"), 
        @ApiResponse(responseCode = "403", description = "Prohibido"), 
        @ApiResponse(responseCode = "500", description = "Error interno del servidor") })
    User getCurrentUser(
        @Parameter(description = "Encabezado de autorización", required = true)
        String authorizationHeader)  
    throws NoSuchAlgorithmException, InvalidKeySpecException ;  
}
