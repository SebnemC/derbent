package tech.derbent.users.view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;

import tech.derbent.abstracts.views.CDBEditDialog;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;

/**
 * Dialog for assigning a user to a project. Inherits generic dialog logic from CDialog.
 */
public class CUserProjectSettingsDialog extends CDBEditDialog<CUserProjectSettings> {

    private static final long serialVersionUID = 1L;

    private final CProjectService projectService;

    private final CUser user;

    // Form components
    private ComboBox<CProject> projectComboBox;

    private TextField rolesField;

    private TextField permissionsField;

    // Resource allocation fields
    private BigDecimalField allocatedHoursField;

    private BigDecimalField hourlyRateField;

    private DatePicker startDatePicker;

    private DatePicker dueDatePicker;

    private BigDecimalField workloadPercentageField;

    private Checkbox isActiveCheckbox;

    public CUserProjectSettingsDialog(final CProjectService projectService, final CUserProjectSettings settings,
            final CUser user, final Consumer<CUserProjectSettings> onSave) {
        // Call parent constructor with provided settings or new instance if null Use new
        // CUserProjectSettings() if settings is null to ensure non-null data This allows
        // the dialog to handle both new assignments and edits without requiring a
        // separate constructor for new assignments.
        super(settings != null ? settings : new CUserProjectSettings(), onSave, settings == null);
        this.projectService = projectService;
        this.user = user;
        setupDialog();// call setupDialog() to initialize the dialog
        populateForm(); // Call after fields are initialized
    }

    /** Returns available projects for selection. */
    private List<CProject> getAvailableProjects() {
        final List<CProject> allProjects = projectService.findAll();

        if (!isNew && (data.getProject() != null)) {
            projectService.getById(data.getProject().getId()).ifPresent(project -> {

                if (!allProjects.contains(project)) {
                    allProjects.add(project);
                }
            });
        }
        return allProjects;
    }

    @Override
    protected Icon getFormIcon() {
        return VaadinIcon.USER_CHECK.create();
    }

    @Override
    protected String getFormTitle() {
        return isNew ? "Assign User to Project" : "Edit Project Assignment";
    }

    @Override
    public String getHeaderTitle() {
        return isNew ? "Add Project Assignment" : "Edit Project Assignment";
    }

    @Override
    protected String getSuccessCreateMessage() {
        return "Project assignment created successfully";
    }

    @Override
    protected String getSuccessUpdateMessage() {
        return "Project assignment updated successfully";
    }

