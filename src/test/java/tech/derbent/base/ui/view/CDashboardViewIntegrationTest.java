package tech.derbent.base.ui.view;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.service.CUserService;
import tech.derbent.activities.service.CActivityService;

/**
 * Integration test for dashboard data availability.
 * This test verifies that the database is properly initialized with sample data
 * and that the dashboard services can retrieve the data correctly.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.sql.init.mode=always",
    "spring.jpa.defer-datasource-initialization=true"
})
@Transactional
class CDashboardViewIntegrationTest {

    @Autowired
    private CProjectService projectService;

    @Autowired  
    private CUserService userService;

    @Autowired
    private CActivityService activityService;

    @Test
    void testDashboardDataInitialization() {
        System.out.println("Testing dashboard data initialization...");
        
        // Test project count - should be 4 from CSampleDataInitializer
        long projectCount = projectService.getTotalProjectCount();
        System.out.println("Project count: " + projectCount);
        assertTrue(projectCount > 0, "Project count should be greater than 0");
        
        // Test project list retrieval
        List<CProject> projects = projectService.findAll();
        assertNotNull(projects, "Projects list should not be null");
        System.out.println("Projects found: " + projects.size());
        assertTrue(projects.size() > 0, "Should have at least one project");
        
        // Log each project
        for (CProject project : projects) {
            System.out.println("  - " + project.getName() + " (ID: " + project.getId() + ")");
            
            // Test user count for this project
            long userCount = userService.countUsersByProjectId(project.getId());
            System.out.println("    Users: " + userCount);
            
            // Test activity count for this project  
            long activityCount = activityService.countByProject(project);
            System.out.println("    Activities: " + activityCount);
        }
        
        // Verify we have the expected projects from CSampleDataInitializer
        boolean hasDigitalTransformation = projects.stream()
            .anyMatch(p -> "Digital Transformation Initiative".equals(p.getName()));
        assertTrue(hasDigitalTransformation, "Should have 'Digital Transformation Initiative' from CSampleDataInitializer");
        
        boolean hasProductDevelopment = projects.stream()
            .anyMatch(p -> "Product Development Phase 2".equals(p.getName()));
        assertTrue(hasProductDevelopment, "Should have 'Product Development Phase 2' from CSampleDataInitializer");
        
        boolean hasInfrastructureModernization = projects.stream()
            .anyMatch(p -> "Infrastructure Modernization".equals(p.getName()));
        assertTrue(hasInfrastructureModernization, "Should have 'Infrastructure Modernization' from CSampleDataInitializer");
        
        boolean hasCustomerExperience = projects.stream()
            .anyMatch(p -> "Customer Experience Enhancement".equals(p.getName()));
        assertTrue(hasCustomerExperience, "Should have 'Customer Experience Enhancement' from CSampleDataInitializer");
    }
}