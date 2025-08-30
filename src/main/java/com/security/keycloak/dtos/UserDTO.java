package com.security.keycloak.dtos;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserDTO {

    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private List<String> roles;
    
}
