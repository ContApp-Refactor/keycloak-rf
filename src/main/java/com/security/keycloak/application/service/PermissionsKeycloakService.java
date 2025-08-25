package com.security.keycloak.application.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.security.keycloak.application.input.IPermissionsKeycloakInputPort;
import com.security.keycloak.application.output.IPermissionsKeycloakOutputPort;

@Service
public class PermissionsKeycloakService implements IPermissionsKeycloakInputPort {

    @Autowired
    private IPermissionsKeycloakOutputPort permissionsKeycloakOutputPort;

    @Override
    public List<String> findAllPermissions() {
        return permissionsKeycloakOutputPort.findAllPermissions();
    }

    @Override
    public boolean addPolicytoPermissions(List<String> permissions, String roleName) {
        return permissionsKeycloakOutputPort.addPolicytoPermissions(permissions, roleName);
    }

    @Override
    public Map<String, List<String>> getRolesWithPermissions() {
        return permissionsKeycloakOutputPort.getRolesWithPermissions();
    }

    @Override
    public boolean updatePermissionsForRole(List<String> newPermissions, String roleName) {
        return permissionsKeycloakOutputPort.updatePermissionsForRole(newPermissions, roleName);
    }

}
