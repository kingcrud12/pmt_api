package fr.techcrud.pmt_api.integration;

import fr.techcrud.pmt_api.models.*;
import fr.techcrud.pmt_api.repositories.*;
import fr.techcrud.pmt_api.services.PermissionVerificationService;
import fr.techcrud.pmt_api.services.UserRoleService;
import fr.techcrud.pmt_api.services.RolePermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for complete RBAC flow:
 * 1. Create user, role, and permission
 * 2. Grant permission to role
 * 3. Assign role to user
 * 4. Verify user has permission
 * 5. Cache is working correctly
 * 6. Revoke role from user
 * 7. Verify user no longer has permission
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RBACIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private RolePermissionService rolePermissionService;

    @Autowired
    private PermissionVerificationService permissionVerificationService;

    private User testUser;
    private User adminUser;
    private Role testRole;
    private Permission readPermission;
    private Permission writePermission;

    @BeforeEach
    void setUp() {
        // Create admin user
        adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setFirstName("admin");
        adminUser.setPassword("password");
        adminUser.setRole("ADMIN");
        adminUser = userRepository.save(adminUser);

        // Create test user
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setFirstName("testuser");
        testUser.setPassword("password");
        testUser.setRole("USER");
        testUser = userRepository.save(testUser);

        // Create role
        testRole = new Role();
        testRole.setName("PROJECT_MANAGER");
        testRole.setDescription("Project Manager role");
        testRole.setActive(true);
        testRole = roleRepository.save(testRole);

        // Create permissions
        readPermission = new Permission();
        readPermission.setResource("PROJECT");
        readPermission.setAction("READ");
        readPermission.setDescription("Read projects");
        readPermission.setActive(true);
        readPermission = permissionRepository.save(readPermission);

        writePermission = new Permission();
        writePermission.setResource("PROJECT");
        writePermission.setAction("WRITE");
        writePermission.setDescription("Write projects");
        writePermission.setActive(true);
        writePermission = permissionRepository.save(writePermission);
    }

    @Test
    void testCompleteRBACFlow() {
        // Step 1: Initially user has no permissions
        boolean hasReadPermission = permissionVerificationService.hasPermission(
                testUser.getId(), "PROJECT:READ");
        assertThat(hasReadPermission).isFalse();

        // Step 2: Grant permissions to role
        RolePermission rolePermission1 = rolePermissionService.grantPermissionToRole(
                testRole.getId(), readPermission.getId());
        assertThat(rolePermission1).isNotNull();

        RolePermission rolePermission2 = rolePermissionService.grantPermissionToRole(
                testRole.getId(), writePermission.getId());
        assertThat(rolePermission2).isNotNull();

        // Step 3: Assign role to user
        UserRole userRole = userRoleService.assignRoleToUser(
                testUser.getId(), testRole.getId(), adminUser.getId(), "Promoted to PM");
        assertThat(userRole).isNotNull();
        assertThat(userRole.getUser().getId()).isEqualTo(testUser.getId());
        assertThat(userRole.getRole().getId()).isEqualTo(testRole.getId());

        // Step 4: Verify user now has permissions
        hasReadPermission = permissionVerificationService.hasPermission(
                testUser.getId(), "PROJECT:READ");
        assertThat(hasReadPermission).isTrue();

        boolean hasWritePermission = permissionVerificationService.hasPermission(
                testUser.getId(), "PROJECT:WRITE");
        assertThat(hasWritePermission).isTrue();

        // Step 5: Test hasAllPermissions
        boolean hasAllPermissions = permissionVerificationService.hasAllPermissions(
                testUser.getId(), new String[]{"PROJECT:READ", "PROJECT:WRITE"});
        assertThat(hasAllPermissions).isTrue();

        // Step 6: Test hasAnyPermission
        boolean hasAnyPermission = permissionVerificationService.hasAnyPermission(
                testUser.getId(), new String[]{"PROJECT:READ", "PROJECT:DELETE"});
        assertThat(hasAnyPermission).isTrue();

        // Step 7: User does not have DELETE permission
        boolean hasDeletePermission = permissionVerificationService.hasPermission(
                testUser.getId(), "PROJECT:DELETE");
        assertThat(hasDeletePermission).isFalse();

        // Step 8: Get all user permissions
        List<Permission> userPermissions = permissionVerificationService.getUserPermissions(testUser.getId());
        assertThat(userPermissions).hasSize(2);
        assertThat(userPermissions).extracting(Permission::getPermissionString)
                .containsExactlyInAnyOrder("PROJECT:READ", "PROJECT:WRITE");

        // Step 9: Revoke role from user
        userRoleService.revokeRoleFromUser(
                testUser.getId(), testRole.getId(), adminUser.getId(), "Demotion");

        // Step 10: Verify user no longer has permissions
        hasReadPermission = permissionVerificationService.hasPermission(
                testUser.getId(), "PROJECT:READ");
        assertThat(hasReadPermission).isFalse();

        hasWritePermission = permissionVerificationService.hasPermission(
                testUser.getId(), "PROJECT:WRITE");
        assertThat(hasWritePermission).isFalse();
    }

    @Test
    void testMultipleRolesForUser() {
        // Create another role
        Role secondRole = new Role();
        secondRole.setName("TEAM_LEAD");
        secondRole.setDescription("Team Lead role");
        secondRole.setActive(true);
        secondRole = roleRepository.save(secondRole);

        // Create another permission
        Permission deletePermission = new Permission();
        deletePermission.setResource("TASK");
        deletePermission.setAction("DELETE");
        deletePermission.setDescription("Delete tasks");
        deletePermission.setActive(true);
        deletePermission = permissionRepository.save(deletePermission);

        // Grant read permission to first role
        rolePermissionService.grantPermissionToRole(testRole.getId(), readPermission.getId());

        // Grant delete permission to second role
        rolePermissionService.grantPermissionToRole(secondRole.getId(), deletePermission.getId());

        // Assign both roles to user
        userRoleService.assignRoleToUser(
                testUser.getId(), testRole.getId(), adminUser.getId(), "First role");
        userRoleService.assignRoleToUser(
                testUser.getId(), secondRole.getId(), adminUser.getId(), "Second role");

        // Verify user has permissions from both roles
        boolean hasReadPermission = permissionVerificationService.hasPermission(
                testUser.getId(), "PROJECT:READ");
        assertThat(hasReadPermission).isTrue();

        boolean hasDeletePermission = permissionVerificationService.hasPermission(
                testUser.getId(), "TASK:DELETE");
        assertThat(hasDeletePermission).isTrue();

        List<Permission> allPermissions = permissionVerificationService.getUserPermissions(testUser.getId());
        assertThat(allPermissions).hasSize(2);
    }

    @Test
    void testInactivePermissionsAreNotGranted() {
        // Grant permission to role
        rolePermissionService.grantPermissionToRole(testRole.getId(), readPermission.getId());

        // Assign role to user
        userRoleService.assignRoleToUser(
                testUser.getId(), testRole.getId(), adminUser.getId(), "Initial assignment");

        // Verify user has permission
        boolean hasPermission = permissionVerificationService.hasPermission(
                testUser.getId(), "PROJECT:READ");
        assertThat(hasPermission).isTrue();

        // Deactivate permission
        readPermission.setActive(false);
        permissionRepository.save(readPermission);

        // Clear cache to force reload
        permissionVerificationService.clearUserPermissionCache(testUser.getId());

        // Verify user no longer has permission (because it's inactive)
        hasPermission = permissionVerificationService.hasPermission(
                testUser.getId(), "PROJECT:READ");
        assertThat(hasPermission).isFalse();
    }

    @Test
    void testInactiveRolesAreNotConsidered() {
        // Grant permission to role
        rolePermissionService.grantPermissionToRole(testRole.getId(), readPermission.getId());

        // Assign role to user
        userRoleService.assignRoleToUser(
                testUser.getId(), testRole.getId(), adminUser.getId(), "Initial assignment");

        // Verify user has permission
        boolean hasPermission = permissionVerificationService.hasPermission(
                testUser.getId(), "PROJECT:READ");
        assertThat(hasPermission).isTrue();

        // Deactivate role
        testRole.setActive(false);
        roleRepository.save(testRole);

        // Clear cache to force reload
        permissionVerificationService.clearUserPermissionCache(testUser.getId());

        // Verify user no longer has permission (because role is inactive)
        hasPermission = permissionVerificationService.hasPermission(
                testUser.getId(), "PROJECT:READ");
        assertThat(hasPermission).isFalse();
    }

    @Test
    void testDuplicateRoleAssignmentPrevented() {
        // Assign role to user
        UserRole userRole1 = userRoleService.assignRoleToUser(
                testUser.getId(), testRole.getId(), adminUser.getId(), "First assignment");
        assertThat(userRole1).isNotNull();

        // Try to assign same role again
        UserRole userRole2 = userRoleService.assignRoleToUser(
                testUser.getId(), testRole.getId(), adminUser.getId(), "Duplicate assignment");
        assertThat(userRole2).isNull();
    }

    @Test
    void testDuplicatePermissionGrantPrevented() {
        // Grant permission to role
        RolePermission rolePermission1 = rolePermissionService.grantPermissionToRole(
                testRole.getId(), readPermission.getId());
        assertThat(rolePermission1).isNotNull();

        // Try to grant same permission again
        RolePermission rolePermission2 = rolePermissionService.grantPermissionToRole(
                testRole.getId(), readPermission.getId());
        assertThat(rolePermission2).isNull();
    }
}
