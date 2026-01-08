package fr.techcrud.pmt_api.services;

import fr.techcrud.pmt_api.models.UserRole;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface UserRoleService {

    List<UserRole> findByUserId(UUID userId);

    List<UserRole> findByRoleId(UUID roleId);

    UserRole assignRoleToUser(UUID userId, UUID roleId, UUID assignedById, String reason);

    void revokeRoleFromUser(UUID userId, UUID roleId, UUID revokedById, String reason);

    boolean userHasRole(UUID userId, UUID roleId);
}
