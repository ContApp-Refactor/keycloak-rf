package com.security.keycloak.domain.service;

import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.security.keycloak.application.input.IKeycloakTokenInputPort;
import com.security.keycloak.application.input.IRealmResourceInputPort;
import com.security.keycloak.application.output.IKeycloakTokenOutputPort;
import com.security.keycloak.application.output.IRealmResourceOutputPort;
import com.security.keycloak.domain.models.Auth;

@Service
public class KeycloakProviderService implements IKeycloakTokenInputPort, IRealmResourceInputPort{

    @Autowired
    private IKeycloakTokenOutputPort keycloakOutputPort;

    @Autowired
    private IRealmResourceOutputPort realmResourceOutputPort;

    @Override
    public RealmResource getRealmResource() {
        return realmResourceOutputPort.getRealmResource();
    }

    @Override
    public String getToken(Auth auth) {
        return keycloakOutputPort.getToken(auth);
    }

    @Override
    public UsersResource getUserResource() {
        return realmResourceOutputPort.getUserResource();
    }
    
    
}
