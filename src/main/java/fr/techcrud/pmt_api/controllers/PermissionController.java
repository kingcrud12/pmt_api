package fr.techcrud.pmt_api.controllers;

import fr.techcrud.pmt_api.dto.PermissionCreateDto;
import fr.techcrud.pmt_api.dto.PermissionDto;
import fr.techcrud.pmt_api.exceptions.BadRequestException;
import fr.techcrud.pmt_api.exceptions.PermissionNotFoundException;
import fr.techcrud.pmt_api.models.Permission;
import fr.techcrud.pmt_api.security.annotations.RequiresPermission;
import fr.techcrud.pmt_api.services.permissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/permissions")
@Tag(name = "Permissions", description = "Endpoints for managing permissions")
@SecurityRequirement(name = "bearerAuth")
public class PermissionController {

    @Autowired
    private permissionService permissionService;

    @GetMapping
    @RequiresPermission("PERMISSION:READ")
    @Operation(summary = "Get all permissions", description = "Retrieve all permissions")
    public List<PermissionDto> findAll() {
        return permissionService.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/active")
    @RequiresPermission("PERMISSION:READ")
    @Operation(summary = "Get active permissions", description = "Retrieve all active permissions")
    public List<PermissionDto> findAllActive() {
        return permissionService.findAllActive().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @RequiresPermission("PERMISSION:READ")
    @Operation(summary = "Get permission by ID", description = "Retrieve a permission by its ID")
    public PermissionDto findById(@PathVariable UUID id) {
        Permission permission = permissionService.findById(id);
        if (permission == null) {
            throw new PermissionNotFoundException("Permission not found with id: " + id);
        }
        return toDto(permission);
    }

    @GetMapping("/resource/{resource}")
    @RequiresPermission("PERMISSION:READ")
    @Operation(summary = "Get permissions by resource", description = "Retrieve all permissions for a resource")
    public List<PermissionDto> findByResource(@PathVariable String resource) {
        return permissionService.findByResource(resource).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    @RequiresPermission("PERMISSION:GRANT")
    @Operation(summary = "Create permission", description = "Create a new permission")
    public PermissionDto create(@RequestBody PermissionCreateDto createDto) {
        Permission permission = new Permission();
        permission.setResource(createDto.getResource().toUpperCase());
        permission.setAction(createDto.getAction().toUpperCase());
        permission.setDescription(createDto.getDescription());

        Permission created = permissionService.create(permission);
        if (created == null) {
            throw new BadRequestException("Permission already exists: " +
                createDto.getResource() + ":" + createDto.getAction());
        }
        return toDto(created);
    }

    @DeleteMapping("/{id}")
    @RequiresPermission("PERMISSION:REVOKE")
    @Operation(summary = "Delete permission", description = "Delete a permission")
    public void delete(@PathVariable UUID id) {
        Permission permission = permissionService.findById(id);
        if (permission == null) {
            throw new PermissionNotFoundException("Permission not found with id: " + id);
        }
        permissionService.delete(id);
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
