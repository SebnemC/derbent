package tech.derbent.users.view;

import java.util.List;
import java.util.function.Supplier;
import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.views.CPanelUserProjectBase;
import tech.derbent.base.ui.dialogs.CWarningDialog;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.service.CUserProjectSettingsService;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.CUserTypeService;

/** Simplified panel for managing a user's project assignments. This panel displays all projects assigned to a specific user and allows: - Adding new
 * project assignments - Editing existing project roles/permissions - Removing project assignments The panel automatically updates when the current
 * user changes and maintains data consistency through proper accessor patterns. */
public class CPanelUserProjectSettings extends CPanelUserProjectBase<CUser, CUserProjectSettings> {

	private static final long serialVersionUID = 1L;
	private CUser currentUser;
	private final CProjectService projectService;
	private final CUserProjectSettingsService userProjectSettingsService;

	public CPanelUserProjectSettings(final CUser currentEntity, final CEnhancedBinder<CUser> beanValidationBinder, final CUserService entityService,
			final CUserTypeService userTypeService, final CCompanyService companyService, final CProjectService projectService,
			final CUserProjectSettingsService userProjectSettingsService) throws Exception {
		super("Project Settings", currentEntity, beanValidationBinder, CUser.class, entityService, userProjectSettingsService);
		this.userProjectSettingsService = userProjectSettingsService;
		this.projectService = projectService;
		initPanel();
	}

	@Override
	protected void onSettingsSaved(final CUserProjectSettings settings) {
		LOGGER.debug("Saving user project settings: {}", settings);
		try {
			// Use the service layer to properly persist the relationship
			final CUserProjectSettings savedSettings;
			if (settings.getId() == null) {
				// New relationship - create it
				savedSettings = userProjectSettingsService.save(settings);
				LOGGER.debug("Created new user project settings with ID: {}", savedSettings.getId());
			} else {
				// Existing relationship - update it
				savedSettings = userProjectSettingsService.save(settings);
				LOGGER.debug("Updated user project settings with ID: {}", savedSettings.getId());
			}
			// Update the local collection if accessors are available
			if (getSettings != null) {
				final List<CUserProjectSettings> settingsList = getSettings.get();
				if (settingsList != null) {
					boolean found = false;
					// Find and update existing entry or add new one
					for (int i = 0; i < settingsList.size(); i++) {
						final CUserProjectSettings existing = settingsList.get(i);
						if ((existing.getId() != null) && existing.getId().equals(savedSettings.getId())) {
							settingsList.set(i, savedSettings);
							found = true;
							break;
						}
					}
					if (!found) {
						settingsList.add(savedSettings);
					}
				}
			}
			// Save the parent entity if needed
			if (saveEntity != null) {
				saveEntity.run();
			}
			refresh();
		} catch (final Exception e) {
			LOGGER.error("Error saving user project settings", e);
			throw new RuntimeException("Failed to save user project settings: " + e.getMessage(), e);
		}
	}

	/** Validates preconditions and opens dialog for adding new project assignment.
	 * @throws Exception */
	@Override
	protected void openAddDialog() throws Exception {
		if (!validateUserSelection() || !validateServiceAvailability("Project")) {
			return;
		}
		final CUserProjectSettingsDialog dialog =
				new CUserProjectSettingsDialog((CUserService) entityService, projectService, null, currentUser, this::onSettingsSaved);
		dialog.open();
	}

	/** Validates preconditions and opens dialog for editing selected project assignment.
	 * @throws Exception */
	@Override
	protected void openEditDialog() throws Exception {
		if (!validateGridSelection("edit") || !validateUserSelection()) {
			return;
		}
		final CUserProjectSettings selected = grid.asSingleSelect().getValue();
		final CUserProjectSettingsDialog dialog =
				new CUserProjectSettingsDialog((CUserService) entityService, projectService, selected, currentUser, this::onSettingsSaved);
		dialog.open();
	}

	public void setAccessors(final Supplier<List<CUserProjectSettings>> getProjectUsers, final Runnable saveEntity) {
		LOGGER.debug("Setting project users accessors");
		setSettingsAccessors(getProjectUsers, saveEntity);
	}

	public void setCurrentUser(final CUser user) { this.currentUser = user; }

	public void setProjectSettingsAccessors(final java.util.function.Supplier<List<CUserProjectSettings>> getProjectSettings,
			final Runnable saveEntity) {
		LOGGER.debug("Setting project settings accessors");
		setSettingsAccessors(getProjectSettings, saveEntity);
	}

	/** Validates that a user is currently selected.
	 * @return true if currentUser is not null, false otherwise */
	private boolean validateUserSelection() {
		if (currentUser == null) {
			new CWarningDialog("Please select a user first before managing project settings.").open();
			return false;
		}
		return true;
	}
}
