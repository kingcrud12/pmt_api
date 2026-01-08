package fr.techcrud.pmt_api.repositories;

import fr.techcrud.pmt_api.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
	"spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;NON_KEYWORDS=USER",
	"spring.jpa.hibernate.ddl-auto=create-drop",
	"spring.flyway.enabled=false"
})
@ActiveProfiles("test")
@Transactional
class RolePermissionRepositoryTest {

	@Autowired
	private RolePermissionRepository rolePermissionRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PermissionRepository permissionRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserRoleRepository userRoleRepository;

	private Role testRole;
	private Permission testPermission;
	private RolePermission testRolePermission;

	@BeforeEach
	void setUp() {
		rolePermissionRepository.deleteAllInBatch();
		userRoleRepository.deleteAllInBatch();
		roleRepository.deleteAllInBatch();
		permissionRepository.deleteAllInBatch();
		userRepository.deleteAllInBatch();
		testRole = new Role();
		testRole.setName("TEST_ROLE");
		testRole.setDescription("Test role");
		testRole.setActive(true);
		testRole = roleRepository.saveAndFlush(testRole);

		testPermission = new Permission();
		testPermission.setResource("USER");
		testPermission.setAction("READ");
		testPermission.setDescription("Read user");
		testPermission.setActive(true);
		testPermission = permissionRepository.saveAndFlush(testPermission);

		testRolePermission = new RolePermission();
		testRolePermission.setRole(testRole);
		testRolePermission.setPermission(testPermission);
	}

	@Test
	void whenSaveRolePermission_thenRolePermissionIsPersisted() {
		RolePermission saved = rolePermissionRepository.save(testRolePermission);

		assertThat(saved.getId()).isNotNull().describedAs("RolePermission id is not null.");
		assertThat(saved.getRole().getId()).isEqualTo(testRole.getId());
		assertThat(saved.getPermission().getId()).isEqualTo(testPermission.getId());
		assertThat(saved.getAssignedAt()).isNotNull().describedAs("Role is assigned to non null.");
	}

	@Test
	void whenFindByRoleId_thenReturnRolePermissions() {
		rolePermissionRepository.saveAndFlush(testRolePermission);

		Permission anotherPermission = new Permission();
		anotherPermission.setResource("USER");
		anotherPermission.setAction("WRITE");
		anotherPermission.setDescription("Write user");
		anotherPermission.setActive(true);
		anotherPermission = permissionRepository.saveAndFlush(anotherPermission);

		RolePermission anotherRolePermission = new RolePermission();
		anotherRolePermission.setRole(testRole);
		anotherRolePermission.setPermission(anotherPermission);
		rolePermissionRepository.saveAndFlush(anotherRolePermission);

		List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleId(testRole.getId());

		assertThat(rolePermissions).hasSize(2);
	}

	@Test
	void whenFindByRoleIdAndPermissionId_thenReturnRolePermission() {
		rolePermissionRepository.saveAndFlush(testRolePermission);

		Optional<RolePermission> found = rolePermissionRepository.findByRoleIdAndPermissionId(
				testRole.getId(), testPermission.getId());

		assertThat(found).isPresent();
		assertThat(found.get().getRole().getId()).isEqualTo(testRole.getId());
		assertThat(found.get().getPermission().getId()).isEqualTo(testPermission.getId());
	}

	@Test
	void whenExistsByRoleIdAndPermissionId_thenReturnTrue() {
		rolePermissionRepository.saveAndFlush(testRolePermission);

		boolean exists = rolePermissionRepository.existsByRoleIdAndPermissionId(
				testRole.getId(), testPermission.getId());

		assertThat(exists).isTrue();
	}

	@Test
	void whenFindPermissionsByUserId_thenReturnUserPermissions() {
		// Create user
		User testUser = new User();
		testUser.setEmail("test@example.com");
		testUser.setLastName("Lastname");
		testUser.setFirstName("testuser");
		testUser.setPassword("password");
		testUser.setRole("USER");
		testUser = userRepository.saveAndFlush(testUser);

		// Assign role to user
		UserRole userRole = new UserRole();
		userRole.setUser(testUser);
		userRole.setRole(testRole);
		userRole.setAssignedBy(testUser);
		userRoleRepository.saveAndFlush(userRole);

		// Assign permission to role
		rolePermissionRepository.saveAndFlush(testRolePermission);

		// Add another permission
		Permission anotherPermission = new Permission();
		anotherPermission.setResource("PROJECT");
		anotherPermission.setAction("READ");
		anotherPermission.setDescription("Read project");
		anotherPermission.setActive(true);
		anotherPermission = permissionRepository.saveAndFlush(anotherPermission);

		RolePermission anotherRolePermission = new RolePermission();
		anotherRolePermission.setRole(testRole);
		anotherRolePermission.setPermission(anotherPermission);
		rolePermissionRepository.saveAndFlush(anotherRolePermission);

		List<Permission> permissions = rolePermissionRepository.findPermissionsByUserId(testUser.getId());

		assertThat(permissions).hasSize(2);
		assertThat(permissions).extracting(Permission::getPermissionString)
				.containsExactlyInAnyOrder("USER:READ", "PROJECT:READ");
	}

	@Test
	void whenFindPermissionsByUserIdWithInactivePermission_thenReturnOnlyActivePermissions() {
		// Create user
		User testUser = new User();
		testUser.setEmail("test@example.com");
		testUser.setLastName("Lastname");
		testUser.setFirstName("testuser");
		testUser.setPassword("password");
		testUser.setRole("USER");
		testUser = userRepository.saveAndFlush(testUser);

		// Assign role to user
		UserRole userRole = new UserRole();
		userRole.setUser(testUser);
		userRole.setRole(testRole);
		userRole.setAssignedBy(testUser);
		userRole = userRoleRepository.saveAndFlush(userRole);

		// Assign active permission
		rolePermissionRepository.saveAndFlush(testRolePermission);

		// Assign inactive permission
		Permission inactivePermission = new Permission();
		inactivePermission.setResource("PROJECT");
		inactivePermission.setAction("DELETE");
		inactivePermission.setDescription("Delete project");
		inactivePermission.setActive(false);
		inactivePermission = permissionRepository.saveAndFlush(inactivePermission);

		RolePermission inactiveRolePermission = new RolePermission();
		inactiveRolePermission.setRole(testRole);
		inactiveRolePermission.setPermission(inactivePermission);
		inactiveRolePermission = rolePermissionRepository.saveAndFlush(inactiveRolePermission);

		List<Permission> permissions = rolePermissionRepository.findPermissionsByUserId(testUser.getId());

		assertThat(permissions).hasSize(1);
		assertThat(permissions.get(0).getPermissionString()).isEqualTo("USER:READ");
	}
}
