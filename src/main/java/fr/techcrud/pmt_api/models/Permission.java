package fr.techcrud.pmt_api.models;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
@Table(name = "permission", uniqueConstraints = {
		@UniqueConstraint(name = "unique_permission", columnNames = { "resource", "action" })
})
public class Permission {

	@Id
	@GeneratedValue(generator = "UUID")
	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "id", updatable = false, nullable = false, columnDefinition = "CHAR(36)")
	private UUID id;

	@Column(name = "resource", nullable = false, length = 50)
	private String resource;

	@Column(name = "action", nullable = false, length = 50)
	private String action;

	@Column(name = "description", length = 255)
	private String description;

	@Column(name = "active", nullable = false)
	private Boolean active = true;

	public Permission() {
	}

	// Getters and Setters
	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

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

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	/**
	 * Get formatted permission string (RESOURCE:ACTION)
	 */
	public String getPermissionString() {
		return resource + ":" + action;
	}

	private static final Pattern PERMISSION_PATTERN = Pattern.compile("^([A-Z_]+):([A-Z_]+)$");

	public boolean setPermissionString(String permissionString) {
		if (permissionString == null)
			return false;
		Matcher matcher = PERMISSION_PATTERN.matcher(permissionString);
		if (!matcher.matches())
			return (false);
		this.resource = matcher.group(1);
		this.action = matcher.group(2);
		return true;
	}
}
