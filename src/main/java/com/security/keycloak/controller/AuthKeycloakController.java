package com.security.keycloak.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.security.keycloak.dtos.AuthDTO;
import com.security.keycloak.service.IAuthKeycloakService;

@RestController
@RequestMapping("/api/keycloak/token")
public class AuthKeycloakController {

    @Autowired
    private IAuthKeycloakService authKeycloakService;

    
    @PostMapping("/")
    public ResponseEntity<?> getToken(@RequestBody AuthDTO authDTO) throws JsonMappingException, JsonProcessingException {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(authKeycloakService.getToken(authDTO));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        authKeycloakService.logoutAndBlacklist(authHeader);
        return ResponseEntity.noContent().build();
    }
}
