package com.security.keycloak.application.input;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import com.security.keycloak.domain.models.User;

public interface IAuthInputPort {
    
    User getCurrentUser(String authorizationHeader)  throws NoSuchAlgorithmException, InvalidKeySpecException ;  
}
