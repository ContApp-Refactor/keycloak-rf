package com.security.keycloak.message.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionEventDTO {

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("user_id")
    private String userId;
    
    @JsonProperty("user_name")
    private String userName;
    
    @JsonProperty("user_role")
    private String userRole; 
    
    @JsonProperty("action")
    private String action;
    
    @JsonProperty("action_at")
    private Instant actionAt;
    
    @JsonProperty("ip_address")
    private String ipAddress;
}

