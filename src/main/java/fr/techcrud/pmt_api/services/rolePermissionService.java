package fr.techcrud.pmt_api.services;

import fr.techcrud.pmt_api.models.Permission;
import fr.techcrud.pmt_api.models.RolePermission;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface rolePermissionService {

    List<RolePermission> findByRoleId(UUID roleId);

    List<Permission> findPermissionsByRoleId(UUID roleId);

    RolePermission grantPermissionToRole(UUID roleId, UUID permissionId);

    void revokePermissionFromRole(UUID roleId, UUID permissionId);

    boolean roleHasPermission(UUID roleId, UUID permissionId);
}
