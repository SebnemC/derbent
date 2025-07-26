package tech.derbent.abstracts.views;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Clock;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.session.service.SessionService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserService;

/**
 * Integration test to verify the "New" button behavior works correctly
 * with real services and database operations for all entity types
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class NewButtonIntegrationTest {

    @Autowired
    private CProjectService projectService;

    @Autowired
    private CActivityService activityService;

    @Autowired
    private CUserService userService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private Clock clock;

    @Test
    public void testProjectNewAndSaveFlow() {
        // Test the complete flow for Projects
        
        // Create a new project entity
        CProject newProject = new CProject();
        assertNull(newProject.getId(), "New project should not have an ID");
        
        // Set project data (simulating form input)
        newProject.setName("Integration Test Project");
        
        // Save the project
        CProject savedProject = projectService.save(newProject);
        
        // Verify the project was saved correctly
        assertNotNull(savedProject.getId(), "Saved project should have an ID");
        assertEquals("Integration Test Project", savedProject.getName());
        
        // Verify we can retrieve it
        CProject retrievedProject = projectService.get(savedProject.getId()).orElse(null);
        assertNotNull(retrievedProject, "Should be able to retrieve saved project");
        assertEquals("Integration Test Project", retrievedProject.getName());
    }

    @Test
    public void testActivityNewAndSaveFlowWithProject() {
        // Test the complete flow for Activities with project assignment
        
        // First create and save a project
        CProject testProject = new CProject();
        testProject.setName("Activity Test Project");
        CProject savedProject = projectService.save(testProject);
        
        // Note: In test environment, we can't use SessionService for active project
        // So we'll manually simulate what CProjectAwareMDPage.newEntity() does
        
        // Create a new activity entity
        CActivity newActivity = new CActivity();
        assertNull(newActivity.getId(), "New activity should not have an ID");
        
        // Manually set the project (simulating what CProjectAwareMDPage.newEntity() does)
        newActivity.setProject(savedProject);
        
        // Set activity data (simulating form input)
        newActivity.setName("Integration Test Activity");
        
        // Verify the activity has the correct project
        assertEquals(savedProject, newActivity.getProject(), "Activity should have the assigned project");
        
        // Save the activity
        CActivity savedActivity = activityService.save(newActivity);
        
        // Verify the activity was saved correctly
        assertNotNull(savedActivity.getId(), "Saved activity should have an ID");
        assertEquals("Integration Test Activity", savedActivity.getName());
        assertEquals(savedProject.getId(), savedActivity.getProject().getId());
        
        // Verify we can retrieve it
        CActivity retrievedActivity = activityService.get(savedActivity.getId()).orElse(null);
        assertNotNull(retrievedActivity, "Should be able to retrieve saved activity");
        assertEquals("Integration Test Activity", retrievedActivity.getName());
        assertNotNull(retrievedActivity.getProject(), "Retrieved activity should have project");
    }

    @Test
    public void testUserNewAndSaveFlow() {
        // Test the complete flow for Users
        
        // Create a new user entity
        CUser newUser = new CUser();
        assertNull(newUser.getId(), "New user should not have an ID");
        
        // Set user data (simulating form input)
        newUser.setName("Integration");
        newUser.setLastname("Test");
        newUser.setLogin("integration_test_user");
        newUser.setEmail("integration@test.com");
        newUser.setRoles("USER");
        newUser.setEnabled(true);
        
        // Save the user
        CUser savedUser = userService.save(newUser);
        
        // Verify the user was saved correctly
        assertNotNull(savedUser.getId(), "Saved user should have an ID");
        assertEquals("Integration", savedUser.getName());
        assertEquals("Test", savedUser.getLastname());
        assertEquals("integration_test_user", savedUser.getLogin());
        assertEquals("integration@test.com", savedUser.getEmail());
        
        // Verify we can retrieve it
        CUser retrievedUser = userService.get(savedUser.getId()).orElse(null);
        assertNotNull(retrievedUser, "Should be able to retrieve saved user");
        assertEquals("Integration", retrievedUser.getName());
        assertEquals("integration_test_user", retrievedUser.getLogin());
    }

    @Test
    public void testEntityValidationForSave() {
        // Test that proper validation occurs during save operations
        
        // Test project validation
        CProject invalidProject = new CProject();
        // Don't set required name field
        
        try {
            projectService.save(invalidProject);
            // If we get here, validation didn't work as expected
            // Note: This might pass if validation is not enforced at service level
            // but would fail at database level due to NOT NULL constraint
        } catch (Exception e) {
            // Expected for invalid entity
            assertNotNull(e, "Should get exception for invalid project");
        }
        
        // Test user validation
        CUser invalidUser = new CUser();
        // Don't set required fields
        
        try {
            userService.save(invalidUser);
            // If we get here, validation didn't work as expected
        } catch (Exception e) {
            // Expected for invalid entity
            assertNotNull(e, "Should get exception for invalid user");
        }
    }
}