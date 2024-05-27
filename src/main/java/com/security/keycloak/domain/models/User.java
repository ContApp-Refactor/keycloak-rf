package com.security.keycloak.domain.models;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class User {
    
    String id;
    String username;
    String email;
    String firstName;
    String lastName;
    String password;

    List<String> roles;
}
