package tech.derbent.abstracts.annotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.binder.BeanValidationBinder;

/**
 * Test class to verify CEntityFormBuilder improvements including: - Enhanced null pointer
 * checking - Better logging and error handling - Robust parameter validation
 */
class CEntityFormBuilderValidationTest {

	/**
	 * Test entity with various MetaData annotations and proper getters/setters
	 */
	public static class TestEntity {

		@MetaData(displayName = "Test String", required = true,
			description = "A test string field", order = 1, maxLength = 50,
			defaultValue = "test")
		private String testString;
		@MetaData(displayName = "Test Boolean", required = false,
			description = "A test boolean field", order = 2, defaultValue = "false")
		private Boolean testBoolean;

		public Boolean getTestBoolean() { return testBoolean; }

		// Getters and setters required for Vaadin binding
		public String getTestString() { return testString; }

		public void setTestBoolean(final Boolean testBoolean) {
			this.testBoolean = testBoolean;
		}

		public void setTestString(final String testString) {
			this.testString = testString;
		}
	}

	@Test
	@DisplayName("buildForm should handle null binder gracefully")
	void testBuildFormWithNullBinder() {
		final IllegalArgumentException exception =
			assertThrows(IllegalArgumentException.class,
				() -> CEntityFormBuilder.buildForm(TestEntity.class, null));
		assertEquals("Binder cannot be null", exception.getMessage());
	}

	@Test
	@DisplayName("buildForm should handle null entity class gracefully")
	void testBuildFormWithNullEntityClass() {
		final BeanValidationBinder<TestEntity> binder =
			new BeanValidationBinder<>(TestEntity.class);
		final IllegalArgumentException exception =
			assertThrows(IllegalArgumentException.class,
				() -> CEntityFormBuilder.buildForm(null, binder));
		assertEquals("Entity class cannot be null", exception.getMessage());
	}

	@Test
	@DisplayName("buildForm should handle entity class without MetaData annotations")
	void testBuildFormWithoutMetaData() {
		// Test entity without MetaData annotations
		class EntityWithoutMetaData {

			@SuppressWarnings("unused")
			private String plainField;
		}
		final BeanValidationBinder<EntityWithoutMetaData> binder =
			new BeanValidationBinder<>(EntityWithoutMetaData.class);
		final Div result =
			CEntityFormBuilder.buildForm(EntityWithoutMetaData.class, binder);
		assertNotNull(result, "Form should be created even without MetaData fields");
		assertEquals("editor-layout", result.getClassName());
		// Should have minimal content since no fields have MetaData
	}

	@Test
	@DisplayName("buildForm should successfully create form with valid parameters")
	void testBuildFormWithValidParameters() {
		final BeanValidationBinder<TestEntity> binder =
			new BeanValidationBinder<>(TestEntity.class);
		final Div result = CEntityFormBuilder.buildForm(TestEntity.class, binder);
		assertNotNull(result, "Form should be created successfully");
		assertEquals("editor-layout", result.getClassName());
		assertTrue(result.getChildren().count() > 0, "Form should contain components");
	}
}