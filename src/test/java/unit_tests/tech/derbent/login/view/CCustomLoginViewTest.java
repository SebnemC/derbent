package unit_tests.tech.derbent.login.view;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

import com.microsoft.playwright.Page;

import ui_tests.tech.derbent.ui.automation.CBaseUITest;

/**
 * UI tests for the custom login view functionality.
 */
@SpringBootTest (
	webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class
)
@TestPropertySource (properties = {
	"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa",
	"spring.datasource.password=", "spring.datasource.driver-class-name=org.h2.Driver",
	"spring.datasource.url=jdbc:h2:mem:testdb",
	"spring.jpa.hibernate.ddl-auto=create-drop", "server.port=8080" }
)
public class CCustomLoginViewTest extends CBaseUITest {

	static final Logger LOGGER = LoggerFactory.getLogger(CCustomLoginViewTest.class);

	@Test
	void testCustomLoginFormValidation() {
		LOGGER.info("🧪 Testing custom login form validation...");

		if (page == null) {
			LOGGER.warn("⚠️ Browser not available, skipping UI test");
			return;
		}
		// Navigate to custom login (route is "login" for CCustomLoginView)
		page.navigate(baseUrl + "/login");
		// Wait for the page to load
		page.waitForSelector(".custom-login-view",
			new Page.WaitForSelectorOptions().setTimeout(10000));
		// Try to submit empty form
		page.locator("#custom-submit-button").click();
		// Check for validation error (should show error message) Wait a moment for the
		// error to appear
		page.waitForTimeout(1000);
		// The error message should be visible
		assertTrue(
			page.locator("text=Please enter both username and password").isVisible());
		LOGGER.info("✅ Custom login form validation test completed");
	}

	@Test
	void testCustomLoginFormWithCredentials() {
		LOGGER.info("🧪 Testing custom login form with credentials...");

		if (page == null) {
			LOGGER.warn("⚠️ Browser not available, skipping UI test");
			return;
		}
		// Navigate to custom login (route is "login" for CCustomLoginView)
		page.navigate(baseUrl + "/login");
		// Wait for the page to load
		page.waitForSelector(".custom-login-view",
			new Page.WaitForSelectorOptions().setTimeout(10000));
		// Fill in credentials
		page.locator("#custom-username-input").fill("admin");
		page.locator("#custom-password-input").fill("test123");
		// Submit form
		page.locator("#custom-submit-button").click();
		// Should be redirected to dashboard or main page after successful login Wait a
		// moment for potential redirect
		page.waitForTimeout(3000);
		// Verify we're no longer on login page (successful login)
		final String currentUrl = page.url();
		assertTrue(!currentUrl.contains("login"));
		LOGGER.info("✅ Custom login form with credentials test completed");
	}

	@Test
	void testCustomLoginViewLoads() {
		LOGGER.info("🧪 Testing custom login view loads...");

		if (page == null) {
			LOGGER.warn("⚠️ Browser not available, skipping UI test");
			return;
		}
		// Navigate directly to custom login (route is "login" for CCustomLoginView)
		page.navigate(baseUrl + "/login");
		// Wait for the page to load
		page.waitForSelector(".custom-login-view",
			new Page.WaitForSelectorOptions().setTimeout(10000));
		// Verify page title
		final String title = page.title();
		assertNotNull(title);
		assertTrue(title.contains("Login"));
		LOGGER.info("✅ Custom login view title: {}", title);
		// Verify custom login form elements are present
		assertTrue(page.locator("#custom-username-input").isVisible());
		assertTrue(page.locator("#custom-password-input").isVisible());
		assertTrue(page.locator("#custom-submit-button").isVisible());
		// Verify back link to original login is present (button text from
		// CCustomLoginView)
		assertTrue(page.locator("text=Back to Original Login").isVisible());
		LOGGER.info("✅ Custom login view loads test completed");
	}

	@Test
	void testNavigationFromCustomToOriginalLogin() {
		LOGGER.info("🧪 Testing navigation from custom to original login...");

		if (page == null) {
			LOGGER.warn("⚠️ Browser not available, skipping UI test");
			return;
		}
		// Navigate to custom login (route is "login" for CCustomLoginView)
		page.navigate(baseUrl + "/login");
		// Wait for the page to load
		page.waitForSelector(".custom-login-view",
			new Page.WaitForSelectorOptions().setTimeout(10000));
		// Click back to original login link (button text from CCustomLoginView)
		page.locator("text=Back to Original Login").click();
		// Verify we're now on original login page (route is "overlay-login" for
		// CLoginView)
		page.waitForSelector("vaadin-login-overlay",
			new Page.WaitForSelectorOptions().setTimeout(10000));
		assertTrue(page.url().contains("overlay-login"));
		LOGGER.info("✅ Navigation from custom to original login test completed");
	}

	@Test
	void testNavigationFromOriginalToCustomLogin() {
		LOGGER.info("🧪 Testing navigation from original to custom login...");

		if (page == null) {
			LOGGER.warn("⚠️ Browser not available, skipping UI test");
			return;
		}
		// Navigate to original login (route is "overlay-login" for CLoginView)
		page.navigate(baseUrl + "/overlay-login");
		// Wait for login overlay
		page.waitForSelector("vaadin-login-overlay",
			new Page.WaitForSelectorOptions().setTimeout(10000));
		// Look for the custom login link (from CLoginView footer)
		page.locator("text=Try Custom Login").click();
		// Verify we're now on custom login page (route is "login" for CCustomLoginView)
		page.waitForSelector(".custom-login-view",
			new Page.WaitForSelectorOptions().setTimeout(10000));
		assertTrue(page.url().contains("login"));
		assertTrue(!page.url().contains("overlay-login"));
		LOGGER.info("✅ Navigation from original to custom login test completed");
	}
}