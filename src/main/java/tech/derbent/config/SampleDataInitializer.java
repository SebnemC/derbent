package tech.derbent.config;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.domain.CActivityType;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.activities.service.CActivityTypeService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.domain.CUserType;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.CUserTypeService;

/**
 * Sample data initializer that ensures basic data exists in the database.
 * This runs after the application starts and creates sample data if the database is empty.
 */
@Component
public class SampleDataInitializer implements ApplicationRunner {

    private final CProjectService projectService;
    private final CUserService userService;
    private final CActivityService activityService;
    private final CUserTypeService userTypeService;
    private final CActivityTypeService activityTypeService;

    public SampleDataInitializer(final CProjectService projectService,
                                final CUserService userService,
                                final CActivityService activityService,
                                final CUserTypeService userTypeService,
                                final CActivityTypeService activityTypeService) {
        this.projectService = projectService;
        this.userService = userService;
        this.activityService = activityService;
        this.userTypeService = userTypeService;
        this.activityTypeService = activityTypeService;
    }

    @Override
    @Transactional
    public void run(final ApplicationArguments args) throws Exception {
        // Check if we already have data
        if (!projectService.findAll().isEmpty()) {
            System.out.println("Sample data already exists, skipping initialization");
            return;
        }

        System.out.println("Initializing sample data...");

        // Create user types
        final CUserType developerType = new CUserType();
        developerType.setName("Developer");
        developerType.setDescription("Software developers and engineers");
        userTypeService.save(developerType);

        final CUserType managerType = new CUserType();
        managerType.setName("Manager");
        managerType.setDescription("Project and team managers");
        userTypeService.save(managerType);

        // Create activity types
        final CActivityType developmentType = new CActivityType();
        developmentType.setName("Development");
        developmentType.setDescription("Software development tasks");
        activityTypeService.save(developmentType);

        final CActivityType testingType = new CActivityType();
        testingType.setName("Testing");
        testingType.setDescription("Quality assurance and testing activities");
        activityTypeService.save(testingType);

        final CActivityType designType = new CActivityType();
        designType.setName("Design");
        designType.setDescription("UI/UX and graphic design work");
        activityTypeService.save(designType);

        // Create projects
        final CProject project1 = new CProject();
        project1.setName("Derbent Project");
        projectService.save(project1);

        final CProject project2 = new CProject();
        project2.setName("Website Redesign");
        projectService.save(project2);

        final CProject project3 = new CProject();
        project3.setName("Mobile App Development");
        projectService.save(project3);

        // Create users
        final CUser user1 = new CUser();
        user1.setName("user");
        user1.setLastname("Lova");
        user1.setLogin("user");
        user1.setEmail("test@example.com");
        user1.setPassword("$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu");
        user1.setPhone("1234567890");
        user1.setRoles("USER");
        user1.setEnabled(true);
        user1.setUserType(developerType);
        user1.setProjectSettings(new ArrayList<>());
        userService.save(user1);

        final CUser user2 = new CUser();
        user2.setName("user2");
        user2.setLastname("Lova2");
        user2.setLogin("user2");
        user2.setEmail("test2@example.com");
        user2.setPassword("$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu");
        user2.setPhone("1234567890");
        user2.setRoles("USER2");
        user2.setEnabled(true);
        user2.setUserType(managerType);
        user2.setProjectSettings(new ArrayList<>());
        userService.save(user2);

        // Create user-project relationships
        final CUserProjectSettings userProject1 = new CUserProjectSettings();
        userProject1.setUser(user1);
        userProject1.setProjectId(project1.getId());
        userProject1.setRole("Developer");
        userProject1.setPermission("READ_WRITE");

        final CUserProjectSettings userProject2 = new CUserProjectSettings();
        userProject2.setUser(user1);
        userProject2.setProjectId(project3.getId());
        userProject2.setRole("Lead Developer");
        userProject2.setPermission("READ_WRITE");

        final CUserProjectSettings userProject3 = new CUserProjectSettings();
        userProject3.setUser(user2);
        userProject3.setProjectId(project1.getId());
        userProject3.setRole("Manager");
        userProject3.setPermission("ADMIN");

        final CUserProjectSettings userProject4 = new CUserProjectSettings();
        userProject4.setUser(user2);
        userProject4.setProjectId(project2.getId());
        userProject4.setRole("Project Manager");
        userProject4.setPermission("ADMIN");

        // Add project settings to users
        user1.getProjectSettings().add(userProject1);
        user1.getProjectSettings().add(userProject2);
        user2.getProjectSettings().add(userProject3);
        user2.getProjectSettings().add(userProject4);

        userService.save(user1);
        userService.save(user2);

        // Create activities
        final CActivity activity1 = new CActivity("Setup Development Environment", project1);
        activity1.setActivityType(developmentType);
        activityService.save(activity1);

        final CActivity activity2 = new CActivity("Design User Interface", project1);
        activity2.setActivityType(designType);
        activityService.save(activity2);

        final CActivity activity3 = new CActivity("Implement Authentication", project1);
        activity3.setActivityType(developmentType);
        activityService.save(activity3);

        final CActivity activity4 = new CActivity("Write Unit Tests", project1);
        activity4.setActivityType(testingType);
        activityService.save(activity4);

        final CActivity activity5 = new CActivity("Analyze Current Website", project2);
        activity5.setActivityType(developmentType);
        activityService.save(activity5);

        final CActivity activity6 = new CActivity("Mobile App Architecture", project3);
        activity6.setActivityType(developmentType);
        activityService.save(activity6);

        final CActivity activity7 = new CActivity("iOS Development Setup", project3);
        activity7.setActivityType(developmentType);
        activityService.save(activity7);

        final CActivity activity8 = new CActivity("Mobile UI Design", project3);
        activity8.setActivityType(designType);
        activityService.save(activity8);

        System.out.println("Sample data initialization completed successfully!");
        System.out.println("Created 3 projects, 2 users, 8 activities, and user-project relationships");
    }
}