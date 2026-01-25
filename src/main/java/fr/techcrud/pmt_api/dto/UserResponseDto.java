package fr.techcrud.pmt_api.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;


@JsonPropertyOrder({ "id", "firstName", "lastName", "email", "role" })
public class UserResponseDto {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private boolean status;
    private String message;

    // getters et setters
    public boolean getStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
