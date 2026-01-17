package fr.techcrud.pmt_api.controllers;

import fr.techcrud.pmt_api.dto.UserResponseDto;
import fr.techcrud.pmt_api.exceptions.RessourceNotFoundException;
import fr.techcrud.pmt_api.models.User;
import fr.techcrud.pmt_api.services.userService;
import fr.techcrud.pmt_api.utils.UserResponseSerializer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Endpoints for managing users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    @Autowired
    private userService userService;
    @Autowired
    private UserResponseSerializer userResponseSerializer;

    @GetMapping
    @Operation(summary = "Get list of users", description = "Retrieve all users")
    public List<User> findAll() {
        return userService.findAll();
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Retrieve the currently authenticated user")
    public UserResponseDto getMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        String email = (String) authentication.getPrincipal();

        User user = userService.findByEmail(email);
        if (user == null) {
            throw new RessourceNotFoundException("User not found");
        }

        return userResponseSerializer.toResponse(user);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve a user by their unique ID")
    public UserResponseDto findById(@PathVariable UUID id) {
        User user = userService.findById(id);

        if (user == null) {
            throw new RessourceNotFoundException("User not found");
        }

        return userResponseSerializer.toResponse(user);
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user", description = "Update the profile of the currently authenticated user")
    public UserResponseDto updateMe(@RequestBody User userData) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        String email = (String) authentication.getPrincipal();

        User updatedUser = userService.updateByEmail(email, userData);
        if (updatedUser == null) {
            throw new RessourceNotFoundException("User not found");
        }

        return userResponseSerializer.toResponse(updatedUser);
    }
}
