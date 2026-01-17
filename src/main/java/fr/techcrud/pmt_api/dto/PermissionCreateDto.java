package fr.techcrud.pmt_api.dto;

public class PermissionCreateDto {
    private String resource;
    private String action;
    private String description;

    // Getters and Setters
    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
