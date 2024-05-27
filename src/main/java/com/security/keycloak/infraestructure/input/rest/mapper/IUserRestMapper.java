package com.security.keycloak.infraestructure.input.rest.mapper;

import org.mapstruct.Mapper;
import com.security.keycloak.domain.models.User;
import com.security.keycloak.infraestructure.input.rest.data.response.UserResponse;

@Mapper
public interface IUserRestMapper {

    UserResponse toUserResponse(User user);
}
