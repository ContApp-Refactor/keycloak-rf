package com.security.keycloak.domain.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Auth {

    String username;
    String password;
      
}
