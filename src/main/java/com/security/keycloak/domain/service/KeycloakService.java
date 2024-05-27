package com.security.keycloak.domain.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.security.keycloak.application.input.IKeycloakInputPort;
import com.security.keycloak.application.output.IKeycloakOutputPort;
import com.security.keycloak.domain.models.User;
import com.security.keycloak.infraestructure.input.rest.data.response.UserResponse;

@Service
public class KeycloakService implements IKeycloakInputPort {

    @Autowired
    private IKeycloakOutputPort keycloakOutputPort;

    @Override
    public List<UserResponse> findAllUsers() {
        return keycloakOutputPort.findAllUsers();
    }

    @Override
    public List<UserResponse> findUserByUsername(String username) {
        return keycloakOutputPort.findUserByUsername(username);
    }

    @Override
    public String createUser(User user) {
        return keycloakOutputPort.createUser(user);
    }

    @Override
    public void deleteUser(String userId) {
        keycloakOutputPort.deleteUser(userId);
    }

    @Override
    public void updateUser(String userId, User user) {
        keycloakOutputPort.updateUser(userId, user);
    }
    
    
}
