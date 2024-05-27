package com.security.keycloak.application.input;

import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;

public interface IRealmResourceInputPort {
    RealmResource getRealmResource();

    UsersResource getUserResource();
}
