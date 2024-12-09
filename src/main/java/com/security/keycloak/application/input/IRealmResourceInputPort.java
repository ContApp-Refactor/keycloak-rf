package com.security.keycloak.application.input;

import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import io.swagger.v3.oas.annotations.Operation; 

public interface IRealmResourceInputPort {
    @Operation(summary = "Obtener recurso de reino", description = "Devuelve el recurso de reino")
    RealmResource getRealmResource();
    @Operation(summary = "Obtener recurso de usuarios", description = "Devuelve el recurso de usuarios")
    UsersResource getUserResource();
}
