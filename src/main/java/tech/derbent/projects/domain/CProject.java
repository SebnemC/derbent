package tech.derbent.projects.domain;

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
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CEntityNamed;
import tech.derbent.api.interfaces.ISearchable;
import tech.derbent.api.utils.Check;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.users.domain.CUserProjectSettings;

/** CProject - Domain entity representing projects. Layer: Domain (MVC) Inherits from CEntityDB to provide database functionality. */
@Entity
@Table (name = "cproject")
@AttributeOverride (name = "id", column = @Column (name = "project_id"))
public class CProject extends CEntityNamed<CProject> implements ISearchable {

	public static final String DEFAULT_COLOR = "#905300";
	public static final String DEFAULT_ICON = "vaadin:credit-card";
	public static final String VIEW_NAME = "Projects View";
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "company_id", nullable = true)
	@AMetaData (
			displayName = "Company", required = false, readOnly = false, description = "Company associated with this project", hidden = false,
			order = 5
	)
	private CCompany company;
	// lets keep it layzily loaded to avoid loading all user settings at once
	@OneToMany (mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@AMetaData (
			displayName = "User Settings", required = false, readOnly = false, description = "User project settings for this project", hidden = false,
			order = 10, createComponentMethod = "createProjectUserSettingsComponent"
	)
	private final List<CUserProjectSettings> userSettings = new ArrayList<>();

	/** Default constructor for JPA. */
	public CProject() {
		super();
	}

	public CProject(final String name) {
		super(CProject.class, name);
	}

	/** Gets the company associated with this project. */
	public CCompany getCompany() { return company; }

	/** Sets the company associated with this project. */
	public void setCompany(final CCompany company) { this.company = company; }

	/** Gets the list of user project settings for this project. */
	public List<CUserProjectSettings> getUserSettings() { return userSettings; }

	/** Add a user setting to this project and maintain bidirectional relationship.
	 * @param userSettings the user settings to add */
	public void addUserSettings(final CUserProjectSettings userSettings) {
		if (userSettings == null) {
			return;
		}
		if (!this.userSettings.contains(userSettings)) {
			this.userSettings.add(userSettings);
			userSettings.setProject(this);
		}
	}

	/** Remove a user setting from this project and maintain bidirectional relationship.
	 * @param userSettings the user settings to remove */
	public void removeUserSettings(final CUserProjectSettings userSettings) {
		Check.notNull(userSettings, "User settings cannot be null");
		if (this.userSettings.remove(userSettings)) {
			userSettings.setProject(null);
		}
	}

	@Override
	public boolean matches(final String searchText) {
		if ((searchText == null) || searchText.trim().isEmpty()) {
			return true; // Empty search matches all
		}
		final String lowerSearchText = searchText.toLowerCase().trim();
		// Search in name field
		if ((getName() != null) && getName().toLowerCase().contains(lowerSearchText)) {
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

	@Override
	public void initializeAllFields() {
		// TODO Auto-generated method stub
	}
}
