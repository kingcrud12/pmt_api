package fr.techcrud.pmt_api.controllers;


import fr.techcrud.pmt_api.dto.UserResponseDto;
import fr.techcrud.pmt_api.exceptions.BadRequestException;
import fr.techcrud.pmt_api.exceptions.RessourceNotFoundException;
import fr.techcrud.pmt_api.models.User;
import fr.techcrud.pmt_api.services.userService;
import fr.techcrud.pmt_api.utils.UserResponseSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private userService userService;
    @Autowired
    private UserResponseSerializer userResponseSerializer;

    @GetMapping
    public List<User> findAll() {
        return userService.findAll();
    }

    @GetMapping("/me")
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

    @PutMapping("/me")
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



    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public UserResponseDto create(@RequestBody User user) {
        User response = userService.create(user);
        if (response == null) {
            throw new BadRequestException("User already exists");
        }
        return userResponseSerializer.toResponse(user);
    }
}
