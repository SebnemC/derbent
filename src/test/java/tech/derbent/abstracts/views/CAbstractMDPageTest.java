package tech.derbent.abstracts.views;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.services.CAbstractService;

/**
 * Test class for CAbstractMDPage to verify the details view tab functionality.
 */
@ExtendWith(MockitoExtension.class)
class CAbstractMDPageTest {

	/**
	 * Test entity for testing purposes.
	 */
	private static class TestEntity extends CEntityDB {
		
		private String name;
		
		public String getName() { return name; }
		
		public void setName(final String name) { this.name = name; }
		
		// Test implementation
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
	void testCreateButtonLayoutIsEmptyByDefault() {
		// Arrange
		final Div testLayout = new Div();
		// Act
		testPage.createButtonLayout(testLayout);
		// Assert The new implementation should not add any buttons to the main layout
		assertEquals(0, testLayout.getChildren().count(),
			"createButtonLayout should not add buttons to main layout anymore");
	}

	@Test
	void testCreateDetailsTabButtonLayout() {
		// Act
		final HorizontalLayout buttonLayout = testPage.createDetailsTabButtonLayout();
		// Assert
		assertNotNull(buttonLayout, "Button layout should not be null");
		assertTrue(buttonLayout.getClassName().contains("details-tab-button-layout"),
			"Button layout should have correct CSS class");
		// Verify that buttons are present (Save, New, Cancel, Delete)
		final long buttonCount =
			buttonLayout.getChildren().filter(CButton.class::isInstance).count();
		assertEquals(4, buttonCount,
			"Should have exactly 4 buttons (Save, New, Cancel, Delete)");
	}

	@Test
	void testCreateDetailsTabLeftContent() {
		// Act
		final Div leftContent = testPage.createDetailsTabLeftContent();
		// Assert
		assertNotNull(leftContent, "Left content should not be null");
		assertEquals("Details", leftContent.getText(),
			"Default left content should be 'Details'");
		assertTrue(leftContent.getClassName().contains("details-tab-label"),
			"Left content should have correct CSS class");
	}

	@Test
	void testDetailsTabLayoutContainsButtons() {
		// Act
		testPage.createDetailsTabLayout();
		// Assert
		final Div detailsTabLayout = testPage.getDetailsTabLayout();
		assertNotNull(detailsTabLayout, "Details tab layout should not be null");
		// Verify that the tab layout has content
		assertTrue(detailsTabLayout.getChildren().count() > 0,
			"Details tab layout should have content");
		// Find the HorizontalLayout that should contain the tab content
		final HorizontalLayout tabContent =
			detailsTabLayout.getChildren().filter(HorizontalLayout.class::isInstance)
				.map(HorizontalLayout.class::cast).findFirst().orElse(null);
		assertNotNull(tabContent, "Tab content layout should exist");
		assertTrue(tabContent.getClassName().contains("details-tab-content"),
			"Tab content should have correct CSS class");
	}

	@Test
	void testClearFormResetsCurrentEntity() {
		// Arrange
		final TestEntity testEntity = new TestEntity();
		testEntity.setName("Test Entity");
		testPage.setCurrentEntity(testEntity);
		
		// Act
		testPage.clearForm();
		
		// Assert - clearForm should now set currentEntity to a new entity, not null
		assertNotNull(testPage.getCurrentEntity(), 
			"Current entity should be a new entity after clearing form");
		assertNull(testPage.getCurrentEntity().getName(),
			"Current entity should have null name after clearing form");
		assertNull(testPage.getCurrentEntity().getId(),
			"Current entity should have null ID after clearing form (indicating it's new)");
	}

	@Test
	void testClearFormClearsBinderData() {
		// Arrange
		final TestEntity testEntity = new TestEntity();
		testEntity.setName("Test Entity");
		
		// Populate form with entity data
		testPage.populateForm(testEntity);
		assertNotNull(testPage.getCurrentEntity(), 
			"Current entity should be set after populate");
		assertEquals("Test Entity", testPage.getCurrentEntity().getName(),
			"Entity name should be populated");
		
		// Act
		testPage.clearForm();
		
		// Assert - Form should be cleared and ready for new entity creation
		assertNotNull(testPage.getCurrentEntity(), 
			"Current entity should be a new entity after clearing form");
		assertNull(testPage.getCurrentEntity().getName(),
			"New entity should have null name field after clearing form");
		assertNull(testPage.getCurrentEntity().getId(),
			"New entity should have null ID (indicating it's new)");
	}

	@Test
	void testButtonsHaveIcons() {
		// Act - Create buttons
		final CButton saveButton = testPage.createSaveButton("Save");
		final CButton newButton = testPage.createNewButton("New");
		final CButton cancelButton = testPage.createCancelButton("Cancel");
		final CButton deleteButton = testPage.createDeleteButton("Delete");
		
		// Assert - All buttons should have icons (checking that getIcon() doesn't return null)
		assertNotNull(saveButton.getIcon(), "Save button should have an icon");
		assertNotNull(newButton.getIcon(), "New button should have an icon");
		assertNotNull(cancelButton.getIcon(), "Cancel button should have an icon");
		assertNotNull(deleteButton.getIcon(), "Delete button should have an icon");
	}
}