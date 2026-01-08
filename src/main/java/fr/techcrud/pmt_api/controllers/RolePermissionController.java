package fr.techcrud.pmt_api.controllers;

import fr.techcrud.pmt_api.dto.PermissionDto;
import fr.techcrud.pmt_api.dto.RolePermissionAssignmentDto;
import fr.techcrud.pmt_api.exceptions.BadRequestException;
import fr.techcrud.pmt_api.models.Permission;
import fr.techcrud.pmt_api.models.RolePermission;
import fr.techcrud.pmt_api.security.annotations.RequiresPermission;
import fr.techcrud.pmt_api.services.RolePermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/role-permissions")
@Tag(name = "Role Permissions", description = "Endpoints for managing role permissions")
@SecurityRequirement(name = "bearerAuth")
public class RolePermissionController {

    @Autowired
    private RolePermissionService rolePermissionService;

    @GetMapping("/role/{roleId}")
    @RequiresPermission("PERMISSION:READ")
    @Operation(summary = "Get role permissions", description = "Get all permissions for a role")
    public List<PermissionDto> getRolePermissions(@PathVariable UUID roleId) {
        return rolePermissionService.findPermissionsByRoleId(roleId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @PostMapping("/grant")
    @RequiresPermission("PERMISSION:GRANT")
    @Operation(summary = "Grant permission to role", description = "Grant a permission to a role")
    public void grantPermission(@RequestBody RolePermissionAssignmentDto assignmentDto) {
        UUID roleId = UUID.fromString(assignmentDto.getRoleId());
        UUID permissionId = UUID.fromString(assignmentDto.getPermissionId());

        RolePermission rolePermission = rolePermissionService.grantPermissionToRole(roleId, permissionId);
        if (rolePermission == null) {
            throw new BadRequestException("Role already has this permission");
        }
    }

    @DeleteMapping("/revoke")
    @RequiresPermission("PERMISSION:REVOKE")
    @Operation(summary = "Revoke permission from role", description = "Revoke a permission from a role")
    public void revokePermission(@RequestBody RolePermissionAssignmentDto assignmentDto) {
        UUID roleId = UUID.fromString(assignmentDto.getRoleId());
        UUID permissionId = UUID.fromString(assignmentDto.getPermissionId());

        rolePermissionService.revokePermissionFromRole(roleId, permissionId);
    }

    private PermissionDto toDto(Permission permission) {
        PermissionDto dto = new PermissionDto();
        dto.setId(permission.getId().toString());
        dto.setResource(permission.getResource());
        dto.setAction(permission.getAction());
        dto.setDescription(permission.getDescription());
        dto.setActive(permission.getActive());
        dto.setPermissionString(permission.getPermissionString());
        return dto;
    }
}
