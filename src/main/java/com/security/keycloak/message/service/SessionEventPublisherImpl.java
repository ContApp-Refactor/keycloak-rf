package com.security.keycloak.message.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.security.keycloak.message.dto.SessionEventDTO;

import static com.security.keycloak.config.rabbitConfig.RabbitPublisherConfig.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SessionEventPublisherImpl implements ISessionEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishLoginEvent(SessionEventDTO sessionEventDTO) {
        try {
            rabbitTemplate.convertAndSend(
                    AUDIT_EXCHANGE,
                    SESSION_EVENT_ROUTING_KEY,
                    sessionEventDTO);
        } catch (Exception e) {
            log.error("Failed to publish login event: {}", e.getMessage());
        }
    }

    @Override
    public void publishLogoutEvent(SessionEventDTO sessionEventDTO) {
        try {
            rabbitTemplate.convertAndSend(
                    AUDIT_EXCHANGE,
                    SESSION_EVENT_ROUTING_KEY,
                    sessionEventDTO);
        } catch (Exception e) {
            log.error("Error publishing logout event for user: {}", sessionEventDTO.getUserName(), e);
        }
    }

}
