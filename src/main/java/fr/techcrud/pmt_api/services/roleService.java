package fr.techcrud.pmt_api.services;

import fr.techcrud.pmt_api.models.Role;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface roleService {

    List<Role> findAll();

    List<Role> findAllActive();

    Role findById(UUID id);

    Role findByName(String name);

    Role create(Role role);

    Role update(UUID id, Role roleData);

    void delete(UUID id);

    void activate(UUID id);

    void deactivate(UUID id);
}
