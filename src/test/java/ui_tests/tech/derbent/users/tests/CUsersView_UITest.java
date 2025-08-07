package ui_tests.tech.derbent.users.tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.derbent.projects.view.CProjectsView;
import tech.derbent.users.view.CUsersView;
import ui_tests.tech.derbent.ui.automation.CBaseUITest;

/**
 * CUsersViewPlaywrightTest - Comprehensive Playwright tests for the Users view. Tests all
 * aspects of the Users view including CRUD operations, grid interactions, form
 * validation, ComboBox selections, and UI behaviors following the strict coding
 * guidelines for Playwright testing.
 */
public class CUsersView_UITest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CUsersView_UITest.class);

	@Test
	void testUsersComboBoxes() {
		LOGGER.info("🧪 Testing Users ComboBox components...");
		navigateToViewByClass(CUsersView.class);
		clickNew();
		// Test User Role ComboBox
		final var comboBoxes = page.locator("vaadin-combo-box");

		if (comboBoxes.count() > 0) {
			LOGGER.debug("Testing User Role ComboBox");
			// Click to open first ComboBox
			comboBoxes.first().click();
			wait_500();
			// Check options are available
			final var options = page.locator("vaadin-combo-box-item");
			final int optionCount = options.count();
			LOGGER.debug("Found {} options in User Role ComboBox", optionCount);

			if (optionCount > 0) {
				// Select first option
				options.first().click();
				wait_500();
			}
			takeScreenshot("users-role-combobox");
		}

		// Test Company ComboBox if available
		if (comboBoxes.count() > 1) {
			LOGGER.debug("Testing Company ComboBox");
			comboBoxes.nth(1).click();
			wait_500();
			takeScreenshot("users-company-combobox");
			// Select option if available
			final var options = page.locator("vaadin-combo-box-item");

			if (options.count() > 0) {
				options.first().click();
				wait_500();
			}
		}

		// Test User Type ComboBox if available
		if (comboBoxes.count() > 2) {
			LOGGER.debug("Testing User Type ComboBox");
			comboBoxes.nth(2).click();
			wait_500();
			takeScreenshot("users-type-combobox");
			// Close by clicking elsewhere
			page.click("body");
			wait_500();
		}
		clickCancel();
		LOGGER.info("✅ Users ComboBox test completed");
	}

	@Test
	void testUsersCompleteWorkflow() {
		LOGGER.info("🧪 Testing Users complete workflow...");
		navigateToViewByClass(CUsersView.class);
		LOGGER.debug("Initial grid has {} rows", getGridRowCount());
		// Create new user
		clickNew();
		takeScreenshot("users-workflow-new-form");
		// Fill user name (first name)
		final String firstName = "TestUser" + System.currentTimeMillis();

		if (fillFirstTextField(firstName)) {
			LOGGER.debug("Filled first name: {}", firstName);
		}
		// Fill lastname if available
		final var textFields = page.locator("vaadin-text-field");

		if (textFields.count() > 1) {
			textFields.nth(1).fill("TestLastname");
		}

		// Fill email if available
		if (textFields.count() > 2) {
			textFields.nth(2).fill("test" + System.currentTimeMillis() + "@example.com");
		}

		// Fill login if available
		if (textFields.count() > 3) {
			textFields.nth(3).fill("testuser" + System.currentTimeMillis());
		}
		takeScreenshot("users-workflow-form-filled");
		// Save
		clickSave();
		LOGGER.info("✅ Users complete workflow test completed");
	}

	@Test
	void testUsersCRUDOperations() {
		LOGGER.info("🧪 Testing Users CRUD operations...");
		navigateToViewByClass(CUsersView.class);
		// Use the auxiliary CRUD testing method
		testCRUDOperationsInView("Users", "new-button", "save-button", "delete-button");
		LOGGER.info("✅ Users CRUD operations test completed");
	}

	@Test
	void testUsersEntityRelationGrid() {
		LOGGER.info("🧪 Testing Users entity relation grid display...");
		// Test that user type, company, and other relations are displayed in grid
		testEntityRelationGrid(CUsersView.class);
		LOGGER.info("✅ Users entity relation grid test completed");
	}

	@Test
	void testUsersFormValidation() {
		LOGGER.info("🧪 Testing Users form validation...");
		navigateToViewByClass(CUsersView.class);
		// Try to create new user
		clickNew();
		final boolean validationWorking = testFormValidationById("save-button");
		LOGGER.debug("Form validation working: {}", validationWorking);
		takeScreenshot("users-form-validation");
		// Test email validation specifically
		final var emailFields =
			page.locator("vaadin-text-field[type='email'], vaadin-email-field");

		if (emailFields.count() > 0) {
			emailFields.first().fill("invalid-email");
			wait_500();
			takeScreenshot("users-email-validation");
		}
		clickCancel();
		LOGGER.info("✅ Users form validation test completed");
	}

	@Test
	void testUsersGridInteractions() {
		LOGGER.info("🧪 Testing Users grid interactions...");
		testAdvancedGridInView(CUsersView.class);
		// Additional grid interaction testing for users
		navigateToViewByClass(CUsersView.class);
		// Test grid selection changes
		final int gridRowCount = getGridRowCount();

		if (gridRowCount > 0) {
			LOGGER.debug("Testing grid selection with {} rows", gridRowCount);
			clickGrid(0); // Select first row
			wait_500();
			// Test that selection triggers form population
			final var textFields = page.locator("vaadin-text-field");

			if (textFields.count() > 0) {
				final String firstFieldValue = textFields.first().inputValue();
				LOGGER.debug("First field populated with: {}", firstFieldValue);
				assertTrue(firstFieldValue != null && !firstFieldValue.trim().isEmpty(),
					"Grid selection should populate form fields");
			}
		}
		LOGGER.info("✅ Users grid interactions test completed");
	}

	@Test
	void testUsersNavigation() {
		testNavigationTo(CUsersView.class, CProjectsView.class);
	}

	@Test
	void testUsersProfilePictureDisplay() {
		LOGGER.info("🧪 Testing Users profile picture display in grid...");
		navigateToViewByClass(CUsersView.class);
		// Wait for grid to load
		wait_2000();
		// Check if profile pictures are displayed in the grid
		final var profileImages = page.locator("vaadin-grid img");
		final int imageCount = profileImages.count();
		LOGGER.debug("Found {} profile images in grid", imageCount);
		assertTrue(imageCount > 0, "Should find profile images in the grid");

		// Check that images have src attributes (not broken)
		for (int i = 0; i < Math.min(imageCount, 5); i++) {
			final var image = profileImages.nth(i);
			final String src = image.getAttribute("src");
			assertNotNull(src, "Profile image should have src attribute");
			assertFalse(src.isEmpty(), "Profile image src should not be empty");
			LOGGER.debug("Profile image {} src: {}", i,
				src.length() > 50 ? src.substring(0, 50) + "..." : src);
		}
		takeScreenshot("users-profile-pictures-grid-validation");
		LOGGER.info("✅ Users profile picture display test completed");
	}

	@Test
	void testUsersProfilePictureHandling() {
		LOGGER.info("🧪 Testing Users profile picture handling...");
		navigateToViewByClass(CUsersView.class);
		// Check if profile pictures are displayed in the grid
		final var profileImages = page.locator("vaadin-grid img");

		if (profileImages.count() > 0) {
			LOGGER.debug("Found {} profile images in grid", profileImages.count());
			takeScreenshot("users-profile-pictures-grid");
		}
		clickNew();
		// Check if profile picture upload is available
		final var uploadComponents = page.locator("vaadin-upload, input[type='file']");

		if (uploadComponents.count() > 0) {
			LOGGER.debug("Profile picture upload component found");
			takeScreenshot("users-profile-picture-upload");
		}
		clickCancel();
		LOGGER.info("✅ Users profile picture handling test completed");
	}

	@Test
	void testUsersSearchFunctionality() {
		LOGGER.info("🧪 Testing Users search functionality...");
		// Test search with common user fields
		testSearchFunctionality(CUsersView.class, "admin");
		LOGGER.info("✅ Users search functionality test completed");
	}
}
