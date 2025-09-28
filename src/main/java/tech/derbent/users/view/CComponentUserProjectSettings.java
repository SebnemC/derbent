package tech.derbent.users.view;

import java.util.List;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.ui.dialogs.CWarningDialog;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.CComponentUserProjectRelationBase;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.service.CUserProjectSettingsService;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.CUserTypeService;

/** Component for managing a user's project assignments (User->Project direction). This component displays all projects assigned to a specific user
 * and allows: - Adding new project assignments - Editing existing project roles/permissions - Removing project assignments The component
 * automatically updates when the current user changes. */
public class CComponentUserProjectSettings extends CComponentUserProjectRelationBase<CUser, CUserProjectSettings> {

	private static final long serialVersionUID = 1L;
	private CUser currentUser;
	private final CProjectService projectService;

	public CComponentUserProjectSettings(IContentOwner parentContent, final CUser currentEntity, final CEnhancedBinder<CUser> beanValidationBinder,
			final CUserService entityService, final CUserTypeService userTypeService, final CCompanyService companyService,
			final CProjectService projectService, final CUserProjectSettingsService userProjectSettingsService) throws Exception {
		super("Project Settings", parentContent, beanValidationBinder, CUser.class, entityService, userProjectSettingsService);
		Check.notNull(projectService, "Project service cannot be null");
		this.projectService = projectService;
		initPanel();
	}

	public List<CProject> getAvailableProjects() {
		Check.notNull(currentUser, "Current user must be selected to get available projects");
		try {
			return projectService.getAvailableProjectsForUser(getCurrentEntity().getId());
		} catch (Exception e) {
			LOGGER.error("Failed to get available projects for user {}: {}", getCurrentEntity().getId(), e.getMessage(), e);
			throw new RuntimeException("Failed to get available projects", e);
		}
	}

	@Override
	protected void onSettingsSaved(final CUserProjectSettings settings) {
		Check.notNull(settings, "Settings cannot be null when saving");
		LOGGER.debug("Saving user project settings: {}", settings);
		try {
			final CUserProjectSettings savedSettings = settings.getId() == null ? userProjectSettingsService.addUserToProject(settings.getUser(),
					settings.getProject(), settings.getRole(), settings.getPermission()) : userProjectSettingsService.save(settings);
			LOGGER.info("Successfully saved user project settings: {}", savedSettings);
			populateForm();
		} catch (final Exception e) {
			LOGGER.error("Error saving user project settings: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to save user project settings: " + e.getMessage(), e);
		}
	}

	@Override
	protected void openAddDialog() throws Exception {
		try {
			LOGGER.debug("Opening add dialog for user project settings");
			final CUser user = getCurrentEntity();
			Check.notNull(user, "Please select a user first.");
			currentUser = user;
			final CUserProjectSettingsDialog dialog = new CUserProjectSettingsDialog(this, (CUserService) entityService, projectService,
					userProjectSettingsService, null, user, this::onSettingsSaved);
			dialog.open();
		} catch (Exception e) {
			LOGGER.error("Failed to open add dialog: {}", e.getMessage(), e);
			new CWarningDialog("Failed to open add dialog: " + e.getMessage()).open();
			throw e;
		}
	}

	@Override
	protected void openEditDialog() throws Exception {
		try {
			LOGGER.debug("Opening edit dialog for user project settings");
			final CUserProjectSettings selected = grid.asSingleSelect().getValue();
			Check.notNull(selected, "Please select a project setting to edit.");
			final CUser user = getCurrentEntity();
			Check.notNull(user, "Current user is not available.");
			currentUser = user;
			final CUserProjectSettingsDialog dialog = new CUserProjectSettingsDialog(this, (CUserService) entityService, projectService,
					userProjectSettingsService, selected, user, this::onSettingsSaved);
			dialog.open();
		} catch (Exception e) {
			LOGGER.error("Failed to open edit dialog: {}", e.getMessage(), e);
			new CWarningDialog("Failed to open edit dialog: " + e.getMessage()).open();
			throw e;
		}
	}

	@Override
	protected void setupDataAccessors() {
		createStandardDataAccessors(() -> userProjectSettingsService.findByUser(getCurrentEntity()), () -> entityService.save(getCurrentEntity()));
	}
}
