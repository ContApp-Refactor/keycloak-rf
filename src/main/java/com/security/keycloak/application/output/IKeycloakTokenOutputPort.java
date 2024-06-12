package com.security.keycloak.application.output;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.security.keycloak.domain.models.Auth;

public interface IKeycloakTokenOutputPort {
    String getToken(Auth auth) throws JsonMappingException, JsonProcessingException;
}
