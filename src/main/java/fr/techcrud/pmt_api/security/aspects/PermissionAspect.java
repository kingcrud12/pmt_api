package fr.techcrud.pmt_api.security.aspects;

import fr.techcrud.pmt_api.exceptions.PermissionDeniedException;
import fr.techcrud.pmt_api.security.annotations.RequiresAllPermissions;
import fr.techcrud.pmt_api.security.annotations.RequiresAnyPermission;
import fr.techcrud.pmt_api.security.annotations.RequiresPermission;
import fr.techcrud.pmt_api.security.annotations.RequiresRole;
import fr.techcrud.pmt_api.services.permissionVerificationService;
import fr.techcrud.pmt_api.services.userService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
public class PermissionAspect {

    @Autowired
    private permissionVerificationService permissionVerificationService;

    @Autowired
    private userService userService;

    @Around("@annotation(requiresPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequiresPermission requiresPermission)
            throws Throwable {
        UUID userId = getAuthenticatedUserId();
        String permission = requiresPermission.value();

        if (!permissionVerificationService.hasPermission(userId, permission)) {
            throw new PermissionDeniedException(
                "User does not have required permission: " + permission);
        }

        return joinPoint.proceed();
    }

    @Around("@annotation(requiresRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RequiresRole requiresRole)
            throws Throwable {
        UUID userId = getAuthenticatedUserId();
        String role = requiresRole.value();

        if (!permissionVerificationService.hasRole(userId, role)) {
            throw new PermissionDeniedException(
                "User does not have required role: " + role);
        }

        return joinPoint.proceed();
    }

    @Around("@annotation(requiresAnyPermission)")
    public Object checkAnyPermission(ProceedingJoinPoint joinPoint,
            RequiresAnyPermission requiresAnyPermission) throws Throwable {
        UUID userId = getAuthenticatedUserId();
        String[] permissions = requiresAnyPermission.value();

        if (!permissionVerificationService.hasAnyPermission(userId, permissions)) {
            throw new PermissionDeniedException(
                "User does not have any of the required permissions: " + String.join(", ", permissions));
        }

        return joinPoint.proceed();
    }

    @Around("@annotation(requiresAllPermissions)")
    public Object checkAllPermissions(ProceedingJoinPoint joinPoint,
            RequiresAllPermissions requiresAllPermissions) throws Throwable {
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

        String email = (String) authentication.getPrincipal();
        var user = userService.findByEmail(email);
        if (user == null) {
            throw new PermissionDeniedException("User not found");
        }

        return user.getId();
    }
}
