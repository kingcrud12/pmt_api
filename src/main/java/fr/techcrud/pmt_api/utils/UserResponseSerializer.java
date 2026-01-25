package fr.techcrud.pmt_api.utils;

import fr.techcrud.pmt_api.models.User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserResponseSerializer {
    public Map<String, Object> toResponse (boolean success, String message, User user) {
        return Map.of(
            "success", success,
            "message", message,
            "data", Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "role", user.getRole(),
                "phoneNumber", user.getPhoneNumber()
            )
        );
    }

}
