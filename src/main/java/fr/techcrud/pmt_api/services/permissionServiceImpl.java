package fr.techcrud.pmt_api.services;

import fr.techcrud.pmt_api.models.Permission;
import fr.techcrud.pmt_api.repositories.permissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class permissionServiceImpl implements permissionService {

    @Autowired
    private permissionRepository permissionRepository;

    @Override
    public List<Permission> findAll() {
        return permissionRepository.findAll();
    }

    @Override
    public List<Permission> findAllActive() {
        return permissionRepository.findAllActivePermissions();
    }

    @Override
    public Permission findById(UUID id) {
        return permissionRepository.findById(id).orElse(null);
    }

    @Override
    public Permission findByResourceAndAction(String resource, String action) {
        return permissionRepository.findByResourceAndAction(resource, action).orElse(null);
    }

    @Override
    public List<Permission> findByResource(String resource) {
        return permissionRepository.findByResource(resource);
    }

    @Override
    @Transactional
    public Permission create(Permission permission) {
        if (permissionRepository.existsByResourceAndAction(
                permission.getResource(), permission.getAction())) {
            return null;
        }
        if (permission.getActive() == null) {
            permission.setActive(true);
        }
        return permissionRepository.save(permission);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        permissionRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void activate(UUID id) {
        Permission permission = permissionRepository.findById(id).orElse(null);
        if (permission != null) {
            permission.setActive(true);
            permissionRepository.save(permission);
        }
    }

    @Override
    @Transactional
    public void deactivate(UUID id) {
        Permission permission = permissionRepository.findById(id).orElse(null);
        if (permission != null) {
            permission.setActive(false);
            permissionRepository.save(permission);
        }
    }
}
