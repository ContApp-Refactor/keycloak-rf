package com.security.keycloak.util;

import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;

public class KeycloakProvider {
    private static final String KEYCLOAK_SERVER_URL = "http://contables.unicauca.edu.co:80";
    private static final String REALM_NAME = "oauth2-realm";
    private static final String REALM_MASTER =  "master";
    private static final String ADMIN_CLI = "admin-cli";
    private static final String USER_CONSOLE = "contables";
    private static final String PASSWORD_CONSOLE = "kPiP4WSxSH9Gdq";
    private static final String CLIENT_SECRET = "fS55Kzk07rJYpkMotC07eFGaLeaKkgF0";

    //RealmResource para manipulação do Keycloak
    public static RealmResource getRealmResource() {
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(KEYCLOAK_SERVER_URL)
                .realm(REALM_MASTER)
                .username(USER_CONSOLE)
                .password(PASSWORD_CONSOLE)
                .clientId(ADMIN_CLI)
                .clientSecret(CLIENT_SECRET)
                .resteasyClient(new ResteasyClientBuilderImpl()
                        .connectionPoolSize(10)
                        .build())
                .build();
        
        return keycloak.realm(REALM_NAME);        
    }

    public static UsersResource getUserResource() {
        RealmResource realmResource = getRealmResource();
        return realmResource.users();
    }
}
