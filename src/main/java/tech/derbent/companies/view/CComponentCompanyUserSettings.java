package tech.derbent.companies.view;

import java.util.List;
import java.util.function.Supplier;
import org.springframework.context.ApplicationContext;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.ui.dialogs.CWarningDialog;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.components.CComponentUserCompanyBase;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserCompanySetting;
import tech.derbent.users.service.CUserService;

/** Simplified component for managing users within a company. This component displays all users assigned to a specific company and allows: - Adding
 * new user assignments - Editing existing user roles/departments/ownership - Removing user assignments The component automatically updates when the
 * current company changes and maintains data consistency through proper accessor patterns. */
public class CComponentCompanyUserSettings extends CComponentUserCompanyBase<CCompany, CUserCompanySetting> {

	private static final long serialVersionUID = 1L;
	private final CUserService userService;

	public CComponentCompanyUserSettings(IContentOwner parentContent, final CCompanyService entityService, ApplicationContext applicationContext)
			throws Exception {
		super("User Settings", parentContent, CCompany.class, entityService, applicationContext);
		Check.notNull(relationService, "User company settings service cannot be null");
		userService = applicationContext.getBean(CUserService.class);
		Check.notNull(userService, "User service cannot be null");
		initPanel();
	}

	public List<CUser> getAvailableUsers() {
		try {
			// Get all users that are not yet members of this company
			return userService.getAvailableUsersForCompany(getCurrentEntity().getId());
		} catch (Exception e) {
			LOGGER.error("Failed to get available users for company {}: {}", getCurrentEntity().getId(), e.getMessage(), e);
			throw new RuntimeException("Failed to get available users", e);
		}
	}

	@Override
	public void initPanel() throws Exception {
		try {
			super.initPanel();
			setupDataAccessors();
			openPanel();
		} catch (Exception e) {
			LOGGER.error("Failed to initialize panel: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to initialize panel", e);
		}
	}

	@Override
	protected void onSettingsSaved(final CUserCompanySetting settings) {
		Check.notNull(settings, "Settings cannot be null when saving");
		LOGGER.debug("Saving user company settings: {}", settings);
		try {
			final CUserCompanySetting savedSettings = settings.getId() == null ? relationService.addUserToCompany(settings.getUser(),
					settings.getCompany(), settings.getOwnershipLevel(), settings.getRole(), settings.getDepartment(), settings.isPrimaryCompany())
					: relationService.save(settings);
			LOGGER.info("Successfully saved user company settings: {}", savedSettings);
			populateForm();
		} catch (final Exception e) {
			LOGGER.error("Error saving user company settings: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to save user company settings: " + e.getMessage(), e);
		}
	}

	@Override
	protected void openAddDialog() throws Exception {
		try {
			LOGGER.debug("Opening add dialog for company user settings");
			final CCompany company = getCurrentEntity();
			Check.notNull(company, "Please select a company first.");
			final CCompanyUserSettingsDialog dialog = new CCompanyUserSettingsDialog(this, (CCompanyService) entityService, userService,
					relationService, null, null, company, this::onSettingsSaved);
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
			LOGGER.debug("Opening edit dialog for company user settings");
			final CUserCompanySetting selected = grid.asSingleSelect().getValue();
			Check.notNull(selected, "Please select a user setting to edit.");
			final CCompany company = getCurrentEntity();
			Check.notNull(company, "Current company is not available.");
			final CCompanyUserSettingsDialog dialog = new CCompanyUserSettingsDialog(this, (CCompanyService) entityService, userService,
					relationService, selected, selected.getUser(), company, this::onSettingsSaved);
			dialog.open();
		} catch (Exception e) {
			LOGGER.error("Failed to open edit dialog: {}", e.getMessage(), e);
			new CWarningDialog("Failed to open edit dialog: " + e.getMessage()).open();
			throw e;
		}
	}

	private void setupDataAccessors() {
		try {
			final Supplier<List<CUserCompanySetting>> getterFunction = () -> {
				final CCompany entity = getCurrentEntity();
				if (entity == null) {
					LOGGER.debug("No current entity available, returning empty list");
					return List.of();
				}
				try {
					final List<CUserCompanySetting> settings = relationService.findByCompany(entity);
					LOGGER.debug("Retrieved {} user settings for company: {}", settings.size(), entity.getName());
					return settings;
				} catch (final Exception e) {
					LOGGER.error("Error retrieving user settings for company: {}", e.getMessage(), e);
					return List.of();
				}
			};
			final Runnable saveEntityFunction = () -> {
				try {
					final CCompany entity = getCurrentEntity();
					Check.notNull(entity, "Current entity cannot be null when saving");
					entityService.save(entity);
				} catch (final Exception e) {
					LOGGER.error("Error saving entity: {}", e.getMessage(), e);
					throw new RuntimeException("Failed to save entity", e);
				}
			};
			setSettingsAccessors(getterFunction, saveEntityFunction);
		} catch (Exception e) {
			LOGGER.error("Failed to setup data accessors: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to setup data accessors", e);
		}
	}
}
