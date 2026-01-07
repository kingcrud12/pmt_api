package fr.techcrud.pmt_api.dto;

public class PermissionCheckResponseDto {
    private Boolean hasPermission;
    private String permission;
    private String userId;

    // Getters and Setters
    public Boolean getHasPermission() {
        return hasPermission;
    }

    public void setHasPermission(Boolean hasPermission) {
        this.hasPermission = hasPermission;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
