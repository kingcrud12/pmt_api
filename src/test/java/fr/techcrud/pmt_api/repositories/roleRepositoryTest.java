package fr.techcrud.pmt_api.repositories;

import fr.techcrud.pmt_api.models.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class roleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setName("TEST_ROLE");
        testRole.setDescription("Test role for unit tests");
        testRole.setActive(true);
    }

    @Test
    void whenSaveRole_thenRoleIsPersisted() {
        Role savedRole = roleRepository.save(testRole);

        assertThat(savedRole.getId()).isNotNull();
        assertThat(savedRole.getName()).isEqualTo("TEST_ROLE");
        assertThat(savedRole.getDescription()).isEqualTo("Test role for unit tests");
        assertThat(savedRole.getActive()).isTrue();
        assertThat(savedRole.getCreatedAt()).isNotNull();
    }

    @Test
    void whenFindByName_thenReturnRole() {
        entityManager.persistAndFlush(testRole);

        Optional<Role> found = roleRepository.findByName("TEST_ROLE");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("TEST_ROLE");
    }

    @Test
    void whenFindByNameNotExists_thenReturnEmpty() {
        Optional<Role> found = roleRepository.findByName("NON_EXISTENT");

        assertThat(found).isEmpty();
    }

    @Test
    void whenFindByActiveTrue_thenReturnOnlyActiveRoles() {
        Role activeRole = new Role();
        activeRole.setName("ACTIVE_ROLE");
        activeRole.setDescription("Active role");
        activeRole.setActive(true);
        entityManager.persistAndFlush(activeRole);

        Role inactiveRole = new Role();
        inactiveRole.setName("INACTIVE_ROLE");
        inactiveRole.setDescription("Inactive role");
        inactiveRole.setActive(false);
        entityManager.persistAndFlush(inactiveRole);

        List<Role> activeRoles = roleRepository.findByActiveTrue();

        assertThat(activeRoles).hasSize(1);
        assertThat(activeRoles.get(0).getName()).isEqualTo("ACTIVE_ROLE");
    }

    @Test
    void whenExistsByName_thenReturnTrue() {
        entityManager.persistAndFlush(testRole);

        boolean exists = roleRepository.existsByName("TEST_ROLE");

        assertThat(exists).isTrue();
    }

    @Test
    void whenExistsByNameNotExists_thenReturnFalse() {
        boolean exists = roleRepository.existsByName("NON_EXISTENT");

        assertThat(exists).isFalse();
    }

    @Test
    void whenFindAllActiveRoles_thenReturnActiveRoles() {
        Role activeRole1 = new Role();
        activeRole1.setName("ACTIVE_1");
        activeRole1.setDescription("Active 1");
        activeRole1.setActive(true);
        entityManager.persistAndFlush(activeRole1);

        Role activeRole2 = new Role();
        activeRole2.setName("ACTIVE_2");
        activeRole2.setDescription("Active 2");
        activeRole2.setActive(true);
        entityManager.persistAndFlush(activeRole2);

        Role inactiveRole = new Role();
        inactiveRole.setName("INACTIVE");
        inactiveRole.setDescription("Inactive");
        inactiveRole.setActive(false);
        entityManager.persistAndFlush(inactiveRole);

        List<Role> activeRoles = roleRepository.findAllActiveRoles();

        assertThat(activeRoles).hasSize(2);
        assertThat(activeRoles).extracting(Role::getName)
                .containsExactlyInAnyOrder("ACTIVE_1", "ACTIVE_2");
    }
}
