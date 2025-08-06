package com.security.keycloak.application.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.security.keycloak.application.input.IProfileKeycloakInputPort;
import com.security.keycloak.application.output.IProfileKeycloakOutputPort;
import com.security.keycloak.domain.models.Profile;
import com.security.keycloak.infraestructure.input.rest.data.response.ProfileResponse;

@Service
public class ProfileKeycloakService implements IProfileKeycloakInputPort {

    @Autowired
    private IProfileKeycloakOutputPort profileKeycloakOutputPort;

    @Override
    public List<ProfileResponse> findAllProfiles() {
        return profileKeycloakOutputPort.findAllProfiles();
    }

    @Override
    public List<ProfileResponse> findProfileByName(String name) {
        return profileKeycloakOutputPort.findProfileByName(name);
    }

    @Override
    public ProfileResponse findProfileById(String profileId) {
        return profileKeycloakOutputPort.findProfileById(profileId);
    }

    @Override
    public Profile createProfile(Profile profile) {
        return profileKeycloakOutputPort.createProfile(profile);
    }

    @Override
    public void deleteProfile(String profileId) {
        profileKeycloakOutputPort.deleteProfile(profileId);
    }

    @Override
    public Profile updateProfile(String profileId, Profile profile) {
        return profileKeycloakOutputPort.updateProfile(profileId, profile);
    }
    
}
