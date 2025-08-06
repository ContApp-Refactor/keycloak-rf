package com.security.keycloak.infraestructure.input.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.security.keycloak.application.output.IProfileKeycloakOutputPort;
import com.security.keycloak.domain.models.Profile;
import com.security.keycloak.infraestructure.output.keycloakAdapter.Exception.ProfileException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;

@RestController
@PreAuthorize("hasRole('admin_client')")
@RequestMapping("/api/keycloak/profile")
public class ProfileKeycloakController {

    @Autowired
    private IProfileKeycloakOutputPort profileKeycloakService;


    @Operation(summary = "Obtener todos los perfiles", description = "Recupera una lista de todos los perfiles registrados en el sistema.", responses = {
            @ApiResponse(responseCode = "200", description = "Lista de perfiles recuperada con éxito", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Error interno al recuperar los perfiles", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/findAll")
    public ResponseEntity<?> findAllProfiles() {
        return ResponseEntity.ok(profileKeycloakService.findAllProfiles());
    }

    @Operation(summary = "Obtener un perfil por ID", description = "Recupera los detalles de un perfil específico utilizando su ID.", responses = {
            @ApiResponse(responseCode = "200", description = "Perfil encontrado con éxito", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Perfil no encontrado", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Error interno al buscar el perfil", content = @Content(mediaType = "application/json"))
    })
        @GetMapping("/findById/{profileId}")
        public ResponseEntity<?> findProfileById(@PathVariable String profileId) {
            return ResponseEntity.ok(profileKeycloakService.findProfileById(profileId));
        }

    @Operation(summary = "Buscar un perfil por nombre", description = "Recupera los detalles de un perfil específico utilizando su nombre.", responses = {
            @ApiResponse(responseCode = "200", description = "Perfil encontrado con éxito", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Perfil no encontrado", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Error interno al buscar el perfil", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/findByName/{profileName}")
    public ResponseEntity<?> findProfileByName(@PathVariable String profileName) {
        return ResponseEntity.ok(profileKeycloakService.findProfileByName(profileName));
    }

    @Operation(summary = "Crear un nuevo perfil", description = "Crea un nuevo perfil en el sistema.", responses = {
            @ApiResponse(responseCode = "200", description = "Perfil creado con éxito", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "Profile ya existente", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Error interno al crear el perfil", content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/create")
    public ResponseEntity<?> createProfile(@RequestBody Profile profileDTO) {
        try {
            Profile createdProfile = profileKeycloakService.createProfile(profileDTO);
            return ResponseEntity.ok(createdProfile);
        } catch (ProfileException e) {
            if (e.getStatus() == 409) {
                return ResponseEntity.status(409).body(e.getMessage());
            } else {
                return ResponseEntity.status(500).body(e.getMessage());
            }
        }
    }
    

    @Operation(summary = "Actualizar un perfil", description = "Actualiza un perfil existente en el sistema.", responses = {
            @ApiResponse(responseCode = "200", description = "Perfil actualizado con éxito", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Perfil no encontrado", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Error interno al actualizar el perfil", content = @Content(mediaType = "application/json"))
    })
    @PutMapping("/update/{profileId}")
    public ResponseEntity<?> updateProfile(@PathVariable String profileId, @RequestBody Profile profileDTO) {
        return ResponseEntity.ok(profileKeycloakService.updateProfile(profileId, profileDTO));
    }

    @Operation(summary = "Eliminar un perfil", description = "Elimina un perfil del sistema utilizando su ID.", responses = {
            @ApiResponse(responseCode = "200", description = "Perfil eliminado con éxito", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Perfil no encontrado", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Error interno al eliminar el perfil", content = @Content(mediaType = "application/json"))
    })
    @DeleteMapping("/delete/{profileId}")
    public ResponseEntity<?> deleteProfile(@PathVariable String profileId) {
        profileKeycloakService.deleteProfile(profileId);
        return ResponseEntity.ok().build();
    }
}

