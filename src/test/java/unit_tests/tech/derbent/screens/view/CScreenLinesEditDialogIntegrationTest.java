package unit_tests.tech.derbent.screens.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.domain.CScreenLines;
import tech.derbent.screens.service.CEntityFieldService;
import tech.derbent.screens.service.CViewsService;

/**
 * Integration test for the enhanced CScreenLinesEditDialog functionality. Tests the interaction between CViewsService
 * and CEntityFieldService.
 */
class CScreenLinesEditDialogIntegrationTest {

    private CViewsService viewsService;
    private CEntityFieldService entityFieldService;
    private CScreen testScreen;

    @BeforeEach
    void setUp() {
        viewsService = new CViewsService();
        entityFieldService = new CEntityFieldService();

        // Create a test screen for CActivity
        testScreen = new CScreen();
        testScreen.setEntityType("CActivity");
        testScreen.setName("Test Activity Screen");
    }

    @Test
    void testEntityLineTypeFlowForActivity() {
        // 1. Get available entity line types for the screen's entity type
        final List<String> entityLineTypes = viewsService.getAvailableEntityLineTypes(testScreen.getEntityType());

        assertNotNull(entityLineTypes);
        assertTrue(!entityLineTypes.isEmpty());
        assertTrue(entityLineTypes.contains("CActivity"));
        assertTrue(entityLineTypes.contains("Project of Activity"));

        // 2. For each entity line type, get the actual entity class name
        final String directEntityClass = viewsService.getEntityClassNameForLineType("CActivity");
        assertEquals("CActivity", directEntityClass);

        final String projectEntityClass = viewsService.getEntityClassNameForLineType("Project of Activity");
        assertEquals("CProject", projectEntityClass);

        // 3. Get available fields for the direct entity
        final List<CEntityFieldService.EntityFieldInfo> activityFields = entityFieldService
                .getEntityFields(directEntityClass);
        assertNotNull(activityFields);
        assertTrue(!activityFields.isEmpty());

        // 4. Get available fields for a related entity
        final List<CEntityFieldService.EntityFieldInfo> projectFields = entityFieldService
                .getEntityFields(projectEntityClass);
        assertNotNull(projectFields);
        assertTrue(!projectFields.isEmpty());

        // 5. Verify that we can determine field types
        final CEntityFieldService.EntityFieldInfo nameField = activityFields.stream()
                .filter(field -> "name".equals(field.getFieldName())).findFirst().orElse(null);

        assertNotNull(nameField);
        assertEquals("TEXT", nameField.getFieldType());
    }

    @Test
    void testScreenLinesEntityLineTypeField() {
        // Test that the new entityLineType field works correctly
        final CScreenLines screenLine = new CScreenLines();

        // Test setting and getting entity line type
        screenLine.setEntityLineType("Project of Activity");
        assertEquals("Project of Activity", screenLine.getEntityLineType());

        // Test setting field name and type
        screenLine.setEntityFieldName("name");
        screenLine.setFieldType("TEXT");

        assertEquals("name", screenLine.getEntityFieldName());
        assertEquals("TEXT", screenLine.getFieldType());
    }

    @Test
    void testCompleteWorkflowForDifferentEntities() {
        // Test the complete workflow for different entity types
        final String[] entityTypes = { "CActivity", "CProject", "CRisk", "CMeeting", "CUser" };

        for (final String entityType : entityTypes) {
            // Get entity line types
            final List<String> lineTypes = viewsService.getAvailableEntityLineTypes(entityType);
            assertNotNull(lineTypes, "Entity line types should not be null for " + entityType);
            assertTrue(!lineTypes.isEmpty(), "Entity line types should not be empty for " + entityType);
            assertTrue(lineTypes.contains(entityType), "Should contain the base entity type: " + entityType);

            // For each line type, get the class name and fields
            for (final String lineType : lineTypes) {
                final String className = viewsService.getEntityClassNameForLineType(lineType);
                assertNotNull(className, "Class name should not be null for line type: " + lineType);

                final List<CEntityFieldService.EntityFieldInfo> fields = entityFieldService.getEntityFields(className);
                assertNotNull(fields, "Fields should not be null for class: " + className);
                // Note: Some entities might have empty field lists if they don't have MetaData annotations
            }
        }
    }
}