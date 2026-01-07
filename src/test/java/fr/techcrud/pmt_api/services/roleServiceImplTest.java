package fr.techcrud.pmt_api.services;

import fr.techcrud.pmt_api.models.Role;
import fr.techcrud.pmt_api.repositories.RoleRepository;
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
class roleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role testRole;
    private UUID testRoleId;

    @BeforeEach
    void setUp() {
        testRoleId = UUID.randomUUID();
        testRole = new Role();
        testRole.setId(testRoleId);
        testRole.setName("TEST_ROLE");
        testRole.setDescription("Test role");
        testRole.setActive(true);
    }

    @Test
    void whenFindAll_thenReturnAllRoles() {
        Role anotherRole = new Role();
        anotherRole.setName("ANOTHER_ROLE");
        List<Role> roles = Arrays.asList(testRole, anotherRole);
        when(roleRepository.findAll()).thenReturn(roles);

        List<Role> result = roleService.findAll();

        assertThat(result).hasSize(2);
        verify(roleRepository).findAll();
    }

    @Test
    void whenFindAllActive_thenReturnOnlyActiveRoles() {
        List<Role> activeRoles = Arrays.asList(testRole);
        when(roleRepository.findAllActiveRoles()).thenReturn(activeRoles);

        List<Role> result = roleService.findAllActive();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getActive()).isTrue();
        verify(roleRepository).findAllActiveRoles();
    }

    @Test
    void whenFindById_thenReturnRole() {
        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));

        Role result = roleService.findById(testRoleId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testRoleId);
        verify(roleRepository).findById(testRoleId);
    }

    @Test
    void whenFindByIdNotExists_thenReturnNull() {
        when(roleRepository.findById(testRoleId)).thenReturn(Optional.empty());

        Role result = roleService.findById(testRoleId);

        assertThat(result).isNull();
    }

    @Test
    void whenFindByName_thenReturnRole() {
        when(roleRepository.findByName("TEST_ROLE")).thenReturn(Optional.of(testRole));

        Role result = roleService.findByName("TEST_ROLE");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("TEST_ROLE");
    }

    @Test
    void whenCreateRoleNotExists_thenSaveRole() {
        when(roleRepository.existsByName("NEW_ROLE")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        Role newRole = new Role();
        newRole.setName("NEW_ROLE");
        newRole.setDescription("New role");

        Role result = roleService.create(newRole);

        assertThat(result).isNotNull();
        verify(roleRepository).existsByName("NEW_ROLE");
        verify(roleRepository).save(newRole);
    }

    @Test
    void whenCreateRoleAlreadyExists_thenReturnNull() {
        when(roleRepository.existsByName("TEST_ROLE")).thenReturn(true);

        Role newRole = new Role();
        newRole.setName("TEST_ROLE");

        Role result = roleService.create(newRole);

        assertThat(result).isNull();
        verify(roleRepository).existsByName("TEST_ROLE");
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void whenUpdateRole_thenUpdateFields() {
        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        Role updateData = new Role();
        updateData.setDescription("Updated description");
        updateData.setActive(false);

        Role result = roleService.update(testRoleId, updateData);

        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Updated description");
        assertThat(result.getActive()).isFalse();
        verify(roleRepository).save(testRole);
    }

    @Test
    void whenUpdateRoleNotExists_thenReturnNull() {
        when(roleRepository.findById(testRoleId)).thenReturn(Optional.empty());

        Role updateData = new Role();
        updateData.setDescription("Updated");

        Role result = roleService.update(testRoleId, updateData);

        assertThat(result).isNull();
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void whenDelete_thenDeleteRole() {
        doNothing().when(roleRepository).deleteById(testRoleId);

        roleService.delete(testRoleId);

        verify(roleRepository).deleteById(testRoleId);
    }

    @Test
    void whenActivate_thenSetActiveTrue() {
        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        testRole.setActive(false);
        roleService.activate(testRoleId);

        verify(roleRepository).save(testRole);
        assertThat(testRole.getActive()).isTrue();
    }

    @Test
    void whenDeactivate_thenSetActiveFalse() {
        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        testRole.setActive(true);
        roleService.deactivate(testRoleId);

        verify(roleRepository).save(testRole);
        assertThat(testRole.getActive()).isFalse();
    }
}
