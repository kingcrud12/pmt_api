package fr.techcrud.pmt_api.services;

import fr.techcrud.pmt_api.models.Permission;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface PermissionVerificationService {

    /**
     * Check if user has a specific permission (with caching)
     */
    boolean hasPermission(UUID userId, String permissionString);

    /**
     * Check if user has ANY of the specified permissions
     */
    boolean hasAnyPermission(UUID userId, String[] permissions);

    /**
     * Check if user has ALL of the specified permissions
     */
    boolean hasAllPermissions(UUID userId, String[] permissions);

    /**
     * Check if user has a specific role
     */
    boolean hasRole(UUID userId, String roleName);

	/**
	 * 
	 * Checks if those type of permission exists
	 */
	boolean exists(Permission permission);

    /**
     * Get all permissions for a user (cached)
     */
    List<Permission> getUserPermissions(UUID userId);

    /**
     * Clear permission cache for a user
     */
    void clearUserPermissionCache(UUID userId);

    /**
     * Clear all permission caches
     */
    void clearAllPermissionCaches();
}
