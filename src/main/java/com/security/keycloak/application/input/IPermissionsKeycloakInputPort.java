package com.security.keycloak.application.input;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

public interface IPermissionsKeycloakInputPort {


    @Operation(summary = "Obtener todos los permisos", description = "Devuelve una lista de todos los nombres de los permisos")
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Permisos obtenidos exitosamente"), 
        @ApiResponse(responseCode = "500", description = "Error interno del servidor") 
    })
    List<String> findAllPermissions();

    @Operation(summary = "Agregar política a permisos", description = "Asocia una política a una lista de permisos")
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Política agregada exitosamente a los permisos"), 
        @ApiResponse(responseCode = "400", description = "Solicitud incorrecta"), 
        @ApiResponse(responseCode = "404", description = "Permisos no encontrados") 
    })
    boolean addPolicytoPermissions(List<String> permissions, String roleName);    
    
}
