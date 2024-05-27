package com.security.keycloak.domain.service;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.security.keycloak.application.input.IAuthInputPort;
import com.security.keycloak.application.output.IAuthOutputPort;
import com.security.keycloak.domain.models.User;

@Service
public class AuthService implements IAuthInputPort{

    @Autowired
    private IAuthOutputPort authOutputPort;

    @Override
    public User getCurrentUser(String authorizationHeader) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return authOutputPort.getCurrentUser(authorizationHeader);
    } 
}
