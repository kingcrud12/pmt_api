package fr.techcrud.pmt_api.controllers;

import fr.techcrud.pmt_api.dto.RoleCreateDto;
import fr.techcrud.pmt_api.dto.RoleDto;
import fr.techcrud.pmt_api.dto.RoleUpdateDto;
import fr.techcrud.pmt_api.exceptions.BadRequestException;
import fr.techcrud.pmt_api.exceptions.RoleNotFoundException;
import fr.techcrud.pmt_api.models.Role;
import fr.techcrud.pmt_api.security.annotations.RequiresPermission;
import fr.techcrud.pmt_api.services.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/roles")
@Tag(name = "Roles", description = "Endpoints for managing roles")
@SecurityRequirement(name = "bearerAuth")
public class RoleController {

	@Autowired
	private RoleService roleService;

	@GetMapping
	@RequiresPermission("ROLE:READ")
	@Operation(summary = "Get all roles", description = "Retrieve all roles")
	public List<RoleDto> findAll() {
		return roleService.findAll().stream()
				.map(this::toDto)
				.collect(Collectors.toList());
	}

	@GetMapping("/active")
	@RequiresPermission("ROLE:READ")
	@Operation(summary = "Get active roles", description = "Retrieve all active roles")
	public List<RoleDto> findAllActive() {
		return roleService.findAllActive().stream()
				.map(this::toDto)
				.collect(Collectors.toList());
	}

	@GetMapping("/{id}")
	@RequiresPermission("ROLE:READ")
	@Operation(summary = "Get role by ID", description = "Retrieve a role by its ID")
	public RoleDto findById(@PathVariable UUID id) {
		Role role = roleService.findById(id);
		if (role == null) {
			throw new RoleNotFoundException("Role not found with id: " + id);
		}
		return toDto(role);
	}

	@GetMapping("/name/{name}")
	@RequiresPermission("ROLE:READ")
	@Operation(summary = "Get role by name", description = "Retrieve a role by its name")
	public RoleDto findByName(@PathVariable String name) {
		Role role = roleService.findByName(name);
		if (role == null) {
			throw new RoleNotFoundException("Role not found with name: " + name);
		}
		return toDto(role);
	}

	@PostMapping
	@RequiresPermission("ROLE:CREATE")
	@Operation(summary = "Create role", description = "Create a new role")
	public RoleDto create(@RequestBody RoleCreateDto createDto) {
		Role role = new Role();
		role.setName(createDto.getName());
		role.setDescription(createDto.getDescription());

		Role created = roleService.create(role);
		if (created == null) {
			throw new BadRequestException("Role with name '" + createDto.getName() + "' already exists");
		}
		return toDto(created);
	}

	@PutMapping("/{id}")
	@RequiresPermission("ROLE:UPDATE")
	@Operation(summary = "Update role", description = "Update an existing role")
	public RoleDto update(@PathVariable UUID id, @RequestBody RoleUpdateDto updateDto) {
		Role roleData = new Role();
		roleData.setDescription(updateDto.getDescription());
		roleData.setActive(updateDto.getActive());

		Role updated = roleService.update(id, roleData);
		if (updated == null) {
			throw new RoleNotFoundException("Role not found with id: " + id);
		}
		return toDto(updated);
	}

	@DeleteMapping("/{id}")
	@RequiresPermission("ROLE:DELETE")
	@Operation(summary = "Delete role", description = "Delete a role")
	public void delete(@PathVariable UUID id) {
		Role role = roleService.findById(id);
		if (role == null) {
			throw new RoleNotFoundException("Role not found with id: " + id);
		}
		roleService.delete(id);
	}

	@PutMapping("/{id}/activate")
	@RequiresPermission("ROLE:UPDATE")
	@Operation(summary = "Activate role", description = "Activate a deactivated role")
	public void activate(@PathVariable UUID id) {
		Role role = roleService.findById(id);
		if (role == null) {
			throw new RoleNotFoundException("Role not found with id: " + id);
		}
		roleService.activate(id);
	}

	@PutMapping("/{id}/deactivate")
	@RequiresPermission("ROLE:UPDATE")
	@Operation(summary = "Deactivate role", description = "Deactivate an active role")
	public void deactivate(@PathVariable UUID id) {
		Role role = roleService.findById(id);
		if (role == null) {
			throw new RoleNotFoundException("Role not found with id: " + id);
		}
		roleService.deactivate(id);
	}

	private RoleDto toDto(Role role) {
		RoleDto dto = new RoleDto();
		if (role.getId() != null)
			dto.setId(role.getId().toString());
		else
			dto.setId(null);
		dto.setName(role.getName());
		dto.setDescription(role.getDescription());
		dto.setActive(role.getActive());
		if (role.getCreatedAt() != null)
			dto.setCreatedAt(role.getCreatedAt().toString());
		else
			dto.setCreatedAt(null);
		return dto;
	}
}
