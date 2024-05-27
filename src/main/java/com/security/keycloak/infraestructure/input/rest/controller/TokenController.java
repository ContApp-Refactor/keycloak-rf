package com.security.keycloak.infraestructure.input.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.security.keycloak.application.input.IKeycloakTokenInputPort;
import com.security.keycloak.domain.models.Auth;

@RestController
@RequestMapping("/keycloak/token")
@CrossOrigin("*")
public class TokenController {

    @Autowired
    private IKeycloakTokenInputPort KeycloakProvider;

    @PostMapping("/")
    public ResponseEntity<?> getToken(@RequestBody Auth auth) {

        return ResponseEntity.ok(KeycloakProvider.getToken(auth));
    }
    
}
