package com.security.keycloak.infraestructure.input.rest.data.request;

import java.util.List;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AddPolicyToPermissionsRequest {

    private List<String> permissionNames;
    private String roleName;
    
}
