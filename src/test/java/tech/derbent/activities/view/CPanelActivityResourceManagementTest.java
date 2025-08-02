package tech.derbent.activities.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.abstracts.domains.CTestBase;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.CSessionService;

/**
 * Test class for CPanelActivityResourceManagement to ensure proper field grouping.
 */
@ExtendWith (MockitoExtension.class)
class CPanelActivityResourceManagementTest extends CTestBase {

	@Mock
	private CActivityService activityService;

	@Mock
	private CSessionService sessionService;

	private CPanelActivityResourceManagement panel;

	private CActivity testActivity;

	private CProject project;

	private BeanValidationBinder<CActivity> binder;

	@BeforeEach
	void setUp() {
		project = new CProject("Test Project");
		testActivity = new CActivity("Test Activity", project);
		testActivity.setName("Test Activity");
		binder = new BeanValidationBinder<>(CActivity.class);
	}

	@Test
	void testFieldGrouping() {
		// When
		panel =
			new CPanelActivityResourceManagement(testActivity, binder, activityService);
		// Then
		assertNotNull(panel.getEntityFields(), "Entity fields should be set");
		assertEquals(2, panel.getEntityFields().size(),
			"Should have exactly 2 resource management fields");
		// Verify correct fields are included
		assertEquals("assignedTo", panel.getEntityFields().get(0),
			"First field should be assignedTo");
		assertEquals("createdBy", panel.getEntityFields().get(1),
			"Second field should be createdBy");
	}

	@Test
	void testPanelCreation() {
		// When
		panel =
			new CPanelActivityResourceManagement(testActivity, binder, activityService);
		// Then
		assertNotNull(panel, "Panel should be created successfully");
		assertNotNull(panel.getBaseLayout(), "Base layout should be initialized");
		assertEquals("Resource Management", panel.getAccordionTitle(),
			"Panel should have correct title");
	}

	@Test
	void testPopulateFormWithValidEntity() {
		// Given
		panel =
			new CPanelActivityResourceManagement(testActivity, binder, activityService);
		final CActivity newActivity = new CActivity("Test Activity", project);
		newActivity.setName("New Activity");
		// When
		panel.populateForm(newActivity);
		// Then - Should not throw exception Test passes if no exception is thrown
	}

	@Override
	protected void setupForTest() {
		// TODO Auto-generated method stub
		
	}
}