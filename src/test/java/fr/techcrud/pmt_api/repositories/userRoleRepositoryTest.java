package fr.techcrud.pmt_api.repositories;

import fr.techcrud.pmt_api.models.Role;
import fr.techcrud.pmt_api.models.User;
import fr.techcrud.pmt_api.models.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class userRoleRepositoryTest {

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;
    private Role testRole;
    private UserRole testUserRole;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setFirstName("testuser");
        testUser.setPassword("password123");
        testUser.setRole("USER");
        testUser = entityManager.persistAndFlush(testUser);

        testRole = new Role();
        testRole.setName("TEST_ROLE");
        testRole.setDescription("Test role");
        testRole.setActive(true);
        testRole = entityManager.persistAndFlush(testRole);

        testUserRole = new UserRole();
        testUserRole.setUser(testUser);
        testUserRole.setRole(testRole);
        testUserRole.setAssignedBy(testUser);
    }

    @Test
    void whenSaveUserRole_thenUserRoleIsPersisted() {
        UserRole saved = userRoleRepository.save(testUserRole);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUser().getId()).isEqualTo(testUser.getId());
        assertThat(saved.getRole().getId()).isEqualTo(testRole.getId());
        assertThat(saved.getAssignedAt()).isNotNull();
    }

    @Test
    void whenFindByUserId_thenReturnUserRoles() {
        entityManager.persistAndFlush(testUserRole);

        List<UserRole> userRoles = userRoleRepository.findByUserId(testUser.getId());

        assertThat(userRoles).hasSize(1);
        assertThat(userRoles.get(0).getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void whenFindByRoleId_thenReturnUsersWithRole() {
        entityManager.persistAndFlush(testUserRole);

        User anotherUser = new User();
        anotherUser.setEmail("another@example.com");
        anotherUser.setFirstName("another");
        anotherUser.setPassword("password");
        anotherUser.setRole("USER");
        anotherUser = entityManager.persistAndFlush(anotherUser);

        UserRole anotherUserRole = new UserRole();
        anotherUserRole.setUser(anotherUser);
        anotherUserRole.setRole(testRole);
        anotherUserRole.setAssignedBy(testUser);
        entityManager.persistAndFlush(anotherUserRole);

        List<UserRole> usersWithRole = userRoleRepository.findByRoleId(testRole.getId());

        assertThat(usersWithRole).hasSize(2);
    }

    @Test
    void whenFindByUserIdAndRoleId_thenReturnUserRole() {
        entityManager.persistAndFlush(testUserRole);

        Optional<UserRole> found = userRoleRepository.findByUserIdAndRoleId(
                testUser.getId(), testRole.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getUser().getId()).isEqualTo(testUser.getId());
        assertThat(found.get().getRole().getId()).isEqualTo(testRole.getId());
    }

    @Test
    void whenExistsByUserIdAndRoleId_thenReturnTrue() {
        entityManager.persistAndFlush(testUserRole);

        boolean exists = userRoleRepository.existsByUserIdAndRoleId(
                testUser.getId(), testRole.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void whenFindByUserIdWithRole_thenReturnUserRolesWithRoleData() {
        entityManager.persistAndFlush(testUserRole);

        List<UserRole> userRoles = userRoleRepository.findByUserIdWithRole(testUser.getId());

        assertThat(userRoles).hasSize(1);
        assertThat(userRoles.get(0).getRole().getName()).isEqualTo("TEST_ROLE");
    }
}
