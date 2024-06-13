package com.security.keycloak.application.input;

import java.util.List;

import com.security.keycloak.domain.models.User;
import com.security.keycloak.infraestructure.input.rest.data.response.UserResponse;

public interface IKeycloakInputPort {
    List<UserResponse> findAllUsers();
    List<UserResponse> findUserByUsername(String username);
    UserResponse findUserById(String userId);
    User createUser(User user);
    void deleteUser(String userId);
    User updateUser(String userId, User user);
}
