package tech.derbent.api.views;

import java.util.List;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.domains.CEntityNamed;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.ui.dialogs.CConfirmationDialog;
import tech.derbent.api.ui.dialogs.CWarningDialog;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.components.CButton;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.service.CUserProjectSettingsService;

/** Generic base class for User-Project relationship components. This class provides common functionality for both User->Project and Project->User
 * relationship components, reducing code duplication while maintaining flexibility for specific implementations.
 * @param <MasterClass>     The main entity type (CUser for user-centric, CProject for project-centric)
 * @param <RelationalClass> The relationship entity type (always CUserProjectSettings) */
public abstract class CComponentUserProjectRelationBase<MasterClass extends CEntityNamed<MasterClass>,
		RelationalClass extends CEntityDB<RelationalClass>> extends CComponentRelationBase<MasterClass, CUserProjectSettings> {

	private static final long serialVersionUID = 1L;
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	protected CUserProjectSettingsService userProjectSettingsService;

	public CComponentUserProjectRelationBase(final String title, IContentOwner parentContent, final CEnhancedBinder<MasterClass> beanValidationBinder,
			final Class<MasterClass> entityClass, final CAbstractService<MasterClass> entityService,
			final CUserProjectSettingsService userProjectSettingsService) {
		super(title, parentContent, beanValidationBinder, entityClass, entityService, CUserProjectSettings.class);
		Check.notNull(userProjectSettingsService, "User project settings service cannot be null");
		this.userProjectSettingsService = userProjectSettingsService;
		setupGrid();
		setupButtons();
		closePanel();
	}

	/** Creates a delete confirmation message. Subclasses can override for custom messages. */
	protected String createDeleteConfirmationMessage(final CUserProjectSettings selected) {
		Check.notNull(selected, "Selected settings cannot be null");
		Check.notNull(selected.getProject(), "Project cannot be null");
		final String projectName = selected.getProject().getName();
		return String.format("Are you sure you want to delete the project setting for '%s'? This action cannot be undone.", projectName);
	}

	/** Deletes the selected user-project settings. */
	protected void deleteSelected() {
		final CUserProjectSettings selected = grid.asSingleSelect().getValue();
		Check.notNull(selected, "Please select a project setting to delete.");
		try {
			final String confirmationMessage = createDeleteConfirmationMessage(selected);
			new CConfirmationDialog(confirmationMessage, () -> {
				try {
					userProjectSettingsService.deleteByUserProject(selected.getUser(), selected.getProject());
					populateForm();
					LOGGER.info("Deleted user project setting: {}", selected);
				} catch (final Exception e) {
					LOGGER.error("Error deleting user project setting: {}", e.getMessage(), e);
					new CWarningDialog("Failed to delete project setting: " + e.getMessage()).open();
				}
			}).open();
		} catch (Exception e) {
			LOGGER.error("Failed to show delete confirmation: {}", e.getMessage(), e);
			new CWarningDialog("Failed to delete project setting").open();
		}
	}

	/** Gets display text for various field types. */
	protected String getDisplayText(final CUserProjectSettings settings, final String type) {
		Check.notNull(settings, "Settings cannot be null when getting display text");
		try {
			switch (type) {
			case "project":
				Check.notNull(settings.getProject(), "Project cannot be null");
				return CColorUtils.getDisplayTextFromEntity(settings.getProject());
			case "user":
				Check.notNull(settings.getUser(), "User cannot be null");
				return CColorUtils.getDisplayTextFromEntity(settings.getUser());
			case "role":
				return settings.getRole() != null ? CColorUtils.getDisplayTextFromEntity(settings.getRole()) : "";
			case "permission":
				return settings.getPermission() != null ? settings.getPermission() : "";
			default:
				return "";
			}
		} catch (Exception e) {
			LOGGER.error("Failed to get display text for type {}: {}", type, e.getMessage(), e);
			return "";
		}
	}

	/** Abstract methods that subclasses must implement */
	protected abstract void onSettingsSaved(final CUserProjectSettings settings);
	protected abstract void openAddDialog() throws Exception;
	protected abstract void openEditDialog() throws Exception;

	/** Setup for panel initialization. Subclasses can override to add custom setup. */
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

	/** Abstract method for setting up data accessors - subclasses provide specific implementations */
	protected abstract void setupDataAccessors();

	/** Sets up the action buttons (Add, Edit, Delete) with common behavior. */
	private void setupButtons() {
		try {
			final CButton addButton = CButton.createPrimary("Add", VaadinIcon.PLUS.create(), e -> {
				try {
					openAddDialog();
				} catch (final Exception ex) {
					LOGGER.error("Error opening add dialog: {}", ex.getMessage(), ex);
					new CWarningDialog("Failed to open add dialog: " + ex.getMessage()).open();
				}
			});
			final CButton editButton = new CButton("Edit", VaadinIcon.EDIT.create(), e -> {
				try {
					openEditDialog();
				} catch (final Exception ex) {
					LOGGER.error("Error opening edit dialog: {}", ex.getMessage(), ex);
					new CWarningDialog("Failed to open edit dialog: " + ex.getMessage()).open();
				}
			});
			editButton.setEnabled(false);
			final CButton deleteButton = CButton.createError("Delete", VaadinIcon.TRASH.create(), e -> deleteSelected());
			deleteButton.setEnabled(false);
			grid.addSelectionListener(selection -> {
				final boolean hasSelection = !selection.getAllSelectedItems().isEmpty();
				editButton.setEnabled(hasSelection);
				deleteButton.setEnabled(hasSelection);
			});
			final HorizontalLayout buttonLayout = new HorizontalLayout(addButton, editButton, deleteButton);
			buttonLayout.setSpacing(true);
			add(buttonLayout);
		} catch (Exception e) {
			LOGGER.error("Failed to setup buttons: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to setup buttons", e);
		}
	}

	/** Sets up the grid with common columns. Subclasses can override to customize columns. */
	protected void setupGrid() {
		try {
			grid.addColumn(settings -> getDisplayText(settings, "project")).setHeader("Project").setAutoWidth(true);
			grid.addColumn(settings -> getDisplayText(settings, "user")).setHeader("User").setAutoWidth(true);
			grid.addColumn(settings -> getDisplayText(settings, "role")).setHeader("Role").setAutoWidth(true);
			grid.addColumn(settings -> getDisplayText(settings, "permission")).setHeader("Permissions").setAutoWidth(true);
			grid.setWidthFull();
			grid.setHeight("300px");
			add(grid);
		} catch (Exception e) {
			LOGGER.error("Failed to setup grid: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to setup grid", e);
		}
	}

	/** Helper method to create standard data accessors pattern.
	 * @param settingsSupplier Function to get the list of settings
	 * @param entitySaver      Function to save the entity */
	protected void createStandardDataAccessors(Supplier<List<CUserProjectSettings>> settingsSupplier, Runnable entitySaver) {
		final Supplier<List<CUserProjectSettings>> getterFunction = () -> {
			final MasterClass entity = getCurrentEntity();
			if (entity == null) {
				LOGGER.debug("No current entity available, returning empty list");
				return List.of();
			}
			try {
				final List<CUserProjectSettings> settings = settingsSupplier.get();
				LOGGER.debug("Retrieved {} settings for entity: {}", settings.size(), entity.getName());
				return settings;
			} catch (final Exception e) {
				LOGGER.error("Error retrieving settings for entity: {}", e.getMessage(), e);
				return List.of();
			}
		};
		final Runnable saveEntityFunction = () -> {
			try {
				final MasterClass entity = getCurrentEntity();
				Check.notNull(entity, "Current entity cannot be null when saving");
				entitySaver.run();
			} catch (final Exception e) {
				LOGGER.error("Error saving entity: {}", e.getMessage(), e);
				throw new RuntimeException("Failed to save entity", e);
			}
		};
		setSettingsAccessors(getterFunction, saveEntityFunction);
	}
}
