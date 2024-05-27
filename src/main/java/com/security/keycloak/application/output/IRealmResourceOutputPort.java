package com.security.keycloak.application.output;

import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;

public interface IRealmResourceOutputPort {
    RealmResource getRealmResource();

    UsersResource getUserResource();
}