    /** Populates form fields from data. */
    @Override
    protected void populateForm() {
        LOGGER.debug("Populating form for {}", getClass().getSimpleName());

        if ((projectService == null) || (user == null)) {
            throw new IllegalStateException("ProjectService and User must be initialized before populating form");
        }
        // Project selection
        projectComboBox = new ComboBox<>("Project");
        // Following coding guidelines: All selective ComboBoxes must be selection only
        // (user must not be able to type arbitrary text)
        projectComboBox.setAllowCustomValue(false);
        projectComboBox.setItemLabelGenerator(CProject::getName);
        projectComboBox.setItems(getAvailableProjects());
        projectComboBox.setRequired(true);
        projectComboBox.setEnabled(isNew);
        // Roles field
        rolesField = new TextField("Roles");
        rolesField.setPlaceholder("Enter roles separated by commas (e.g., DEVELOPER, MANAGER)");
        rolesField.setHelperText("Comma-separated list of roles for this project");
        // Permissions field
        permissionsField = new TextField("Permissions");
        permissionsField.setPlaceholder("Enter permissions separated by commas (e.g., READ, WRITE, DELETE)");
        permissionsField.setHelperText("Comma-separated list of permissions for this project");

        // Resource allocation fields (addressing "relqtwd" - Resource aLlocation Quantity With Time Due)
        allocatedHoursField = new BigDecimalField("Allocated Hours");
        allocatedHoursField.setPlaceholder("0.00");
        allocatedHoursField.setHelperText("Total hours allocated to this user for this project");

        hourlyRateField = new BigDecimalField("Hourly Rate");
        hourlyRateField.setPlaceholder("0.00");
        hourlyRateField.setHelperText("Hourly rate for this user on this project");

        startDatePicker = new DatePicker("Start Date");
        startDatePicker.setHelperText("Date when user assignment starts");

        dueDatePicker = new DatePicker("Due Date");
        dueDatePicker.setHelperText("Date when user assignment is due to complete");

        workloadPercentageField = new BigDecimalField("Workload %");
        workloadPercentageField.setPlaceholder("0.00");
        workloadPercentageField.setHelperText("Percentage of user's time allocated to this project (0-100%)");

        isActiveCheckbox = new Checkbox("Active Assignment");
        isActiveCheckbox.setHelperText("Whether this assignment is currently active");

        formLayout.add(projectComboBox, rolesField, permissionsField, 
                      allocatedHoursField, hourlyRateField, workloadPercentageField,
                      startDatePicker, dueDatePicker, isActiveCheckbox);

        if (!isNew) {

            if (data.getProject() != null) {
                projectService.getById(data.getProject().getId()).ifPresent(projectComboBox::setValue);
            }

            if (data.getRole() != null) {
                rolesField.setValue(data.getRole());
            }

            if (data.getPermission() != null) {
                permissionsField.setValue(data.getPermission());
            }

            // Populate resource allocation fields
            if (data.getAllocatedHours() != null) {
                allocatedHoursField.setValue(data.getAllocatedHours());
            }

            if (data.getHourlyRate() != null) {
                hourlyRateField.setValue(data.getHourlyRate());
            }

            if (data.getStartDate() != null) {
                startDatePicker.setValue(data.getStartDate());
            }

            if (data.getDueDate() != null) {
                dueDatePicker.setValue(data.getDueDate());
            }

            if (data.getWorkloadPercentage() != null) {
                workloadPercentageField.setValue(data.getWorkloadPercentage());
            }

            isActiveCheckbox.setValue(data.getIsActive() != null ? data.getIsActive() : Boolean.TRUE);
        } else {
            // Set defaults for new assignments
            isActiveCheckbox.setValue(Boolean.TRUE);
        }
    }

    /** Validates form fields. Throws exception if invalid. */
    @Override
    protected void validateForm() {

        if (projectComboBox.getValue() == null) {
            throw new IllegalArgumentException("Please select a project");
        }
        // Set user and project
        data.setUser(user);
        final CProject selectedProject = projectComboBox.getValue();

        if (selectedProject != null) {
            data.setProject(selectedProject);
        }
        data.setRole(rolesField.getValue());
        data.setPermission(permissionsField.getValue());

        // Set resource allocation fields
        data.setAllocatedHours(allocatedHoursField.getValue());
        data.setHourlyRate(hourlyRateField.getValue());
        data.setStartDate(startDatePicker.getValue());
        data.setDueDate(dueDatePicker.getValue());
        data.setWorkloadPercentage(workloadPercentageField.getValue());
        data.setIsActive(isActiveCheckbox.getValue());

        // Validate business rules
        validateResourceAllocation();
    }

    /**
     * Validates resource allocation business rules.
     * @throws IllegalArgumentException if validation fails
     */
    private void validateResourceAllocation() {
        // Validate date range
        if (startDatePicker.getValue() != null && dueDatePicker.getValue() != null) {
            if (startDatePicker.getValue().isAfter(dueDatePicker.getValue())) {
                throw new IllegalArgumentException("Start date cannot be after due date");
            }
        }

        // Validate workload percentage
        if (workloadPercentageField.getValue() != null) {
            BigDecimal workload = workloadPercentageField.getValue();
            if (workload.compareTo(BigDecimal.ZERO) < 0 || workload.compareTo(new BigDecimal("100")) > 0) {
                throw new IllegalArgumentException("Workload percentage must be between 0 and 100");
            }
        }

        // Validate allocated hours
        if (allocatedHoursField.getValue() != null && allocatedHoursField.getValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Allocated hours cannot be negative");
        }

        // Validate hourly rate
        if (hourlyRateField.getValue() != null && hourlyRateField.getValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Hourly rate cannot be negative");
        }
    }
}