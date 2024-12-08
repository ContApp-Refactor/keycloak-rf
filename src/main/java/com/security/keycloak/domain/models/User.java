package com.security.keycloak.domain.models;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.*; 

@Getter
@Setter
@Builder
public class User {
    @Schema(description = "Identificador", example = "1")
    String id;
    @Schema(description = "Nombre de usuario", example = "admin")
    String username;
    @Schema(description = "Correo electrónico", example = "tuemail@example.com")
    String email;
    @Schema(description = "Nombre", example = "Admin")
    String firstName;
    @Schema(description = "Apellido", example = "Admin")
    String lastName;
    @Schema(description = "Contraseña", example = "admin")
    String password;
    @Schema(description = "Roles", example = "[\"admin\"]")
    List<String> roles;
}
