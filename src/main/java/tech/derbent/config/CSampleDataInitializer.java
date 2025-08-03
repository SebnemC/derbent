package tech.derbent.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.activities.domain.CActivityType;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.activities.service.CActivityStatusService;
import tech.derbent.activities.service.CActivityTypeService;
import tech.derbent.comments.domain.CCommentPriority;
import tech.derbent.comments.service.CCommentPriorityService;
import tech.derbent.comments.service.CCommentService;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.decisions.domain.CDecision;
import tech.derbent.decisions.domain.CDecisionStatus;
import tech.derbent.decisions.service.CDecisionService;
import tech.derbent.decisions.service.CDecisionStatusService;
import tech.derbent.decisions.service.CDecisionTypeService;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.domain.CMeetingStatus;
import tech.derbent.meetings.service.CMeetingService;
import tech.derbent.meetings.service.CMeetingStatusService;
import tech.derbent.meetings.service.CMeetingTypeService;
import tech.derbent.orders.domain.CCurrency;
import tech.derbent.orders.domain.COrder;
import tech.derbent.orders.domain.COrderStatus;
import tech.derbent.orders.service.CApprovalStatusService;
import tech.derbent.orders.service.CCurrencyService;
import tech.derbent.orders.service.COrderService;
import tech.derbent.orders.service.COrderStatusService;
import tech.derbent.orders.service.COrderTypeService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.risks.domain.CRisk;
import tech.derbent.risks.domain.ERiskSeverity;
import tech.derbent.risks.service.CRiskService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserRole;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.CUserTypeService;

/**
 * CSampleDataInitializer - Enhanced sample data initializer following coding guidelines. Layer: Configuration (MVC)
 * This component runs after the application starts and creates comprehensive sample data for core entities if the
 * database is empty. Follows the coding guidelines: - Class name starts with "C" as per coding standards - Creates at
 * least 4 sample items per core entity type - Uses proper service layer for entity creation - Implements proper error
 * handling and logging - Uses standard test123 password for all users Core entities initialized: - CCompany (4
 * companies with full details) - CUser (5+ users with different roles and companies) - CProject (4 projects with
 * different scopes) - CActivity (4+ comprehensive activities) - Supporting type entities as available
 */
