package tech.derbent.users.domain;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.domains.CEntityDB;

@Entity
@Table(name = "cuser") // table name for the entity as the default is the class name in lowercase
public class CUser extends CEntityDB {

	public static final int MAX_LENGTH_NAME = 255; // Define maximum length for name fields
	@Column(name = "name", nullable = false, length = MAX_LENGTH_NAME, unique = false)
	@Size(max = MAX_LENGTH_NAME)
	@MetaData(displayName = "User Name", required = true, readOnly = false, defaultValue = "-", description = "User's first name", hidden = false)
	private String name;
	@Column(name = "lastname", nullable = true, length = MAX_LENGTH_NAME, unique = false)
	@MetaData(displayName = "Last Name", required = true, readOnly = false, defaultValue = "-", description = "User's lastname", hidden = false)
	@Size(max = MAX_LENGTH_NAME)
	private String lastname;
	@MetaData(displayName = "Login", required = true, readOnly = false, defaultValue = "-", description = "Login name for the system", hidden = false)
	@Column(name = "login", nullable = true, length = MAX_LENGTH_NAME, unique = true)
	@Size(max = MAX_LENGTH_NAME)
	private String login;
	@MetaData(displayName = "Email", required = true, readOnly = false, defaultValue = "", description = "", hidden = false)
	@Column(name = "email", nullable = true, length = MAX_LENGTH_NAME, unique = false)
	@Size(max = MAX_LENGTH_NAME)
	private String email;
	@MetaData(displayName = "Phone", required = true, readOnly = false, defaultValue = "-", description = "Phone number", hidden = false)
	@Column(name = "phone", nullable = true, length = MAX_LENGTH_NAME, unique = false)
	@Size(max = MAX_LENGTH_NAME)
	private String phone;
	/**
	 * User roles for authorization (e.g., "USER", "ADMIN"). Comma-separated string
	 * of roles for simplicity. Used by Spring Security for access control.
	 */
	@Column(name = "roles", nullable = false, length = 255)
	@Size(max = 255)
	@MetaData(displayName = "Roles", required = true, readOnly = false, defaultValue = "USER", description = "User roles (comma-separated)", hidden = false)
	private String roles = "USER";
	/**
	 * Password field for authentication. Stored as encoded hash (never plain text).
	 * Uses BCrypt encoding for security.
	 */
	@Column(name = "password", nullable = false, length = 255)
	@Size(max = 255)
	@MetaData(displayName = "Password", required = false, readOnly = false, description = "User password (stored as hash)", hidden = true)
	private String password; // Encoded password
	@MetaData(displayName = "Enabled", required = true, readOnly = false, defaultValue = "true", description = "Is user account enabled?", hidden = false)
	@Column(name = "enabled", nullable = false)
	private boolean enabled = true; // User account status, default is enabled
	@Column(name = "created_date", nullable = true)
	private LocalDateTime created_date;
	@Column(name = "updated_date", nullable = true)
	private LocalDateTime updated_date;
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CUserProjectSettings> projectSettings;

	public CUser() {
		super();
	}

	public CUser(final String username, final String password, final String name, final String email) {
		super();
		this.login = username;
		this.name = name;
		this.email = email;
		this.setPassword(password);
	}

	public CUser(final String username, final String password, final String name, final String email, final String roles) {
		super();
		this.login = username;
		this.name = name;
		this.email = email;
		this.setPassword(password);
		this.setRoles(roles);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CUser)) {
			return false;
		}
		final CUser cUser = (CUser) o;
		return (enabled == cUser.enabled) && name.equals(cUser.name) && lastname.equals(cUser.lastname) && login.equals(cUser.login) && email.equals(cUser.email) && phone.equals(cUser.phone)
			&& roles.equals(cUser.roles);
	}

	public String getEmail() { return email; }

	public String getLastname() { return lastname; }

	public String getLogin() { return login; }

	public String getName() { return name; }

	public String getPassword() {
		return password; // Return the encoded password
	}

	public String getPhone() { return phone; }

	// Getter and setter
	public List<CUserProjectSettings> getProjectSettings() { return projectSettings; }

	public String getRoles() { return roles; }

	public String getUsername() {
		return getLogin(); // Convenience method to get username for authentication
	}

	public boolean isEnabled() {
		return enabled; // Return the enabled status
	}

	public void setEmail(final String email) { this.email = email; }

	public void setEnabled(final boolean enabled) {
		this.enabled = enabled; // Set the enabled status
	}

	public void setLastname(final String lastname) { this.lastname = lastname; }

	public void setLogin(final String login) { this.login = login; }

	public void setName(final String name) { this.name = name; }

	public void setPassword(final String password) {
		// Password should be encoded before setting
		this.password = password; // Assuming password is already encoded
	}

	public void setPhone(final String phone) { this.phone = phone; }

	public void setProjectSettings(final List<CUserProjectSettings> projectSettings) { this.projectSettings = projectSettings; }

	public void setRoles(final String roles) { this.roles = roles != null ? roles : "USER"; }

	@Override
	public String toString() {
		return "CUser{" + "name='" + name + '\'' + ", lastname='" + lastname + '\'' + ", login='" + login + '\'' + ", email='" + email + '\'' + ", phone='" + phone + '\'' + ", roles='" + roles + '\''
			+ ", enabled=" + enabled + '}';
	}
}
