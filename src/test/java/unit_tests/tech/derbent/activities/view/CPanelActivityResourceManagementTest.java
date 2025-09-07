package unit_tests.tech.derbent.activities.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.view.CPanelActivityResourceManagement;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/** Test class for CPanelActivityResourceManagement to ensure proper field grouping. */
@ExtendWith (MockitoExtension.class)
class CPanelActivityResourceManagementTest extends CTestBase {

	private CPanelActivityResourceManagement panel;
	private CEnhancedBinder<CActivity> binder;

	@Override
	protected void setupForTest() {
		binder = new CEnhancedBinder<>(CActivity.class);
	}

	@Test
	void testFieldGrouping() throws Exception {
		// When
		panel = new CPanelActivityResourceManagement(testActivity, binder, activityService);
		// Then
		assertNotNull(panel.getEntityFields(), "Entity fields should be set");
		assertEquals(2, panel.getEntityFields().size(), "Should have exactly 2 resource management fields");
		// Verify correct fields are included
		assertEquals("assignedTo", panel.getEntityFields().get(0), "First field should be assignedTo");
		assertEquals("createdBy", panel.getEntityFields().get(1), "Second field should be createdBy");
	}

	@Test
	void testPanelCreation() throws Exception {
		// When
		panel = new CPanelActivityResourceManagement(testActivity, binder, activityService);
		// Then
		assertNotNull(panel, "Panel should be created successfully");
		assertEquals("Resource Management", panel.getAccordionTitle(), "Panel should have correct title");
	}

	@Test
	void testPopulateFormWithValidEntity() throws Exception {
		// Given
		panel = new CPanelActivityResourceManagement(testActivity, binder, activityService);
		final CActivity newActivity = new CActivity("Test Activity", project);
		newActivity.setName("New Activity");
		// When
		panel.populateForm(newActivity);
		// Then - Should not throw exception Test passes if no exception is thrown
	}
}
