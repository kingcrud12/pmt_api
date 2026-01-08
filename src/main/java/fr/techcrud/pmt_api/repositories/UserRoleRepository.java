package fr.techcrud.pmt_api.repositories;

import fr.techcrud.pmt_api.models.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    List<UserRole> findByUserId(UUID userId);

    List<UserRole> findByRoleId(UUID roleId);

    Optional<UserRole> findByUserIdAndRoleId(UUID userId, UUID roleId);

    boolean existsByUserIdAndRoleId(UUID userId, UUID roleId);

    void deleteByUserIdAndRoleId(UUID userId, UUID roleId);

    @Query("SELECT ur FROM UserRole ur JOIN FETCH ur.role WHERE ur.user.id = :userId")
    List<UserRole> findByUserIdWithRole(@Param("userId") UUID userId);

    @Query("SELECT ur FROM UserRole ur JOIN FETCH ur.user WHERE ur.role.id = :roleId")
    List<UserRole> findByRoleIdWithUser(@Param("roleId") UUID roleId);
}
