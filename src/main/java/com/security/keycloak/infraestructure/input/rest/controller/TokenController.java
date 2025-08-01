package com.security.keycloak.infraestructure.input.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.security.keycloak.application.input.IKeycloakTokenInputPort;
import com.security.keycloak.domain.models.Auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/keycloak/token")
@CrossOrigin("*")
public class TokenController {

    @Autowired
    private IKeycloakTokenInputPort KeycloakProvider;

    @Operation(summary = "Obtener token de autenticación", description = "Este endpoint permite obtener un token de autenticación JWT a partir de las credenciales proporcionadas en el cuerpo de la solicitud.", responses = {
            @ApiResponse(responseCode = "200", description = "Token de autenticación obtenido con éxito", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Credenciales inválidas", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/")
    public ResponseEntity<?> getToken(@RequestBody Auth auth) throws JsonMappingException, JsonProcessingException {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(KeycloakProvider.getToken(auth));
    }

}
