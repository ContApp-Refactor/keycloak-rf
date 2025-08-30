package com.security.keycloak.util;

import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KeycloakProvider {

    private final String realmName;
    private final Keycloak keycloak;

    public KeycloakProvider(
            @Value("${keycloak.server.url}") String serverUrl,
            @Value("${keycloak.realm.master}") String realmMaster,
            @Value("${keycloak.realm.name}") String realmName,
            @Value("${keycloak.admin}") String clientId,
            @Value("${keycloak.client.secret:}") String clientSecret,
            @Value("${keycloak.user.console}") String username,
            @Value("${keycloak.password}") String password
    ) {
        this.realmName = realmName;

        KeycloakBuilder builder = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realmMaster)
                .clientId(clientId)
                .username(username)
                .password(password)
                .resteasyClient(new ResteasyClientBuilderImpl()
                        .connectionPoolSize(10)
                        .build());

        if (clientSecret != null && !clientSecret.isBlank()) {
            builder.clientSecret(clientSecret);
        }

        this.keycloak = builder.build();
    }

    public RealmResource getRealmResource() {
        return keycloak.realm(realmName);
    }

    public UsersResource getUserResource() {
        return getRealmResource().users();
    }

    public String getAdminAccessToken() {
        return keycloak.tokenManager().getAccessTokenString();
    }
}
