package fr.techcrud.pmt_api.repositories;

import fr.techcrud.pmt_api.models.Permission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class permissionRepositoryTest {

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Permission testPermission;

    @BeforeEach
    void setUp() {
        testPermission = new Permission();
        testPermission.setResource("USER");
        testPermission.setAction("READ");
        testPermission.setDescription("Read user data");
        testPermission.setActive(true);
    }

    @Test
    void whenSavePermission_thenPermissionIsPersisted() {
        Permission saved = permissionRepository.save(testPermission);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getResource()).isEqualTo("USER");
        assertThat(saved.getAction()).isEqualTo("READ");
        assertThat(saved.getPermissionString()).isEqualTo("USER:READ");
        assertThat(saved.getActive()).isTrue();
    }

    @Test
    void whenFindByResourceAndAction_thenReturnPermission() {
        entityManager.persistAndFlush(testPermission);

        Optional<Permission> found = permissionRepository.findByResourceAndAction("USER", "READ");

        assertThat(found).isPresent();
        assertThat(found.get().getPermissionString()).isEqualTo("USER:READ");
    }

    @Test
    void whenFindByResourceAndActionNotExists_thenReturnEmpty() {
        Optional<Permission> found = permissionRepository.findByResourceAndAction("USER", "DELETE");

        assertThat(found).isEmpty();
    }

    @Test
    void whenFindByResource_thenReturnAllPermissionsForResource() {
        Permission readPerm = new Permission();
        readPerm.setResource("PROJECT");
        readPerm.setAction("READ");
        readPerm.setDescription("Read project");
        readPerm.setActive(true);
        entityManager.persistAndFlush(readPerm);

        Permission writePerm = new Permission();
        writePerm.setResource("PROJECT");
        writePerm.setAction("WRITE");
        writePerm.setDescription("Write project");
        writePerm.setActive(true);
        entityManager.persistAndFlush(writePerm);

        Permission userPerm = new Permission();
        userPerm.setResource("USER");
        userPerm.setAction("READ");
        userPerm.setDescription("Read user");
        userPerm.setActive(true);
        entityManager.persistAndFlush(userPerm);

        List<Permission> projectPermissions = permissionRepository.findByResource("PROJECT");

        assertThat(projectPermissions).hasSize(2);
        assertThat(projectPermissions).extracting(Permission::getResource)
                .containsOnly("PROJECT");
    }

    @Test
    void whenFindByActiveTrue_thenReturnOnlyActivePermissions() {
        Permission activePerm = new Permission();
        activePerm.setResource("TASK");
        activePerm.setAction("CREATE");
        activePerm.setDescription("Create task");
        activePerm.setActive(true);
        entityManager.persistAndFlush(activePerm);

        Permission inactivePerm = new Permission();
        inactivePerm.setResource("TASK");
        inactivePerm.setAction("DELETE");
        inactivePerm.setDescription("Delete task");
        inactivePerm.setActive(false);
        entityManager.persistAndFlush(inactivePerm);

        List<Permission> activePermissions = permissionRepository.findByActiveTrue();

        assertThat(activePermissions).hasSize(1);
        assertThat(activePermissions.get(0).getAction()).isEqualTo("CREATE");
    }

    @Test
    void whenExistsByResourceAndAction_thenReturnTrue() {
        entityManager.persistAndFlush(testPermission);

        boolean exists = permissionRepository.existsByResourceAndAction("USER", "READ");

        assertThat(exists).isTrue();
    }

    @Test
    void whenExistsByResourceAndActionNotExists_thenReturnFalse() {
        boolean exists = permissionRepository.existsByResourceAndAction("USER", "ADMIN");

        assertThat(exists).isFalse();
    }
}
