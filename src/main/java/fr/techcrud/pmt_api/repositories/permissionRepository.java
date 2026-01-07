package fr.techcrud.pmt_api.repositories;

import fr.techcrud.pmt_api.models.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface permissionRepository extends JpaRepository<Permission, UUID> {

    Optional<Permission> findByResourceAndAction(String resource, String action);

    List<Permission> findByResource(String resource);

    List<Permission> findByActiveTrue();

    boolean existsByResourceAndAction(String resource, String action);

    @Query("SELECT p FROM Permission p WHERE p.active = true ORDER BY p.resource, p.action")
    List<Permission> findAllActivePermissions();
}
