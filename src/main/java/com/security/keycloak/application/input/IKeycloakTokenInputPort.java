package com.security.keycloak.application.input;

import com.security.keycloak.domain.models.Auth;

public interface IKeycloakTokenInputPort {
    String getToken(Auth auth);
}
    
   
