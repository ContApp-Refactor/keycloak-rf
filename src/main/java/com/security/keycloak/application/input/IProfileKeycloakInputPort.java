package com.security.keycloak.application.input;

import java.util.List;

import com.security.keycloak.domain.models.Profile;
import com.security.keycloak.infraestructure.input.rest.data.response.ProfileResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

public interface IProfileKeycloakInputPort {

    @Operation(summary = "Obtener todos los perfiles", description = "Devuelve una lista de todos los perfiles")
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Perfiles obtenidos exitosamente"), 
        @ApiResponse(responseCode = "500", description = "Error interno del servidor") 
    })
    List<ProfileResponse> findAllProfiles();

    @Operation(summary = "Buscar perfil por nombre", description = "Devuelve una lista de perfiles que coinciden con el nombre proporcionado")
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Perfiles encontrados exitosamente"), 
        @ApiResponse(responseCode = "404", description = "Perfiles no encontrados") 
    })
    List<ProfileResponse> findProfileByName(String name);

    @Operation(summary = "Buscar perfil por ID", description = "Devuelve un perfil que coincide con el ID proporcionado")
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Perfil encontrado exitosamente"), 
        @ApiResponse(responseCode = "404", description = "Perfil no encontrado") 
    })
    ProfileResponse findProfileById(String profileId);

    @Operation(summary = "Crear perfil", description = "Crea un nuevo perfil")
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "201", description = "Perfil creado exitosamente"), 
        @ApiResponse(responseCode = "400", description = "Solicitud incorrecta") 
    })
    Profile createProfile(Profile profile);

    @Operation(summary = "Eliminar perfil", description = "Elimina un perfil basado en el ID proporcionado")
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "204", description = "Perfil eliminado exitosamente"), 
        @ApiResponse(responseCode = "404", description = "Perfil no encontrado") 
    })
    void deleteProfile(String profileId);

    @Operation(summary = "Actualizar perfil", description = "Actualiza un perfil basado en el ID proporcionado")
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Perfil actualizado exitosamente"), 
        @ApiResponse(responseCode = "400", description = "Solicitud incorrecta"), 
        @ApiResponse(responseCode = "404", description = "Perfil no encontrado") 
    })
    Profile updateProfile(String profileId, Profile profile);
    
}
