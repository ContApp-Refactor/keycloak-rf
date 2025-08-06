package com.security.keycloak.infraestructure.input.rest.data.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class ProfileResponse {

    @Schema(description = "Identificador del perfil", example = "1")
    String id;
    @Schema(description = "Nombre del perfil", example = "Administrador")
    String name;
    @Schema(description = "Descripción del perfil", example = "Perfil con permisos de administrador")
    String description;
    
}
