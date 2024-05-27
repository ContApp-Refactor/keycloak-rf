package com.security.keycloak.infraestructure.input.rest.data.response;

import java.util.List;

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
public class UserResponse{

    String id;
    String username;
    String email;
    String firstName;
    String lastName;

    List<String> roles;

}
