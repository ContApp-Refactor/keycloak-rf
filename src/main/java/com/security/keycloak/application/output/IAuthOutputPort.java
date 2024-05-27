package com.security.keycloak.application.output;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import com.security.keycloak.domain.models.User;

public interface IAuthOutputPort {

    User getCurrentUser(String authorizationHeader) throws NoSuchAlgorithmException, InvalidKeySpecException ;  
}
