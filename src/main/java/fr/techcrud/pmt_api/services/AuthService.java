package fr.techcrud.pmt_api.services;

import fr.techcrud.pmt_api.models.User;
import fr.techcrud.pmt_api.repositories.UserRepository;
import fr.techcrud.pmt_api.utils.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public String login(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return null;
        }

        User user = userOpt.get();

        if (!BCrypt.checkpw(password, user.getPassword())) {
            return null;
        }

        return jwtUtil.generateToken(user.getEmail());
    }
}
