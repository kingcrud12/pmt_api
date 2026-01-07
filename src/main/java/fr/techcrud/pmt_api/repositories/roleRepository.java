package fr.techcrud.pmt_api.repositories;

import fr.techcrud.pmt_api.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface roleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(String name);

    List<Role> findByActiveTrue();

    boolean existsByName(String name);

    @Query("SELECT r FROM Role r WHERE r.active = true ORDER BY r.name")
    List<Role> findAllActiveRoles();
}
