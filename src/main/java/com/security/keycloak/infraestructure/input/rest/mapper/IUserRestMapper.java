package com.security.keycloak.infraestructure.input.rest.mapper;

import org.mapstruct.Mapper;
import com.security.keycloak.domain.models.User;
import com.security.keycloak.infraestructure.input.rest.data.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation; 

@Mapper
public interface IUserRestMapper {
    @Operation(summary = "Mapear usuario a respuesta de usuario", description = "Mapea un usuario a una respuesta de usuario")
    UserResponse toUserResponse(User user);
}
