package com.security.keycloak.infraestructure.output.keycloakAdapter.Exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class ProfileException extends RuntimeException {

    @Schema(description = "Estado", example = "400")
    private int status;

    public ProfileException(String message, int status) {
        super(message);
        this.status = status;
    }
    
}
