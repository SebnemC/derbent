package tech.derbent.abstracts.annotations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.component.html.Div;

/**
 * Test class to verify CEntityFormBuilder improvements including:
 * - Enhanced null pointer checking
 * - Better logging and error handling
 * - Robust parameter validation
 */
class CEntityFormBuilderValidationTest {

	/**
	 * Test entity with various MetaData annotations and proper getters/setters
	 */
	public static class TestEntity {
		
		@MetaData(
			displayName = "Test String",
			required = true,
			description = "A test string field",
			order = 1,
			maxLength = 50,
			defaultValue = "test"
		)
		private String testString;
		
		@MetaData(
			displayName = "Test Boolean",
			required = false,
			description = "A test boolean field",
			order = 2,
			defaultValue = "false"
		)
		private Boolean testBoolean;

		// Getters and setters required for Vaadin binding
		public String getTestString() {
			return testString;
		}

		public void setTestString(String testString) {
			this.testString = testString;
		}

		public Boolean getTestBoolean() {
			return testBoolean;
		}

		public void setTestBoolean(Boolean testBoolean) {
			this.testBoolean = testBoolean;
		}
	}
	
	@Test
	@DisplayName("buildForm should handle null entity class gracefully")
	void testBuildFormWithNullEntityClass() {
		BeanValidationBinder<TestEntity> binder = new BeanValidationBinder<>(TestEntity.class);
		
		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class, 
			() -> CEntityFormBuilder.buildForm(null, binder)
		);
		
		assertEquals("Entity class cannot be null", exception.getMessage());
	}
	
	@Test
	@DisplayName("buildForm should handle null binder gracefully")
	void testBuildFormWithNullBinder() {
		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class, 
			() -> CEntityFormBuilder.buildForm(TestEntity.class, null)
		);
		
		assertEquals("Binder cannot be null", exception.getMessage());
	}
	
	@Test
	@DisplayName("buildForm should successfully create form with valid parameters")
	void testBuildFormWithValidParameters() {
		BeanValidationBinder<TestEntity> binder = new BeanValidationBinder<>(TestEntity.class);
		
		Div result = CEntityFormBuilder.buildForm(TestEntity.class, binder);
		
		assertNotNull(result, "Form should be created successfully");
		assertEquals("editor-layout", result.getClassName());
		assertTrue(result.getChildren().count() > 0, "Form should contain components");
	}
	
	@Test
	@DisplayName("buildForm should handle entity class without MetaData annotations")
	void testBuildFormWithoutMetaData() {
		// Test entity without MetaData annotations
		class EntityWithoutMetaData {
			private String plainField;
		}
		
		BeanValidationBinder<EntityWithoutMetaData> binder = new BeanValidationBinder<>(EntityWithoutMetaData.class);
		
		Div result = CEntityFormBuilder.buildForm(EntityWithoutMetaData.class, binder);
		
		assertNotNull(result, "Form should be created even without MetaData fields");
		assertEquals("editor-layout", result.getClassName());
		// Should have minimal content since no fields have MetaData
	}
}