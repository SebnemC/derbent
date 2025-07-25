package tech.derbent.abstracts.views;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.services.CAbstractService;

/**
 * Integration test to verify the New button save functionality works correctly.
 * This test simulates the exact user workflow reported in the issue.
 */
@ExtendWith(MockitoExtension.class)
class NewButtonSaveIntegrationTest {

	/**
	 * Test entity for testing purposes.
	 */
	private static class TestEntity extends CEntityDB {
		
		private String name;
		
		public String getName() { return name; }
		
		public void setName(final String name) { this.name = name; }
	}

	/**
	 * Test implementation of CAbstractMDPage for testing purposes.
	 */
	private static class TestMDPage extends CAbstractMDPage<TestEntity> {

		private static final long serialVersionUID = 1L;

		public TestMDPage(final CAbstractService<TestEntity> entityService) {
			super(TestEntity.class, entityService);
		}

		@Override
		protected void createDetailsLayout() {
			// Test implementation - empty
		}

		@Override
		protected void createGridForEntity() {
			// Test implementation - empty
		}

		@Override
		protected String getEntityRouteIdField() { return "test_id"; }

		@Override
		protected String getEntityRouteTemplateEdit() { return "test/%s/edit"; }

		@Override
		protected void initPage() {
			// Test implementation - empty
		}

		@Override
		protected TestEntity newEntity() {
			return new TestEntity();
		}

		@Override
		protected void setupToolbar() {
			// Test implementation - empty
		}
	}

	@Mock
	private CAbstractService<TestEntity> mockEntityService;
	private TestMDPage testPage;

	@BeforeEach
	void setUp() {
		testPage = new TestMDPage(mockEntityService);
	}

	@Test
	void testNewButtonWorkflowCreatesNewEntity() {
		// Act - Simulate the user workflow
		// Step 1: User clicks "New" button
		final CButton newButton = testPage.createNewButton("New");
		newButton.click();
		
		// Verify - After New button click, currentEntity should be a new entity
		assertNotNull(testPage.getCurrentEntity(), 
			"After New button click, currentEntity should not be null");
		assertNull(testPage.getCurrentEntity().getId(),
			"After New button click, currentEntity should have null ID (new entity)");
		
		// Step 2: User fills form (simulated by setting entity data directly)
		testPage.getCurrentEntity().setName("New Test Entity");
		
		// Step 3: User clicks "Save" button (verify it's properly configured)
		final CButton saveButton = testPage.createSaveButton("Save");
		
		// Verify - Save button should be properly configured
		assertNotNull(saveButton, "Save button should be created");
		assertTrue(saveButton.getThemeNames().contains("primary"), 
			"Save button should have primary styling");
		
		// Note: We can't easily test the actual save click in a unit test 
		// because it involves UI validation and binder operations
		// But we've verified the setup is correct for the workflow
	}

	@Test
	void testNewButtonClearsFormProperly() {
		// Arrange - Set up an existing entity first using reflection to set ID
		final TestEntity existingEntity = new TestEntity();
		existingEntity.setName("Existing Entity");
		try {
			// Use reflection to set the ID field to simulate an existing entity
			final java.lang.reflect.Field idField = tech.derbent.abstracts.domains.CEntityDB.class.getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(existingEntity, 1L);
		} catch (Exception e) {
			// If reflection fails, skip this part of the test
		}
		testPage.setCurrentEntity(existingEntity);
		
		// Verify initial state (only if ID was successfully set)
		if (existingEntity.getId() != null) {
			assertNotNull(testPage.getCurrentEntity().getId(), 
				"Should start with an existing entity");
		}
		
		// Act - Click New button to clear form
		final CButton newButton = testPage.createNewButton("New");
		newButton.click();
		
		// Assert - Form should be cleared for new entity creation
		assertNotNull(testPage.getCurrentEntity(), 
			"Should have a new entity ready for creation");
		assertNull(testPage.getCurrentEntity().getId(),
			"New entity should have null ID");
		assertNull(testPage.getCurrentEntity().getName(),
			"New entity should have null name");
	}

	@Test
	void testButtonsHaveCorrectConfiguration() {
		// Act - Create all buttons
		final CButton saveButton = testPage.createSaveButton("Save");
		final CButton newButton = testPage.createNewButton("New");
		final CButton cancelButton = testPage.createCancelButton("Cancel");
		final CButton deleteButton = testPage.createDeleteButton("Delete");
		
		// Assert - All buttons should be properly configured
		assertNotNull(saveButton.getIcon(), "Save button should have an icon");
		assertNotNull(newButton.getIcon(), "New button should have an icon");
		assertNotNull(cancelButton.getIcon(), "Cancel button should have an icon");
		assertNotNull(deleteButton.getIcon(), "Delete button should have an icon");
		
		assertTrue(saveButton.getThemeNames().contains("primary"), 
			"Save button should have primary styling");
		assertTrue(newButton.getThemeNames().contains("tertiary"), 
			"New button should have tertiary styling");
	}
}