package fr.techcrud.pmt_api.controllers;

import fr.techcrud.pmt_api.dto.UserRoleAssignmentDto;
import fr.techcrud.pmt_api.dto.UserRoleDto;
import fr.techcrud.pmt_api.exceptions.DuplicateRoleAssignmentException;
import fr.techcrud.pmt_api.exceptions.RessourceNotFoundException;
import fr.techcrud.pmt_api.models.User;
import fr.techcrud.pmt_api.models.UserRole;
import fr.techcrud.pmt_api.security.annotations.RequiresPermission;
import fr.techcrud.pmt_api.services.UserRoleService;
import fr.techcrud.pmt_api.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/user-roles")
@Tag(name = "User Roles", description = "Endpoints for assigning roles to users")
@SecurityRequirement(name = "bearerAuth")
public class UserRoleController {

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private UserService userService;

    @GetMapping("/user/{userId}")
    @RequiresPermission("ROLE:READ")
    @Operation(summary = "Get user roles", description = "Get all roles assigned to a user")
    public List<UserRoleDto> getUserRoles(@PathVariable UUID userId) {
        return userRoleService.findByUserId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/role/{roleId}")
    @RequiresPermission("ROLE:READ")
    @Operation(summary = "Get users by role", description = "Get all users with a specific role")
    public List<UserRoleDto> getUsersByRole(@PathVariable UUID roleId) {
        return userRoleService.findByRoleId(roleId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @PostMapping("/assign")
    @RequiresPermission("ROLE:ASSIGN")
    @Operation(summary = "Assign role to user", description = "Assign a role to a user")
    public UserRoleDto assignRole(@RequestBody UserRoleAssignmentDto assignmentDto) {
        UUID userId = UUID.fromString(assignmentDto.getUserId());
        UUID roleId = UUID.fromString(assignmentDto.getRoleId());
        UUID assignedById = getAuthenticatedUserId();

        UserRole userRole = userRoleService.assignRoleToUser(
            userId, roleId, assignedById, assignmentDto.getReason());

        if (userRole == null) {
            throw new DuplicateRoleAssignmentException("User already has this role assigned");
        }

        return toDto(userRole);
    }

    @DeleteMapping("/revoke")
    @RequiresPermission("ROLE:ASSIGN")
    @Operation(summary = "Revoke role from user", description = "Revoke a role from a user")
    public void revokeRole(@RequestBody UserRoleAssignmentDto assignmentDto) {
        UUID userId = UUID.fromString(assignmentDto.getUserId());
        UUID roleId = UUID.fromString(assignmentDto.getRoleId());
        UUID revokedById = getAuthenticatedUserId();

        userRoleService.revokeRoleFromUser(userId, roleId, revokedById, assignmentDto.getReason());
    }

    private UUID getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = (String) authentication.getPrincipal();
        User user = userService.findByEmail(email);
        if (user == null) {
            throw new RessourceNotFoundException("Authenticated user not found");
        }
        return user.getId();
    }

    private UserRoleDto toDto(UserRole userRole) {
        UserRoleDto dto = new UserRoleDto();
        dto.setId(userRole.getId().toString());
        dto.setUserId(userRole.getUser().getId().toString());
        dto.setRoleId(userRole.getRole().getId().toString());
        dto.setRoleName(userRole.getRole().getName());
        dto.setAssignedAt(userRole.getAssignedAt().toString());
        if (userRole.getAssignedBy() != null) {
            dto.setAssignedByEmail(userRole.getAssignedBy().getEmail());
        }
        return dto;
    }
}
