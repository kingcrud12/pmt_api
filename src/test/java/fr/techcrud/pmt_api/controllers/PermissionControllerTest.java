package fr.techcrud.pmt_api.controllers;

import fr.techcrud.pmt_api.models.Permission;
import fr.techcrud.pmt_api.services.PermissionService;
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

@WebMvcTest(PermissionController.class)
class PermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private PermissionService permissionService;

    private Permission testPermission;
    private UUID testPermissionId;

    @BeforeEach
    void setUp() {
        testPermissionId = UUID.randomUUID();
        testPermission = new Permission();
        testPermission.setId(testPermissionId);
        testPermission.setResource("USER");
        testPermission.setAction("READ");
        testPermission.setDescription("Read user data");
        testPermission.setActive(true);
    }

    @Test
    @WithMockUser
    void whenGetAllPermissions_thenReturnPermissionsList() throws Exception {
        Permission anotherPermission = new Permission();
        anotherPermission.setId(UUID.randomUUID());
        anotherPermission.setResource("PROJECT");
        anotherPermission.setAction("WRITE");
        anotherPermission.setActive(true);

        List<Permission> permissions = Arrays.asList(testPermission, anotherPermission);
        when(permissionService.findAll()).thenReturn(permissions);

        mockMvc.perform(get("/api/v1/permissions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].permissionString").value("USER:READ"))
                .andExpect(jsonPath("$[1].permissionString").value("PROJECT:WRITE"));
    }

    @Test
    @WithMockUser
    void whenGetActivePermissions_thenReturnOnlyActivePermissions() throws Exception {
        List<Permission> activePermissions = Arrays.asList(testPermission);
        when(permissionService.findAllActive()).thenReturn(activePermissions);

        mockMvc.perform(get("/api/v1/permissions/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    @WithMockUser
    void whenGetPermissionById_thenReturnPermission() throws Exception {
        when(permissionService.findById(testPermissionId)).thenReturn(testPermission);

        mockMvc.perform(get("/api/v1/permissions/" + testPermissionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testPermissionId.toString()))
                .andExpect(jsonPath("$.resource").value("USER"));
    }

    @Test
    @WithMockUser
    void whenGetPermissionByIdNotFound_thenReturn404() throws Exception {
        when(permissionService.findById(testPermissionId)).thenReturn(null);

        mockMvc.perform(get("/api/v1/permissions/" + testPermissionId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void whenGetPermissionsByResource_thenReturnResourcePermissions() throws Exception {
        Permission readPermission = new Permission();
        readPermission.setResource("USER");
        readPermission.setAction("READ");
        readPermission.setActive(true);

        Permission writePermission = new Permission();
        writePermission.setResource("USER");
        writePermission.setAction("WRITE");
        writePermission.setActive(true);

        List<Permission> userPermissions = Arrays.asList(readPermission, writePermission);
        when(permissionService.findByResource("USER")).thenReturn(userPermissions);

        mockMvc.perform(get("/api/v1/permissions/resource/USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resource").value("USER"))
                .andExpect(jsonPath("$[1].resource").value("USER"));
    }

    @Test
    @WithMockUser
    void whenCreatePermission_thenReturnCreatedPermission() throws Exception {
        when(permissionService.create(any(Permission.class))).thenReturn(testPermission);

        String permissionJson = """
                {
                    "resource": "user",
                    "action": "read",
                    "description": "Read user data"
                }
                """;

        mockMvc.perform(post("/api/v1/permissions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(permissionJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resource").value("USER"))
                .andExpect(jsonPath("$.action").value("READ"));
    }

    @Test
    @WithMockUser
    void whenCreatePermissionAlreadyExists_thenReturn400() throws Exception {
        when(permissionService.create(any(Permission.class))).thenReturn(null);

        String permissionJson = """
                {
                    "resource": "USER",
                    "action": "READ",
                    "description": "Duplicate permission"
                }
                """;

        mockMvc.perform(post("/api/v1/permissions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(permissionJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void whenDeletePermission_thenReturn200() throws Exception {
        when(permissionService.findById(testPermissionId)).thenReturn(testPermission);

        mockMvc.perform(delete("/api/v1/permissions/" + testPermissionId)
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}
