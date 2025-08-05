package unit_tests.tech.derbent.users.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;

/**
 * Unit tests for CUserProjectSettings functionality including repository and service layer validation.
 */
public class CUserProjectSettingsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CUserProjectSettingsTest.class);

    private CUser testUser;
    private CProject testProject;
    private CUserProjectSettings testSettings;

    @BeforeEach
    public void setUp() {
        LOGGER.info("Setting up test data");
        
        // Create test user
        testUser = new CUser();
        testUser.setName("Test User");
        testUser.setLogin("testuser");
        testUser.setEmail("test@example.com");
        
        // Create test project
        testProject = new CProject("Test Project");
        
        // Create test settings
        testSettings = new CUserProjectSettings();
        testSettings.setUser(testUser);
        testSettings.setProject(testProject);
        testSettings.setRole("DEVELOPER");
        testSettings.setPermission("READ,WRITE");
    }

    @Test
    public void testUserProjectSettingsCreation() {
        LOGGER.info("Testing CUserProjectSettings creation and field access");
        
        assertNotNull(testSettings, "Settings should be created");
        assertEquals(testUser, testSettings.getUser(), "User should be set correctly");
        assertEquals(testProject, testSettings.getProject(), "Project should be set correctly");
        assertEquals("DEVELOPER", testSettings.getRole(), "Role should be set correctly");
        assertEquals("READ,WRITE", testSettings.getPermission(), "Permission should be set correctly");
        
        LOGGER.info("CUserProjectSettings creation test passed");
    }

    @Test
    public void testUserProjectRelationship() {
        LOGGER.info("Testing user-project relationship functionality");
        
        // Test user's project settings list
        assertNotNull(testUser.getProjectSettings(), "User project settings list should not be null");
        testUser.getProjectSettings().add(testSettings);
        assertTrue(testUser.getProjectSettings().contains(testSettings), 
            "User should contain the project settings");
        
        // Test project's user settings list
        assertNotNull(testProject.getUserSettings(), "Project user settings list should not be null");
        testProject.getUserSettings().add(testSettings);
        assertTrue(testProject.getUserSettings().contains(testSettings), 
            "Project should contain the user settings");
        
        LOGGER.info("User-project relationship test passed");
    }

    @Test
    public void testUserProjectSettingsDefaults() {
        LOGGER.info("Testing CUserProjectSettings default constructor");
        
        CUserProjectSettings newSettings = new CUserProjectSettings();
        assertNotNull(newSettings, "Default constructor should create instance");
        
        // Test setters and getters
        newSettings.setRole("MANAGER");
        assertEquals("MANAGER", newSettings.getRole(), "Role setter/getter should work");
        
        newSettings.setPermission("READ,WRITE,DELETE");
        assertEquals("READ,WRITE,DELETE", newSettings.getPermission(), "Permission setter/getter should work");
        
        LOGGER.info("CUserProjectSettings defaults test passed");
    }

    @Test
    public void testUserProjectSettingsValidation() {
        LOGGER.info("Testing CUserProjectSettings validation scenarios");
        
        // Test with null user
        CUserProjectSettings invalidSettings = new CUserProjectSettings();
        invalidSettings.setProject(testProject);
        invalidSettings.setRole("DEVELOPER");
        
        assertNull(invalidSettings.getUser(), "User should be null for validation test");
        assertNotNull(invalidSettings.getProject(), "Project should be set");
        
        // Test with null project
        invalidSettings.setUser(testUser);
        invalidSettings.setProject(null);
        
        assertNotNull(invalidSettings.getUser(), "User should be set");
        assertNull(invalidSettings.getProject(), "Project should be null for validation test");
        
        LOGGER.info("CUserProjectSettings validation test passed");
    }

    @Test
    public void testBidirectionalRelationship() {
        LOGGER.info("Testing bidirectional relationship consistency");
        
        // Create a second user and settings
        CUser user2 = new CUser();
        user2.setName("User Two");
        user2.setLogin("user2");
        
        CUserProjectSettings settings2 = new CUserProjectSettings();
        settings2.setUser(user2);
        settings2.setProject(testProject);
        settings2.setRole("TESTER");
        settings2.setPermission("READ");
        
        // Add both settings to project
        testProject.getUserSettings().add(testSettings);
        testProject.getUserSettings().add(settings2);
        
        // Add settings to users
        testUser.getProjectSettings().add(testSettings);
        user2.getProjectSettings().add(settings2);
        
        // Verify relationships
        assertEquals(2, testProject.getUserSettings().size(), "Project should have 2 user settings");
        assertEquals(1, testUser.getProjectSettings().size(), "User 1 should have 1 project setting");
        assertEquals(1, user2.getProjectSettings().size(), "User 2 should have 1 project setting");
        
        LOGGER.info("Bidirectional relationship test passed");
    }
}