package fr.techcrud.pmt_api.services;

import fr.techcrud.pmt_api.models.Role;
import fr.techcrud.pmt_api.models.User;
import fr.techcrud.pmt_api.models.UserRole;
import fr.techcrud.pmt_api.repositories.roleRepository;
import fr.techcrud.pmt_api.repositories.userRepository;
import fr.techcrud.pmt_api.repositories.userRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class userRoleServiceImpl implements userRoleService {

    @Autowired
    private userRoleRepository userRoleRepository;

    @Autowired
    private userRepository userRepository;

    @Autowired
    private roleRepository roleRepository;

    @Autowired
    private permissionAuditService permissionAuditService;

    @Autowired
    private permissionVerificationService permissionVerificationService;

    @Override
    public List<UserRole> findByUserId(UUID userId) {
        return userRoleRepository.findByUserIdWithRole(userId);
    }

    @Override
    public List<UserRole> findByRoleId(UUID roleId) {
        return userRoleRepository.findByRoleIdWithUser(roleId);
    }

    @Override
    @Transactional
    public UserRole assignRoleToUser(UUID userId, UUID roleId, UUID assignedById, String reason) {
        if (userRoleRepository.existsByUserIdAndRoleId(userId, roleId)) {
            return null; // Already assigned
        }

        User user = userRepository.findById(userId).orElse(null);
        Role role = roleRepository.findById(roleId).orElse(null);
        User assignedBy = userRepository.findById(assignedById).orElse(null);

        if (user == null || role == null) {
            return null;
        }

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setAssignedBy(assignedBy);

        UserRole saved = userRoleRepository.save(userRole);

        // Log audit trail
        permissionAuditService.logRoleAssignment(user, role, assignedBy, reason);

        // Clear permission cache
        permissionVerificationService.clearUserPermissionCache(userId);

        return saved;
    }

    @Override
    @Transactional
    public void revokeRoleFromUser(UUID userId, UUID roleId, UUID revokedById, String reason) {
        UserRole userRole = userRoleRepository.findByUserIdAndRoleId(userId, roleId).orElse(null);
        if (userRole != null) {
            User user = userRepository.findById(userId).orElse(null);
            Role role = roleRepository.findById(roleId).orElse(null);
            User revokedBy = userRepository.findById(revokedById).orElse(null);

            // Log audit trail before deletion
            if (user != null && role != null) {
                permissionAuditService.logRoleRevocation(user, role, revokedBy, reason);
            }

            userRoleRepository.deleteByUserIdAndRoleId(userId, roleId);

            // Clear permission cache
            permissionVerificationService.clearUserPermissionCache(userId);
        }
    }

    @Override
    public boolean userHasRole(UUID userId, UUID roleId) {
        return userRoleRepository.existsByUserIdAndRoleId(userId, roleId);
    }
}
