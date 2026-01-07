package fr.techcrud.pmt_api.services;

import fr.techcrud.pmt_api.models.Role;
import fr.techcrud.pmt_api.models.User;
import fr.techcrud.pmt_api.models.UserRole;
import fr.techcrud.pmt_api.repositories.RoleRepository;
import fr.techcrud.pmt_api.repositories.UserRepository;
import fr.techcrud.pmt_api.repositories.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class userRoleServiceImplTest {

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionAuditService permissionAuditService;

    @Mock
    private PermissionVerificationService permissionVerificationService;

    @InjectMocks
    private UserRoleServiceImpl userRoleService;

    private UUID userId;
    private UUID roleId;
    private UUID assignedById;
    private User testUser;
    private User assignedByUser;
    private Role testRole;
    private UserRole testUserRole;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        roleId = UUID.randomUUID();
        assignedById = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("testuser");

        assignedByUser = new User();
        assignedByUser.setId(assignedById);
        assignedByUser.setEmail("admin@example.com");
        assignedByUser.setFirstName("admin");

        testRole = new Role();
        testRole.setId(roleId);
        testRole.setName("TEST_ROLE");
        testRole.setActive(true);

        testUserRole = new UserRole();
        testUserRole.setId(UUID.randomUUID());
        testUserRole.setUser(testUser);
        testUserRole.setRole(testRole);
        testUserRole.setAssignedBy(assignedByUser);
    }

    @Test
    void whenFindByUserId_thenReturnUserRoles() {
        List<UserRole> userRoles = Arrays.asList(testUserRole);
        when(userRoleRepository.findByUserIdWithRole(userId)).thenReturn(userRoles);

        List<UserRole> result = userRoleService.findByUserId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getId()).isEqualTo(userId);
        verify(userRoleRepository).findByUserIdWithRole(userId);
    }

    @Test
    void whenFindByRoleId_thenReturnUsersWithRole() {
        List<UserRole> userRoles = Arrays.asList(testUserRole);
        when(userRoleRepository.findByRoleId(roleId)).thenReturn(userRoles);

        List<UserRole> result = userRoleService.findByRoleId(roleId);

        assertThat(result).hasSize(1);
        verify(userRoleRepository).findByRoleId(roleId);
    }

    @Test
    void whenAssignRoleToUser_andNotExists_thenCreateAssignment() {
        when(userRoleRepository.existsByUserIdAndRoleId(userId, roleId)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(testRole));
        when(userRepository.findById(assignedById)).thenReturn(Optional.of(assignedByUser));
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(testUserRole);
        doNothing().when(permissionAuditService).logRoleAssignment(any(), any(), any(), any());
        doNothing().when(permissionVerificationService).clearUserPermissionCache(userId);

        UserRole result = userRoleService.assignRoleToUser(userId, roleId, assignedById, "Initial assignment");

        assertThat(result).isNotNull();
        verify(userRoleRepository).save(any(UserRole.class));
        verify(permissionAuditService).logRoleAssignment(testUser, testRole, assignedByUser, "Initial assignment");
        verify(permissionVerificationService).clearUserPermissionCache(userId);
    }

    @Test
    void whenAssignRoleToUser_andAlreadyExists_thenReturnNull() {
        when(userRoleRepository.existsByUserIdAndRoleId(userId, roleId)).thenReturn(true);

        UserRole result = userRoleService.assignRoleToUser(userId, roleId, assignedById, "Duplicate");

        assertThat(result).isNull();
        verify(userRoleRepository, never()).save(any(UserRole.class));
    }

    @Test
    void whenRevokeRoleFromUser_thenDeleteAssignment() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(testRole));
        when(userRepository.findById(assignedById)).thenReturn(Optional.of(assignedByUser));
        doNothing().when(userRoleRepository).deleteByUserIdAndRoleId(userId, roleId);
        doNothing().when(permissionAuditService).logRoleRevocation(any(), any(), any(), any());
        doNothing().when(permissionVerificationService).clearUserPermissionCache(userId);

        userRoleService.revokeRoleFromUser(userId, roleId, assignedById, "No longer needed");

        verify(userRoleRepository).deleteByUserIdAndRoleId(userId, roleId);
        verify(permissionAuditService).logRoleRevocation(testUser, testRole, assignedByUser, "No longer needed");
        verify(permissionVerificationService).clearUserPermissionCache(userId);
    }
}
