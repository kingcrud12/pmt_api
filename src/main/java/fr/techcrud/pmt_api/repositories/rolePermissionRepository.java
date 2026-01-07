package fr.techcrud.pmt_api.repositories;

import fr.techcrud.pmt_api.models.Permission;
import fr.techcrud.pmt_api.models.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface rolePermissionRepository extends JpaRepository<RolePermission, UUID> {

    List<RolePermission> findByRoleId(UUID roleId);

    List<RolePermission> findByPermissionId(UUID permissionId);

    Optional<RolePermission> findByRoleIdAndPermissionId(UUID roleId, UUID permissionId);

    boolean existsByRoleIdAndPermissionId(UUID roleId, UUID permissionId);

    void deleteByRoleIdAndPermissionId(UUID roleId, UUID permissionId);

    @Query("SELECT rp FROM RolePermission rp JOIN FETCH rp.permission WHERE rp.role.id = :roleId")
    List<RolePermission> findByRoleIdWithPermission(@Param("roleId") UUID roleId);

    @Query("SELECT p FROM Permission p " +
           "JOIN RolePermission rp ON rp.permission.id = p.id " +
           "JOIN UserRole ur ON ur.role.id = rp.role.id " +
           "WHERE ur.user.id = :userId AND p.active = true AND rp.role.active = true")
    List<Permission> findPermissionsByUserId(@Param("userId") UUID userId);
}
