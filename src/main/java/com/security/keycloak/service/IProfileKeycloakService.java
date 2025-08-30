package com.security.keycloak.service;

import java.util.List;

import com.security.keycloak.dtos.ProfileDTO;

public interface IProfileKeycloakService {
    List<ProfileDTO> findAllProfiles();

    ProfileDTO findProfileById(String profileId);
    List<ProfileDTO> findProfilesByName(String name);
    ProfileDTO createProfile(ProfileDTO profileDTO);
    void deleteProfile(String profileId);
    ProfileDTO updateProfile(String profileId, ProfileDTO profileDTO);
    
}
