package tech.derbent.users.view;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.TextField;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.roles.domain.CUserProjectRole;
import tech.derbent.api.roles.service.CUserProjectRoleService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;

/** Unit tests for CUserProjectSettingsComponent to verify component creation, binding, data population, and integration with services. */
@ExtendWith (MockitoExtension.class)
public class CUserProjectSettingsComponentTest {

	@Mock
	private CProjectService projectService;
	@Mock
	private CUserProjectRoleService roleService;
	private CUserProjectSettingsComponent component;
	private CUser testUser;
	private CProject testProject;
	private CUserProjectRole testRole;
	private CUserProjectSettings testSettings;

	@BeforeEach
	void setUp() {
		// Create test data
		testUser = new CUser();
		testUser.setName("Test User");
		testUser.setLogin("testuser");
		testProject = new CProject();
		testProject.setName("Test Project");
		testRole = new CUserProjectRole("Test Role", testProject);
		testSettings = new CUserProjectSettings();
		testSettings.setUser(testUser);
		testSettings.setProject(testProject);
		testSettings.setRole(testRole);
		testSettings.setPermission("READ,WRITE");
		// Create component with mocked services
		component = new CUserProjectSettingsComponent(projectService, roleService);
	}

	@Test
	void testComponentCreation() {
		// Verify component is created successfully
		assertNotNull(component, "Component should be created");
		// Verify UI components are created
		ComboBox<CProject> projectComboBox = component.getProjectComboBox();
		ComboBox<CUserProjectRole> roleComboBox = component.getRoleComboBox();
		TextField permissionField = component.getPermissionField();
		assertNotNull(projectComboBox, "Project ComboBox should be created");
		assertNotNull(roleComboBox, "Role ComboBox should be created");
		assertNotNull(permissionField, "Permission field should be created");
		// Verify basic UI configuration
		assertEquals("Project", projectComboBox.getLabel());
		assertEquals("Role", roleComboBox.getLabel());
		assertEquals("Permissions", permissionField.getLabel());
	}

	@Test
	void testDataPopulation() {
		// Mock service responses
		List<CProject> mockProjects = Arrays.asList(testProject);
		List<CUserProjectRole> mockRoles = Arrays.asList(testRole);
		when(projectService.getAvailableProjectsForUser(anyLong())).thenReturn(mockProjects);
		when(roleService.findAll()).thenReturn(mockRoles);
		// Test data population - create a minimal user for this test
		CUser user = new CUser();
		user.setName("Test User");
		component.populateData(user, testSettings);
		// Verify services were called
		verify(roleService).findAll();
		// Verify ComboBox items are set
		ComboBox<CProject> projectComboBox = component.getProjectComboBox();
		ComboBox<CUserProjectRole> roleComboBox = component.getRoleComboBox();
		assertNotNull(projectComboBox.getGenericDataView().getItems());
		assertNotNull(roleComboBox.getGenericDataView().getItems());
	}

	@Test
	void testBinderIntegration() {
		// Create external binder and bind component to it
		CEnhancedBinder<CUserProjectSettings> externalBinder = new CEnhancedBinder<>(CUserProjectSettings.class);
		component.bindToExternalBinder(externalBinder);
		// Mock service data
		List<CProject> mockProjects = Arrays.asList(testProject);
		List<CUserProjectRole> mockRoles = Arrays.asList(testRole);
		when(projectService.getAvailableProjectsForUser(anyLong())).thenReturn(mockProjects);
		when(roleService.findAll()).thenReturn(mockRoles);
		// Populate with test data
		component.populateData(testUser, testSettings);
		// Get current settings should return the bound data
		CUserProjectSettings currentSettings = component.getCurrentSettings();
		assertNotNull(currentSettings, "Current settings should not be null");
	}

	@Test
	void testValidation() {
		// Create binder and populate
		CEnhancedBinder<CUserProjectSettings> binder = new CEnhancedBinder<>(CUserProjectSettings.class);
		component.bindToExternalBinder(binder);
		// Mock service data
		when(projectService.getAvailableProjectsForUser(anyLong())).thenReturn(Arrays.asList(testProject));
		when(roleService.findAll()).thenReturn(Arrays.asList(testRole));
		component.populateData(testUser, testSettings);
		// Validation should pass with valid data
		boolean validationResult = component.validate();
		assertTrue(validationResult, "Validation should pass with valid data");
	}

	@Test
	void testClearFields() {
		// Populate component first
		when(projectService.getAvailableProjectsForUser(anyLong())).thenReturn(Arrays.asList(testProject));
		when(roleService.findAll()).thenReturn(Arrays.asList(testRole));
		component.populateData(testUser, testSettings);
		// Clear fields
		component.clear();
		// Verify fields are cleared
		ComboBox<CProject> projectComboBox = component.getProjectComboBox();
		ComboBox<CUserProjectRole> roleComboBox = component.getRoleComboBox();
		TextField permissionField = component.getPermissionField();
		assertTrue(projectComboBox.isEmpty(), "Project ComboBox should be empty after clear");
		assertTrue(roleComboBox.isEmpty(), "Role ComboBox should be empty after clear");
		assertTrue(permissionField.isEmpty(), "Permission field should be empty after clear");
	}

	@Test
	void testComponentAccessors() {
		// Test that component accessors return the correct UI components
		ComboBox<CProject> projectComboBox = component.getProjectComboBox();
		ComboBox<CUserProjectRole> roleComboBox = component.getRoleComboBox();
		TextField permissionField = component.getPermissionField();
		assertNotNull(projectComboBox, "Project ComboBox accessor should return component");
		assertNotNull(roleComboBox, "Role ComboBox accessor should return component");
		assertNotNull(permissionField, "Permission field accessor should return component");
		// Verify these are the actual UI components (have proper configuration)
		assertTrue(projectComboBox.isRequiredIndicatorVisible(), "Project field should be required");
		assertEquals("Select a project", projectComboBox.getPlaceholder());
		assertEquals("Select a role", roleComboBox.getPlaceholder());
		assertEquals("Enter custom permissions (optional)", permissionField.getPlaceholder());
	}

	@Test
	void testNullSafetyChecks() {
		// Test constructor null safety
		assertThrows(IllegalArgumentException.class, () -> {
			new CUserProjectSettingsComponent(null, roleService);
		}, "Should throw exception for null project service");
		assertThrows(IllegalArgumentException.class, () -> {
			new CUserProjectSettingsComponent(projectService, null);
		}, "Should throw exception for null role service");
		// Test populateData null safety
		assertThrows(IllegalArgumentException.class, () -> {
			component.populateData(null, testSettings);
		}, "Should throw exception for null user");
	}
}
