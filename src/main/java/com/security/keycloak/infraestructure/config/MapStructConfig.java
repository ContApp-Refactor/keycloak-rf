package com.security.keycloak.infraestructure.config;

import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.security.keycloak.infraestructure.input.rest.mapper.IUserRestMapper;

@Configuration
public class MapStructConfig {

    @Bean
    IUserRestMapper mapStructMapper(){return Mappers.getMapper(IUserRestMapper.class);}
    
    
}
