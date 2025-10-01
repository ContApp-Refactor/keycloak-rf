package com.security.keycloak.dtos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.security.keycloak.validation.IInstitutionalEmail;
import com.security.keycloak.validation.ISanitize;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password")
public class UserDTO {

    private String id;
    @NotBlank(message = "username es obligatorio")
    @ISanitize
    private String username;

    @NotBlank(message = "email es obligatorio")
    @Email(message = "email no tiene formato válido")
    @IInstitutionalEmail(domain = "unicauca.edu.co", message = "El correo debe ser institucional (@unicauca.edu.co)")
    @ISanitize
    private String email;

    @NotBlank(message = "firstName es obligatorio")
    @ISanitize
    private String firstName;

    @NotBlank(message = "lastName es obligatorio")
    @ISanitize
    private String lastName;

    // Contraseña solo para entrada
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Size(min = 8, max = 20, message = "La contraseña debe tener entre 8 y 20 caracteres")
    @com.security.keycloak.validation.IStrongPassword
    private String password;

    private List<String> roles;

}
