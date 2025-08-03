package tech.derbent.abstracts.services;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserService;
import tech.derbent.decisions.service.CDecisionService;
import tech.derbent.decisions.domain.CDecision;

/**
 * Test class for generic search functionality in abstract service classes.
 * Verifies that the reflection-based search methods work correctly.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GenericSearchFunctionalityTest {

    @Autowired
    private CProjectService projectService;

    @Autowired
    private CUserService userService;

    @Autowired
    private CDecisionService decisionService;

    private CProject testProject;
    private CUser testUser;
    private CDecision testDecision;

    @BeforeEach
    void setUp() {
        // Create test data
        testProject = projectService.createEntity("Test Project for Generic Search");
        testUser = userService.createEntity("Test User for Generic Search");
        testDecision = decisionService.createEntity("Test Decision for Generic Search", testProject);
        testDecision.setAssignedTo(testUser);
        testDecision.setCreatedBy(testUser);
        decisionService.save(testDecision);
    }

    @Test
    void testGenericSearchByName() {
        // Test name pattern search using enhanced functionality
        List<CProject> projectResults = projectService.findByNamePattern("Generic Search");
        assertFalse(projectResults.isEmpty(), "Should find projects with name pattern");
        assertTrue(projectResults.stream().anyMatch(p -> p.getName().contains("Generic Search")));

        List<CUser> userResults = userService.findByNamePattern("Generic Search");
        assertFalse(userResults.isEmpty(), "Should find users with name pattern");
        assertTrue(userResults.stream().anyMatch(u -> u.getName().contains("Generic Search")));

        List<CDecision> decisionResults = decisionService.findByNamePattern("Generic Search");
        assertFalse(decisionResults.isEmpty(), "Should find decisions with name pattern");
        assertTrue(decisionResults.stream().anyMatch(d -> d.getName().contains("Generic Search")));
    }

    @Test
    void testGenericSearchByField() {
        // Test generic field search using reflection
        List<CProject> projectsByName = projectService.findByField("name", testProject.getName());
        assertFalse(projectsByName.isEmpty(), "Should find projects by exact name");
        assertTrue(projectsByName.stream().anyMatch(p -> p.getId().equals(testProject.getId())));

        List<CUser> usersByName = userService.findByField("name", testUser.getName());
        assertFalse(usersByName.isEmpty(), "Should find users by exact name");
        assertTrue(usersByName.stream().anyMatch(u -> u.getId().equals(testUser.getId())));
    }

    @Test
    void testProjectEntitySearchByAssignedTo() {
        // Test search by assignedTo user
        List<CDecision> decisionsByAssignedTo = decisionService.findByAssignedTo(testUser);
        assertFalse(decisionsByAssignedTo.isEmpty(), "Should find decisions assigned to user");
        assertTrue(decisionsByAssignedTo.stream().anyMatch(d -> d.getId().equals(testDecision.getId())));
    }

    @Test
    void testProjectEntitySearchByCreatedBy() {
        // Test search by createdBy user
        List<CDecision> decisionsByCreatedBy = decisionService.findByCreatedBy(testUser);
        assertFalse(decisionsByCreatedBy.isEmpty(), "Should find decisions created by user");
        assertTrue(decisionsByCreatedBy.stream().anyMatch(d -> d.getId().equals(testDecision.getId())));
    }

    @Test
    void testProjectEntitySearchByProject() {
        // Test search by project using generic method
        List<CDecision> decisionsByProject = decisionService.findByProjectGeneric(testProject);
        assertFalse(decisionsByProject.isEmpty(), "Should find decisions in project");
        assertTrue(decisionsByProject.stream().anyMatch(d -> d.getId().equals(testDecision.getId())));
    }

    @Test
    void testPaginatedGenericSearch() {
        // Test paginated search functionality
        Pageable pageable = PageRequest.of(0, 10);
        
        List<CProject> paginatedProjects = projectService.findByNamePattern("Generic Search", pageable);
        assertFalse(paginatedProjects.isEmpty(), "Should find projects with pagination");
        
        List<CDecision> paginatedDecisions = decisionService.findByAssignedTo(testUser, pageable);
        assertFalse(paginatedDecisions.isEmpty(), "Should find decisions with pagination");
    }

    @Test
    void testSearchableFieldsIntrospection() {
        // Test that we can introspect searchable fields
        List<String> projectFields = projectService.getSearchableFields();
        assertFalse(projectFields.isEmpty(), "Should have searchable fields");
        assertTrue(projectFields.contains("name"), "Should include name field");
        assertTrue(projectFields.contains("description"), "Should include description field");

        List<String> decisionFields = decisionService.getSearchableFields();
        assertFalse(decisionFields.isEmpty(), "Should have searchable fields");
        assertTrue(decisionFields.contains("name"), "Should include name field");
        assertTrue(decisionFields.contains("project"), "Should include project field");
        assertTrue(decisionFields.contains("assignedTo"), "Should include assignedTo field");
        assertTrue(decisionFields.contains("createdBy"), "Should include createdBy field");
    }

    @Test
    void testFieldExistenceCheck() {
        // Test field existence checking
        assertTrue(projectService.hasField("name"), "Project should have name field");
        assertTrue(projectService.hasField("description"), "Project should have description field");
        assertFalse(projectService.hasField("nonExistentField"), "Project should not have non-existent field");

        assertTrue(decisionService.hasField("project"), "Decision should have project field");
        assertTrue(decisionService.hasField("assignedTo"), "Decision should have assignedTo field");
        assertTrue(decisionService.hasField("createdBy"), "Decision should have createdBy field");
    }

    @Test
    void testNullHandlingInGenericSearch() {
        // Test that null values are handled gracefully
        List<CDecision> nullProjectSearch = decisionService.findByProjectGeneric(null);
        assertTrue(nullProjectSearch.isEmpty(), "Null project search should return empty list");

        List<CDecision> nullUserSearch = decisionService.findByAssignedTo(null);
        assertTrue(nullUserSearch.isEmpty(), "Null user search should return empty list");

        List<CProject> emptyNameSearch = projectService.findByNamePattern("");
        assertTrue(emptyNameSearch.isEmpty(), "Empty name search should return empty list");
    }
}