package tech.derbent.users.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.domains.CEntityConstants;
import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.companies.domain.CCompany;

@Entity
@Table (name = "cuser") // table name for the entity as the default is the class name in
// lowercase
@AttributeOverride (name = "id", column = @Column (name = "user_id"))
public class CUser extends CEntityNamed<CUser> {

	public static final int MAX_LENGTH_NAME = 255;

	public static String getIconColorCode() {
		return "#6f42c1"; // Purple color for user entities
	}

	public static String getIconFilename() { return "vaadin:users"; }

	@Column (
		name = "lastname", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME,
		unique = false
	)
	@MetaData (
		displayName = "Last Name", required = true, readOnly = false, defaultValue = "",
		description = "User's last name", hidden = false, order = 2,
		maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	private String lastname;

	@MetaData (
		displayName = "Login", required = true, readOnly = false, defaultValue = "",
		description = "Login name for the system", hidden = false, order = 3,
		maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	@Column (
		name = "login", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME,
		unique = true
	)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	private String login;

	@MetaData (
		displayName = "Email", required = true, readOnly = false, defaultValue = "",
		description = "User's email address", hidden = false, order = 4,
		maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	@Column (
		name = "email", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME,
		unique = false
	)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	private String email;

	@MetaData (
		displayName = "Phone", required = false, readOnly = false, defaultValue = "",
		description = "Phone number", hidden = false, order = 5,
		maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	@Column (
		name = "phone", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME,
		unique = false
	)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	private String phone;

	@Column (name = "roles", nullable = false, length = 255)
	@Size (max = 255)
	@MetaData (
		displayName = "Roles", required = true, readOnly = false, defaultValue = "USER",
		description = "User roles (comma-separated)", hidden = false, order = 6,
		maxLength = 255
	)
	private String roles = "USER";

	@Enumerated (EnumType.STRING)
	@Column (name = "user_role", nullable = false, length = 50)
	@MetaData (
		displayName = "User Role", required = true, readOnly = false,
		defaultValue = "TEAM_MEMBER", description = "Primary user role in the system",
		hidden = false, order = 7, maxLength = 50
	)
	private CUserRole userRole = CUserRole.TEAM_MEMBER;

	@Column (name = "password", nullable = true, length = 255)
	@Size (max = 255)
	@MetaData (
		displayName = "Password", required = false, readOnly = false,
		description = "User password (stored as hash)", hidden = true, order = 99
	)
	private String password; // Encoded password

	@MetaData (
		displayName = "Enabled", required = true, readOnly = false, defaultValue = "true",
		description = "Is user account enabled?", hidden = false, order = 8
	)
	@Column (name = "enabled", nullable = false)
	private Boolean enabled = Boolean.TRUE; // User account status, default is enabled

	@MetaData (
		displayName = "Profile Picture", required = false, readOnly = false,
		defaultValue = "", description = "User's profile picture stored as binary data",
		hidden = false, order = 11
	)
	@Column (name = "profile_picture_data", nullable = true, length = 10000)
	private byte[] profilePictureData;

	@OneToMany (mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CUserProjectSettings> projectSettings = new ArrayList<>();

	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "cusertype_id", nullable = true)
	@MetaData (
		displayName = "User Type", required = false, readOnly = false,
		description = "Type category of the user", hidden = false, order = 9
	)
	private CUserType userType;

	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "company_id", nullable = true)
	@MetaData (
		displayName = "Company", required = false, readOnly = false,
		description = "Company the user belongs to", hidden = false, order = 10
	)
	private CCompany company;

	/**
	 * Default constructor for JPA.
	 */
	public CUser() {
		super();
		// Initialize with default values for JPA
		this.roles = "USER";
		this.userRole = CUserRole.TEAM_MEMBER;
		this.enabled = true;
	}

	public CUser(final String name) {
		super(CUser.class, name);
	}

	public CUser(final String username, final String password, final String name,
		final String email) {
		super(CUser.class, name);
		this.login = username;
		this.email = email;
		this.setPassword(password);
	}

	/**
	 * Constructor with user role enum.
	 */
	public CUser(final String username, final String password, final String name,
		final String email, final CUserRole userRole) {
		super(CUser.class, name);
		this.login = username;
		this.email = email;
		this.setPassword(password);
		this.userRole = userRole != null ? userRole : CUserRole.TEAM_MEMBER;
		this.setRoles(this.userRole.name()); // Keep roles string in sync
	}

	public CUser(final String username, final String password, final String name,
		final String email, final String roles) {
		super(CUser.class, name);
		this.login = username;
		super.setName(name);
		this.email = email;
		this.setPassword(password);
		this.setRoles(roles);
		// Set userRole based on roles string for backward compatibility
		this.userRole = parseUserRoleFromRoles(roles);
	}

	@Override
	public boolean equals(final Object o) {
		// Use the superclass (CEntityNamed -> CEntityDB) equals method which properly
		// handles ID-based equality and proxy classes. This is the standard approach for
		// JPA entities.
		return super.equals(o);
	}

	public CCompany getCompany() { return company; }

	public String getEmail() { return email; }

	public String getLastname() { return lastname; }

	public String getLogin() { return login; }

	@Override
	public String getName() { return super.getName(); }

	public String getPassword() {
		return password; // Return the encoded password
	}

	public String getPhone() { return phone; }

	public byte[] getProfilePictureData() { return profilePictureData; }

	// Getter and setter
	public List<CUserProjectSettings> getProjectSettings() { return projectSettings; }

	public String getRoles() { return roles; }

	public String getUsername() {
		return getLogin(); // Convenience method to get username for authentication
	}

	public CUserRole getUserRole() { return userRole; }

	public CUserType getUserType() { return userType; }

	public Boolean isEnabled() {
		return enabled; // Return the enabled status
	}

	/**
	 * Parses user role from legacy roles string.
	 * @param roles comma-separated roles string
	 * @return corresponding CUserRole enum value
	 */
	private CUserRole parseUserRoleFromRoles(final String roles) {

		if ((roles == null) || roles.trim().isEmpty()) {
			return CUserRole.TEAM_MEMBER;
		}
		final String upperRoles = roles.toUpperCase();

		if (upperRoles.contains("ADMIN")) {
			return CUserRole.ADMIN;
		}

		if (upperRoles.contains("PROJECT_MANAGER") || upperRoles.contains("MANAGER")) {
			return CUserRole.PROJECT_MANAGER;
		}

		if (upperRoles.contains("GUEST")) {
			return CUserRole.GUEST;
		}
		return CUserRole.TEAM_MEMBER;
	}

	public void setCompany(final CCompany company) { this.company = company; }

	public void setEmail(final String email) { this.email = email; }

	public void setEnabled(final Boolean enabled) {
		this.enabled = enabled; // Set the enabled status
	}

	public void setLastname(final String lastname) { this.lastname = lastname; }

	public void setLogin(final String login) { this.login = login; }

	@Override
	public void setName(final String name) {
		super.setName(name);
	}

	public void setPassword(final String password) {
		// Password should be encoded before setting
		this.password = password; // Assuming password is already encoded
	}

	public void setPhone(final String phone) { this.phone = phone; }

	public void setProfilePictureData(final byte[] profilePictureData) {
		this.profilePictureData = profilePictureData;
	}

	public void setProjectSettings(final List<CUserProjectSettings> projectSettings) {
		this.projectSettings = projectSettings;
	}

	public void setRoles(final String roles) {
		this.roles = roles != null ? roles : "USER";
	}

	public void setUserRole(final CUserRole userRole) {
		this.userRole = userRole != null ? userRole : CUserRole.TEAM_MEMBER;
		// Keep roles string in sync for backward compatibility
		this.setRoles(this.userRole.name());
	}

	public void setUserType(final CUserType userType) { this.userType = userType; }

	/**
	 * Returns a comprehensive string representation of the user including all key fields.
	 * Note: This method is used for debugging and logging purposes. For ComboBox display
	 * in the UI, the CEntityFormBuilder now uses getName() method automatically to show
	 * only the user's name instead of all fields. This resolves the combobox display
	 * issue where users were listed with complete text with all fields.
	 * @return detailed string representation of the user
	 */
	@Override
	public String toString() {

		// Return user-friendly representation for UI display
		if ((getName() != null) && !getName().trim().isEmpty()) {

			if ((lastname != null) && !lastname.trim().isEmpty()) {
				return getName() + " " + lastname;
			}
			return getName();
		}

		if ((login != null) && !login.trim().isEmpty()) {
			return login;
		}
		return "User #" + getId();
	}
}