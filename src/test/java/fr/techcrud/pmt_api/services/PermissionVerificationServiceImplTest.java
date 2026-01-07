package fr.techcrud.pmt_api.services;

import fr.techcrud.pmt_api.models.Permission;
import fr.techcrud.pmt_api.repositories.RolePermissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionVerificationServiceImplTest {

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @Mock
    private CacheManager cacheManager;

    @InjectMocks
    private PermissionVerificationServiceImpl permissionVerificationService;

    private UUID testUserId;
    private Permission userReadPermission;
    private Permission userWritePermission;
    private Permission projectAdminPermission;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        userReadPermission = new Permission();
        userReadPermission.setId(UUID.randomUUID());
        userReadPermission.setResource("USER");
        userReadPermission.setAction("READ");
        userReadPermission.setActive(true);

        userWritePermission = new Permission();
        userWritePermission.setId(UUID.randomUUID());
        userWritePermission.setResource("USER");
        userWritePermission.setAction("WRITE");
        userWritePermission.setActive(true);

        projectAdminPermission = new Permission();
        projectAdminPermission.setId(UUID.randomUUID());
        projectAdminPermission.setResource("PROJECT");
        projectAdminPermission.setAction("ADMIN");
        projectAdminPermission.setActive(true);
    }

    @Test
    void whenUserHasPermission_thenReturnTrue() {
        List<Permission> permissions = Arrays.asList(userReadPermission, userWritePermission);
        when(rolePermissionRepository.findPermissionsByUserId(testUserId)).thenReturn(permissions);

        boolean hasPermission = permissionVerificationService.hasPermission(testUserId, "USER:READ");

        assertThat(hasPermission).isTrue();
        verify(rolePermissionRepository).findPermissionsByUserId(testUserId);
    }

    @Test
    void whenUserDoesNotHavePermission_thenReturnFalse() {
        List<Permission> permissions = Collections.singletonList(userReadPermission);
        when(rolePermissionRepository.findPermissionsByUserId(testUserId)).thenReturn(permissions);

        boolean hasPermission = permissionVerificationService.hasPermission(testUserId, "PROJECT:ADMIN");

        assertThat(hasPermission).isFalse();
        verify(rolePermissionRepository).findPermissionsByUserId(testUserId);
    }

    @Test
    void whenUserHasNoPermissions_thenReturnFalse() {
        when(rolePermissionRepository.findPermissionsByUserId(testUserId)).thenReturn(Collections.emptyList());

        boolean hasPermission = permissionVerificationService.hasPermission(testUserId, "USER:READ");

        assertThat(hasPermission).isFalse();
    }

    @Test
    void whenCheckPermissionCaseInsensitive_thenReturnTrue() {
        List<Permission> permissions = Collections.singletonList(userReadPermission);
        when(rolePermissionRepository.findPermissionsByUserId(testUserId)).thenReturn(permissions);

        boolean hasPermission = permissionVerificationService.hasPermission(testUserId, "user:read");

        assertThat(hasPermission).isTrue();
    }

    @Test
    void whenHasAnyPermission_withOneMatch_thenReturnTrue() {
        List<Permission> permissions = Collections.singletonList(userReadPermission);
        when(rolePermissionRepository.findPermissionsByUserId(testUserId)).thenReturn(permissions);

        String[] requiredPermissions = {"USER:READ", "USER:WRITE", "PROJECT:ADMIN"};
        boolean hasAnyPermission = permissionVerificationService.hasAnyPermission(testUserId, requiredPermissions);

        assertThat(hasAnyPermission).isTrue();
    }

    @Test
    void whenHasAnyPermission_withNoMatch_thenReturnFalse() {
        List<Permission> permissions = Collections.singletonList(userReadPermission);
        when(rolePermissionRepository.findPermissionsByUserId(testUserId)).thenReturn(permissions);

        String[] requiredPermissions = {"USER:DELETE", "PROJECT:ADMIN"};
        boolean hasAnyPermission = permissionVerificationService.hasAnyPermission(testUserId, requiredPermissions);

        assertThat(hasAnyPermission).isFalse();
    }

    @Test
    void whenHasAllPermissions_withAllMatch_thenReturnTrue() {
        List<Permission> permissions = Arrays.asList(userReadPermission, userWritePermission, projectAdminPermission);
        when(rolePermissionRepository.findPermissionsByUserId(testUserId)).thenReturn(permissions);

        String[] requiredPermissions = {"USER:READ", "USER:WRITE"};
        boolean hasAllPermissions = permissionVerificationService.hasAllPermissions(testUserId, requiredPermissions);

        assertThat(hasAllPermissions).isTrue();
    }

    @Test
    void whenHasAllPermissions_withPartialMatch_thenReturnFalse() {
        List<Permission> permissions = Collections.singletonList(userReadPermission);
        when(rolePermissionRepository.findPermissionsByUserId(testUserId)).thenReturn(permissions);

        String[] requiredPermissions = {"USER:READ", "USER:WRITE"};
        boolean hasAllPermissions = permissionVerificationService.hasAllPermissions(testUserId, requiredPermissions);

        assertThat(hasAllPermissions).isFalse();
    }

    @Test
    void whenGetUserPermissions_thenReturnPermissionList() {
        List<Permission> permissions = Arrays.asList(userReadPermission, userWritePermission);
        when(rolePermissionRepository.findPermissionsByUserId(testUserId)).thenReturn(permissions);

        List<Permission> result = permissionVerificationService.getUserPermissions(testUserId);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(userReadPermission, userWritePermission);
    }
}
