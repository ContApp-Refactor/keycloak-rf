package com.security.keycloak.dtos;

import com.security.keycloak.validation.ISanitize;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthDTO {

    @NotBlank(message = "Username es obligatorio")
    @ISanitize
    String username;

    @NotBlank(message = "Password es obligatorio")
    @Size(min = 8, max = 64, message = "La contraseña debe tener entre 8 y 64 caracteres")
    @com.security.keycloak.validation.IStrongPassword
    String password;
}
