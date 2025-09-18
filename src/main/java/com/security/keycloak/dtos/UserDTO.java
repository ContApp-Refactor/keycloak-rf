package com.security.keycloak.dtos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.security.keycloak.validation.InstitutionalEmail; // si aún no lo creas, quita esta línea y la anotación de abajo


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
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
    private String username;

    @NotBlank(message = "email es obligatorio")
    @Email(message = "email no tiene formato válido")
    @InstitutionalEmail(domain = "unicauca.edu.co", message = "El correo debe ser institucional (@unicauca.edu.co)")
    private String email;

    @NotBlank(message = "firstName es obligatorio")
    private String firstName;

    @NotBlank(message = "lastName es obligatorio")
    private String lastName;

    // Contraseña solo para entrada (no se serializa en respuestas)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Size(min = 8, max = 20, message = "La contraseña debe tener entre 8 y 20 caracteres")
    private String password;

    private List<String> roles;
    
}
