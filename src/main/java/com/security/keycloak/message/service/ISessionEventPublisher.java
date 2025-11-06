package com.security.keycloak.message.service;

import com.security.keycloak.message.dto.SessionEventDTO;

public interface ISessionEventPublisher {

    void publishLoginEvent(SessionEventDTO sessionEventDTO);

    void publishLogoutEvent(SessionEventDTO sessionEventDTO);

}