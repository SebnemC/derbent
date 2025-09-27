package tech.derbent.users.view;

import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import tech.derbent.api.annotations.CFormBuilder;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.interfaces.IBindableComponent;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.roles.domain.CUserProjectRole;
import tech.derbent.api.roles.service.CUserProjectRoleService;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.components.CButton;
import tech.derbent.api.views.components.CVerticalLayout;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.service.CUserProjectSettingsService;
import tech.derbent.users.service.CUserService;

/** Component for managing user project settings that can work both as a standalone component and as part of a dialog. Based on
 * CUserProjectSettingsDialog but redesigned to be more flexible and reusable. Features: - Form fields for project, role, and permission editing -
 * Grid display of existing user project settings - Add/Edit/Delete buttons for CRUD operations - Full binding support with CEnhancedBinder - Service
 * integration for data loading and persistence */
public class CUserProjectSettingsComponent extends CVerticalLayout implements IBindableComponent<CUserProjectSettings> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CUserProjectSettingsComponent.class);
	private static final long serialVersionUID = 1L;
	// Services
	private final CUserService userService;
	private final CProjectService projectService;
	private final CUserProjectRoleService roleService;
	private final CUserProjectSettingsService userProjectSettingsService;
	@Autowired
	private CNotificationService notificationService;
	// UI Components
	private Grid<CUserProjectSettings> grid;
	private ComboBox<CProject> projectComboBox;
	private ComboBox<CUserProjectRole> roleComboBox;
	private TextField permissionField;
	private CButton addButton;
	private CButton editButton;
	private CButton deleteButton;
	// Data and binding
	private CEnhancedBinder<CUserProjectSettings> binder;
	private CUser currentUser;
	private CUserProjectSettings currentSettings;
	private IContentOwner parentContent;
	private Consumer<CUserProjectSettings> onSave;

	/** Constructor for standalone component use.
	 * @param userService                the user service
	 * @param projectService             the project service
	 * @param roleService                the role service
	 * @param userProjectSettingsService the user project settings service */
	public CUserProjectSettingsComponent(CUserService userService, CProjectService projectService, CUserProjectRoleService roleService,
			CUserProjectSettingsService userProjectSettingsService) {
		super(false, true, false); // no padding, with spacing, no margin
		Check.notNull(userService, "User service cannot be null");
		Check.notNull(projectService, "Project service cannot be null");
		Check.notNull(roleService, "Role service cannot be null");
		Check.notNull(userProjectSettingsService, "User project settings service cannot be null");
		this.userService = userService;
		this.projectService = projectService;
		this.roleService = roleService;
		this.userProjectSettingsService = userProjectSettingsService;
		try {
			initializeComponent();
		} catch (Exception e) {
			LOGGER.error("Failed to initialize component: {}", e.getMessage(), e);
		}
	}

	/** Constructor with additional parameters for dialog-like usage.
	 * @param parentContent              the parent content owner
	 * @param currentUser                the user to manage settings for
	 * @param userService                the user service
	 * @param projectService             the project service
	 * @param roleService                the role service
	 * @param userProjectSettingsService the user project settings service
	 * @param onSave                     callback when settings are saved */
	public CUserProjectSettingsComponent(IContentOwner parentContent, CUser currentUser, CUserService userService, CProjectService projectService,
			CUserProjectRoleService roleService, CUserProjectSettingsService userProjectSettingsService, Consumer<CUserProjectSettings> onSave) {
		this(userService, projectService, roleService, userProjectSettingsService);
		this.parentContent = parentContent;
		this.currentUser = currentUser;
		this.onSave = onSave;
		try {
			if (currentUser != null) {
				loadUserProjectSettings(currentUser);
			}
		} catch (Exception e) {
			LOGGER.error("Failed to load user project settings: {}", e.getMessage(), e);
		}
	}

	@Override
	public void initializeComponent() throws Exception {
		setClassName("user-project-settings-component");
		setWidthFull();
		setSpacing(true);
		// Create title
		H3 title = new H3("Project Settings");
		add(title);
		// Create form section
		createFormFields();
		CVerticalLayout formLayout = new CVerticalLayout(false, true, false);
		formLayout.add(projectComboBox, roleComboBox, permissionField);
		// Create button layout
		createButtons();
		HorizontalLayout buttonLayout = new HorizontalLayout(addButton, editButton, deleteButton);
		buttonLayout.setSpacing(true);
		// Create grid
		createGrid();
		// Add all to main layout
		add(formLayout, buttonLayout, grid);
		// Setup binder
		setupBinder();
		LOGGER.debug("Component initialized successfully");
	}

	@Override
	public void bindToExternalBinder(CEnhancedBinder<CUserProjectSettings> externalBinder) throws Exception {
		Check.notNull(externalBinder, "External binder cannot be null");
		this.binder = externalBinder;
		bindFormFields();
		LOGGER.debug("Component bound to external binder");
	}

	@Override
	public void populateData(CUserProjectSettings entity) throws Exception {
		this.currentSettings = entity;
		if (binder != null && entity != null) {
			binder.readBean(entity);
			LOGGER.debug("Populated component with entity data");
		}
		loadServiceData();
	}

	@Override
	public CUserProjectSettings getCurrentData() throws Exception {
		if (binder != null && currentSettings != null) {
			try {
				binder.writeBean(currentSettings);
				return currentSettings;
			} catch (Exception e) {
				LOGGER.error("Error extracting current data: {}", e.getMessage(), e);
				if (notificationService != null) {
					notificationService.showError("Validation failed: " + e.getMessage());
				}
				return null;
			}
		}
		return currentSettings;
	}

	@Override
	public boolean validateData() {
		if (binder != null) {
			return binder.validate().isOk();
		}
		return true;
	}

	@Override
	public void clearData() {
		if (projectComboBox != null)
			projectComboBox.clear();
		if (roleComboBox != null)
			roleComboBox.clear();
		if (permissionField != null)
			permissionField.clear();
		if (grid != null)
			grid.getDataProvider().refreshAll();
		currentSettings = null;
		LOGGER.debug("Component data cleared");
	}

	@Override
	public CEnhancedBinder<CUserProjectSettings> getBinder() { return binder; }

	/** Sets the current user and loads their project settings.
	 * @param user the user to load settings for */
	public void setCurrentUser(CUser user) {
		this.currentUser = user;
		try {
			loadUserProjectSettings(user);
		} catch (Exception e) {
			LOGGER.error("Failed to load user project settings: {}", e.getMessage(), e);
		}
	}

	/** Gets the grid component for external access.
	 * @return the project settings grid */
	public Grid<CUserProjectSettings> getGrid() { return grid; }

	private void createFormFields() {
		// Project ComboBox
		projectComboBox = new ComboBox<>("Project");
		projectComboBox.setItemLabelGenerator(project -> CColorUtils.getDisplayTextFromEntity(project));
		projectComboBox.setWidthFull();
		projectComboBox.setRequired(true);
		projectComboBox.setPlaceholder("Select a project");
		CAuxillaries.setId(projectComboBox);
		// Role ComboBox
		roleComboBox = new ComboBox<>("Role");
		roleComboBox.setItemLabelGenerator(role -> CColorUtils.getDisplayTextFromEntity(role));
		roleComboBox.setWidthFull();
		roleComboBox.setPlaceholder("Select a role");
		CAuxillaries.setId(roleComboBox);
		// Permission TextField
		permissionField = new TextField("Permissions");
		permissionField.setWidthFull();
		permissionField.setPlaceholder("Enter permissions (optional)");
		permissionField.setHelperText("Comma-separated permissions like: READ,WRITE,DELETE");
		CAuxillaries.setId(permissionField);
	}

	private void createButtons() {
		addButton = CButton.createPrimary("Add", VaadinIcon.PLUS.create(), e -> handleAdd());
		editButton = new CButton("Edit", VaadinIcon.EDIT.create(), e -> handleEdit());
		deleteButton = CButton.createError("Delete", VaadinIcon.TRASH.create(), e -> handleDelete());
		editButton.setEnabled(false);
		deleteButton.setEnabled(false);
	}

	private void createGrid() {
		grid = new Grid<>(CUserProjectSettings.class, false);
		grid.addColumn(settings -> CColorUtils.getDisplayTextFromEntity(settings.getProject())).setHeader("Project").setAutoWidth(true);
		grid.addColumn(settings -> settings.getRole() != null ? CColorUtils.getDisplayTextFromEntity(settings.getRole()) : "").setHeader("Role")
				.setAutoWidth(true);
		grid.addColumn(CUserProjectSettings::getPermission).setHeader("Permissions").setAutoWidth(true);
		grid.addSelectionListener(selection -> {
			boolean hasSelection = !selection.getAllSelectedItems().isEmpty();
			editButton.setEnabled(hasSelection);
			deleteButton.setEnabled(hasSelection);
			if (hasSelection) {
				CUserProjectSettings selected = selection.getFirstSelectedItem().orElse(null);
				if (selected != null) {
					populateFormFromSettings(selected);
				}
			}
		});
		grid.setHeight("300px");
	}

	private void setupBinder() {
		if (binder == null) {
			binder = new CEnhancedBinder<>(CUserProjectSettings.class);
		}
		bindFormFields();
	}

	private void bindFormFields() {
		if (binder != null) {
			try {
				binder.forField(projectComboBox).asRequired("Project is required").bind(CUserProjectSettings::getProject,
						CUserProjectSettings::setProject);
				binder.bind(roleComboBox, CUserProjectSettings::getRole, CUserProjectSettings::setRole);
				binder.bind(permissionField, CUserProjectSettings::getPermission, CUserProjectSettings::setPermission);
				LOGGER.debug("Form fields bound successfully");
			} catch (Exception e) {
				LOGGER.error("Error binding form fields: {}", e.getMessage(), e);
			}
		}
	}

	private void loadServiceData() {
		try {
			// Load projects
			List<CProject> projects = projectService.findAll();
			projectComboBox.setItems(projects);
			// Load roles
			List<CUserProjectRole> roles = roleService.findAll();
			roleComboBox.setItems(roles);
			LOGGER.debug("Service data loaded successfully");
		} catch (Exception e) {
			LOGGER.error("Error loading service data: {}", e.getMessage(), e);
			if (notificationService != null) {
				notificationService.showError("Failed to load data: " + e.getMessage());
			}
		}
	}

	private void loadUserProjectSettings(CUser user) {
		if (user != null && grid != null) {
			try {
				List<CUserProjectSettings> settings = userProjectSettingsService.findByUser(user);
				grid.setItems(settings);
				LOGGER.debug("Loaded {} project settings for user {}", settings.size(), user.getName());
			} catch (Exception e) {
				LOGGER.error("Error loading user project settings: {}", e.getMessage(), e);
			}
		}
	}

	private void populateFormFromSettings(CUserProjectSettings settings) {
		if (settings != null) {
			try {
				currentSettings = settings;
				if (binder != null) {
					binder.readBean(settings);
				}
			} catch (Exception e) {
				LOGGER.error("Error populating form from settings: {}", e.getMessage(), e);
			}
		}
	}

	private void handleAdd() {
		try {
			clearData();
			currentSettings = new CUserProjectSettings();
			if (currentUser != null) {
				currentSettings.setUser(currentUser);
			}
			if (binder != null) {
				binder.readBean(currentSettings);
			}
		} catch (Exception e) {
			LOGGER.error("Error handling add: {}", e.getMessage(), e);
		}
	}

	private void handleEdit() {
		CUserProjectSettings selected = grid.asSingleSelect().getValue();
		if (selected != null) {
			populateFormFromSettings(selected);
		}
	}

	private void handleDelete() {
		CUserProjectSettings selected = grid.asSingleSelect().getValue();
		if (selected != null) {
			try {
				userProjectSettingsService.delete(selected);
				if (currentUser != null) {
					loadUserProjectSettings(currentUser);
				}
				if (notificationService != null) {
					notificationService.showSuccess("Project settings deleted successfully");
				}
			} catch (Exception e) {
				LOGGER.error("Error deleting settings: {}", e.getMessage(), e);
				if (notificationService != null) {
					notificationService.showError("Failed to delete: " + e.getMessage());
				}
			}
		}
	}
}
