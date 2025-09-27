package tech.derbent.users.domain;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.domains.CEntityNamed;
import tech.derbent.api.interfaces.CFieldInfoGenerator;
import tech.derbent.api.interfaces.CSearchable;
import tech.derbent.api.utils.Check;
import tech.derbent.companies.domain.CCompany;

@Entity
@Table (name = "cuser") // Using quoted identifier to ensure exact case matching in
// PostgreSQL
@AttributeOverride (name = "id", column = @Column (name = "user_id"))
public class CUser extends CEntityNamed<CUser> implements CSearchable, CFieldInfoGenerator {

	public static final String DEFAULT_COLOR = "#00546d";
	public static final String DEFAULT_ICON = "vaadin:book";
	public static final int MAX_LENGTH_NAME = 255;
	public static final String VIEW_NAME = "Users View";
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "company_id", nullable = true)
	@AMetaData (displayName = "Company", required = false, readOnly = false, description = "Company the user belongs to", hidden = false, order = 10)
	private CCompany company;
	@AMetaData (
			displayName = "Email", required = true, readOnly = false, defaultValue = "", description = "User's email address", hidden = false,
			order = 4, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	@Column (name = "email", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME, unique = false)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	private String email;
	@AMetaData (
			displayName = "Enabled", required = true, readOnly = false, defaultValue = "true", description = "Is user account enabled?",
			hidden = false, order = 8
	)
	@Column (name = "enabled", nullable = false)
	private Boolean enabled = Boolean.TRUE; // User account status, default is enabled
	@Column (name = "lastname", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME, unique = false)
	@AMetaData (
			displayName = "Last Name", required = true, readOnly = false, defaultValue = "", description = "User's last name", hidden = false,
			order = 2, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	private String lastname;
	@AMetaData (
			displayName = "Login", required = true, readOnly = false, defaultValue = "", description = "Login name for the system", hidden = false,
			order = 3, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	@Column (name = "login", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME, unique = true)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	private String login;
	@Column (name = "password", nullable = true, length = 255)
	@Size (max = 255)
	@AMetaData (
			displayName = "Password", required = false, readOnly = false, passwordField = true, description = "User password (stored as hash)",
			hidden = false, order = 99, passwordRevealButton = false
	)
	private String password; // Encoded password
	@AMetaData (
			displayName = "Phone", required = false, readOnly = false, defaultValue = "", description = "Phone number", hidden = false, order = 5,
			maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	@Column (name = "phone", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME, unique = false)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	private String phone;
	@AMetaData (
			displayName = "Profile Picture", required = false, readOnly = false, defaultValue = "",
			description = "User's profile picture stored as binary data", hidden = false, order = 11, imageData = true
	)
	@Column (name = "profile_picture_data", nullable = true, length = 10000, columnDefinition = "bytea")
	private byte[] profilePictureData;
	// load it eagerly because there a few projects that use this field
	@OneToMany (mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@AMetaData (
			displayName = "Project Settings", required = false, readOnly = true, description = "User's project memberships and roles", hidden = false,
			order = 20, createComponentMethod = "createUserProjectSettingsComponent"
	)
	private List<CUserProjectSettings> projectSettings = new ArrayList<>();
	// User-Company relationship settings
	@OneToMany (mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@AMetaData (
			displayName = "Company Settings", required = false, readOnly = true, description = "User's company memberships and roles", hidden = false,
			order = 21
	)
	private List<CUserCompanySettings> companySettings = new ArrayList<>();
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "cusertype_id", nullable = true)
	@AMetaData (displayName = "User Type", required = false, readOnly = false, description = "Type category of the user", hidden = false, order = 9)
	private CUserType userType;

	/** Default constructor for JPA. */
	public CUser() {
		super();
		// Initialize with default values for JPA
		enabled = true;
	}

	public CUser(final String name) {
		super(CUser.class, name);
	}

	public CUser(final String username, final String password, final String name, final String email) {
		super(CUser.class, name);
		login = username;
		this.email = email;
		setPassword(password);
	}

	public CUser(final String username, final String password, final String name, final String email, final String roles) {
		super(CUser.class, name);
		login = username;
		super.setName(name);
		this.email = email;
		setPassword(password);
	}

	@Override
	public boolean equals(final Object o) {
		return super.equals(o);
	}

	@Override
	public Class<?> getClassName() { // TODO Auto-generated method stub
		return CUser.class;
	}

	public CCompany getCompany() { return company; }

	public String getEmail() { return email; }

	public Boolean getEnabled() {
		return enabled; // Return the enabled status
	}

	public String getLastname() { return lastname; }

	public String getLogin() { return login; }

	@Override
	public String getName() { return super.getName(); }

	public String getPassword() {
		return password; // Return the encoded password
	}

	public String getPhone() { return phone; }

	public byte[] getProfilePictureData() { return profilePictureData; }

	// Getter and setter with safe initialization to prevent lazy loading issues
	public List<CUserProjectSettings> getProjectSettings() { return projectSettings; }

	public String getUsername() {
		return getLogin(); // Convenience method to get username for authentication
	}

	public CUserType getUserType() { return userType; }

	public Boolean isEnabled() {
		return enabled; // Return the enabled status
	}

	@Override
	public boolean matches(final String searchText) {
		if ((searchText == null) || searchText.trim().isEmpty()) {
			return true; // Empty search matches all
		}
		final String lowerSearchText = searchText.toLowerCase().trim();
		// Search in name field (first name)
		if ((getName() != null) && getName().toLowerCase().contains(lowerSearchText)) {
			return true;
		}
		// Search in lastname field
		if ((lastname != null) && lastname.toLowerCase().contains(lowerSearchText)) {
			return true;
		}
		// Search in login field
		if ((login != null) && login.toLowerCase().contains(lowerSearchText)) {
			return true;
		}
		// Search in email field
		if ((email != null) && email.toLowerCase().contains(lowerSearchText)) {
			return true;
		}
		// Search in description field
		if ((getDescription() != null) && getDescription().toLowerCase().contains(lowerSearchText)) {
			return true;
		}
		// Search in ID as string
		if ((getId() != null) && getId().toString().contains(lowerSearchText)) {
			return true;
		}
		return false;
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

	public void setProfilePictureData(final byte[] profilePictureData) { this.profilePictureData = profilePictureData; }

	public void setProjectSettings(final List<CUserProjectSettings> projectSettings) {
		this.projectSettings = projectSettings != null ? projectSettings : new ArrayList<>();
	}

	public void setUserType(final CUserType userType) { this.userType = userType; }

	/** Add a project setting to this user and maintain bidirectional relationship.
	 * @param projectSettings the project settings to add */
	public void addProjectSettings(final CUserProjectSettings projectSettings) {
		if (projectSettings == null) {
			return;
		}
		if (this.projectSettings == null) {
			this.projectSettings = new ArrayList<>();
		}
		if (!this.projectSettings.contains(projectSettings)) {
			this.projectSettings.add(projectSettings);
			projectSettings.setUser(this);
		}
	}

	/** Remove a project setting from this user and maintain bidirectional relationship.
	 * @param projectSettings the project settings to remove */
	public void removeProjectSettings(final CUserProjectSettings projectSettings) {
		Check.notNull(projectSettings, "Project settings cannot be null");
		Check.notNull(this.projectSettings, "User's project settings collection cannot be null");
		if (this.projectSettings.remove(projectSettings)) {
			projectSettings.setUser(null);
		}
	}

	// Getter and setter for company settings with safe initialization
	public List<CUserCompanySettings> getCompanySettings() {
		if (companySettings == null) {
			companySettings = new ArrayList<>();
		}
		return companySettings;
	}

	public void setCompanySettings(final List<CUserCompanySettings> companySettings) {
		this.companySettings = companySettings != null ? companySettings : new ArrayList<>();
	}

	/** Add a company setting to this user and maintain bidirectional relationship.
	 * @param companySettings the company settings to add */
	public void addCompanySettings(final CUserCompanySettings companySettings) {
		if (companySettings == null) {
			return;
		}
		if (this.companySettings == null) {
			this.companySettings = new ArrayList<>();
		}
		if (!this.companySettings.contains(companySettings)) {
			this.companySettings.add(companySettings);
			companySettings.setUser(this);
		}
	}

	/** Remove a company setting from this user and maintain bidirectional relationship.
	 * @param companySettings the company settings to remove */
	public void removeCompanySettings(final CUserCompanySettings companySettings) {
		if (companySettings == null || this.companySettings == null) {
			return;
		}
		if (this.companySettings.remove(companySettings)) {
			companySettings.setUser(null);
		}
	}

	/** Get the user's primary company.
	 * @return the primary company or null if not set */
	public CCompany getPrimaryCompany() {
		if (companySettings == null) {
			return null;
		}
		return companySettings.stream().filter(CUserCompanySettings::isPrimaryCompany).map(CUserCompanySettings::getCompany).findFirst().orElse(null);
	}

	/** Check if user has admin privileges in any company.
	 * @return true if user is admin in at least one company */
	public boolean isCompanyAdmin() {
		if (companySettings == null) {
			return false;
		}
		return companySettings.stream().anyMatch(CUserCompanySettings::isCompanyAdmin);
	}

	/** Check if user has admin privileges in a specific company.
	 * @param company the company to check
	 * @return true if user is admin in the specified company */
	public boolean isCompanyAdmin(final CCompany company) {
		if (companySettings == null || company == null) {
			return false;
		}
		return companySettings.stream().filter(settings -> settings.getCompany().equals(company)).anyMatch(CUserCompanySettings::isCompanyAdmin);
	}

	/** Returns a comprehensive string representation of the user including all key fields. Note: This method is used for debugging and logging
	 * purposes. For ComboBox display in the UI, the CEntityFormBuilder now uses getName() method automatically to show only the user's name instead
	 * of all fields. This resolves the combobox display issue where users were listed with complete text with all fields.
	 * @return detailed string representation of the user */
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
