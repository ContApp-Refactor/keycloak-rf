package com.security.keycloak.infraestructure.input.rest.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.security.keycloak.application.output.IAuthOutputPort;
import com.security.keycloak.application.output.IKeycloakOutputPort;
import com.security.keycloak.domain.models.User;
import com.security.keycloak.infraestructure.input.rest.data.response.UserResponse;
import com.security.keycloak.infraestructure.input.rest.mapper.IUserRestMapper;

@RestController
@PreAuthorize("hasRole('admin_client')")
@RequestMapping("/keycloak")
@CrossOrigin("*")
public class KeycloakController {

    @Autowired
    private IKeycloakOutputPort keycloakService;

    @Autowired
    private IAuthOutputPort authService;

    @Autowired
    private IUserRestMapper userMapper;


    @GetMapping("/users")
    public ResponseEntity<?> findAllUsers() {
        return ResponseEntity.ok(keycloakService.findAllUsers());
    }

    @GetMapping("/users/{username}")
    public ResponseEntity<?> findUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(keycloakService.findUserByUsername(username));
    }

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody User userDTO) throws URISyntaxException {
        String response = keycloakService.createUser(userDTO);
        return ResponseEntity.created(new URI("/keycloak/create")).body(response);
    }

    @PutMapping("/update/{username}")
    public ResponseEntity<?> updateUser(@PathVariable String username, @RequestBody User userDTO) {
        keycloakService.updateUser(username, userDTO);
        return ResponseEntity.ok("User updated successfully");
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        keycloakService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully");
    }

    @PreAuthorize("hasRole('user_client') or hasRole('admin_client')")
    @GetMapping("/getCurrentUser")
    public UserResponse obtenerUsername(@RequestHeader("Authorization") String authorizationHeader) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return userMapper.toUserResponse(authService.getCurrentUser(authorizationHeader));
    }

}
