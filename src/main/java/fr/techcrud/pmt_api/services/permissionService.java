package fr.techcrud.pmt_api.services;

import fr.techcrud.pmt_api.models.Permission;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface permissionService {

    List<Permission> findAll();

    List<Permission> findAllActive();

    Permission findById(UUID id);

    Permission findByResourceAndAction(String resource, String action);

    List<Permission> findByResource(String resource);

    Permission create(Permission permission);

    void delete(UUID id);

    void activate(UUID id);

    void deactivate(UUID id);
}
