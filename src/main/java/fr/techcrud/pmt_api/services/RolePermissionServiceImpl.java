package fr.techcrud.pmt_api.services;

import fr.techcrud.pmt_api.models.Permission;
import fr.techcrud.pmt_api.models.Role;
import fr.techcrud.pmt_api.models.RolePermission;
import fr.techcrud.pmt_api.repositories.PermissionRepository;
import fr.techcrud.pmt_api.repositories.RolePermissionRepository;
import fr.techcrud.pmt_api.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RolePermissionServiceImpl implements RolePermissionService {

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PermissionVerificationService permissionVerificationService;

    @Override
    public List<RolePermission> findByRoleId(UUID roleId) {
        return rolePermissionRepository.findByRoleIdWithPermission(roleId);
    }

    @Override
    public List<Permission> findPermissionsByRoleId(UUID roleId) {
        return rolePermissionRepository.findByRoleIdWithPermission(roleId).stream()
                .map(RolePermission::getPermission)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RolePermission grantPermissionToRole(UUID roleId, UUID permissionId) {
        if (rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId)) {
            return null; // Already granted
        }

        Role role = roleRepository.findById(roleId).orElse(null);
        Permission permission = permissionRepository.findById(permissionId).orElse(null);

        if (role == null || permission == null) {
            return null;
        }

        RolePermission rolePermission = new RolePermission();
        rolePermission.setRole(role);
        rolePermission.setPermission(permission);

        RolePermission saved = rolePermissionRepository.save(rolePermission);

        // Clear all permission caches since role permissions changed
        permissionVerificationService.clearAllPermissionCaches();

        return saved;
    }

    @Override
    @Transactional
    public void revokePermissionFromRole(UUID roleId, UUID permissionId) {
        if (rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId)) {
            rolePermissionRepository.deleteByRoleIdAndPermissionId(roleId, permissionId);

            // Clear all permission caches since role permissions changed
            permissionVerificationService.clearAllPermissionCaches();
        }
    }

    @Override
    public boolean roleHasPermission(UUID roleId, UUID permissionId) {
        return rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId);
    }
}
