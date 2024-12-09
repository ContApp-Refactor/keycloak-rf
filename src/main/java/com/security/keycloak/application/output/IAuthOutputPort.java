package com.security.keycloak.application.output;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import com.security.keycloak.domain.models.User;
import io.swagger.v3.oas.annotations.Operation; 

public interface IAuthOutputPort {
    @Operation(summary = "Obtener el usuario actual", description = "Devuelve el usuario actual basado en el encabezado de autorización proporcionado")
    User getCurrentUser(String authorizationHeader) throws NoSuchAlgorithmException, InvalidKeySpecException ;  
}
