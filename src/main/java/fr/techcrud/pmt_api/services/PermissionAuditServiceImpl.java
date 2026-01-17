package fr.techcrud.pmt_api.services;

import fr.techcrud.pmt_api.models.Permission;
import fr.techcrud.pmt_api.models.PermissionAuditLog;
import fr.techcrud.pmt_api.models.Role;
import fr.techcrud.pmt_api.models.User;
import fr.techcrud.pmt_api.repositories.PermissionAuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PermissionAuditServiceImpl implements PermissionAuditService {

    @Autowired
    private PermissionAuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public void logRoleAssignment(User user, Role role, User assignedBy, String reason) {
        PermissionAuditLog log = new PermissionAuditLog();
        log.setUser(user);
        log.setRole(role);
        log.setAction("ASSIGN_ROLE");
        log.setPerformedBy(assignedBy);
        log.setReason(reason);
        auditLogRepository.save(log);
    }

    @Override
    @Transactional
    public void logRoleRevocation(User user, Role role, User revokedBy, String reason) {
        PermissionAuditLog log = new PermissionAuditLog();
        log.setUser(user);
        log.setRole(role);
        log.setAction("REVOKE_ROLE");
        log.setPerformedBy(revokedBy);
        log.setReason(reason);
        auditLogRepository.save(log);
    }

    @Override
    @Transactional
    public void logPermissionGrant(Role role, Permission permission, User grantedBy) {
        PermissionAuditLog log = new PermissionAuditLog();
        log.setRole(role);
        log.setPermission(permission);
        log.setAction("GRANT_PERMISSION");
        log.setPerformedBy(grantedBy);
        auditLogRepository.save(log);
    }

    @Override
    @Transactional
    public void logPermissionRevoke(Role role, Permission permission, User revokedBy) {
        PermissionAuditLog log = new PermissionAuditLog();
        log.setRole(role);
        log.setPermission(permission);
        log.setAction("REVOKE_PERMISSION");
        log.setPerformedBy(revokedBy);
        auditLogRepository.save(log);
    }

    @Override
    public List<PermissionAuditLog> findByUserId(UUID userId) {
        return auditLogRepository.findByUserIdOrderByPerformedAtDesc(userId);
    }

    @Override
    public List<PermissionAuditLog> findByPerformedBy(UUID performedById) {
        return auditLogRepository.findByPerformedByIdOrderByPerformedAtDesc(performedById);
    }

    @Override
    public List<PermissionAuditLog> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByDateRange(startDate, endDate);
    }

    @Override
    public List<PermissionAuditLog> findByAction(String action) {
        return auditLogRepository.findByAction(action);
    }
}
