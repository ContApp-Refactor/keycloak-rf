package com.security.keycloak.dtos;

import com.security.keycloak.validation.IInstitutionalEmail;
import com.security.keycloak.validation.ISanitize;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordDTO {
    @NotBlank(message = "email es obligatorio")
    @Email(message = "email no tiene formato valido")
    @IInstitutionalEmail(domain = "unicauca.edu.co", message = "El correo debe ser institucional (@unicauca.edu.co)")
    @ISanitize
    private String email;
}