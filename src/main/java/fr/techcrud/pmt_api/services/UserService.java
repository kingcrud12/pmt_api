package fr.techcrud.pmt_api.services;


import fr.techcrud.pmt_api.models.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface UserService {
    public List <User> findAll();
    public User findById(UUID id);
    public User create(User user);
    public User update(UUID id, User user);

    public User findByEmail(String email);

    public User updateByEmail(String email, User user);
}
