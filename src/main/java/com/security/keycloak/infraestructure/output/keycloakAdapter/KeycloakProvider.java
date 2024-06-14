package com.security.keycloak.infraestructure.output.keycloakAdapter;

import java.util.HashMap;
import java.util.Map;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.security.keycloak.application.output.IKeycloakTokenOutputPort;
import com.security.keycloak.application.output.IRealmResourceOutputPort;
import com.security.keycloak.domain.models.Auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;


/**
 * KeycloakProvider es un componente que implementa las interfaces IRealmResourceOutputPort e IKeycloakTokenOutputPort,
 * proporcionando métodos para obtener el recurso Realm y el token de autenticación de Keycloak.
 */

@Component
public class KeycloakProvider implements IRealmResourceOutputPort , IKeycloakTokenOutputPort{

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
   
    @Value("${jwt.token.url}")
    private String tokenUrl; 

    @Value("${jwt.auth.converter.resource-id}")
    private String CLIENT_ID;

    private Keycloak keycloak;
    private ResteasyClient resteasyClient;

    /**
     * Metodo para obtener el recurso Realm de Keycloak
     * @return RealmResource
     */
    @Override
    public RealmResource getRealmResource() {
        // Solo se crea una instancia de Keycloak si es nula (Singleton Pattern)
        if (keycloak == null) {
            resteasyClient = new ResteasyClientBuilderImpl()
                    .connectionPoolSize(10)
                    .build();
    
            keycloak = KeycloakBuilder.builder()
                    .serverUrl(KEYCLOAK_SERVER_URL)
                    .realm(REALM_MASTER)
                    .username(USER_CONSOLE)
                    .password(PASSWORD_CONSOLE)
                    .clientId(ADMIN_CLI)
                    .clientSecret(CLIENT_SECRET)
                    .resteasyClient(resteasyClient)
                    .build();
        }
    
        return keycloak.realm(REALM_NAME);
    }

    /**
     * Metodo para obtener el recurso Users de Keycloak
     * @return UsersResource
     */
    @Override
    public UsersResource getUserResource() {
        RealmResource realmResource = getRealmResource();
        return realmResource.users();
    }



    @SuppressWarnings("unchecked")
    @Override
    public String getToken(Auth auth) throws JsonMappingException, JsonProcessingException {
        
        // Preparar los datos del formulario para la solicitud de token
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", CLIENT_ID);
        formData.add("grant_type", "password");
        formData.add("username", auth.getUsername());
        formData.add("password", auth.getPassword());
        formData.add("client_secret", CLIENT_SECRET);
        
        // Configurar los encabezados de la solicitud
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Crear la entidad de la solicitud HTTP con los datos del formulario y los encabezados
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

        ResponseEntity<String> response = new RestTemplate().postForEntity(tokenUrl, requestEntity, String.class);
        
        String responseBody = response.getBody();

        // Obtener el token de acceso del mapa de respuesta
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> responseMap = mapper.readValue(responseBody, Map.class);
        String accessToken = (String) responseMap.get("access_token");

        // Obtener los tiempos de expiración del token de acceso y del token de actualización
        Object expiresInObject = responseMap.get("expires_in");
        Object refreshExpires = responseMap.get("refresh_expires_in");
   
         // Crear un nuevo mapa para almacenar la información del token de acceso
        Map<String, Object> accessTokenInfo = new HashMap<>();
        accessTokenInfo.put("access_token", accessToken);
        accessTokenInfo.put("expires_in", expiresInObject);
        accessTokenInfo.put("refresh_expires_in", refreshExpires);
    
        // Convertir el mapa de información del token de acceso a una cadena JSON
        String accessTokenJson = mapper.writeValueAsString(accessTokenInfo);
        return accessTokenJson;
    }

    
    
}