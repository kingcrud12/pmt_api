package fr.techcrud.pmt_api.controllers;

import fr.techcrud.pmt_api.models.Role;
import fr.techcrud.pmt_api.services.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoleController.class)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private RoleService roleService;

    private Role testRole;
    private UUID testRoleId;

    @BeforeEach
    void setUp() {
        testRoleId = UUID.randomUUID();
        testRole = new Role();
        testRole.setId(testRoleId);
        testRole.setName("TEST_ROLE");
        testRole.setDescription("Test role description");
        testRole.setActive(true);
    }

    @Test
    @WithMockUser
    void whenGetAllRoles_thenReturnRolesList() throws Exception {
        Role anotherRole = new Role();
        anotherRole.setId(UUID.randomUUID());
        anotherRole.setName("ANOTHER_ROLE");
        anotherRole.setActive(true);

        List<Role> roles = Arrays.asList(testRole, anotherRole);
        when(roleService.findAll()).thenReturn(roles);

        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("TEST_ROLE"))
                .andExpect(jsonPath("$[1].name").value("ANOTHER_ROLE"));
    }

    @Test
    @WithMockUser
    void whenGetActiveRoles_thenReturnOnlyActiveRoles() throws Exception {
        List<Role> activeRoles = Arrays.asList(testRole);
        when(roleService.findAllActive()).thenReturn(activeRoles);

        mockMvc.perform(get("/api/v1/roles/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    @WithMockUser
    void whenGetRoleById_thenReturnRole() throws Exception {
        when(roleService.findById(testRoleId)).thenReturn(testRole);

        mockMvc.perform(get("/api/v1/roles/" + testRoleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testRoleId.toString()))
                .andExpect(jsonPath("$.name").value("TEST_ROLE"));
    }

    @Test
    @WithMockUser
    void whenGetRoleByIdNotFound_thenReturn404() throws Exception {
        when(roleService.findById(testRoleId)).thenReturn(null);

        mockMvc.perform(get("/api/v1/roles/" + testRoleId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void whenGetRoleByName_thenReturnRole() throws Exception {
        when(roleService.findByName("TEST_ROLE")).thenReturn(testRole);

        mockMvc.perform(get("/api/v1/roles/name/TEST_ROLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("TEST_ROLE"));
    }

    @Test
    @WithMockUser
    void whenCreateRole_thenReturnCreatedRole() throws Exception {
        when(roleService.create(any(Role.class))).thenReturn(testRole);

        String roleJson = """
                {
                    "name": "TEST_ROLE",
                    "description": "Test role description"
                }
                """;

        mockMvc.perform(post("/api/v1/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("TEST_ROLE"));
    }

    @Test
    @WithMockUser
    void whenCreateRoleAlreadyExists_thenReturn400() throws Exception {
        when(roleService.create(any(Role.class))).thenReturn(null);

        String roleJson = """
                {
                    "name": "EXISTING_ROLE",
                    "description": "Duplicate role"
                }
                """;

        mockMvc.perform(post("/api/v1/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void whenUpdateRole_thenReturnUpdatedRole() throws Exception {
        Role updatedRole = new Role();
        updatedRole.setId(testRoleId);
        updatedRole.setName("TEST_ROLE");
        updatedRole.setDescription("Updated description");
        updatedRole.setActive(true);

        when(roleService.update(any(UUID.class), any(Role.class))).thenReturn(updatedRole);

        String updateJson = """
                {
                    "description": "Updated description",
                    "active": true
                }
                """;

        mockMvc.perform(put("/api/v1/roles/" + testRoleId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    @WithMockUser
    void whenDeleteRole_thenReturn200() throws Exception {
        when(roleService.findById(testRoleId)).thenReturn(testRole);

        mockMvc.perform(delete("/api/v1/roles/" + testRoleId)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void whenActivateRole_thenReturn200() throws Exception {
        mockMvc.perform(put("/api/v1/roles/" + testRoleId + "/activate")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void whenDeactivateRole_thenReturn200() throws Exception {
        mockMvc.perform(put("/api/v1/roles/" + testRoleId + "/deactivate")
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}