@Component
public class CSampleDataInitializer implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(CSampleDataInitializer.class);

    // Standard password for all users as per coding guidelines
    private static final String STANDARD_PASSWORD = "test123";

    // Sample user profile pictures (base64 encoded SVG icons)
    @SuppressWarnings("unused")
    private static final String PROFILE_PICTURE_USER = "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjQiIGhlaWdodD0iMjQiIHZpZXdCb3g9IjAgMCAyNCAyNCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTEyIDJDMTMuMSAyIDE0IDIuOSAxNCA0QzE0IDUuMSAxMy4xIDYgMTIgNkMxMC45IDYgMTAgNS4xIDEwIDRDMTAgMi45IDEwLjkgMiAxMiAyWk0yMSA5VjIySDNWOUwxMiA2TDIxIDlaIiBmaWxsPSIjMzMzIi8+Cjwvc3ZnPgo=";

    // Service dependencies - injected via constructor
    private final CProjectService projectService;

    private final CUserService userService;

    private final CActivityService activityService;

    private final CUserTypeService userTypeService;

    private final CActivityTypeService activityTypeService;

    private final CMeetingTypeService meetingTypeService;

    private final CDecisionTypeService decisionTypeService;

    private final COrderTypeService orderTypeService;

    private final CCompanyService companyService;

    private final CCommentService commentService;

    private final CCommentPriorityService commentPriorityService;

    private final CMeetingService meetingService;

    private final CRiskService riskService;

    private final CMeetingStatusService meetingStatusService;

    private final CDecisionStatusService decisionStatusService;

    private final CActivityStatusService activityStatusService;

    private final COrderStatusService orderStatusService;

    private final CApprovalStatusService approvalStatusService;

    private final CDecisionService decisionService;

    private final COrderService orderService;

    private final CCurrencyService currencyService;

    public CSampleDataInitializer(final CProjectService projectService, final CUserService userService,
            final CActivityService activityService, final CUserTypeService userTypeService,
            final CActivityTypeService activityTypeService, final CMeetingTypeService meetingTypeService,
            final CDecisionTypeService decisionTypeService, final COrderTypeService orderTypeService,
            final CCompanyService companyService, final CCommentService commentService,
            final CCommentPriorityService commentPriorityService, final CMeetingService meetingService,
            final CRiskService riskService, final CMeetingStatusService meetingStatusService,
            final CDecisionStatusService decisionStatusService, final CActivityStatusService activityStatusService,
            final COrderStatusService orderStatusService, final CApprovalStatusService approvalStatusService,
            final CDecisionService decisionService, final COrderService orderService,
            final CCurrencyService currencyService) {
        LOGGER.info("CSampleDataInitializer constructor called with service dependencies");
        this.projectService = projectService;
        this.userService = userService;
        this.activityService = activityService;
        this.userTypeService = userTypeService;
        this.activityTypeService = activityTypeService;
        this.meetingTypeService = meetingTypeService;
        this.decisionTypeService = decisionTypeService;
        this.orderTypeService = orderTypeService;
        this.companyService = companyService;
        this.commentService = commentService;
        this.commentPriorityService = commentPriorityService;
        this.meetingService = meetingService;
        this.riskService = riskService;
        this.meetingStatusService = meetingStatusService;
        this.decisionStatusService = decisionStatusService;
        this.activityStatusService = activityStatusService;
        this.orderStatusService = orderStatusService;
        this.approvalStatusService = approvalStatusService;
        this.decisionService = decisionService;
        this.orderService = orderService;
        this.currencyService = currencyService;
    }

    private void createActivityStatus(final String name, final CProject project, final String description,
            final String color, final boolean isFinal, final int sortOrder) {
        final CActivityStatus status = new CActivityStatus(name, project);
        status.setDescription(description);
        status.setColor(color);
        status.setFinal(isFinal);
        status.setSortOrder(sortOrder);
        activityStatusService.save(status);
        LOGGER.debug("Created activity status: {}", name);
    }

    /**
     * Creates additional activities for Customer Experience Enhancement project.
     */
    private void createAdditionalCustomerExperienceActivities() {
        final CProject project = findProjectByName("Customer Experience Enhancement");

        if (project == null) {
            return;
        }
        // User Research Activity
        final CActivity userResearch = new CActivity("User Research & Analysis", project);
        final CActivityType researchType = findActivityTypeByNameAndProject("Research", project);
        activityService.setActivityType(userResearch, researchType,
                "Conduct user interviews and analyze customer feedback");
        final CUser analyst = findUserByLogin("ademir");
        final CUser manager = findUserByLogin("mkaradeniz");
        activityService.setAssignedUsers(userResearch, analyst, manager);
        activityService.setTimeTracking(userResearch, new BigDecimal("22.00"), new BigDecimal("22.00"),
                new BigDecimal("0.00"));
        activityService.setDateInfo(userResearch, LocalDate.now().minusDays(20), LocalDate.now().minusDays(10),
                LocalDate.now().minusDays(10));
        final CActivityStatus completedStatus = findActivityStatusByName("Completed");
        activityService.setStatusAndPriority(userResearch, completedStatus, null, 100);
        commentService.createComment("User research methodology defined", userResearch, manager);
        commentService.createComment("Conducted 15 user interviews", userResearch, analyst);
        commentService.createComment("Analysis complete, insights documented", userResearch, analyst);
        // UI/UX Design Activity
        final CActivity uxDesign = new CActivity("UI/UX Design Improvements", project);
        final CActivityType designType = findActivityTypeByNameAndProject("Design", project);
        activityService.setActivityType(uxDesign, designType,
                "Design improved user interface based on research findings");
        final CUser dev2 = findUserByLogin("msahin");
        activityService.setAssignedUsers(uxDesign, dev2, analyst);
        activityService.setTimeTracking(uxDesign, new BigDecimal("28.00"), new BigDecimal("15.00"),
                new BigDecimal("13.00"));
        activityService.setDateInfo(uxDesign, LocalDate.now().minusDays(8), LocalDate.now().plusDays(5), null);
        final CActivityStatus inProgressStatus = findActivityStatusByName("In Progress");
        activityService.setStatusAndPriority(uxDesign, inProgressStatus, null, 55);
        commentService.createComment("Design system updated with new patterns", uxDesign, dev2);
        commentService.createComment("Wireframes created for key user journeys", uxDesign, dev2);
        commentService.createComment("Prototypes ready for user testing", uxDesign, analyst);
    }
    // Additional meeting creation methods

    /**
     * Creates additional activities for Digital Transformation Initiative project.
     */
    private void createAdditionalDigitalTransformationActivities() {
        final CProject project = findProjectByName("Digital Transformation Initiative");

        if (project == null) {
            return;
        }
        // Frontend Development Activity
        final CActivity frontendDev = new CActivity("Frontend Development", project);
        final CActivityType developmentType = findActivityTypeByNameAndProject("Development", project);
        activityService.setActivityType(frontendDev, developmentType,
                "Develop responsive user interface components using modern frameworks");
        final CUser dev1 = findUserByLogin("msahin");
        final CUser manager = findUserByLogin("mkaradeniz");
        activityService.setAssignedUsers(frontendDev, dev1, manager);
        activityService.setTimeTracking(frontendDev, new BigDecimal("32.00"), new BigDecimal("28.00"),
                new BigDecimal("4.00"));
        activityService.setDateInfo(frontendDev, LocalDate.now().minusDays(12), LocalDate.now().plusDays(8), null);
        final CActivityStatus inProgressStatus = findActivityStatusByName("In Progress");
        activityService.setStatusAndPriority(frontendDev, inProgressStatus, null, 70);
        commentService.createComment("Frontend development started with React components", frontendDev, dev1);
        commentService.createComment("Implemented responsive design patterns", frontendDev, dev1);
        commentService.createComment("Working on integration with backend APIs", frontendDev, manager);
        // Database Migration Activity
        final CActivity dbMigration = new CActivity("Database Migration", project);
        activityService.setActivityType(dbMigration, developmentType, "Migrate legacy data to new database schema");
        final CUser admin = findUserByLogin("admin");
        activityService.setAssignedUsers(dbMigration, admin, manager);
        activityService.setTimeTracking(dbMigration, new BigDecimal("20.00"), new BigDecimal("5.00"),
                new BigDecimal("15.00"));
        activityService.setDateInfo(dbMigration, LocalDate.now().plusDays(5), LocalDate.now().plusDays(15), null);
        final CActivityStatus notStartedStatus = findActivityStatusByName("Not Started");
        activityService.setStatusAndPriority(dbMigration, notStartedStatus, null, 0);
        commentService.createComment("Database migration plan prepared", dbMigration, admin);
        commentService.createComment("Waiting for backend API completion", dbMigration, manager);
    }

    /**
     * Creates additional activities for Infrastructure Modernization project.
     */
    private void createAdditionalInfrastructureActivities() {
        final CProject project = findProjectByName("Infrastructure Modernization");

        if (project == null) {
            return;
        }
        // Security Audit Activity
        final CActivity securityAudit = new CActivity("Security Audit", project);
        final CActivityType researchType = findActivityTypeByNameAndProject("Research", project);
        activityService.setActivityType(securityAudit, researchType,
                "Comprehensive security assessment and vulnerability analysis");
        final CUser admin = findUserByLogin("admin");
        final CUser manager = findUserByLogin("mkaradeniz");
        activityService.setAssignedUsers(securityAudit, admin, manager);
        activityService.setTimeTracking(securityAudit, new BigDecimal("25.00"), new BigDecimal("0.00"),
                new BigDecimal("25.00"));
        activityService.setDateInfo(securityAudit, LocalDate.now().plusDays(10), LocalDate.now().plusDays(18), null);
        final CActivityStatus notStartedStatus = findActivityStatusByName("Not Started");
        activityService.setStatusAndPriority(securityAudit, notStartedStatus, null, 0);
        commentService.createComment("Security audit requirements defined", securityAudit, admin);
        commentService.createComment("External security firm selected for audit", securityAudit, manager);
        // Server Migration Activity
        final CActivity serverMigration = new CActivity("Server Migration", project);
        final CActivityType developmentType = findActivityTypeByNameAndProject("Development", project);
        activityService.setActivityType(serverMigration, developmentType,
                "Migrate applications to new server infrastructure");
        final CUser dev1 = findUserByLogin("bozkan");
        activityService.setAssignedUsers(serverMigration, dev1, admin);
        activityService.setTimeTracking(serverMigration, new BigDecimal("35.00"), new BigDecimal("20.00"),
                new BigDecimal("15.00"));
        activityService.setDateInfo(serverMigration, LocalDate.now().minusDays(8), LocalDate.now().plusDays(12), null);
        final CActivityStatus onHoldStatus = findActivityStatusByName("On Hold");
        activityService.setStatusAndPriority(serverMigration, onHoldStatus, null, 55);
        commentService.createComment("Server migration plan created", serverMigration, dev1);
        commentService.createComment("Testing environment successfully migrated", serverMigration, dev1);
        commentService.createComment("Production migration on hold pending approval", serverMigration, admin);
    }

    /**
     * Creates additional activities for Product Development Phase 2 project.
     */
    private void createAdditionalProductDevelopmentActivities() {
        final CProject project = findProjectByName("Product Development Phase 2");

        if (project == null) {
            return;
        }
        // Code Review Activity
        final CActivity codeReview = new CActivity("Code Review Process", project);
        final CActivityType testingType = findActivityTypeByNameAndProject("Testing", project);
        activityService.setActivityType(codeReview, testingType, "Comprehensive code review and quality assurance");
        final CUser analyst = findUserByLogin("ademir");
        final CUser manager = findUserByLogin("mkaradeniz");
        activityService.setAssignedUsers(codeReview, analyst, manager);
        activityService.setTimeTracking(codeReview, new BigDecimal("12.00"), new BigDecimal("12.00"),
                new BigDecimal("0.00"));
        activityService.setDateInfo(codeReview, LocalDate.now().minusDays(3), LocalDate.now().minusDays(1),
                LocalDate.now().minusDays(1));
        final CActivityStatus completedStatus = findActivityStatusByName("Completed");
        activityService.setStatusAndPriority(codeReview, completedStatus, null, 100);
        commentService.createComment("Code review process initiated", codeReview, manager);
        commentService.createComment("Found minor issues, created fix recommendations", codeReview, analyst);
        commentService.createComment("All issues resolved, code approved", codeReview, analyst);
        // Performance Testing Activity
        final CActivity perfTesting = new CActivity("Performance Testing", project);
        activityService.setActivityType(perfTesting, testingType, "Load testing and performance optimization");
        final CUser dev2 = findUserByLogin("bozkan");
        activityService.setAssignedUsers(perfTesting, dev2, manager);
        activityService.setTimeTracking(perfTesting, new BigDecimal("18.00"), new BigDecimal("10.00"),
                new BigDecimal("8.00"));
        activityService.setDateInfo(perfTesting, LocalDate.now().minusDays(5), LocalDate.now().plusDays(2), null);
        final CActivityStatus inProgressStatus = findActivityStatusByName("In Progress");
        activityService.setStatusAndPriority(perfTesting, inProgressStatus, null, 60);
        commentService.createComment("Performance testing framework setup", perfTesting, dev2);
        commentService.createComment("Baseline performance metrics collected", perfTesting, dev2);
    }

    /**
     * Creates system administrator user.
     */
    private void createAdminUser() {
        LOGGER.info("createAdminUser called - creating system administrator");
        final CUser admin = userService.createLoginUser("admin", STANDARD_PASSWORD, "Ahmet", "admin@of.gov.tr",
                "ADMIN,USER");
        // Set user profile using auxiliary method
        final byte[] profilePictureBytes = "profile-picture".getBytes();
        userService.setUserProfile(admin, "Yılmaz", "+90-462-751-1001", profilePictureBytes);
        // Set user role using auxiliary method
        userService.setUserRole(admin, CUserRole.ADMIN, "ADMIN,USER");
        // Set company association using auxiliary method
        final CCompany company = findCompanyByName("Of Teknoloji Çözümleri");
        userService.setCompanyAssociation(admin, company);
        LOGGER.info("Administrator user created successfully");
    }

    /**
     * Creates backend development activity.
     */
    private void createBackendDevActivity() {
        LOGGER.info("createBackendDevActivity called - creating backend API development activity");
        final CProject project = findProjectByName("Digital Transformation Initiative");

        if (project == null) {
            LOGGER.warn("Project 'Digital Transformation Initiative' not found, skipping backend activity");
            return;
        }
        // Create the activity using new auxiliary methods
        final CActivity backendDev = new CActivity("Backend API Development", project);
        // Find and set the activity type
        final CActivityType developmentType = findActivityTypeByNameAndProject("Development", project);

        if (developmentType == null) {
            LOGGER.warn("Development activity type not found for project, using null");
        }
        // Set activity type and description using auxiliary method
        activityService.setActivityType(backendDev, developmentType,
                "Develop REST API endpoints for user management and authentication");
        // Set assigned users using auxiliary method
        final CUser manager = findUserByLogin("mkaradeniz");
        final CUser admin = findUserByLogin("admin");
        activityService.setAssignedUsers(backendDev, manager, admin);
        // Set time tracking using auxiliary method
        activityService.setTimeTracking(backendDev, new BigDecimal("40.00"), new BigDecimal("35.50"),
                new BigDecimal("4.50"));
        // Set date information using auxiliary method
        activityService.setDateInfo(backendDev, LocalDate.now().minusDays(10), LocalDate.now().plusDays(5), null);
        // Set status and priority using auxiliary method
        final CActivityStatus inProgressStatus = findActivityStatusByName("In Progress");
        activityService.setStatusAndPriority(backendDev, inProgressStatus, null, 75);
        // Create comments
        commentService.createComment("Initial backend API development started", backendDev, admin);
        commentService.createComment("API endpoints for user registration and login implemented", backendDev, admin);
        commentService.createComment("Working on authentication and authorization features", backendDev, manager);
        LOGGER.info("Backend development activity created successfully");
    }

    private void createCommentPriority(final String name, final String description, final String color,
            final Integer priorityLevel, final boolean isDefault, final int sortOrder) {
        final CProject project = findProjectByName("Digital Transformation Initiative");

        if (project == null) {
            LOGGER.warn("Project not found for comment priority creation, using null");
        }
        final CCommentPriority priority = new CCommentPriority(name, project, color, sortOrder);
        priority.setDescription(description);
        priority.setPriorityLevel(priorityLevel);
        priority.setDefault(isDefault);
        commentPriorityService.save(priority);
        LOGGER.debug("Created comment priority: {}", name);
    }

    /**
     * Creates consulting company.
     */
    private void createConsultingCompany() {
        final CCompany consulting = new CCompany("Of Stratejik Danışmanlık");
        consulting.setDescription("Yönetim danışmanlığı ve stratejik planlama hizmetleri");
        consulting.setAddress("Merkez Mahallesi, Gülbahar Sokağı No:7, Of/Trabzon");
        consulting.setPhone("+90-462-751-0303");
        consulting.setEmail("merhaba@ofdanismanlik.com.tr");
        consulting.setWebsite("https://www.ofdanismanlik.com.tr");
        consulting.setTaxNumber("TR-456789123");
        consulting.setEnabled(true);
        companyService.save(consulting);
        LOGGER.info("Consulting company created successfully");
    }

    /**
     * Creates sample currencies for order management.
     */
    private void createSampleCurrencies() {
        LOGGER.info("createSampleCurrencies called - creating sample currencies");

        try {
            // Create basic currencies
            final CCurrency usd = new CCurrency("US Dollar");
            usd.setDescription("US Dollar");
            usd.setCurrencyCode("USD");
            usd.setCurrencySymbol("$");
            currencyService.save(usd);

            final CCurrency eur = new CCurrency("Euro");
            eur.setDescription("Euro");
            eur.setCurrencyCode("EUR");
            eur.setCurrencySymbol("€");
            currencyService.save(eur);

            final CCurrency try_ = new CCurrency("Turkish Lira");
            try_.setDescription("Turkish Lira");
            try_.setCurrencyCode("TRY");
            try_.setCurrencySymbol("₺");
            currencyService.save(try_);

            final CCurrency gbp = new CCurrency("British Pound");
            gbp.setDescription("British Pound");
            gbp.setCurrencyCode("GBP");
            gbp.setCurrencySymbol("£");
            currencyService.save(gbp);

            LOGGER.info("Sample currencies created successfully");
        } catch (final Exception e) {
            LOGGER.error("Error creating sample currencies", e);
            throw new RuntimeException("Failed to create sample currencies", e);
        }
    }

    /**
     * Creates sample decisions for project management.
     */
    private void createSampleDecisions() {
        LOGGER.info("createSampleDecisions called - creating sample decisions");

        try {
            final CProject project1 = findProjectByName("Digital Transformation Initiative");
            final CProject project2 = findProjectByName("Product Development Phase 2");

            if (project1 == null || project2 == null) {
                LOGGER.warn("Projects not found for decisions");
                return;
            }

            final CUser manager = findUserByLogin("mkaradeniz");
            final CUser admin = findUserByLogin("admin");

            // Decision 1: Technology Stack
            final CDecision techStackDecision = new CDecision("Technology Stack Selection", project1);
            techStackDecision.setDescription(
                    "Decision on the primary technology stack for the digital transformation initiative including frontend framework, backend services, and database choices");
            techStackDecision.setImplementationDate(LocalDate.now().minusDays(15).atStartOfDay());
            if (manager != null) {
                techStackDecision.setAccountableUser(manager);
            }
            decisionService.save(techStackDecision);

            // Decision 2: Cloud Provider
            final CDecision cloudDecision = new CDecision("Cloud Provider Selection", project1);
            cloudDecision.setDescription(
                    "Strategic decision on cloud infrastructure provider for hosting and scalability requirements");
            cloudDecision.setImplementationDate(LocalDate.now().minusDays(10).atStartOfDay());
            if (admin != null) {
                cloudDecision.setAccountableUser(admin);
            }
            decisionService.save(cloudDecision);

            // Decision 3: Development Methodology
            final CDecision methodologyDecision = new CDecision("Development Methodology", project2);
            methodologyDecision.setDescription(
                    "Decision on agile development methodology and sprint structure for product development phase 2");
            methodologyDecision.setImplementationDate(LocalDate.now().minusDays(5).atStartOfDay());
            if (manager != null) {
                methodologyDecision.setAccountableUser(manager);
            }
            decisionService.save(methodologyDecision);

            // Decision 4: Security Framework
            final CDecision securityDecision = new CDecision("Security Framework Implementation", project2);
            securityDecision.setDescription(
                    "Decision on comprehensive security framework including authentication, authorization, and data protection measures");
            securityDecision.setImplementationDate(LocalDate.now().minusDays(3).atStartOfDay());
            if (admin != null) {
                securityDecision.setAccountableUser(admin);
            }
            decisionService.save(securityDecision);

            LOGGER.info("Sample decisions created successfully");
        } catch (final Exception e) {
            LOGGER.error("Error creating sample decisions", e);
            throw new RuntimeException("Failed to create sample decisions", e);
        }
    }

    /**
     * Creates sample orders for project resource management.
     */
    private void createSampleOrders() {
        LOGGER.info("createSampleOrders called - creating sample orders");

        try {
            final CProject project1 = findProjectByName("Infrastructure Modernization");
            final CProject project2 = findProjectByName("Customer Experience Enhancement");

            if (project1 == null || project2 == null) {
                LOGGER.warn("Projects not found for orders");
                return;
            }

            final CUser manager = findUserByLogin("mkaradeniz");
            final CUser admin = findUserByLogin("admin");

            // Order 1: Hardware Equipment
            final COrder hardwareOrder = new COrder("Server Hardware Procurement", project1);
            hardwareOrder.setDescription(
                    "Procurement order for new server hardware including high-performance computing units and storage systems for infrastructure modernization");
            hardwareOrder.setOrderDate(LocalDate.now().minusDays(20));
            hardwareOrder.setDeliveryDate(LocalDate.now().plusDays(30));
            hardwareOrder.setEstimatedCost(new BigDecimal("45000.00"));
            if (manager != null) {
                hardwareOrder.setRequestor(manager);
            }
            orderService.save(hardwareOrder);

            // Order 2: Software Licenses
            final COrder softwareOrder = new COrder("Development Software Licenses", project1);
            softwareOrder.setDescription(
                    "Annual software licenses for development tools, database management systems, and security software");
            softwareOrder.setOrderDate(LocalDate.now().minusDays(15));
            softwareOrder.setDeliveryDate(LocalDate.now().plusDays(5));
            softwareOrder.setEstimatedCost(new BigDecimal("12000.00"));
            if (admin != null) {
                softwareOrder.setRequestor(admin);
            }
            orderService.save(softwareOrder);

            // Order 3: Consulting Services
            final COrder consultingOrder = new COrder("UX Consulting Services", project2);
            consultingOrder.setDescription(
                    "Professional UX consulting services for customer experience enhancement including user research and interface design");
            consultingOrder.setOrderDate(LocalDate.now().minusDays(8));
            consultingOrder.setDeliveryDate(LocalDate.now().plusDays(45));
            consultingOrder.setEstimatedCost(new BigDecimal("28000.00"));
            if (manager != null) {
                consultingOrder.setRequestor(manager);
            }
            orderService.save(consultingOrder);

            // Order 4: Training Services
            final COrder trainingOrder = new COrder("Team Training Programs", project2);
            trainingOrder.setDescription(
                    "Comprehensive training programs for team members on new technologies and customer service excellence");
            trainingOrder.setOrderDate(LocalDate.now().minusDays(3));
            trainingOrder.setDeliveryDate(LocalDate.now().plusDays(60));
            trainingOrder.setEstimatedCost(new BigDecimal("15000.00"));
            if (admin != null) {
                trainingOrder.setRequestor(admin);
            }
            orderService.save(trainingOrder);

            LOGGER.info("Sample orders created successfully");
        } catch (final Exception e) {
            LOGGER.error("Error creating sample orders", e);
            throw new RuntimeException("Failed to create sample orders", e);
        }
    }

    private void createCriticalSecurityRisk() {
        final CProject project = findProjectByName("Customer Experience Enhancement");

        if (project == null) {
            LOGGER.warn("Project not found for security risk");
            return;
        }
        final CRisk risk = new CRisk("Data Privacy Compliance Gaps", project, ERiskSeverity.CRITICAL);
        risk.setDescription("Current implementation may not fully comply with GDPR and data protection regulations");
        riskService.save(risk);
        LOGGER.info("Critical security risk created successfully");
    }

    private void createDecisionStatus(final String name, final CProject project, final String description,
            final String color, final boolean isFinal, final int sortOrder) {
        final CDecisionStatus status = new CDecisionStatus(name, project);
        status.setDescription(description);
        status.setColor(color);
        status.setFinal(isFinal);
        status.setSortOrder(sortOrder);
        decisionStatusService.save(status);
        LOGGER.debug("Created decision status: {}", name);
    }

    /**
     * Creates healthcare company.
     */
    private void createHealthcareCompany() {
        final CCompany healthcare = new CCompany("Of Sağlık Teknolojileri");
        healthcare.setDescription("İleri tıp teknolojisi ve sağlık çözümleri");
        healthcare.setAddress("Yeni Mahalle, Sağlık Sokağı No:21, Of/Trabzon");
        healthcare.setPhone("+90-462-751-0404");
        healthcare.setEmail("iletisim@ofsaglik.com.tr");
        healthcare.setWebsite("https://www.ofsaglik.com.tr");
        healthcare.setTaxNumber("TR-789123456");
        healthcare.setEnabled(true);
        companyService.save(healthcare);
        LOGGER.info("Healthcare company created successfully");
    }

    /**
     * Creates high priority technical risk.
     */
    private void createHighPriorityTechnicalRisk() {
        LOGGER.info("createHighPriorityTechnicalRisk called - creating technical risk");
        final CProject project = findProjectByName("Digital Transformation Initiative");

        if (project == null) {
            LOGGER.warn("Project not found for technical risk");
            return;
        }
        final CRisk risk = new CRisk("Legacy System Integration Challenges", project, ERiskSeverity.HIGH);
        risk.setDescription(
                "Integration with legacy systems may cause compatibility issues and performance bottlenecks");
        riskService.save(risk);
        LOGGER.info("High priority technical risk created successfully");
    }

    /**
     * Creates low priority resource risk.
     */
    private void createLowPriorityResourceRisk() {
        LOGGER.info("createLowPriorityResourceRisk called - creating resource risk");
        final CProject project = findProjectByName("Infrastructure Modernization");

        if (project == null) {
            LOGGER.warn("Project not found for resource risk");
            return;
        }
        final CRisk risk = new CRisk("Team Member Vacation Scheduling Conflicts", project, ERiskSeverity.LOW);
        risk.setDescription("Overlapping vacation schedules may temporarily reduce team capacity");
        riskService.save(risk);
        LOGGER.info("Low priority resource risk created successfully");
    }

    /**
     * Creates low priority schedule risk.
     */
    private void createLowPriorityScheduleRisk() {
        LOGGER.info("createLowPriorityScheduleRisk called - creating schedule risk");
        final CProject project = findProjectByName("Digital Transformation Initiative");

        if (project == null) {
            LOGGER.warn("Project not found for schedule risk");
            return;
        }
        final CRisk risk = new CRisk("Minor Delays in Third-Party Integrations", project, ERiskSeverity.LOW);
        risk.setDescription("External vendor may experience minor delays in API delivery");
        riskService.save(risk);
        LOGGER.info("Low priority schedule risk created successfully");
    }

    /**
     * Creates manufacturing company.
     */
    private void createManufacturingCompany() {
        LOGGER.info("createManufacturingCompany called - creating Of Endüstri Dinamikleri");
        final CCompany manufacturing = new CCompany("Of Endüstri Dinamikleri");
        manufacturing.setDescription("Hassas mühendislik bileşenlerinde lider üretici");
        manufacturing.setAddress("Sanayi Mahallesi, İstiklal Caddesi No:42, Of/Trabzon");
        manufacturing.setPhone("+90-462-751-0202");
        manufacturing.setEmail("bilgi@ofendüstri.com.tr");
        manufacturing.setWebsite("https://www.ofendüstri.com.tr");
        manufacturing.setTaxNumber("TR-987654321");
        manufacturing.setEnabled(true);
        companyService.save(manufacturing);
        LOGGER.info("Manufacturing company created successfully");
    }

    /**
     * Creates medium priority budget risk.
     */
    private void createMediumPriorityBudgetRisk() {
        final CProject project = findProjectByName("Product Development Phase 2");

        if (project == null) {
            LOGGER.warn("Project not found for budget risk");
            return;
        }
        final CRisk risk = new CRisk("Budget Overrun Due to Scope Creep", project, ERiskSeverity.MEDIUM);
        risk.setDescription("Uncontrolled feature additions may cause budget to exceed allocated resources");
        riskService.save(risk);
        LOGGER.info("Medium priority budget risk created successfully");
    }

    private void createMeetingStatus(final String name, final CProject project, final String description,
            final String color, final boolean isFinal, final int sortOrder) {
        final CMeetingStatus status = new CMeetingStatus(name, project);
        status.setDescription(description);
        status.setColor(color);
        status.setFinal(isFinal);
        status.setSortOrder(sortOrder);
        meetingStatusService.save(status);
        LOGGER.debug("Created meeting status: {}", name);
    }

    private void createOrderStatus(final String name, final CProject project, final String description,
            final String color, final boolean isFinal, final int sortOrder) {
        final COrderStatus status = new COrderStatus(name, project);
        status.setName(name);
        status.setDescription(description);
        // COrderStatus extends CEntityNamed, not CStatus, so it doesn't have color,
        // isFinal, sortOrder
        orderStatusService.save(status);
        LOGGER.debug("Created order status: {}", name);
    }

    /**
     * Creates project manager user.
     */
    private void createProjectManagerUser() {
        LOGGER.info("createProjectManagerUser called - creating project manager");
        final CUser manager = userService.createLoginUser("mkaradeniz", STANDARD_PASSWORD, "Mehmet Emin",
                "mehmet.karadeniz@ofteknoloji.com.tr", "MANAGER,USER");
        // Set user profile using auxiliary method
        final byte[] profilePictureBytes = "profile-picture".getBytes();
        userService.setUserProfile(manager, "Karadeniz", "+90-462-751-1002", profilePictureBytes);
        // Set user role using auxiliary method
        userService.setUserRole(manager, CUserRole.PROJECT_MANAGER, "MANAGER,USER");
        // Set company association using auxiliary method
        final CCompany company = findCompanyByName("Of Teknoloji Çözümleri");
        userService.setCompanyAssociation(manager, company);
        LOGGER.info("Project manager user created successfully");
    }

    private void createProjectWithDescription(final String name, final String description) {
        final CProject project = new CProject(name);
        project.setDescription(description);
        projectService.save(project);
        LOGGER.debug("Created project: {} with description", name);
    }

    /**
     * Creates sample planning meeting.
     */
    private void createSamplePlanningMeeting() {
        LOGGER.info("createSamplePlanningMeeting called - creating sprint planning meeting");
        final CProject project = findProjectByName("Product Development Phase 2");

        if (project == null) {
            LOGGER.warn("Project not found for planning meeting");
            return;
        }
        final CMeeting meeting = new CMeeting("Sprint Planning - Q1 2024", project);
        meeting.setDescription("Planning for next sprint with story estimation and task assignment");
        meeting.setMeetingDate(LocalDateTime.now().plusDays(3).withHour(14).withMinute(0));
        meeting.setEndDate(LocalDateTime.now().plusDays(3).withHour(16).withMinute(0));
        meeting.setLocation("Meeting Room B");
        // Add participants
        final Set<CUser> participants = new HashSet<>();
        final CUser manager = findUserByLogin("mkaradeniz");
        final CUser analyst = findUserByLogin("ademir");

        if (manager != null) {
            participants.add(manager);
        }

        if (analyst != null) {
            participants.add(analyst);
        }
        meeting.setParticipants(participants);
        meetingService.save(meeting);
        LOGGER.info("Sample planning meeting created successfully");
    }

    /**
     * Creates sample project meeting using auxiliary service methods. Demonstrates the use of auxiliary meeting service
     * methods.
     */
    private void createSampleProjectMeeting() {
        LOGGER.info("createSampleProjectMeeting called - creating sample project meeting");
        final CProject project = findProjectByName("Digital Transformation Initiative");

        if (project == null) {
            LOGGER.warn("Project 'Digital Transformation Initiative' not found, skipping meeting creation");
            return;
        }
        // Create the meeting using new auxiliary methods
        final CMeeting meeting = new CMeeting("Weekly Project Status Meeting", project);
        meeting.setDescription(
                "Weekly status update on project progress, blockers discussion, and next steps planning");
        // Set meeting details using auxiliary method
        meetingService.setMeetingDetails(meeting, null, LocalDateTime.now().plusDays(1).withHour(14).withMinute(0),
                LocalDateTime.now().plusDays(1).withHour(15).withMinute(0), "Conference Room A");
        // Set meeting content using auxiliary method
        final CUser responsible = findUserByLogin("mkaradeniz");
        meetingService.setMeetingContent(meeting,
                "Weekly status update on project progress, blockers discussion, and next steps planning", null,
                responsible);
        // Set participants using auxiliary method
        final Set<CUser> participants = new HashSet<>();
        participants.add(findUserByLogin("admin"));
        participants.add(findUserByLogin("mkaradeniz"));
        participants.add(findUserByLogin("bozkan"));
        participants.add(findUserByLogin("msahin"));
        meetingService.setParticipants(meeting, participants);
        // Set meeting status using proper status entity
        final CMeetingStatus scheduledStatus = findMeetingStatusByName("Scheduled");
        meeting.setStatus(scheduledStatus);
        // Set meeting status using auxiliary method
        meetingService.setMeetingStatus(meeting, scheduledStatus, "Meeting agenda prepared, participants notified",
                "Project management system");
        LOGGER.info("Sample project meeting created successfully using auxiliary methods");
    }

    /**
     * Creates sample retrospective meeting.
     */
    private void createSampleRetrospectiveMeeting() {
        LOGGER.info("createSampleRetrospectiveMeeting called - creating sprint retrospective");
        final CProject project = findProjectByName("Customer Experience Enhancement");

        if (project == null) {
            LOGGER.warn("Project not found for retrospective meeting");
            return;
        }
        final CMeeting meeting = new CMeeting("Sprint Retrospective", project);
        meeting.setDescription("Team reflection on what went well, what could be improved, and action items");
        meeting.setMeetingDate(LocalDateTime.now().minusDays(7).withHour(15).withMinute(0));
        meeting.setEndDate(LocalDateTime.now().minusDays(7).withHour(16).withMinute(0));
        meeting.setLocation("Conference Room C");
        // Set proper status for completed meeting
        final CMeetingStatus completedStatus = findMeetingStatusByName("Completed");
        meeting.setStatus(completedStatus);
        // Add participants and attendees
        final Set<CUser> participants = new HashSet<>();
        final Set<CUser> attendees = new HashSet<>();
        final CUser manager = findUserByLogin("mkaradeniz");
        final CUser dev1 = findUserByLogin("ademir");
        final CUser dev2 = findUserByLogin("msahin");

        if (manager != null) {
            participants.add(manager);
            attendees.add(manager);
        }

        if (dev1 != null) {
            participants.add(dev1);
            attendees.add(dev1);
        }

        if (dev2 != null) {
            participants.add(dev2);
        }
        meeting.setParticipants(participants);
        meeting.setAttendees(attendees);
        meetingService.save(meeting);
        LOGGER.info("Sample retrospective meeting created successfully");
    }

    /**
     * Creates sample review meeting.
     */
    private void createSampleReviewMeeting() {
        LOGGER.info("createSampleReviewMeeting called - creating code review meeting");
        final CProject project = findProjectByName("Infrastructure Modernization");

        if (project == null) {
            LOGGER.warn("Project not found for review meeting");
            return;
        }
        final CMeeting meeting = new CMeeting("Code Review Session", project);
        meeting.setDescription("Review of architectural changes and code quality improvements");
        meeting.setMeetingDate(LocalDateTime.now().minusDays(2).withHour(10).withMinute(0));
        meeting.setEndDate(LocalDateTime.now().minusDays(2).withHour(11).withMinute(30));
        meeting.setLocation("Virtual - Zoom");
        // Add participants
        final Set<CUser> participants = new HashSet<>();
        final CUser manager = findUserByLogin("mkaradeniz");
        final CUser dev = findUserByLogin("msahin");

        if (manager != null) {
            participants.add(manager);
        }

        if (dev != null) {
            participants.add(dev);
        }
        meeting.setParticipants(participants);
        meetingService.save(meeting);
        LOGGER.info("Sample review meeting created successfully");
    }

    /**
     * Creates sample standup meeting.
     */
    private void createSampleStandupMeeting() {
        LOGGER.info("createSampleStandupMeeting called - creating daily standup meeting");
        final CProject project = findProjectByName("Digital Transformation Initiative");

        if (project == null) {
            LOGGER.warn("Project not found for standup meeting");
            return;
        }
        final CMeeting meeting = new CMeeting("Daily Standup - Sprint 3", project);
        meeting.setDescription("Daily progress sync and impediment discussion");
        meeting.setMeetingDate(LocalDateTime.now().plusDays(1).withHour(9).withMinute(0));
        meeting.setEndDate(LocalDateTime.now().plusDays(1).withHour(9).withMinute(30));
        meeting.setLocation("Conference Room A");
        // Set proper status
        final CMeetingStatus scheduledStatus = findMeetingStatusByName("Scheduled");
        meeting.setStatus(scheduledStatus);
        // Add participants
        final Set<CUser> participants = new HashSet<>();
        final CUser manager = findUserByLogin("mkaradeniz");
        final CUser dev1 = findUserByLogin("ademir");
        final CUser dev2 = findUserByLogin("msahin");

        if (manager != null) {
            participants.add(manager);
        }

        if (dev1 != null) {
            participants.add(dev1);
        }

        if (dev2 != null) {
            participants.add(dev2);
        }
        meeting.setParticipants(participants);
        meetingService.save(meeting);
        LOGGER.info("Sample standup meeting created successfully");
    }

    /**
     * Creates system architecture design activity.
     */
    private void createSystemArchitectureActivity() {
        LOGGER.info("createSystemArchitectureActivity called - creating system architecture design activity");
        final CProject project = findProjectByName("Infrastructure Modernization");

        if (project == null) {
            LOGGER.warn("Project 'Infrastructure Modernization' not found, skipping architecture activity");
            return;
        }
        // Create the activity using new auxiliary methods
        final CActivity archDesign = new CActivity("System Architecture Design", project);
        // Find and set the activity type
        final CActivityType designType = findActivityTypeByNameAndProject("Design", project);

        if (designType == null) {
            LOGGER.warn("Design activity type not found for project, using null");
        }
        // Set activity type and description using auxiliary method
        activityService.setActivityType(archDesign, designType,
                "Design scalable system architecture for infrastructure modernization");
        // Set assigned users using auxiliary method
        final CUser teamMember2 = findUserByLogin("bozkan");
        final CUser admin = findUserByLogin("admin");
        activityService.setAssignedUsers(archDesign, teamMember2, admin);
        // Set time tracking using auxiliary method
        activityService.setTimeTracking(archDesign, new BigDecimal("60.00"), new BigDecimal("45.00"),
                new BigDecimal("15.00"));
        // Set date information using auxiliary method
        activityService.setDateInfo(archDesign, LocalDate.now().minusDays(15), LocalDate.now().plusDays(10), null);
        // Set status and priority using auxiliary method
        final CActivityStatus onHoldStatus = findActivityStatusByName("On Hold");
        activityService.setStatusAndPriority(archDesign, onHoldStatus, null, 65);
        // Create comments
        commentService.createComment("Initial system architecture design phase started", archDesign, admin);
        commentService.createComment("Completed high-level architecture diagrams and component definitions", archDesign,
                admin);
        commentService.createComment("Reviewed architecture with team and incorporated feedback", archDesign,
                teamMember2);
        commentService.createComment("Activity on hold pending stakeholder approval of design changes", archDesign,
                teamMember2);
        LOGGER.info("System architecture activity created successfully");
    }

    /**
     * Creates team member Alice Davis.
     */
    private void createTeamMemberAlice() {
        LOGGER.info("createTeamMemberAlice called - creating Ayşe Demir from Günebakan village");
        final CUser analyst = userService.createLoginUser("ademir", STANDARD_PASSWORD, "Ayşe",
                "ayse.demir@ofsaglik.com.tr", "USER");
        // Set user profile using auxiliary method
        final byte[] profilePictureBytes = "profile-picture".getBytes();
        userService.setUserProfile(analyst, "Demir", "+90-462-751-1005", profilePictureBytes);
        // Set user role using auxiliary method
        userService.setUserRole(analyst, CUserRole.TEAM_MEMBER, "USER");
        // Set company association using auxiliary method
        final CCompany company = findCompanyByName("Of Sağlık Teknolojileri");
        userService.setCompanyAssociation(analyst, company);
        LOGGER.info("Team member Ayşe Demir created successfully");
    }

    /**
     * Creates team member Bob Wilson.
     */
    private void createTeamMemberBob() {
        LOGGER.info("createTeamMemberBob called - creating Burak Özkan from Çamburnu village");
        final CUser developer = userService.createLoginUser("bozkan", STANDARD_PASSWORD, "Burak",
                "burak.ozkan@ofdanismanlik.com.tr", "USER");
        // Set user profile using auxiliary method
        final byte[] profilePictureBytes = "profile-picture".getBytes();
        userService.setUserProfile(developer, "Özkan", "+90-462-751-1004", profilePictureBytes);
        // Set user role using auxiliary method
        userService.setUserRole(developer, CUserRole.TEAM_MEMBER, "USER");
        // Set company association using auxiliary method
        final CCompany company = findCompanyByName("Of Stratejik Danışmanlık");
        userService.setCompanyAssociation(developer, company);
        LOGGER.info("Team member Burak Özkan created successfully");
    }

    /**
     * Creates team member Mary Johnson.
     */
    private void createTeamMemberMary() {
        LOGGER.info("createTeamMemberMary called - creating Merve Şahin from Ballıköy village");
        final CUser teamMember = userService.createLoginUser("msahin", STANDARD_PASSWORD, "Merve",
                "merve.sahin@ofendüstri.com.tr", "USER");
        // Set user profile using auxiliary method
        final byte[] profilePictureBytes = "profile-picture".getBytes();
        userService.setUserProfile(teamMember, "Şahin", "+90-462-751-1003", profilePictureBytes);
        // Set user role using auxiliary method
        userService.setUserRole(teamMember, CUserRole.TEAM_MEMBER, "USER");
        // Set company association using auxiliary method
        final CCompany company = findCompanyByName("Of Endüstri Dinamikleri");
        userService.setCompanyAssociation(teamMember, company);
        LOGGER.info("Team member Merve Şahin created successfully");
    }

    /**
     * Creates team member users across different companies.
     */
    private void createTeamMemberUsers() {
        LOGGER.info("createTeamMemberUsers called - creating team member users");
        createTeamMemberMary();
        createTeamMemberBob();
        createTeamMemberAlice();
        LOGGER.info("Team member users created successfully");
    }

    /**
     * Creates technology startup company.
     */
    private void createTechCompany() {
        LOGGER.info("createTechCompany called - creating Of Teknoloji Çözümleri");
        final CCompany techStartup = new CCompany("Of Teknoloji Çözümleri");
        techStartup.setDescription("Dijital dönüşüm için yenilikçi teknoloji çözümleri");
        techStartup.setAddress("Cumhuriyet Mahallesi, Atatürk Caddesi No:15, Of/Trabzon");
        techStartup.setPhone("+90-462-751-0101");
        techStartup.setEmail("iletisim@ofteknoloji.com.tr");
        techStartup.setWebsite("https://www.ofteknoloji.com.tr");
        techStartup.setTaxNumber("TR-123456789");
        techStartup.setEnabled(true);
        companyService.save(techStartup);
        LOGGER.info("Technology company created successfully");
    }

    /**
     * Creates technical documentation activity.
     */
    private void createTechnicalDocumentationActivity() {
        LOGGER.info("createTechnicalDocumentationActivity called - creating technical documentation activity");
        final CProject project = findProjectByName("Customer Experience Enhancement");

        if (project == null) {
            LOGGER.warn("Project 'Customer Experience Enhancement' not found, skipping documentation activity");
            return;
        }
        // Create the activity using new auxiliary methods
        final CActivity techDoc = new CActivity("Technical Documentation Update", project);
        // Find and set the activity type
        final CActivityType documentationType = findActivityTypeByNameAndProject("Documentation", project);

        if (documentationType == null) {
            LOGGER.warn("Documentation activity type not found for project, using null");
        }
        // Set activity type and description using auxiliary method
        activityService.setActivityType(techDoc, documentationType,
                "Update and enhance technical documentation for customer experience features");
        // Set assigned users using auxiliary method
        final CUser analyst = findUserByLogin("ademir");
        final CUser manager = findUserByLogin("mkaradeniz");
        activityService.setAssignedUsers(techDoc, analyst, manager);
        // Set time tracking using auxiliary method (completed activity)
        activityService.setTimeTracking(techDoc, new BigDecimal("16.00"), new BigDecimal("16.00"),
                new BigDecimal("0.00"));
        // Set date information using auxiliary method (completed activity)
        activityService.setDateInfo(techDoc, LocalDate.now().minusDays(5), LocalDate.now().minusDays(1),
                LocalDate.now().minusDays(1));
        // Set status and priority using auxiliary method (completed activity)
        final CActivityStatus completedStatus = findActivityStatusByName("Completed");
        activityService.setStatusAndPriority(techDoc, completedStatus, null, 100);
        // Create comments
        commentService.createComment("Initial technical documentation review and updates started", techDoc, manager);
        commentService.createComment("Completed updates to user guides and API documentation", techDoc, analyst);
        commentService.createComment("Reviewed documentation with team and incorporated feedback", techDoc, analyst);
        commentService.createComment("Documentation successfully updated and approved by stakeholders", techDoc,
                manager);
        LOGGER.info("Technical documentation activity created successfully");
    }

    /**
     * Creates UI testing activity.
     */
    private void createUITestingActivity() {
        LOGGER.info("createUITestingActivity called - creating user interface testing activity");
        final CProject project = findProjectByName("Product Development Phase 2");

        if (project == null) {
            LOGGER.warn("Project 'Product Development Phase 2' not found, skipping UI testing activity");
            return;
        }
        // Create the activity using new auxiliary methods
        final CActivity uiTesting = new CActivity("User Interface Testing", project);
        // Find and set the activity type
        final CActivityType testingType = findActivityTypeByNameAndProject("Testing", project);

        if (testingType == null) {
            LOGGER.warn("Testing activity type not found for project, using null");
        }
        // Set activity type and description using auxiliary method
        activityService.setActivityType(uiTesting, testingType,
                "Comprehensive testing of user interface components and workflows");
        // Set assigned users using auxiliary method
        final CUser teamMember1 = findUserByLogin("msahin");
        final CUser manager = findUserByLogin("mkaradeniz");
        activityService.setAssignedUsers(uiTesting, teamMember1, manager);
        // Set time tracking using auxiliary method
        activityService.setTimeTracking(uiTesting, new BigDecimal("24.00"), new BigDecimal("20.00"),
                new BigDecimal("4.00"));
        // Set date information using auxiliary method
        activityService.setDateInfo(uiTesting, LocalDate.now().minusDays(7), LocalDate.now().plusDays(3), null);
        // Set status and priority using auxiliary method
        final CActivityStatus inProgressStatus = findActivityStatusByName("In Progress");
        activityService.setStatusAndPriority(uiTesting, inProgressStatus, null, 85);
        // Create comments
        commentService.createComment("UI testing activity initiated with comprehensive test plan", uiTesting, manager);
        commentService.createComment("Completed responsive design testing across multiple devices", uiTesting,
                teamMember1);
        commentService.createComment("Working on accessibility testing and user experience validation", uiTesting,
                teamMember1);
        LOGGER.info("UI testing activity created successfully");
    }

    /**
     * Helper method to find activity status by name.
     * 
     * @param name
     *            the status name to search for
     * @return the CActivityStatus entity or null if not found
     */
    private CActivityStatus findActivityStatusByName(final String name) {
        LOGGER.debug("findActivityStatusByName called with name: {}", name);

        try {
            final var statuses = activityStatusService.list(org.springframework.data.domain.Pageable.unpaged());
            return statuses.stream().filter(status -> name.equals(status.getName())).findFirst().orElse(null);
        } catch (final Exception e) {
            LOGGER.error("Error finding activity status by name: {}", name, e);
            return null;
        }
    }

    private CActivityType findActivityTypeByNameAndProject(final String name, final CProject project) {

        try {
            // Get all activity types for the project and find by name
            final var activityTypes = activityTypeService.list(org.springframework.data.domain.Pageable.unpaged());
            return activityTypes.stream()
                    .filter(type -> name.equals(type.getName()) && project.equals(type.getProject())).findFirst()
                    .orElse(null);
        } catch (final Exception e) {
            LOGGER.error("Error finding activity type by name: {} for project: {}", name, project.getName(), e);
            return null;
        }
    }

    /**
     * Helper method to find company by name.
     * 
     * @param name
     *            the company name to search for
     * @return the CCompany entity or null if not found
     */
    private CCompany findCompanyByName(final String name) {
        LOGGER.info("findCompanyByName called with name: {}", name);

        try {
            return companyService.findByName(name).orElse(null);
        } catch (final Exception e) {
            LOGGER.warn("Could not find company with name: {}", name);
            return null;
        }
    }

    /**
     * Helper method to find meeting status by name.
     * 
     * @param name
     *            the status name to search for
     * @return the CMeetingStatus entity or null if not found
     */
    private CMeetingStatus findMeetingStatusByName(final String name) {
        LOGGER.debug("findMeetingStatusByName called with name: {}", name);

        try {
            final var statuses = meetingStatusService.list(org.springframework.data.domain.Pageable.unpaged());
            return statuses.stream().filter(status -> name.equals(status.getName())).findFirst().orElse(null);
        } catch (final Exception e) {
            LOGGER.error("Error finding meeting status by name: {}", name, e);
            return null;
        }
    }

    /**
     * Helper method to find project by name.
     * 
     * @param name
     *            the project name to search for
     * @return the CProject entity or null if not found
     */
    private CProject findProjectByName(final String name) {
        LOGGER.info("findProjectByName called with name: {}", name);

        try {
            return projectService.findByName(name).orElse(null);
        } catch (final Exception e) {
            LOGGER.warn("Could not find project with name: {}", name);
            return null;
        }
    }

    /**
     * Helper method to find user by login.
     * 
     * @param login
     *            the user login to search for
     * @return the CUser entity or null if not found
     */
    private CUser findUserByLogin(final String login) {
        // LOGGER.info("findUserByLogin called with login: {}", login);

        try {
            return userService.findByLogin(login);
        } catch (final Exception e) {
            LOGGER.warn("Could not find user with login: {}", login);
            return null;
        }
    }

    /**
     * Initializes comprehensive activity data with available fields populated.
     */
    private void initializeActivities() {
        LOGGER.info("initializeActivities called - creating comprehensive activity samples");

        try {
            // Create at least 3 activities per project
            createBackendDevActivity();
            createUITestingActivity();
            createSystemArchitectureActivity();
            createTechnicalDocumentationActivity();
            // Additional activities to meet 3+ per project requirement
            createAdditionalDigitalTransformationActivities();
            createAdditionalProductDevelopmentActivities();
            createAdditionalInfrastructureActivities();
            createAdditionalCustomerExperienceActivities();
            // LOGGER.info("Successfully created comprehensive activity samples");
        } catch (final Exception e) {
            LOGGER.error("Error creating sample activities", e);
            throw new RuntimeException("Failed to initialize activities", e);
        }
    }

    /**
     * Initialize activity status entities with comprehensive sample data.
     */
    private void initializeActivityStatuses() {
        LOGGER.info("initializeActivityStatuses called - creating activity status classifications");

        try {
            // Get all projects
            final String[] projectNames = { "Digital Transformation Initiative", "Product Development Phase 2",
                    "Infrastructure Modernization", "Customer Experience Enhancement" };

            // Define meeting types to create for each project
            for (final String projectName : projectNames) {
                final CProject project = findProjectByName(projectName);

                if (project == null) {
                    LOGGER.warn("Project '{}' not found, skipping meeting type creation", projectName);
                    continue;
                }
                createActivityStatus("Not Started", project, "Activity has not been started yet", "#95a5a6", false, 1);
                createActivityStatus("In Progress", project, "Activity is currently in progress", "#3498db", false, 2);
                createActivityStatus("On Hold", project, "Activity is temporarily on hold", "#f39c12", false, 3);
                createActivityStatus("Completed", project, "Activity has been completed", "#27ae60", true, 4);
                createActivityStatus("Cancelled", project, "Activity has been cancelled", "#e74c3c", true, 5);
                LOGGER.info("Activity statuses initialized successfully");
            }
        } catch (final Exception e) {
            LOGGER.error("Error initializing activity statuses", e);
            throw new RuntimeException("Failed to initialize activity statuses", e);
        }
    }

    /**
     * Initializes activity types for categorizing different kinds of work. Creates types for all projects to ensure
     * project-specific categorization.
     */
    private void initializeActivityTypes() {
        LOGGER.info("initializeActivityTypes called - creating activity type classifications for all projects");

        try {
            // Get all projects
            final String[] projectNames = { "Digital Transformation Initiative", "Product Development Phase 2",
                    "Infrastructure Modernization", "Customer Experience Enhancement" };
            // Define activity types to create for each project
            final String[][] activityTypes = { { "Development", "Software development and coding tasks" },
                    { "Testing", "Quality assurance and testing activities" },
                    { "Design", "UI/UX design and system architecture" },
                    { "Documentation", "Technical writing and documentation" },
                    { "Research", "Research and analysis activities" } };

            // Create activity types for each project
            for (final String projectName : projectNames) {
                final CProject project = findProjectByName(projectName);

                if (project == null) {
                    LOGGER.warn("Project '{}' not found, skipping activity type creation", projectName);
                    continue;
                }
                LOGGER.info("Creating activity types for project: {}", projectName);

                for (final String[] typeData : activityTypes) {
                    final CActivityType item = activityTypeService.createEntity(typeData[0], project);
                    item.setDescription(typeData[1]);
                }
            }
            LOGGER.info("Successfully created activity types for all projects");
        } catch (final Exception e) {
            LOGGER.error("Error creating activity types", e);
            throw new RuntimeException("Failed to initialize activity types", e);
        }
    }

    /**
     * Initialize comment priority entities with comprehensive sample data.
     */
    private void initializeCommentPriorities() {
        LOGGER.info("initializeCommentPriorities called - creating comment priority classifications");

        try {
            createCommentPriority("Critical", "Critical priority requiring immediate attention", "#e74c3c", 1, false,
                    1);
            createCommentPriority("High", "High priority requiring urgent attention", "#f39c12", 2, false, 2);
            createCommentPriority("Normal", "Normal priority for standard processing", "#3498db", 3, true, 3);
            createCommentPriority("Low", "Low priority for non-urgent matters", "#95a5a6", 4, false, 4);
            createCommentPriority("Info", "Informational priority for reference", "#27ae60", 5, false, 5);
            LOGGER.info("Comment priorities initialized successfully");
        } catch (final Exception e) {
            LOGGER.error("Error initializing comment priorities", e);
            throw new RuntimeException("Failed to initialize comment priorities", e);
        }
    }

    /**
     * Initializes sample companies with full details.
     */
    private void initializeCompanies() {
        LOGGER.info("initializeCompanies called - creating 4 sample companies");

        try {
            createTechCompany();
            createManufacturingCompany();
            createConsultingCompany();
            createHealthcareCompany();
            // LOGGER.info("Successfully created 4 sample companies");
        } catch (final Exception e) {
            LOGGER.error("Error creating sample companies", e);
            throw new RuntimeException("Failed to initialize companies", e);
        }
    }

    /**
     * Initialize decision status entities with comprehensive sample data.
     */
    private void initializeDecisionStatuses() {
        LOGGER.info("initializeDecisionStatuses called - creating decision status classifications");

        try {
            // Get all projects
            final String[] projectNames = { "Digital Transformation Initiative", "Product Development Phase 2",
                    "Infrastructure Modernization", "Customer Experience Enhancement" };

            // Define meeting types to create for each project
            for (final String projectName : projectNames) {
                final CProject project = findProjectByName(projectName);

                if (project == null) {
                    LOGGER.warn("Project '{}' not found, skipping meeting type creation", projectName);
                    continue;
                }
                createDecisionStatus("Draft", project, "Decision is in draft state", "#95a5a6", false, 1);
                createDecisionStatus("Under Review", project, "Decision is being reviewed", "#f39c12", false, 2);
                createDecisionStatus("Approved", project, "Decision has been approved", "#27ae60", false, 3);
                createDecisionStatus("Implemented", project, "Decision has been implemented", "#2ecc71", true, 4);
                createDecisionStatus("Rejected", project, "Decision has been rejected", "#e74c3c", true, 5);
            }
            LOGGER.info("Decision statuses initialized successfully");
        } catch (final Exception e) {
            LOGGER.error("Error initializing decision statuses", e);
            throw new RuntimeException("Failed to initialize decision statuses", e);
        }
    }
    // Risk creation methods

    /**
     * Initializes decision types for categorizing different kinds of decisions. Creates types for all projects to
     * ensure project-specific categorization.
     */
    private void initializeDecisionTypes() {
        // TODO fix
    }

    /**
     * Initializes sample meetings with participants and content.
     */
    private void initializeMeetings() {
        LOGGER.info("initializeMeetings called - creating sample meetings");

        try {
            createSampleProjectMeeting();
            createSampleStandupMeeting();
            createSamplePlanningMeeting();
            createSampleReviewMeeting();
            createSampleRetrospectiveMeeting();
            LOGGER.info("Successfully created 5 sample meetings");
        } catch (final Exception e) {
            LOGGER.error("Error creating sample meetings", e);
            throw new RuntimeException("Failed to initialize meetings", e);
        }
    }

    /**
     * Initialize meeting status entities with comprehensive sample data.
     */
    private void initializeMeetingStatuses() {
        LOGGER.info("initializeMeetingStatuses called - creating meeting status classifications");

        try {
            // Get all projects
            final String[] projectNames = { "Digital Transformation Initiative", "Product Development Phase 2",
                    "Infrastructure Modernization", "Customer Experience Enhancement" };

            // Define meeting types to create for each project
            for (final String projectName : projectNames) {
                final CProject project = findProjectByName(projectName);

                if (project == null) {
                    LOGGER.warn("Project '{}' not found, skipping meeting type creation", projectName);
                    continue;
                }
                LOGGER.info("Creating meeting types for project: {}", projectName);
                createMeetingStatus("Scheduled", project, "Meeting is scheduled but not yet started", "#3498db", false,
                        1);
                createMeetingStatus("In Progress", project, "Meeting is currently in progress", "#f39c12", false, 2);
                createMeetingStatus("Completed", project, "Meeting has been completed successfully", "#27ae60", true,
                        3);
                createMeetingStatus("Cancelled", project, "Meeting has been cancelled", "#e74c3c", true, 4);
                createMeetingStatus("Postponed", project, "Meeting has been postponed to a later date", "#9b59b6",
                        false, 5);
            }
            LOGGER.info("Meeting statuses initialized successfully");
        } catch (final Exception e) {
            LOGGER.error("Error initializing meeting statuses", e);
            throw new RuntimeException("Failed to initialize meeting statuses", e);
        }
    }

    /**
     * Initializes meeting types for categorizing different kinds of meetings. Creates types for all projects to ensure
     * project-specific categorization.
     */
    private void initializeMeetingTypes() {
        LOGGER.info("initializeMeetingTypes called - creating meeting type classifications for all projects");

        try {
            // Get all projects
            final String[] projectNames = { "Digital Transformation Initiative", "Product Development Phase 2",
                    "Infrastructure Modernization", "Customer Experience Enhancement" };
            // Define meeting types to create for each project
            final String[][] meetingTypes = { { "Standup" }, { "Planning" }, { "Review" }, { "Retrospective" },
                    { "One-on-One" } };

            // Create meeting types for each project
            for (final String projectName : projectNames) {
                final CProject project = findProjectByName(projectName);

                if (project == null) {
                    LOGGER.warn("Project '{}' not found, skipping meeting type creation", projectName);
                    continue;
                }
                LOGGER.info("Creating meeting types for project: {}", projectName);

                for (final String[] typeData : meetingTypes) {
                    meetingTypeService.createEntity(typeData[0], project);
                }
            }
            LOGGER.info("Successfully created meeting types for all projects");
        } catch (final Exception e) {
            LOGGER.error("Error creating meeting types", e);
            throw new RuntimeException("Failed to initialize meeting types", e);
        }
    }

    /**
     * Initialize order status entities with comprehensive sample data.
     */
    private void initializeOrderStatuses() {
        LOGGER.info("initializeOrderStatuses called - creating order status classifications");
    }

    /**
     * Initializes order types for categorizing different kinds of orders. Creates types for all projects to ensure
     * project-specific categorization.
     */
    private void initializeOrderTypes() {
        LOGGER.info("initializeOrderTypes called - creating order type classifications for all projects");

        try {
            // Get all projects
            final String[] projectNames = { "Digital Transformation Initiative", "Product Development Phase 2",
                    "Infrastructure Modernization", "Customer Experience Enhancement" };
            // Define order types to create for each project
            final String[][] orderTypes = { { "Service Order" }, { "Purchase Order" }, { "Maintenance Order" },
                    { "Change Order" }, { "Support Order" } };

            // Create order types for each project
            for (final String projectName : projectNames) {
                final CProject project = findProjectByName(projectName);

                if (project == null) {
                    LOGGER.warn("Project '{}' not found, skipping order type creation", projectName);
                    continue;
                }
                LOGGER.info("Creating order types for project: {}", projectName);

                for (final String[] typeData : orderTypes) {
                    orderTypeService.createEntity(typeData[0], project);
                }
            }
            LOGGER.info("Successfully created order types for all projects");
        } catch (final Exception e) {
            LOGGER.error("Error creating order types", e);
            throw new RuntimeException("Failed to initialize order types", e);
        }
    }

    /**
     * Initializes sample projects with different scopes and characteristics.
     */
    private void initializeProjects() {
        LOGGER.info("initializeProjects called - creating 4 sample projects");

        try {
            // Create projects with comprehensive descriptions
            createProjectWithDescription("Digital Transformation Initiative",
                    "A comprehensive digital transformation project aimed at modernizing business processes and improving operational efficiency across all departments.");
            createProjectWithDescription("Product Development Phase 2",
                    "Second phase of new product development focusing on advanced features, user experience improvements, and market expansion strategies.");
            createProjectWithDescription("Infrastructure Modernization",
                    "Complete infrastructure overhaul including server upgrades, network optimization, security enhancements, and cloud migration initiatives.");
            createProjectWithDescription("Customer Experience Enhancement",
                    "Strategic initiative to improve customer journey, implement feedback systems, and enhance service quality across all touchpoints.");
        } catch (final Exception e) {
            LOGGER.error("Error creating sample projects", e);
            throw new RuntimeException("Failed to initialize projects", e);
        }
    }

    /**
     * Initializes sample risks with different severity levels and mitigation strategies.
     */
    private void initializeRisks() {
        LOGGER.info("initializeRisks called - creating sample risks");

        try {
            createHighPriorityTechnicalRisk();
            createMediumPriorityBudgetRisk();
            createLowPriorityResourceRisk();
            createCriticalSecurityRisk();
            createLowPriorityScheduleRisk();
            LOGGER.info("Successfully created 5 sample risks");
        } catch (final Exception e) {
            LOGGER.error("Error creating sample risks", e);
            throw new RuntimeException("Failed to initialize risks", e);
        }
    }

    /**
     * Initializes comprehensive user data with different roles and companies.
     */
    private void initializeUsers() {
        LOGGER.info("initializeUsers called - creating 5+ sample users with different roles");

        try {
            createAdminUser();
            createProjectManagerUser();
            createTeamMemberUsers();
        } catch (final Exception e) {
            LOGGER.error("Error creating sample users", e);
            throw new RuntimeException("Failed to initialize users", e);
        }
    }

    /**
     * Initializes user types for role-based access control.
     */
    private void initializeUserTypes() {
        LOGGER.info("initializeUserTypes called - creating user type classifications for all projects");

        try {
            // Get all projects
            final String[] projectNames = { "Digital Transformation Initiative", "Product Development Phase 2",
                    "Infrastructure Modernization", "Customer Experience Enhancement" };
            // Define user types to create for each project
            final String[][] userTypes = { { "Employee" }, { "Manager" }, { "Executive" }, { "Contractor" } };

            // Create user types for each project
            for (final String projectName : projectNames) {
                final CProject project = findProjectByName(projectName);

                if (project == null) {
                    LOGGER.warn("Project '{}' not found, skipping user type creation", projectName);
                    continue;
                }
                LOGGER.info("Creating user types for project: {}", projectName);

                for (final String[] typeData : userTypes) {
                    userTypeService.createEntity(typeData[0], project);
                }
            }
            LOGGER.info("Successfully created user types for all projects");
        } catch (final Exception e) {
            LOGGER.error("Error creating user types", e);
            throw new RuntimeException("Failed to initialize user types", e);
        }
    }

    /**
     * Checks if the database is empty by counting users.
     * 
     * @return true if database is empty, false otherwise
     */
    private boolean isDatabaseEmpty() {
        /// LOGGER.info("isDatabaseEmpty called - checking user count");
        final long userCount = userService.count();
        LOGGER.info("User count in database: {}", userCount);
        return userCount == 0;
    }

    public void loadSampleData() {
        LOGGER.info("loadSampleData called - initializing sample data for core entities");

        try {
            // Initialize data in proper dependency order
            initializeCompanies();
            initializeProjects(); // Projects must be created before project-aware
                                  // entities
            initializeMeetingStatuses(); // Initialize status entities first
            initializeDecisionStatuses();
            initializeActivityStatuses();
            initializeOrderStatuses();
            initializeCommentPriorities(); // Initialize comment priorities
            initializeUserTypes(); // Now can use projects
            initializeMeetingTypes();
            initializeDecisionTypes();
            initializeOrderTypes();
            initializeUsers();
            initializeActivityTypes();
            initializeActivities();
            initializeMeetings();
            initializeRisks();
            createSampleCurrencies();
            createSampleDecisions();
            createSampleOrders();
            LOGGER.info("Sample data initialization completed successfully");
        } catch (final Exception e) {
            LOGGER.error("Error loading sample data", e);
            throw new RuntimeException("Failed to load sample data", e);
        }
    }

    @Override
    @Transactional
    public void run(final ApplicationArguments args) throws Exception {
        LOGGER.info("CSampleDataInitializer.run called with ApplicationArguments: {}", args);

        try {

            // Check if database already has data - if so, skip initialization only on app
            // startup
            if (!isDatabaseEmpty()) {
                LOGGER.info("Database already contains data, skipping sample data initialization on startup");
                return;
            }
            loadSampleData(); // Load sample data
        } catch (final Exception e) {
            LOGGER.error("Error during sample data initialization", e);
            throw e;
        }
    }
}