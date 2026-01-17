package fr.techcrud.pmt_api.services;

import fr.techcrud.pmt_api.models.User;
import fr.techcrud.pmt_api.repositories.UserRepository;
import fr.techcrud.pmt_api.utils.UserUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserUpdate userUpdate;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public List <User> findAll(){
        List <User> list = new ArrayList<>();
        userRepository.findAll().forEach(list::add);
        return list;
    }

    @Override
    public User findByEmail(String email){
        if(userRepository.findByEmail(email).isPresent()){
            return userRepository.findByEmail(email).get();
        }
        return null;
    }

    @Override
    public User findById(UUID id){
        if(userRepository.findById(id).isPresent()){
            return userRepository.findById(id).get();
        }
        return null;
    }

    @Override
    public User create(User user) {
        if(userRepository.findByEmail(user.getEmail()).isPresent()){
            return null;
        }
        if (user.getRole() == null) {
            user.setRole("USER");
        }

        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userRepository.save(user);
    }

    @Override
    public User update(UUID id, User userData) {
        return userUpdate.userUpdater(id, userData);
    }

    @Override
    public User updateByEmail(String email, User userData) {
        User existingUser = userRepository.findByEmail(email).orElse(null);
        if (existingUser == null) return null;

        existingUser.setFirstName(userData.getFirstName());
        existingUser.setLastName(userData.getLastName());
        existingUser.setPassword(userData.getPassword());
        existingUser.setRole(userData.getRole() != null ? userData.getRole() : "USER");

        return userRepository.save(existingUser);
    }




}
