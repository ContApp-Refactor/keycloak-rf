package com.security.keycloak.infraestructure.input.rest.data.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema; 

@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class UserResponse{
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
    @Schema(description = "Roles", example = "[\"admin\"]")
    List<String> roles;

}
