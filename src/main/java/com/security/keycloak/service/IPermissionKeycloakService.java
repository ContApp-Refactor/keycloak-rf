package com.security.keycloak.service;

import java.util.List;
import java.util.Map;

public interface IPermissionKeycloakService {

    List<String> findAllPermissions();
    boolean addPolicytoPermissions(List<String> permissions, String roleName);
    Map<String, List<String>> getRolesWithPermissions();
    boolean updatePermissionsForRole(List<String> newPermissions, String roleName);

}
