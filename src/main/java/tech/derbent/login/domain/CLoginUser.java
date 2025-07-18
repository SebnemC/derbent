package tech.derbent.login.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.users.domain.CUser;

/**
 * CLoginUser extends CUser to add authentication-specific fields.
 * This class represents a user that can authenticate in the system.
 * 
 * Authentication Flow:
 * 1. User enters username/password in CLoginView
 * 2. Spring Security calls CLoginUserService to load user by username
 * 3. CLoginUserService queries database using CLoginUserRepository
 * 4. Password is verified against stored encoded password
 * 5. User roles determine access permissions
 */
@Entity
@DiscriminatorValue("LOGIN_USER")
public class CLoginUser extends CUser {

	/**
	 * Password field for authentication. 
	 * Stored as encoded hash (never plain text).
	 * Uses BCrypt encoding for security.
	 */
	@Column(name = "password", nullable = false, length = 255)
	@Size(max = 255)
	@MetaData(displayName = "Password", required = true, readOnly = false, 
			  description = "User password (stored as hash)", hidden = true)
	private String password;

	/**
	 * User roles for authorization (e.g., "USER", "ADMIN").
	 * Comma-separated string of roles for simplicity.
	 * Used by Spring Security for access control.
	 */
	@Column(name = "roles", nullable = false, length = 255)
	@Size(max = 255)
	@MetaData(displayName = "Roles", required = true, readOnly = false, 
			  defaultValue = "USER", description = "User roles (comma-separated)", hidden = false)
	private String roles = "USER";

	/**
	 * Flag to enable/disable user account.
	 * Disabled users cannot authenticate.
	 */
	@Column(name = "enabled", nullable = false)
	@MetaData(displayName = "Enabled", required = true, readOnly = false, 
			  defaultValue = "true", description = "Whether user account is enabled", hidden = false)
	private boolean enabled = true;

	/**
	 * Default constructor required by JPA.
	 */
	public CLoginUser() {
		super();
	}

	/**
	 * Constructor for creating new login user with basic info.
	 * Password should be encoded before setting.
	 * 
	 * @param username the login username
	 * @param password the encoded password
	 * @param name the user's first name
	 * @param email the user's email
	 */
	public CLoginUser(String username, String password, String name, String email) {
		super();
		setLogin(username); // Use inherited login field as username
		this.password = password;
		setName(name);
		setEmail(email);
	}

	// Getters and Setters

	/**
	 * @return the encoded password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password. Should be encoded before calling this method.
	 * 
	 * @param password the encoded password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return comma-separated string of user roles
	 */
	public String getRoles() {
		return roles;
	}

	/**
	 * Sets user roles as comma-separated string.
	 * 
	 * @param roles the roles (e.g., "USER,ADMIN")
	 */
	public void setRoles(String roles) {
		this.roles = roles;
	}

	/**
	 * @return true if user account is enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets whether user account is enabled.
	 * 
	 * @param enabled true to enable account, false to disable
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Convenience method to get username for authentication.
	 * Uses the inherited login field from CUser.
	 * 
	 * @return the username (login field)
	 */
	public String getUsername() {
		return getLogin();
	}

	/**
	 * Convenience method to set username.
	 * Sets the inherited login field from CUser.
	 * 
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		setLogin(username);
	}
}