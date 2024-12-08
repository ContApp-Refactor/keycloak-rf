package com.security.keycloak.application.output;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.security.keycloak.domain.models.Auth;
import io.swagger.v3.oas.annotations.Operation; 

public interface IKeycloakTokenOutputPort {
    @Operation(summary = "Obtener token de autenticación", description = "Devuelve un token de autenticación basado en la información de autenticación proporcionada")
    String getToken(Auth auth) throws JsonMappingException, JsonProcessingException;
}
