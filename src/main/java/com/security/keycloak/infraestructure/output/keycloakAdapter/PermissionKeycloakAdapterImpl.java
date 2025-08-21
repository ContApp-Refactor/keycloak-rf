package com.security.keycloak.infraestructure.output.keycloakAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.security.keycloak.application.output.IPermissionsKeycloakOutputPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PermissionKeycloakAdapterImpl implements IPermissionsKeycloakOutputPort {

    // Configuración base
    private final String REALM = "oauth2-realm";
    private final String CLIENT_ID = "341fd012-a3d7-4223-8cf9-abd4292fd4bb";
    private final String ADMIN_REALM_URL = "http://contables.unicauca.edu.co/auth/admin/realms/" + REALM;

    // URLs para autorización
    private final String RESOURCE_SERVER_URL = ADMIN_REALM_URL + "/clients/" + CLIENT_ID + "/authz/resource-server";
    private final String RESOURCE_SERVER_SETTINGS_URL = RESOURCE_SERVER_URL + "/settings";
    private final String PERMISSIONS_LIST_URL = RESOURCE_SERVER_URL + "/permission";
    private final String POLICY_ROLE_URL = RESOURCE_SERVER_URL + "/policy/role";
    private final String PERMISSION_BY_ID_URL_TEMPLATE = RESOURCE_SERVER_URL + "/permission/%s";

    // URLs para gestión de roles
    private final String ROLES_URL = ADMIN_REALM_URL + "/roles";
    private final String ROLE_BY_NAME_URL = ROLES_URL + "/%s";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private PermissionsKeycloakProvider keycloakProvider;
    
    @Override
    public List<String> findAllPermissions() {
        List<String> permissionNames = new ArrayList<>();
        final String DEFAULT_PERMISSION = "Default Permission";

        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    PERMISSIONS_LIST_URL,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode permissionsArray = objectMapper.readTree(response.getBody());

                if (permissionsArray.isArray()) {
                    for (JsonNode permission : permissionsArray) {
                        JsonNode nameNode = permission.get("name");
                        if (nameNode != null && !nameNode.isNull()) {
                            String name = nameNode.asText();
                            if (!DEFAULT_PERMISSION.equals(name)) {
                                permissionNames.add(name);
                            }
                        }
                    }
                }
            } else {
                log.warn("Error obteniendo permisos. Código: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error al listar permisos: {}", e.getMessage(), e);
        }

        return permissionNames;
    }

    @Override
    public boolean addPolicytoPermissions(List<String> permissions, String roleName) {
        String newPolicyName = roleName + " Policy";

        if (!createRolePolicy(roleName)) {
            log.error("No se pudo crear la política para el rol: {}", roleName);
            return false;
        }

        boolean allSuccessful = true;

        for (String permissionName : permissions) {
            try {
                List<String> currentPolicies = getPoliciesByPermissionName(permissionName);
                if (!currentPolicies.contains(newPolicyName)) {
                    currentPolicies.add(newPolicyName);
                }

                Map<String, Object> updatePayload = new HashMap<>();
                updatePayload.put("name", permissionName);
                updatePayload.put("policies", currentPolicies);

                String jsonBody = objectMapper.writeValueAsString(updatePayload);
                log.info("JSON enviado para actualizar el permiso '{}': {}", permissionName, jsonBody);

                HttpHeaders headers = createJsonAuthHeaders();
                HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

                String permissionId = findPermissionIdByName(permissionName);
                if (permissionId == null) {
                    log.warn("No se encontró el ID para el permiso '{}'. Se omite.", permissionName);
                    allSuccessful = false;
                    continue;
                }

                String updateUrl = String.format(PERMISSION_BY_ID_URL_TEMPLATE, permissionId);
                ResponseEntity<Void> response = restTemplate.exchange(
                        updateUrl,
                        HttpMethod.PUT,
                        entity,
                        Void.class
                );

                if (!isSuccessful((HttpStatus) response.getStatusCode())) {
                    log.warn("Falló la actualización del permiso '{}': Status {}", permissionName, response.getStatusCode());
                    allSuccessful = false;
                }

            } catch (Exception e) {
                log.error("Error al actualizar el permiso '{}' con la política '{}': {}",
                        permissionName, newPolicyName, e.getMessage(), e);
                allSuccessful = false;
            }
        }

        return allSuccessful;
    }
    
   @Override
    public Map<String, List<String>> getRolesWithPermissions() {
        Map<String, List<String>> result = new HashMap<>();

        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // OJO: aquí vamos contra /settings (no /permission)
            ResponseEntity<String> response = restTemplate.exchange(
                    RESOURCE_SERVER_SETTINGS_URL,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode policiesNode = root.get("policies");

                if (policiesNode != null && policiesNode.isArray()) {

                    // 1) Construir un índice: policyName -> type (para saber cuáles son de tipo "role")
                    Map<String, String> policyTypeByName = new HashMap<>();
                    for (JsonNode p : policiesNode) {
                        String name = p.path("name").asText(null);
                        String type = p.path("type").asText(null);
                        if (name != null) {
                            policyTypeByName.put(name, type);
                        }
                    }

                    // 2) Recorrer las policies de tipo "resource" (que representan "permisos")
                    for (JsonNode policy : policiesNode) {
                        if (!"resource".equals(policy.path("type").asText())) continue;

                        String permissionName = policy.path("name").asText();
                        if ("Default Permission".equals(permissionName)) continue;

                        JsonNode configNode = policy.path("config");
                        if (configNode.isMissingNode()) continue;

                        // applyPolicies viene como STRING JSON con un arreglo
                        String applyPoliciesJson = configNode.path("applyPolicies").asText("[]");
                        JsonNode applyPoliciesArray = objectMapper.readTree(applyPoliciesJson);

                        if (applyPoliciesArray.isArray()) {
                            for (JsonNode policyNameNode : applyPoliciesArray) {
                                String policyName = policyNameNode.asText();

                                // Solo considerar policies de tipo "role"
                                if (!"role".equals(policyTypeByName.get(policyName))) continue;

                                // Convención: "<RoleName> Policy" -> "<RoleName>"
                                String roleName = policyName.endsWith(" Policy")
                                        ? policyName.substring(0, policyName.length() - " Policy".length())
                                        : policyName;

                                // Agregar sin duplicar
                                List<String> perms = result.computeIfAbsent(roleName, k -> new ArrayList<>());
                                if (!perms.contains(permissionName)) {
                                    perms.add(permissionName);
                                }
                            }
                        }
                    }
                } else {
                    log.warn("No se encontraron 'policies' en settings del resource-server.");
                }
            } else {
                log.warn("No se pudo obtener settings del resource-server. Status: {}", response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Error al construir el mapa Rol→Permisos: {}", e.getMessage(), e);
        }

        return result;
    }



    public List<String> getPoliciesByPermissionName(String permissionName) {
        List<String> policies = new ArrayList<>();

        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    RESOURCE_SERVER_SETTINGS_URL,
                    HttpMethod.GET,
                    entity,
                    String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode policiesNode = root.get("policies");

                if (policiesNode != null && policiesNode.isArray()) {
                    for (JsonNode policy : policiesNode) {
                        if ("resource".equals(policy.get("type").asText()) &&
                                permissionName.equals(policy.get("name").asText())) {

                            String applyPoliciesJson = policy.get("config").get("applyPolicies").asText();
                            JsonNode applyPoliciesArray = objectMapper.readTree(applyPoliciesJson);

                            for (JsonNode policyName : applyPoliciesArray) {
                                policies.add(policyName.asText());
                            }
                        }
                    }
                }
            } else {
                log.warn("No se pudo obtener la configuración del resource-server, status: {}",
                        response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error al obtener las políticas del permiso {}: {}", permissionName, e.getMessage(), e);
        }

        return policies;
    }
    
    private String findPermissionIdByName(String permissionName) {
        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    PERMISSIONS_LIST_URL,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.isArray()) {
                    for (JsonNode permission : root) {
                        if (permissionName.equals(permission.get("name").asText())) {
                            return permission.get("id").asText();
                        }
                    }
                }
            } else {
                log.warn("No se pudo obtener la lista de permisos, status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error al buscar el ID del permiso '{}': {}", permissionName, e.getMessage(), e);
        }

        return null;
    }

    public boolean createRolePolicy(String roleName) {
        String policyName = roleName + " Policy";
        String roleId = findRoleIdByName(roleName);

        if (roleId == null) {
            log.error("No se encontró el rol con nombre: {}", roleName);
            return false;
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("name", policyName);
            payload.put("type", "role");
            payload.put("logic", "POSITIVE");

            List<Map<String, String>> rolesList = new ArrayList<>();
            Map<String, String> roleConfig = new HashMap<>();
            roleConfig.put("id", roleId);
            roleConfig.put("required", "true");
            rolesList.add(roleConfig);

            payload.put("roles", rolesList);

            String jsonBody = objectMapper.writeValueAsString(payload);
            log.info("JSON para crear política de rol: {}", jsonBody);

            HttpHeaders headers = createJsonAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<Void> response = restTemplate.postForEntity(
                    POLICY_ROLE_URL,
                    entity,
                    Void.class
            );

            if (response.getStatusCode() == HttpStatus.CREATED) {
                log.info("Política de rol '{}' creada exitosamente", policyName);
                return true;
            } else {
                log.warn("Error al crear política de rol. Código: {}", response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            log.error("Error al crear política de rol: {}", e.getMessage(), e);
            return false;
        }
    }

    private String findRoleIdByName(String roleName) {
        try {
            String roleUrl = String.format(ROLE_BY_NAME_URL, roleName);

            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    roleUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode roleNode = objectMapper.readTree(response.getBody());
                return roleNode.get("id").asText();
            } else {
                log.warn("Rol no encontrado. Status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error buscando ID del rol: {}", e.getMessage(), e);
        }
        return null;
    }

    // Métodos auxiliares para crear headers
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(keycloakProvider.getAdminAccessToken());
        return headers;
    }

    private HttpHeaders createJsonAuthHeaders() {
        HttpHeaders headers = createAuthHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private boolean isSuccessful(HttpStatus status) {
        return status == HttpStatus.NO_CONTENT ||
                status == HttpStatus.OK ||
                status == HttpStatus.CREATED;
    }
    
}
