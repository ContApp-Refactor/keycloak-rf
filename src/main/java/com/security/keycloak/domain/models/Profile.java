package com.security.keycloak.domain.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Profile {

    @Schema(description = "Identificador del perfil", example = "1")
    String id;
    @Schema(description = "Nombre del perfil", example = "Administrador")
    String name;
    @Schema(description = "Descripción del perfil", example = "Perfil con permisos de administrador")
    String description;
}
