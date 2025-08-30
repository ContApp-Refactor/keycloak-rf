package com.security.keycloak.service;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.security.keycloak.dtos.AuthDTO;
import com.security.keycloak.dtos.UserDTO;

public interface IAuthKeycloakService {

    String getToken(AuthDTO authDTO) throws JsonMappingException, JsonProcessingException;
    UserDTO getCurrentUser(String authorizationHeader)throws NoSuchAlgorithmException, InvalidKeySpecException ;  

}
