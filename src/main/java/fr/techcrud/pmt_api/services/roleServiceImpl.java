package fr.techcrud.pmt_api.services;

import fr.techcrud.pmt_api.models.Role;
import fr.techcrud.pmt_api.repositories.roleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class roleServiceImpl implements roleService {

    @Autowired
    private roleRepository roleRepository;

    @Override
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    @Override
    public List<Role> findAllActive() {
        return roleRepository.findAllActiveRoles();
    }

    @Override
    public Role findById(UUID id) {
        return roleRepository.findById(id).orElse(null);
    }

    @Override
    public Role findByName(String name) {
        return roleRepository.findByName(name).orElse(null);
    }

    @Override
    @Transactional
    public Role create(Role role) {
        if (roleRepository.existsByName(role.getName())) {
            return null;
        }
        if (role.getActive() == null) {
            role.setActive(true);
        }
        return roleRepository.save(role);
    }

    @Override
    @Transactional
    public Role update(UUID id, Role roleData) {
        Role existingRole = roleRepository.findById(id).orElse(null);
        if (existingRole == null) {
            return null;
        }

        if (roleData.getDescription() != null) {
            existingRole.setDescription(roleData.getDescription());
        }
        if (roleData.getActive() != null) {
            existingRole.setActive(roleData.getActive());
        }

        return roleRepository.save(existingRole);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        roleRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void activate(UUID id) {
        Role role = roleRepository.findById(id).orElse(null);
        if (role != null) {
            role.setActive(true);
            roleRepository.save(role);
        }
    }

    @Override
    @Transactional
    public void deactivate(UUID id) {
        Role role = roleRepository.findById(id).orElse(null);
        if (role != null) {
            role.setActive(false);
            roleRepository.save(role);
        }
    }
}
