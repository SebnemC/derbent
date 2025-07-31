package tech.derbent.comments.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tech.derbent.projects.domain.CProject;

/**
 * Unit tests for CCommentPriority domain class. Tests the priority creation, validation,
 * and basic operations.
 */
@DisplayName ("CCommentPriority Domain Tests")
class CCommentPriorityTest {

	private CCommentPriority priority;

	@BeforeEach
	void setUp() {
		// No common setup needed for individual tests
	}

	@Test
	@DisplayName ("Should set and get color")
	void testColorSetterGetter() {
		// Given
		priority = new CCommentPriority("High", new CProject());
		// When
		priority.setColor("#FF0000");
		// Then
		assertEquals("#FF0000", priority.getColor());
	}

	@Test
	@DisplayName ("Should inherit from CTypeEntity correctly")
	void testInheritance() {
		// Given
		priority = new CCommentPriority("Test Priority", new CProject());
		priority.setDescription("Test description"); // Set description explicitly
		// Then - Should have inherited fields from CTypeEntity -> CEntityNamed ->
		// CEntityDB
		assertNotNull(priority.getName());
		assertNotNull(priority.getDescription());
		// ID will be null until persisted, which is expected
	}
}