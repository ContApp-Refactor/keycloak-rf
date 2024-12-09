package com.security.keycloak.domain.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema; 

@Getter
@Setter
@Builder
public class Auth {
    @Schema(description = "Nombre de usuario", example = "admin")
    String username; 
    @Schema(description = "Contraseña", example = "admin")     
    String password;
      
}
