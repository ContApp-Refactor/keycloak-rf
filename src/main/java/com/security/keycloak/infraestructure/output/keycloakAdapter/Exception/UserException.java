package com.security.keycloak.infraestructure.output.keycloakAdapter.Exception;

import lombok.Getter;

@Getter
public class UserException extends RuntimeException{
    private int status;

    public UserException(String message, int status) {
        super(message);
        this.status = status;
    } 
}
