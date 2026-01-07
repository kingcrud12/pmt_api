package fr.techcrud.pmt_api.repositories;

import fr.techcrud.pmt_api.models.PermissionAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface permissionAuditLogRepository extends JpaRepository<PermissionAuditLog, UUID> {

    List<PermissionAuditLog> findByUserIdOrderByPerformedAtDesc(UUID userId);

    List<PermissionAuditLog> findByPerformedByIdOrderByPerformedAtDesc(UUID performedById);

    List<PermissionAuditLog> findByRoleIdOrderByPerformedAtDesc(UUID roleId);

    @Query("SELECT pal FROM PermissionAuditLog pal WHERE pal.performedAt BETWEEN :startDate AND :endDate ORDER BY pal.performedAt DESC")
    List<PermissionAuditLog> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT pal FROM PermissionAuditLog pal WHERE pal.action = :action ORDER BY pal.performedAt DESC")
    List<PermissionAuditLog> findByAction(@Param("action") String action);
}
