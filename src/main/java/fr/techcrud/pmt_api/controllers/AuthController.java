package fr.techcrud.pmt_api.controllers;

import fr.techcrud.pmt_api.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.techcrud.pmt_api.dto.UserLoginDto;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDto loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        String token = authService.login(email, password);
        if (token == null) {
            return ResponseEntity.status(401).body("Email ou mot de passe invalide");
        }

        return ResponseEntity.ok(Map.of("token", token));
    }
}
