package fr.techcrud.pmt_api.utils;

import fr.techcrud.pmt_api.dto.UserResponseDto;
import fr.techcrud.pmt_api.models.User;
import org.springframework.stereotype.Service;

@Service
public class UserResponseSerializer {
    public UserResponseDto toResponse (User user) {
        UserResponseDto r = new UserResponseDto();
        r.setId(user.getId().toString());
        r.setEmail(user.getEmail());
        r.setFirstName(user.getFirstName());
        r.setLastName(user.getLastName());
        r.setRole(user.getRole());
        return r;
    }

}
