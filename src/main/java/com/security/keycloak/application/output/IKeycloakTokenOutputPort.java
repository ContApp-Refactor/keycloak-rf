package com.security.keycloak.application.output;

import com.security.keycloak.domain.models.Auth;

public interface IKeycloakTokenOutputPort {
    String getToken(Auth auth);
}
