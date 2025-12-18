package fr.techcrud.pmt_api.controllers;

import fr.techcrud.pmt_api.dto.UserResponseDto;
import fr.techcrud.pmt_api.exceptions.BadRequestException;
import fr.techcrud.pmt_api.models.User;
import fr.techcrud.pmt_api.services.AuthService;
import fr.techcrud.pmt_api.services.userService;
import fr.techcrud.pmt_api.utils.UserResponseSerializer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import fr.techcrud.pmt_api.dto.UserLoginDto;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for authentication")
public class AuthController {

    @Autowired
    private userService userService;

    private final AuthService authService;

    @Autowired
    private UserResponseSerializer userResponseSerializer;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Log in", description = "Authenticate a user and return a JWT token")
    public ResponseEntity<?> login(@RequestBody UserLoginDto loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        String token = authService.login(email, password);
        if (token == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Invalid email or password"));
        }
        
        User user = userService.findByEmail(email);

        return ResponseEntity.ok(Map.of("success", true, "message", "Authentication successful", "data", Map.of(
                "token", token,
                "id", user.getId(),
                "email", user.getEmail(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "role", user.getRole(),
                "phoneNumber", user.getPhoneNumber()

        )));
    }

    @PostMapping("/register")
    @ResponseStatus(code = HttpStatus.CREATED)
    @Operation(summary = "Create account", description = "Register a new user")
    public UserResponseDto create(@RequestBody User user) {
        User response = userService.create(user);
        if (response == null) {
            throw new BadRequestException("User already exists");
        }
        return userResponseSerializer.toResponse(user);
    }
}
