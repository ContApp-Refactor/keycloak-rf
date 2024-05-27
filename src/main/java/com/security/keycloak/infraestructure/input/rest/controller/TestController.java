package com.security.keycloak.infraestructure.input.rest.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/keycloak/test")
public class TestController {
    
    @GetMapping("/testAdmin")
    @PreAuthorize("hasRole('admin_client')")
    public String test() {
        return "Test for Admin";
    }

    @GetMapping("/testUser")
    @PreAuthorize("hasRole('user_client') or hasRole('admin_client')")
    public String testUser() {
        return "Test for User";
    }
    
    @GetMapping("/testAll")
    public String testAll() {
        return "Test for All";
    }

}
