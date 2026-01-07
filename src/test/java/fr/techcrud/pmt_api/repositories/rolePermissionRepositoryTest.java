package fr.techcrud.pmt_api.repositories;

import fr.techcrud.pmt_api.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class rolePermissionRepositoryTest {

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Role testRole;
    private Permission testPermission;
    private RolePermission testRolePermission;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setName("TEST_ROLE");
        testRole.setDescription("Test role");
        testRole.setActive(true);
        testRole = entityManager.persistAndFlush(testRole);

        testPermission = new Permission();
        testPermission.setResource("USER");
        testPermission.setAction("READ");
        testPermission.setDescription("Read user");
        testPermission.setActive(true);
        testPermission = entityManager.persistAndFlush(testPermission);

        testRolePermission = new RolePermission();
        testRolePermission.setRole(testRole);
        testRolePermission.setPermission(testPermission);
    }

    @Test
    void whenSaveRolePermission_thenRolePermissionIsPersisted() {
        RolePermission saved = rolePermissionRepository.save(testRolePermission);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getRole().getId()).isEqualTo(testRole.getId());
        assertThat(saved.getPermission().getId()).isEqualTo(testPermission.getId());
        assertThat(saved.getAssignedAt()).isNotNull();
    }

    @Test
    void whenFindByRoleId_thenReturnRolePermissions() {
        entityManager.persistAndFlush(testRolePermission);

        Permission anotherPermission = new Permission();
        anotherPermission.setResource("USER");
        anotherPermission.setAction("WRITE");
        anotherPermission.setDescription("Write user");
        anotherPermission.setActive(true);
        anotherPermission = entityManager.persistAndFlush(anotherPermission);

        RolePermission anotherRolePermission = new RolePermission();
        anotherRolePermission.setRole(testRole);
        anotherRolePermission.setPermission(anotherPermission);
        entityManager.persistAndFlush(anotherRolePermission);

        List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleId(testRole.getId());

        assertThat(rolePermissions).hasSize(2);
    }

    @Test
    void whenFindByRoleIdAndPermissionId_thenReturnRolePermission() {
        entityManager.persistAndFlush(testRolePermission);

        Optional<RolePermission> found = rolePermissionRepository.findByRoleIdAndPermissionId(
                testRole.getId(), testPermission.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getRole().getId()).isEqualTo(testRole.getId());
        assertThat(found.get().getPermission().getId()).isEqualTo(testPermission.getId());
    }

    @Test
    void whenExistsByRoleIdAndPermissionId_thenReturnTrue() {
        entityManager.persistAndFlush(testRolePermission);

        boolean exists = rolePermissionRepository.existsByRoleIdAndPermissionId(
                testRole.getId(), testPermission.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void whenFindPermissionsByUserId_thenReturnUserPermissions() {
        // Create user
        User testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setFirstName("testuser");
        testUser.setPassword("password");
        testUser.setRole("USER");
        testUser = entityManager.persistAndFlush(testUser);

        // Assign role to user
        UserRole userRole = new UserRole();
        userRole.setUser(testUser);
        userRole.setRole(testRole);
        userRole.setAssignedBy(testUser);
        entityManager.persistAndFlush(userRole);

        // Assign permission to role
        entityManager.persistAndFlush(testRolePermission);

        // Add another permission
        Permission anotherPermission = new Permission();
        anotherPermission.setResource("PROJECT");
        anotherPermission.setAction("READ");
        anotherPermission.setDescription("Read project");
        anotherPermission.setActive(true);
        anotherPermission = entityManager.persistAndFlush(anotherPermission);

        RolePermission anotherRolePermission = new RolePermission();
        anotherRolePermission.setRole(testRole);
        anotherRolePermission.setPermission(anotherPermission);
        entityManager.persistAndFlush(anotherRolePermission);

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
        testUser.setFirstName("testuser");
        testUser.setPassword("password");
        testUser.setRole("USER");
        testUser = entityManager.persistAndFlush(testUser);

        // Assign role to user
        UserRole userRole = new UserRole();
        userRole.setUser(testUser);
        userRole.setRole(testRole);
        userRole.setAssignedBy(testUser);
        entityManager.persistAndFlush(userRole);

        // Assign active permission
        entityManager.persistAndFlush(testRolePermission);

        // Assign inactive permission
        Permission inactivePermission = new Permission();
        inactivePermission.setResource("PROJECT");
        inactivePermission.setAction("DELETE");
        inactivePermission.setDescription("Delete project");
        inactivePermission.setActive(false);
        inactivePermission = entityManager.persistAndFlush(inactivePermission);

        RolePermission inactiveRolePermission = new RolePermission();
        inactiveRolePermission.setRole(testRole);
        inactiveRolePermission.setPermission(inactivePermission);
        entityManager.persistAndFlush(inactiveRolePermission);

        List<Permission> permissions = rolePermissionRepository.findPermissionsByUserId(testUser.getId());

        assertThat(permissions).hasSize(1);
        assertThat(permissions.get(0).getPermissionString()).isEqualTo("USER:READ");
    }
}
