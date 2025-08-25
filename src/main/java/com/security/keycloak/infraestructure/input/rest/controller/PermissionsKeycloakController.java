package com.security.keycloak.infraestructure.input.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.security.keycloak.application.output.IPermissionsKeycloakOutputPort;
import com.security.keycloak.infraestructure.input.rest.data.request.AddPolicyToPermissionsRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;

@RestController
@PreAuthorize("hasRole('admin_client')")
@RequestMapping("/api/keycloak/permissions")
public class PermissionsKeycloakController {

    @Autowired
    private IPermissionsKeycloakOutputPort permissionsKeycloakService;


    @Operation(summary = "Obtener todos los permisos", description = "Recupera una lista de todos los nombres de los permisos registrados en el sistema.", responses = {
            @ApiResponse(responseCode = "200", description = "Lista de permisos recuperada con éxito", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Error interno al recuperar los permisos", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/findAll")
    public ResponseEntity<?> findAllPermissions() {
        return ResponseEntity.ok(permissionsKeycloakService.findAllPermissions());
    }

    @Operation(summary = "Agregar política a permisos", description = "Asocia una política a una lista de permisos", responses = {
            @ApiResponse(responseCode = "200", description = "Política agregada con éxito a los permisos", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Solicitud incorrecta", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Permisos no encontrados", content = @Content(mediaType = "application/json"))
    })
    @PutMapping("/assignRoleToPermissions")
    public ResponseEntity<?> addPolicyToPermissions(@RequestBody AddPolicyToPermissionsRequest request) {
        boolean success = permissionsKeycloakService.addPolicytoPermissions(request.getPermissionNames(),
                request.getRoleName());
        if (success) {
            return ResponseEntity.ok("Permisos asignados con éxito");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("No se pudo asignar los permisos o el rol no existe.");
        }
    }


    @Operation(summary = "Obtener roles con permisos", description = "Devuelve un mapa de roles y sus permisos asociados", responses = {
            @ApiResponse(responseCode = "200", description = "Roles y permisos obtenidos exitosamente", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Error interno al recuperar los roles y permisos", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/rolesWithPermissions")
    public ResponseEntity<?> getRolesWithPermissions() {
        return ResponseEntity.ok(permissionsKeycloakService.getRolesWithPermissions());
    }

    @Operation(summary = "Actualizar permisos para un rol", description = "Actualiza la lista de permisos para un rol específico", responses = {
            @ApiResponse(responseCode = "200", description = "Permisos actualizados exitosamente para el rol", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Solicitud incorrecta", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Rol no encontrado", content = @Content(mediaType = "application/json"))
    })
    @PutMapping("/updatePermissionsForRole")
    public ResponseEntity<?> updatePermissionsForRole(@RequestBody AddPolicyToPermissionsRequest request) {
        boolean success = permissionsKeycloakService.updatePermissionsForRole(request.getPermissionNames(),
                request.getRoleName());
        if (success) {
            return ResponseEntity.ok("Permisos actualizados con éxito");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("No se pudo actualizar los permisos o el rol no existe.");
        }
    }


}
