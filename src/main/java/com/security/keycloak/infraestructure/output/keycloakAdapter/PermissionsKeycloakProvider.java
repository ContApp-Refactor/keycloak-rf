package com.security.keycloak.infraestructure.output.keycloakAdapter;

import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class PermissionsKeycloakProvider {

    // Elimina el 'static' para permitir la inyección de Spring
    @Value("${keycloak.server.url}")
    private String KEYCLOAK_SERVER_URL;

    @Value("${keycloak.realm.name}")
    private String REALM_NAME;

    @Value("${keycloak.realm.master}")
    private String REALM_MASTER;

    @Value("${keycloak.admin}")
    private String ADMIN_CLI;

    @Value("${keycloak.user.console}")
    private String USER_CONSOLE;

    @Value("${keycloak.password}")
    private String PASSWORD_CONSOLE;

    @Value("${keycloak.client.secret}")
    private String CLIENT_SECRET;

    private Keycloak keycloakInstance;

    @PostConstruct
    public void init() {
        keycloakInstance = KeycloakBuilder.builder()
                .serverUrl(KEYCLOAK_SERVER_URL)
                .realm(REALM_MASTER)
                .clientId(ADMIN_CLI)
                .username(USER_CONSOLE)
                .password(PASSWORD_CONSOLE)
                .clientSecret(CLIENT_SECRET)
                .resteasyClient(new ResteasyClientBuilderImpl()
                        .connectionPoolSize(10)
                        .build())
                .build();
    }

    // Método para obtener el token de acceso del administrador
    public String getAdminAccessToken() {
        return keycloakInstance.tokenManager().getAccessToken().getToken();
    }
}
