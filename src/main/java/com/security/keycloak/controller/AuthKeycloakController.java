package com.security.keycloak.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.security.keycloak.dtos.AuthDTO;
import com.security.keycloak.service.IAuthKeycloakService;
import com.security.keycloak.service.impl.AuthKeycloakServiceImpl;
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

    
    @PostMapping("/")
    public ResponseEntity<?> getToken(@Valid @RequestBody AuthDTO authDTO, HttpServletRequest request) throws JsonMappingException, JsonProcessingException {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(authKeycloakService.getToken(authDTO, request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {

        authKeycloakService.logoutAndBlacklist(request);
        return ResponseEntity.noContent().build();
    }
}
