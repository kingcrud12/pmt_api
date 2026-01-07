package fr.techcrud.pmt_api.services;

import fr.techcrud.pmt_api.models.Permission;
import fr.techcrud.pmt_api.models.PermissionAuditLog;
import fr.techcrud.pmt_api.models.Role;
import fr.techcrud.pmt_api.models.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public interface permissionAuditService {

    void logRoleAssignment(User user, Role role, User assignedBy, String reason);

    void logRoleRevocation(User user, Role role, User revokedBy, String reason);

    void logPermissionGrant(Role role, Permission permission, User grantedBy);

    void logPermissionRevoke(Role role, Permission permission, User revokedBy);

    List<PermissionAuditLog> findByUserId(UUID userId);

    List<PermissionAuditLog> findByPerformedBy(UUID performedById);

    List<PermissionAuditLog> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    List<PermissionAuditLog> findByAction(String action);
}
