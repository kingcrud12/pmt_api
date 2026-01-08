package fr.techcrud.pmt_api.security.aspects;

import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import fr.techcrud.pmt_api.exceptions.PermissionDeniedException;
import fr.techcrud.pmt_api.exceptions.RessourceNotFoundException;
import fr.techcrud.pmt_api.models.Permission;
import fr.techcrud.pmt_api.security.annotations.RequiresAllPermissions;
import fr.techcrud.pmt_api.security.annotations.RequiresAnyPermission;
import fr.techcrud.pmt_api.security.annotations.RequiresPermission;
import fr.techcrud.pmt_api.security.annotations.RequiresRole;
import fr.techcrud.pmt_api.services.PermissionVerificationService;
import fr.techcrud.pmt_api.services.UserService;

@Aspect
@Component
public class PermissionAspect {

	private PermissionVerificationService permissionVerificationService;

	private UserService userService;

	public PermissionAspect(PermissionVerificationService permissionVerificationService, UserService userService) {
		this.permissionVerificationService = permissionVerificationService;
		this.userService = userService;
	}

	@Around("@annotation(requiresPermission)")
	public Object checkPermission(ProceedingJoinPoint joinPoint, RequiresPermission requiresPermission)
			throws Throwable {
		UUID userId = getAuthenticatedUserId();
		String permissionStr = requiresPermission.value();

		// 1) Format validation
		Permission permission = new Permission();
		if (!permission.setPermissionString(permissionStr)) {
			throw new PermissionDeniedException("Incorrect permission format: " + permissionStr);
		}

		// 2) Existence validation (optional but recommended for clean diagnostics)
		if (!permissionVerificationService.exists(permission)) {
			throw new PermissionDeniedException("Unknown permission: " + permissionStr);
		}

		// 3) Authorization check
		if (!permissionVerificationService.hasPermission(userId, permissionStr)) {
			throw new PermissionDeniedException("User does not have required permission: " + permissionStr);
		}

		return joinPoint.proceed();
	}

	@Around("@annotation(requiresRole)")
	public Object checkRole(ProceedingJoinPoint joinPoint, RequiresRole requiresRole) throws Throwable {
		UUID userId = getAuthenticatedUserId();
		String role = requiresRole.value();

		if (!permissionVerificationService.hasRole(userId, role)) {
			throw new PermissionDeniedException("User does not have required role: " + role);
		}

		return joinPoint.proceed();
	}

	@Around("@annotation(requiresAnyPermission)")
	public Object checkAnyPermission(ProceedingJoinPoint joinPoint, RequiresAnyPermission requiresAnyPermission)
			throws Throwable {
		UUID userId = getAuthenticatedUserId();
		String[] permissions = requiresAnyPermission.value();

		if (!permissionVerificationService.hasAnyPermission(userId, permissions)) {
			throw new PermissionDeniedException(
					"User does not have any of the required permissions: " + String.join(", ", permissions));
		}

		return joinPoint.proceed();
	}

	@Around("@annotation(requiresAllPermissions)")
	public Object checkAllPermissions(ProceedingJoinPoint joinPoint, RequiresAllPermissions requiresAllPermissions)
			throws Throwable {
		UUID userId = getAuthenticatedUserId();
		String[] permissions = requiresAllPermissions.value();

		if (!permissionVerificationService.hasAllPermissions(userId, permissions)) {
			throw new PermissionDeniedException(
					"User does not have all of the required permissions: " + String.join(", ", permissions));
		}

		return joinPoint.proceed();
	}

	private UUID getAuthenticatedUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || authentication.getPrincipal() == null) {
			throw new PermissionDeniedException("No authenticated user found");
		}

		Object principal = authentication.getPrincipal();
		final String email;

		if (principal instanceof UserDetails ud) {
			email = ud.getUsername();
		} else if (principal instanceof String s) {
			email = s;
		} else {
			throw new PermissionDeniedException("Unsupported principal type: " + principal.getClass().getName());
		}

		var user = userService.findByEmail(email);
		if (user == null) {
			throw new RessourceNotFoundException("Authenticated user not found: " + email);
		}

		return user.getId();
	}
}