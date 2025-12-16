package fr.techcrud.pmt_api.controllers;

import fr.techcrud.pmt_api.dto.UserResponseDto;
import fr.techcrud.pmt_api.exceptions.BadRequestException;
import fr.techcrud.pmt_api.models.User;
import fr.techcrud.pmt_api.services.AuthService;
import fr.techcrud.pmt_api.services.userService;
import fr.techcrud.pmt_api.utils.UserResponseSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import fr.techcrud.pmt_api.dto.UserLoginDto;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private userService userService;

    private final AuthService authService;

    @Autowired
    private UserResponseSerializer userResponseSerializer;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> login(@RequestBody UserLoginDto loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        String token = authService.login(email, password);
        if (token == null) {
            return ResponseEntity.status(401).body("Email ou mot de passe invalide");
        }

        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/signup")
    @ResponseStatus(code = HttpStatus.CREATED)
    public UserResponseDto create(@RequestBody User user) {
        User response = userService.create(user);
        if (response == null) {
            throw new BadRequestException("User already exists");
        }
        return userResponseSerializer.toResponse(user);
    }
}
