package fr.techcrud.pmt_api.services;

import fr.techcrud.pmt_api.models.Permission;
import fr.techcrud.pmt_api.models.UserRole;
import fr.techcrud.pmt_api.repositories.RolePermissionRepository;
import fr.techcrud.pmt_api.repositories.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class PermissionVerificationServiceImpl implements PermissionVerificationService {

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Override
    @Cacheable(value = "userPermissions", key = "#userId + '_' + #permissionString")
    public boolean hasPermission(UUID userId, String permissionString) {
        List<Permission> permissions = getUserPermissions(userId);
        return permissions.stream()
                .anyMatch(p -> p.getPermissionString().equalsIgnoreCase(permissionString));
    }

    @Override
    public boolean hasAnyPermission(UUID userId, String[] permissions) {
        return Arrays.stream(permissions)
                .anyMatch(permission -> hasPermission(userId, permission));
    }

    @Override
    public boolean hasAllPermissions(UUID userId, String[] permissions) {
        return Arrays.stream(permissions)
                .allMatch(permission -> hasPermission(userId, permission));
    }

    @Override
    @Cacheable(value = "userRoles", key = "#userId + '_' + #roleName")
    public boolean hasRole(UUID userId, String roleName) {
        List<UserRole> userRoles = userRoleRepository.findByUserIdWithRole(userId);
        return userRoles.stream()
                .anyMatch(ur -> ur.getRole().getName().equalsIgnoreCase(roleName)
                        && ur.getRole().getActive());
    }

    @Override
    @Cacheable(value = "userPermissionsList", key = "#userId")
    public List<Permission> getUserPermissions(UUID userId) {
        return rolePermissionRepository.findPermissionsByUserId(userId);
    }

    @Override
    @CacheEvict(value = {"userPermissions", "userPermissionsList", "userRoles"}, allEntries = false, key = "#userId")
    public void clearUserPermissionCache(UUID userId) {
        // Cache eviction is handled by annotation
    }

    @Override
    @CacheEvict(value = {"userPermissions", "userPermissionsList", "userRoles"}, allEntries = true)
    public void clearAllPermissionCaches() {
        // Cache eviction is handled by annotation
    }
}
