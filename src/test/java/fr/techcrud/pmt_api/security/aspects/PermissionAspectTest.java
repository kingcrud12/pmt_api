package fr.techcrud.pmt_api.security.aspects;

import fr.techcrud.pmt_api.exceptions.PermissionDeniedException;
import fr.techcrud.pmt_api.exceptions.RessourceNotFoundException;
import fr.techcrud.pmt_api.models.Permission;
import fr.techcrud.pmt_api.models.User;
import fr.techcrud.pmt_api.security.annotations.RequiresAllPermissions;
import fr.techcrud.pmt_api.security.annotations.RequiresAnyPermission;
import fr.techcrud.pmt_api.security.annotations.RequiresPermission;
import fr.techcrud.pmt_api.security.annotations.RequiresRole;
import fr.techcrud.pmt_api.services.PermissionVerificationService;
import fr.techcrud.pmt_api.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class PermissionAspectTest {

	@MockitoBean
	private PermissionVerificationService permissionVerificationService;

	@MockitoBean
	private UserService userService;

	@Autowired
	private TestService testService;

	private User testUser;
	private UUID userId;

	@TestConfiguration
	static class TestConfig {
		@Bean
		public TestService testService() {
			return new TestService();
		}
	}

	@Component
	static class TestService {
		@RequiresPermission("USER:READ")
		public String methodWithPermission() {
			return "success";
		}

		@RequiresRole("ADMIN")
		public String methodWithRole() {
			return "success";
		}

		@RequiresAnyPermission({"USER:READ", "USER:WRITE"})
		public String methodWithAnyPermission() {
			return "success";
		}

		@RequiresAllPermissions({"USER:READ", "USER:WRITE"})
		public String methodWithAllPermissions() {
			return "success";
		}
	}

	@BeforeEach
	void setUp() {
		userId = UUID.randomUUID();
		testUser = new User();
		testUser.setId(userId);
		testUser.setEmail("test@example.com");
		testUser.setLastName("Lastname");
		testUser.setFirstName("testuser");

		// Setup Spring Security context
		UsernamePasswordAuthenticationToken authentication =
			new UsernamePasswordAuthenticationToken("test@example.com", null, Collections.emptyList());
		SecurityContextHolder.getContext().setAuthentication(authentication);

		when(userService.findByEmail("test@example.com")).thenReturn(testUser);
	}

	@Test
	void whenUserHasPermission_thenProceed() {
		when(permissionVerificationService.exists(any(Permission.class))).thenReturn(true);
		when(permissionVerificationService.hasPermission(userId, "USER:READ")).thenReturn(true);

		String result = testService.methodWithPermission();

		assertThat(result).isEqualTo("success");
	}

	@Test
	void whenUserDoesNotHavePermission_thenThrowPermissionDeniedException() {
		when(permissionVerificationService.hasPermission(userId, "USER:READ")).thenReturn(false);

		assertThatThrownBy(() -> testService.methodWithPermission())
				.isInstanceOf(PermissionDeniedException.class)
				.hasMessageContaining("USER:READ");
	}

	@Test
	void whenUserNotFound_thenThrowRessourceNotFoundException() {
		when(userService.findByEmail("test@example.com")).thenReturn(null);

		assertThatThrownBy(() -> testService.methodWithPermission())
				.isInstanceOf(RessourceNotFoundException.class)
				.hasMessageContaining("Authenticated user not found");
	}

	@Test
	void whenUserHasRole_thenProceed() {
		when(permissionVerificationService.hasRole(userId, "ADMIN")).thenReturn(true);

		String result = testService.methodWithRole();

		assertThat(result).isEqualTo("success");
	}

	@Test
	void whenUserDoesNotHaveRole_thenThrowPermissionDeniedException() {
		when(permissionVerificationService.hasRole(userId, "ADMIN")).thenReturn(false);

		assertThatThrownBy(() -> testService.methodWithRole())
				.isInstanceOf(PermissionDeniedException.class)
				.hasMessageContaining("ADMIN");
	}

	@Test
	void whenUserHasAnyPermission_thenProceed() {
		String[] permissions = {"USER:READ", "USER:WRITE"};
		when(permissionVerificationService.hasAnyPermission(userId, permissions)).thenReturn(true);

		String result = testService.methodWithAnyPermission();

		assertThat(result).isEqualTo("success");
	}

	@Test
	void whenUserDoesNotHaveAnyPermission_thenThrowPermissionDeniedException() {
		String[] permissions = {"USER:READ", "USER:WRITE"};
		when(permissionVerificationService.hasAnyPermission(userId, permissions)).thenReturn(false);

		assertThatThrownBy(() -> testService.methodWithAnyPermission())
				.isInstanceOf(PermissionDeniedException.class);
	}

	@Test
	void whenUserHasAllPermissions_thenProceed() {
		String[] permissions = {"USER:READ", "USER:WRITE"};
		when(permissionVerificationService.hasAllPermissions(userId, permissions)).thenReturn(true);

		String result = testService.methodWithAllPermissions();

		assertThat(result).isEqualTo("success");
	}

	@Test
	void whenUserDoesNotHaveAllPermissions_thenThrowPermissionDeniedException() {
		String[] permissions = {"USER:READ", "USER:WRITE"};
		when(permissionVerificationService.hasAllPermissions(userId, permissions)).thenReturn(false);

		assertThatThrownBy(() -> testService.methodWithAllPermissions())
				.isInstanceOf(PermissionDeniedException.class);
	}
}
