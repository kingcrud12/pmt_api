package fr.techcrud.pmt_api.security.aspects;

import fr.techcrud.pmt_api.exceptions.PermissionDeniedException;
import fr.techcrud.pmt_api.exceptions.RessourceNotFoundException;
import fr.techcrud.pmt_api.models.User;
import fr.techcrud.pmt_api.security.annotations.RequiresAllPermissions;
import fr.techcrud.pmt_api.security.annotations.RequiresAnyPermission;
import fr.techcrud.pmt_api.security.annotations.RequiresPermission;
import fr.techcrud.pmt_api.security.annotations.RequiresRole;
import fr.techcrud.pmt_api.services.PermissionVerificationService;
import fr.techcrud.pmt_api.services.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionAspectTest {

	@Mock
	private PermissionVerificationService permissionVerificationService;

	@Mock
	private UserService userService;

	@Mock
	private ProceedingJoinPoint joinPoint;

	@Mock
	private SecurityContext securityContext;

	@Mock
	private Authentication authentication;

	@InjectMocks
	private PermissionAspect permissionAspect;

	private User testUser;
	private UUID userId;

	@BeforeEach
	void setUp() {
		userId = UUID.randomUUID();
		testUser = new User();
		testUser.setId(userId);
		testUser.setEmail("test@example.com");
		testUser.setFirstName("testuser");

		SecurityContextHolder.setContext(securityContext);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getPrincipal()).thenReturn("test@example.com");
		when(userService.findByEmail("test@example.com")).thenReturn(testUser);
	}

	@Test
	void whenUserHasPermission_thenProceed() throws Throwable {
		RequiresPermission annotation = mock(RequiresPermission.class);
		when(annotation.value()).thenReturn("USER:READ");
		when(permissionVerificationService.hasPermission(userId, "USER:READ")).thenReturn(true);
		when(joinPoint.proceed()).thenReturn("success");

		permissionAspect.checkPermission(joinPoint, annotation);

		verify(joinPoint).proceed();
		verify(permissionVerificationService).hasPermission(userId, "USER:READ");
	}

	@Test
	void whenUserDoesNotHavePermission_thenThrowPermissionDeniedException() throws Throwable {
		RequiresPermission annotation = mock(RequiresPermission.class);
		when(annotation.value()).thenReturn("USER:DELETE");
		when(permissionVerificationService.hasPermission(userId, "USER:DELETE")).thenReturn(false);

		assertThatThrownBy(() -> permissionAspect.checkPermission(joinPoint, annotation))
				.isInstanceOf(PermissionDeniedException.class)
				.hasMessageContaining("USER:DELETE");

		verify(joinPoint, never()).proceed();
	}

	@Test
	void whenUserNotFound_thenThrowRessourceNotFoundException() {
		RequiresPermission annotation = mock(RequiresPermission.class);
		when(annotation.value()).thenReturn("USER:READ");
		when(userService.findByEmail("test@example.com")).thenReturn(null);

		assertThatThrownBy(() -> permissionAspect.checkPermission(joinPoint, annotation))
				.isInstanceOf(RessourceNotFoundException.class)
				.hasMessageContaining("Authenticated user not found");
	}

	@Test
	void whenUserHasRole_thenProceed() throws Throwable {
		RequiresRole annotation = mock(RequiresRole.class);
		when(annotation.value()).thenReturn("ADMIN");
		when(permissionVerificationService.hasRole(userId, "ADMIN")).thenReturn(true);
		when(joinPoint.proceed()).thenReturn("success");

		permissionAspect.checkRole(joinPoint, annotation);

		verify(joinPoint).proceed();
		verify(permissionVerificationService).hasRole(userId, "ADMIN");
	}

	@Test
	void whenUserDoesNotHaveRole_thenThrowPermissionDeniedException() throws Throwable {
		RequiresRole annotation = mock(RequiresRole.class);
		when(annotation.value()).thenReturn("ADMIN");
		when(permissionVerificationService.hasRole(userId, "ADMIN")).thenReturn(false);

		assertThatThrownBy(() -> permissionAspect.checkRole(joinPoint, annotation))
				.isInstanceOf(PermissionDeniedException.class)
				.hasMessageContaining("ADMIN");

		verify(joinPoint, never()).proceed();
	}

	@Test
	void whenUserHasAnyPermission_thenProceed() throws Throwable {
		RequiresAnyPermission annotation = mock(RequiresAnyPermission.class);
		String[] permissions = { "USER:READ", "USER:WRITE" };
		when(annotation.value()).thenReturn(permissions);
		when(permissionVerificationService.hasAnyPermission(userId, permissions)).thenReturn(true);
		when(joinPoint.proceed()).thenReturn("success");

		permissionAspect.checkAnyPermission(joinPoint, annotation);

		verify(joinPoint).proceed();
		verify(permissionVerificationService).hasAnyPermission(userId, permissions);
	}

	@Test
	void whenUserDoesNotHaveAnyPermission_thenThrowPermissionDeniedException() throws Throwable {
		RequiresAnyPermission annotation = mock(RequiresAnyPermission.class);
		String[] permissions = { "USER:DELETE", "USER:ADMIN" };
		when(annotation.value()).thenReturn(permissions);
		when(permissionVerificationService.hasAnyPermission(userId, permissions)).thenReturn(false);

		assertThatThrownBy(() -> permissionAspect.checkAnyPermission(joinPoint, annotation))
				.isInstanceOf(PermissionDeniedException.class);

		verify(joinPoint, never()).proceed();
	}

	@Test
	void whenUserHasAllPermissions_thenProceed() throws Throwable {
		RequiresAllPermissions annotation = mock(RequiresAllPermissions.class);
		String[] permissions = { "USER:READ", "USER:WRITE" };
		when(annotation.value()).thenReturn(permissions);
		when(permissionVerificationService.hasAllPermissions(userId, permissions)).thenReturn(true);
		when(joinPoint.proceed()).thenReturn("success");

		permissionAspect.checkAllPermissions(joinPoint, annotation);

		verify(joinPoint).proceed();
		verify(permissionVerificationService).hasAllPermissions(userId, permissions);
	}

	@Test
	void whenUserDoesNotHaveAllPermissions_thenThrowPermissionDeniedException() throws Throwable {
		RequiresAllPermissions annotation = mock(RequiresAllPermissions.class);
		String[] permissions = { "USER:READ", "USER:WRITE", "USER:DELETE" };
		when(annotation.value()).thenReturn(permissions);
		when(permissionVerificationService.hasAllPermissions(userId, permissions)).thenReturn(false);

		assertThatThrownBy(() -> permissionAspect.checkAllPermissions(joinPoint, annotation))
				.isInstanceOf(PermissionDeniedException.class);

		verify(joinPoint, never()).proceed();
	}
}
