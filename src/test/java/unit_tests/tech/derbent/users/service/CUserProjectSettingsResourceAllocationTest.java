package unit_tests.tech.derbent.users.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;

/**
 * Test for the new resource allocation fields in CUserProjectSettings (addressing "relqtwd").
 * Tests the Resource aLlocation Quantity With Time Due functionality.
 */
public class CUserProjectSettingsResourceAllocationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CUserProjectSettingsResourceAllocationTest.class);

    private CUser testUser;
    private CProject testProject;
    private CUserProjectSettings testSettings;

    @BeforeEach
    public void setUp() {
        LOGGER.info("Setting up test data for resource allocation tests");
        
        testUser = new CUser();
        testUser.setName("John Developer");
        testUser.setLogin("johndeveloper");
        testUser.setEmail("john@example.com");
        
        testProject = new CProject("Web Application Project");
        
        testSettings = new CUserProjectSettings();
        testSettings.setUser(testUser);
        testSettings.setProject(testProject);
        testSettings.setRole("SENIOR_DEVELOPER");
        testSettings.setPermission("READ,WRITE,REVIEW");
    }

    @Test
    public void testResourceAllocationFields() {
        LOGGER.info("Testing resource allocation fields (relqtwd functionality)");
        
        // Test allocated hours
        testSettings.setAllocatedHours(new BigDecimal("80.00"));
        assertEquals(new BigDecimal("80.00"), testSettings.getAllocatedHours(), 
            "Allocated hours should be set correctly");
        
        // Test hourly rate
        testSettings.setHourlyRate(new BigDecimal("75.50"));
        assertEquals(new BigDecimal("75.50"), testSettings.getHourlyRate(), 
            "Hourly rate should be set correctly");
        
        // Test workload percentage
        testSettings.setWorkloadPercentage(new BigDecimal("40.00"));
        assertEquals(new BigDecimal("40.00"), testSettings.getWorkloadPercentage(), 
            "Workload percentage should be set correctly");
        
        // Test active status (default should be true)
        assertTrue(testSettings.getIsActive(), "Default active status should be true");
        
        LOGGER.info("Resource allocation fields test passed");
    }

    @Test
    public void testTimeAllocationFields() {
        LOGGER.info("Testing time allocation fields (dates for relqtwd)");
        
        LocalDate startDate = LocalDate.now();
        LocalDate dueDate = LocalDate.now().plusDays(30);
        
        testSettings.setStartDate(startDate);
        testSettings.setDueDate(dueDate);
        
        assertEquals(startDate, testSettings.getStartDate(), "Start date should be set correctly");
        assertEquals(dueDate, testSettings.getDueDate(), "Due date should be set correctly");
        
        LOGGER.info("Time allocation fields test passed");
    }

    @Test
    public void testBusinessMethods() {
        LOGGER.info("Testing business methods for resource allocation");
        
        // Test cost calculation
        testSettings.setAllocatedHours(new BigDecimal("100.00"));
        testSettings.setHourlyRate(new BigDecimal("80.00"));
        
        BigDecimal expectedCost = new BigDecimal("8000.00");
        assertEquals(0, expectedCost.compareTo(testSettings.calculateTotalCost()), 
            "Total cost calculation should be correct");
        
        // Test time allocation check
        assertTrue(testSettings.hasTimeAllocation(), 
            "Should have time allocation when hours > 0");
        
        // Test workload assignment check
        testSettings.setWorkloadPercentage(new BigDecimal("50.00"));
        assertTrue(testSettings.hasWorkloadAssignment(), 
            "Should have workload assignment when percentage > 0");
        
        LOGGER.info("Business methods test passed");
    }

    @Test
    public void testOverdueDetection() {
        LOGGER.info("Testing overdue detection functionality");
        
        // Test not overdue (future due date)
        testSettings.setDueDate(LocalDate.now().plusDays(5));
        testSettings.setIsActive(true);
        assertFalse(testSettings.isOverdue(), "Should not be overdue with future due date");
        
        // Test overdue (past due date)
        testSettings.setDueDate(LocalDate.now().minusDays(5));
        assertTrue(testSettings.isOverdue(), "Should be overdue with past due date");
        
        // Test not overdue when inactive
        testSettings.setIsActive(false);
        assertFalse(testSettings.isOverdue(), "Should not be overdue when inactive");
        
        // Test not overdue when no due date
        testSettings.setDueDate(null);
        testSettings.setIsActive(true);
        assertFalse(testSettings.isOverdue(), "Should not be overdue with no due date");
        
        LOGGER.info("Overdue detection test passed");
    }

    @Test
    public void testWorkloadCapacityCalculation() {
        LOGGER.info("Testing workload capacity calculations");
        
        // Test full allocation scenario
        testSettings.setAllocatedHours(new BigDecimal("160.00")); // 160 hours
        testSettings.setWorkloadPercentage(new BigDecimal("100.00")); // 100% allocation
        testSettings.setHourlyRate(new BigDecimal("90.00"));
        
        assertEquals(0, new BigDecimal("14400.00").compareTo(testSettings.calculateTotalCost()), 
            "Full allocation cost should be calculated correctly");
        
        assertTrue(testSettings.hasTimeAllocation(), "Should have time allocation");
        assertTrue(testSettings.hasWorkloadAssignment(), "Should have workload assignment");
        
        // Test partial allocation scenario
        testSettings.setWorkloadPercentage(new BigDecimal("25.00")); // 25% allocation
        assertTrue(testSettings.hasWorkloadAssignment(), "Should still have workload assignment");
        
        LOGGER.info("Workload capacity calculation test passed");
    }

    @Test
    public void testDefaultValues() {
        LOGGER.info("Testing default values for new fields");
        
        CUserProjectSettings newSettings = new CUserProjectSettings();
        
        // Test defaults
        assertNull(newSettings.getAllocatedHours(), "Allocated hours should default to null");
        assertNull(newSettings.getHourlyRate(), "Hourly rate should default to null");
        assertNull(newSettings.getStartDate(), "Start date should default to null");
        assertNull(newSettings.getDueDate(), "Due date should default to null");
        assertNull(newSettings.getWorkloadPercentage(), "Workload percentage should default to null");
        assertTrue(newSettings.getIsActive(), "Active status should default to true");
        
        // Test zero cost when no allocation
        assertEquals(BigDecimal.ZERO, newSettings.calculateTotalCost(), 
            "Cost should be zero when no allocation set");
        
        assertFalse(newSettings.hasTimeAllocation(), 
            "Should not have time allocation when hours not set");
        assertFalse(newSettings.hasWorkloadAssignment(), 
            "Should not have workload assignment when percentage not set");
        
        LOGGER.info("Default values test passed");
    }

    @Test
    public void testProjectResourceManagement() {
        LOGGER.info("Testing project-level resource management");
        
        // Create multiple user assignments for the same project
        CUser user2 = new CUser();
        user2.setName("Jane Manager");
        user2.setLogin("janemanager");
        
        CUserProjectSettings managerSettings = new CUserProjectSettings();
        managerSettings.setUser(user2);
        managerSettings.setProject(testProject);
        managerSettings.setRole("PROJECT_MANAGER");
        managerSettings.setPermission("READ,WRITE,DELETE,ADMIN");
        managerSettings.setAllocatedHours(new BigDecimal("40.00"));
        managerSettings.setHourlyRate(new BigDecimal("120.00"));
        managerSettings.setWorkloadPercentage(new BigDecimal("60.00"));
        
        // Set up developer settings
        testSettings.setAllocatedHours(new BigDecimal("120.00"));
        testSettings.setHourlyRate(new BigDecimal("80.00"));
        testSettings.setWorkloadPercentage(new BigDecimal("80.00"));
        
        // Add to project
        testProject.getUserSettings().add(testSettings);
        testProject.getUserSettings().add(managerSettings);
        
        // Verify project has multiple resource allocations
        assertEquals(2, testProject.getUserSettings().size(), 
            "Project should have 2 user assignments");
        
        // Verify total project costs
        BigDecimal developerCost = testSettings.calculateTotalCost(); // 120 * 80 = 9600
        BigDecimal managerCost = managerSettings.calculateTotalCost(); // 40 * 120 = 4800
        BigDecimal expectedTotal = new BigDecimal("14400.00");
        
        assertEquals(0, expectedTotal.compareTo(developerCost.add(managerCost)), 
            "Total project cost should be sum of individual costs");
        
        LOGGER.info("Project resource management test passed");
    }
}