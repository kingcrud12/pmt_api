package fr.techcrud.pmt_api.utils;

import fr.techcrud.pmt_api.models.User;
import fr.techcrud.pmt_api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserUpdate {

    @Autowired
    UserRepository userRepository;

    public User userUpdater(UUID id, User userData) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            return null;
        }

        User existingUser = user.get();

        if (userData.getFirstName() != null) {
            existingUser.setFirstName(userData.getFirstName());
        }
        if (userData.getLastName() != null) {
            existingUser.setLastName(userData.getLastName());
        }
        if (userData.getPassword() != null && !userData.getPassword().isBlank()) {
            existingUser.setPassword(userData.getPassword());
        }
        if (userData.getRole() != null && !userData.getRole().isBlank()) {
            existingUser.setRole(userData.getRole());
        }
        return userRepository.save(existingUser);
    }
}
