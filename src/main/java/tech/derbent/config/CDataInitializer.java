package tech.derbent.config;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.activities.domain.CActivityType;
import tech.derbent.activities.service.CActivityInitializerService;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.activities.service.CActivityStatusInitializerService;
import tech.derbent.activities.service.CActivityStatusService;
import tech.derbent.activities.service.CActivityTypeInitializerService;
import tech.derbent.activities.service.CActivityTypeService;
import tech.derbent.api.roles.domain.CUserProjectRole;
import tech.derbent.api.roles.service.CUserCompanyRoleInitializerService;
import tech.derbent.api.roles.service.CUserCompanyRoleService;
import tech.derbent.api.roles.service.CUserProjectRoleInitizerService;
import tech.derbent.api.roles.service.CUserProjectRoleService;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.comments.domain.CCommentPriority;
import tech.derbent.comments.service.CCommentPriorityService;
import tech.derbent.comments.service.CCommentService;
import tech.derbent.comments.view.CCommentPriorityInitializerService;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.companies.service.CCompanyInitializerService;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.decisions.domain.CDecision;
import tech.derbent.decisions.domain.CDecisionStatus;
import tech.derbent.decisions.domain.CDecisionType;
import tech.derbent.decisions.service.CDecisionInitializerService;
import tech.derbent.decisions.service.CDecisionService;
import tech.derbent.decisions.service.CDecisionStatusInitializerService;
import tech.derbent.decisions.service.CDecisionStatusService;
import tech.derbent.decisions.service.CDecisionTypeInitializerService;
import tech.derbent.decisions.service.CDecisionTypeService;
import tech.derbent.gannt.service.CGanntViewEntityService;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.domain.CMeetingStatus;
import tech.derbent.meetings.domain.CMeetingType;
import tech.derbent.meetings.service.CMeetingInitializerService;
import tech.derbent.meetings.service.CMeetingService;
import tech.derbent.meetings.service.CMeetingStatusInitializerService;
import tech.derbent.meetings.service.CMeetingStatusService;
import tech.derbent.meetings.service.CMeetingTypeInitializerService;
import tech.derbent.meetings.service.CMeetingTypeService;
import tech.derbent.orders.domain.CCurrency;
import tech.derbent.orders.domain.COrder;
import tech.derbent.orders.domain.COrderStatus;
import tech.derbent.orders.domain.COrderType;
import tech.derbent.orders.service.CApprovalStatusInitializerService;
import tech.derbent.orders.service.CCurrencyInitializerService;
import tech.derbent.orders.service.CCurrencyService;
import tech.derbent.orders.service.COrderInitializerService;
import tech.derbent.orders.service.COrderService;
import tech.derbent.orders.service.COrderStatusInitializerService;
import tech.derbent.orders.service.COrderStatusService;
import tech.derbent.orders.service.COrderTypeInitializerService;
import tech.derbent.orders.service.COrderTypeService;
import tech.derbent.page.service.CPageEntityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectInitializerService;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.risks.domain.CRisk;
import tech.derbent.risks.domain.ERiskSeverity;
import tech.derbent.risks.service.CRiskInitializerService;
import tech.derbent.risks.service.CRiskService;
import tech.derbent.risks.service.CRiskStatusInitializerService;
import tech.derbent.risks.service.CRiskStatusService;
import tech.derbent.screens.service.CDetailLinesService;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.setup.service.CSystemSettingsInitializerService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserType;
import tech.derbent.users.service.CUserCompanySettingsService;
import tech.derbent.users.service.CUserInitializerService;
import tech.derbent.users.service.CUserProjectSettingsService;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.CUserTypeInitializerService;
import tech.derbent.users.service.CUserTypeService;

/** CSampleDataInitializer - System Bootstrap and Sample Data Initialization This class serves dual purposes: 1. SYSTEM INITIALIZATION: Creates
 * essential base entities required for system operation - Companies, Projects, Users (core business entities) - Status entities (Activity, Meeting,
 * Decision statuses) - Type entities (Activity, Meeting, Decision, Order, User types) - System infrastructure (Screens, Grids, Pages) 2. SAMPLE DATA
 * CREATION: Populates demonstration data for user experience - Sample activities, meetings, decisions - Sample currencies and business data -
 * Realistic project data with relationships Key Design Principles: - Base system entities are marked as non-deletable for system stability - All
 * type/status entities have project-scoped unique constraints - Constants are used instead of magic strings for maintainability - Methods follow
 * consistent naming: initializeXxx() for system, initializeXxx(project) for project-specific
 * @author Derbent Team
 * @version 2.0 - Refactored for better organization and maintainability */
public class CDataInitializer {

	private static final String ACTIVITY_TYPE_DESIGN = "Design";
	// Activity Type Names
	private static final String ACTIVITY_TYPE_DEVELOPMENT = "Development";
	private static final String ACTIVITY_TYPE_DOCUMENTATION = "Documentation";
	private static final String ACTIVITY_TYPE_RESEARCH = "Research";
	private static final String ACTIVITY_TYPE_TESTING = "Testing";
	private static final String COMPANY_OF_DANISMANLIK = "Of Stratejik Danışmanlık";
	private static final String COMPANY_OF_ENDUSTRI = "Of Endüstri Dinamikleri";
	private static final String COMPANY_OF_SAGLIK = "Of Sağlık Teknolojileri";
	// Company Names
	private static final String COMPANY_OF_TEKNOLOJI = "Of Teknoloji Çözümleri";
	private static final Logger LOGGER = LoggerFactory.getLogger(CDataInitializer.class);
	// Profile picture filenames mapping for users
	private static final java.util.Map<String, String> PROFILE_PICTURE_MAPPING = java.util.Map.of("admin", "admin.svg", "mkaradeniz",
			"michael_chen.svg", "msahin", "sophia_brown.svg", "bozkan", "david_kim.svg", "ademir", "emma_wilson.svg");
	// Standard password for all users as per coding guidelines
	private static final String STANDARD_PASSWORD = "test123";
	private static final String STATUS_CANCELLED = "Cancelled";
	private static final String STATUS_COMPLETED = "Completed";
	private static final String STATUS_IN_PROGRESS = "In Progress";
	// Status Names
	private static final String STATUS_NOT_STARTED = "Not Started";
	private static final String STATUS_ON_HOLD = "On Hold";
	// User Login Names
	private static final String USER_ADMIN = "admin";
	private static final String USER_MANAGER = "mkaradeniz";
	private static final String USER_MEMBER_AYSE = "ademir";
	private static final String USER_MEMBER_BURAK = "bozkan";
	private static final String USER_MEMBER_MERVE = "msahin";
	private final CActivityService activityService;
	private final CActivityStatusService activityStatusService;
	private final CActivityTypeService activityTypeService;
	private final CCommentPriorityService commentPriorityService;
	private final CCommentService commentService;
	private final CCompanyService companyService;
	private final CCurrencyService currencyService;
	private final CDecisionService decisionService;
	private final CDecisionStatusService decisionStatusService;
	private final CDecisionTypeService decisionTypeService;
	@PersistenceContext
	private EntityManager em;
	private final CGanntViewEntityService ganntViewEntityService;
	private final CGridEntityService gridEntityService;
	private final JdbcTemplate jdbcTemplate;
	private final CMeetingService meetingService;
	private final CMeetingStatusService meetingStatusService;
	private final CMeetingTypeService meetingTypeService;
	private final COrderService orderService;
	private final COrderStatusService orderStatusService;
	private final COrderTypeService orderTypeService;
	private final CPageEntityService pageEntityService;
	// Service dependencies - injected via constructor
	private final CProjectService projectService;
	private final CRiskService riskService;
	private final CRiskStatusService riskStatusService;
	private final CDetailLinesService screenLinesService;
	private final CDetailSectionService screenService;
	private final CUserCompanyRoleService userCompanyRoleService;
	private final CUserCompanySettingsService userCompanySettingsService;
	private final CUserProjectRoleService userProjectRoleService;
	private final CUserProjectSettingsService userProjectSettingsService;
	private final CUserService userService;
	private final CUserTypeService userTypeService;

	public CDataInitializer() {
		gridEntityService = CSpringContext.getBean(CGridEntityService.class);
		projectService = CSpringContext.getBean(CProjectService.class);
		userService = CSpringContext.getBean(CUserService.class);
		userProjectSettingsService = CSpringContext.getBean(CUserProjectSettingsService.class);
		userCompanySettingsService = CSpringContext.getBean(CUserCompanySettingsService.class);
		activityService = CSpringContext.getBean(CActivityService.class);
		userTypeService = CSpringContext.getBean(CUserTypeService.class);
		activityTypeService = CSpringContext.getBean(CActivityTypeService.class);
		meetingTypeService = CSpringContext.getBean(CMeetingTypeService.class);
		orderService = CSpringContext.getBean(COrderService.class);
		orderTypeService = CSpringContext.getBean(COrderTypeService.class);
		orderStatusService = CSpringContext.getBean(COrderStatusService.class);
		companyService = CSpringContext.getBean(CCompanyService.class);
		commentService = CSpringContext.getBean(CCommentService.class);
		commentPriorityService = CSpringContext.getBean(CCommentPriorityService.class);
		meetingService = CSpringContext.getBean(CMeetingService.class);
		riskService = CSpringContext.getBean(CRiskService.class);
		meetingStatusService = CSpringContext.getBean(CMeetingStatusService.class);
		decisionStatusService = CSpringContext.getBean(CDecisionStatusService.class);
		decisionTypeService = CSpringContext.getBean(CDecisionTypeService.class);
		activityStatusService = CSpringContext.getBean(CActivityStatusService.class);
		decisionService = CSpringContext.getBean(CDecisionService.class);
		currencyService = CSpringContext.getBean(CCurrencyService.class);
		screenService = CSpringContext.getBean(CDetailSectionService.class);
		screenLinesService = CSpringContext.getBean(CDetailLinesService.class);
		pageEntityService = CSpringContext.getBean(CPageEntityService.class);
		ganntViewEntityService = CSpringContext.getBean(CGanntViewEntityService.class);
		riskStatusService = CSpringContext.getBean(CRiskStatusService.class);
		userProjectRoleService = CSpringContext.getBean(CUserProjectRoleService.class);
		userCompanyRoleService = CSpringContext.getBean(CUserCompanyRoleService.class);
		final DataSource ds = CSpringContext.getBean(DataSource.class);
		jdbcTemplate = new JdbcTemplate(ds);
	}
	// ========================================================================
	// PUBLIC API METHODS - Main entry points for initialization
	// ========================================================================

	@Transactional
	private void clearSampleData() {
		LOGGER.warn("Clearing sample data from database (forced)");
		try {
			// ---- 1) PostgreSQL yolu: public şemadaki tabloları topla ve TRUNCATE et
			try {
				final List<String> tableNames = jdbcTemplate.queryForList("""
						SELECT tablename
						FROM pg_tables
						WHERE schemaname = 'public'
						  AND tablename NOT IN ('flyway_schema_history')
						""", String.class);
				if (!tableNames.isEmpty()) {
					// Tabloları güvenli biçimde tırnakla ve join et
					final List<String> quoted = tableNames.stream().map(t -> "\"" + t + "\"").toList();
					final String joined = String.join(", ", quoted);
					final String sql = "TRUNCATE TABLE " + joined + " RESTART IDENTITY CASCADE";
					LOGGER.warn("Executing: {}", sql);
					jdbcTemplate.execute(sql);
					LOGGER.info("All public tables truncated (PostgreSQL).");
					return; // İş bitti
				} else {
					LOGGER.warn("No user tables found to truncate in public schema.");
				}
			} catch (final Exception pgEx) {
				LOGGER.warn("PostgreSQL truncate path failed or DB is not PostgreSQL. Falling back to JPA deletes. Cause: {}", pgEx.getMessage());
			}
			// ---- 2) Fallback: JPA batch silme (FK sırasına dikkat)
			commentService.deleteAllInBatch();
			commentPriorityService.deleteAllInBatch();
			meetingService.deleteAllInBatch();
			meetingStatusService.deleteAllInBatch();
			meetingTypeService.deleteAllInBatch();
			decisionService.deleteAllInBatch();
			decisionStatusService.deleteAllInBatch();
			// decisionTypeService.deleteAllInBatch(); // ekleyeceksen
			activityService.deleteAllInBatch();
			activityStatusService.deleteAllInBatch();
			activityTypeService.deleteAllInBatch();
			riskService.deleteAllInBatch();
			screenLinesService.deleteAllInBatch();
			screenService.deleteAllInBatch();
			currencyService.deleteAllInBatch();
			orderTypeService.deleteAllInBatch();
			orderStatusService.deleteAllInBatch();
			userService.deleteAllInBatch();
			userTypeService.deleteAllInBatch();
			companyService.deleteAllInBatch();
			projectService.deleteAllInBatch();
			pageEntityService.deleteAllInBatch();
			ganntViewEntityService.deleteAllInBatch();
			riskStatusService.deleteAllInBatch();
			userProjectRoleService.deleteAllInBatch();
			userCompanyRoleService.deleteAllInBatch();
			LOGGER.info("Fallback JPA deleteAllInBatch completed.");
		} catch (final Exception e) {
			LOGGER.error("Error during sample data cleanup", e);
			throw new RuntimeException("Failed to clear sample data", e);
		}
	}

	private void createActivityStatus(final String name, final CProject project, final String description, final String color, final boolean isFinal,
			final int sortOrder) {
		final CActivityStatus status = new CActivityStatus(name, project);
		status.setDescription(description);
		status.setColor(color);
		status.setSortOrder(sortOrder);
		activityStatusService.save(status);
	}

	/** Creates additional activities for Customer Experience Enhancement project. */
	private void createAdditionalCustomerExperienceActivities(final CProject project) {
		Check.notNull(project, "Project 'Customer Experience Enhancement' not found");
		// User Research Activity
		final CActivity userResearch = new CActivity("User Research & Analysis", project);
		final CActivityType researchType = activityTypeService.findByNameAndProject("Research", project).orElse(null);
		Check.notNull(researchType, "Research activity type not found for project");
		userResearch.setActivityType(researchType);
		userResearch.setDescription("Conduct user interviews and analyze customer feedback");
		final CUser analyst = userService.findByLogin(USER_MEMBER_AYSE);
		final CUser manager = userService.findByLogin(USER_MANAGER);
		userResearch.setAssignedTo(analyst);
		userResearch.setCreatedBy(manager);
		userResearch.setEstimatedHours(new BigDecimal("22.00"));
		userResearch.setActualHours(new BigDecimal("22.00"));
		userResearch.setRemainingHours(new BigDecimal("0.00"));
		userResearch.setStartDate(LocalDate.now().minusDays(20));
		userResearch.setDueDate(LocalDate.now().minusDays(10));
		userResearch.setCompletionDate(LocalDate.now().minusDays(10));
		final CActivityStatus completedStatus = activityStatusService.findByNameAndProject("Completed", project).orElseThrow();
		userResearch.setStatus(completedStatus);
		userResearch.setProgressPercentage(100);
		// Set missing fields
		userResearch.setCreatedBy(userService.getRandom());
		// UI/UX Design Activity
		final CActivity uxDesign = new CActivity("UI/UX Design Improvements", project);
		final CActivityType designType = activityTypeService.findByNameAndProject("Design", project).orElse(null);
		uxDesign.setActivityType(designType);
		uxDesign.setDescription("Design improved user interface based on research findings");
		final CUser dev2 = userService.findByLogin("msahin");
		uxDesign.setAssignedTo(dev2);
		uxDesign.setCreatedBy(analyst);
		uxDesign.setEstimatedHours(new BigDecimal("28.00"));
		uxDesign.setActualHours(new BigDecimal("15.00"));
		uxDesign.setRemainingHours(new BigDecimal("13.00"));
		uxDesign.setStartDate(LocalDate.now().minusDays(8));
		uxDesign.setDueDate(LocalDate.now().plusDays(5));
		final CActivityStatus inProgressStatus = activityStatusService.findByNameAndProject("In Progress", project).orElseThrow();
		uxDesign.setStatus(inProgressStatus);
		uxDesign.setProgressPercentage(55);
		// Set missing fields
		uxDesign.setCreatedBy(userService.getRandom());
		activityService.save(userResearch);
		uxDesign.setParent(userResearch); // Set parent-child relationship)
		activityService.save(uxDesign);
		commentService.createComment("Design system updated with new patterns", uxDesign, dev2);
		commentService.createComment("Wireframes created for key user journeys", uxDesign, dev2);
		commentService.createComment("Prototypes ready for user testing", uxDesign, analyst);
		commentService.createComment("User research methodology defined", userResearch, manager);
		commentService.createComment("Conducted 15 user interviews", userResearch, analyst);
		commentService.createComment("Analysis complete, insights documented", userResearch, analyst);
		// UI/UX Design Activity
	}
	// Additional meeting creation methods

	/** Creates additional activities for Digital Transformation Initiative project. */
	private void createAdditionalDigitalTransformationActivities(final CProject project) {
		// Frontend Development Activity
		final CActivity frontendDev = new CActivity("Frontend Development", project);
		final CActivityType developmentType = activityTypeService.findByNameAndProject("Development", project).orElse(null);
		frontendDev.setActivityType(developmentType);
		frontendDev.setDescription("Develop responsive user interface components using modern frameworks");
		final CUser dev1 = userService.findByLogin("msahin");
		final CUser manager = userService.findByLogin("mkaradeniz");
		frontendDev.setAssignedTo(dev1);
		frontendDev.setCreatedBy(manager);
		frontendDev.setEstimatedHours(new BigDecimal("32.00"));
		frontendDev.setActualHours(new BigDecimal("28.00"));
		frontendDev.setRemainingHours(new BigDecimal("4.00"));
		frontendDev.setStartDate(LocalDate.now().minusDays(12));
		frontendDev.setDueDate(LocalDate.now().plusDays(8));
		final CActivityStatus inProgressStatus = activityStatusService.findByNameAndProject("In Progress", project).orElseThrow();
		frontendDev.setStatus(inProgressStatus);
		frontendDev.setProgressPercentage(70);
		// Set missing fields
		frontendDev.setCreatedBy(userService.getRandom());
		activityService.save(frontendDev);
		commentService.createComment("Frontend development started with React components", frontendDev, dev1);
		commentService.createComment("Implemented responsive design patterns", frontendDev, dev1);
		commentService.createComment("Working on integration with backend APIs", frontendDev, manager);
		// Database Migration Activity
		final CActivity dbMigration = new CActivity("Database Migration", project);
		dbMigration.setActivityType(developmentType);
		dbMigration.setDescription("Migrate legacy data to new database schema");
		final CUser admin = userService.findByLogin("admin");
		dbMigration.setAssignedTo(admin);
		dbMigration.setCreatedBy(manager);
		dbMigration.setEstimatedHours(new BigDecimal("20.00"));
		dbMigration.setActualHours(new BigDecimal("5.00"));
		dbMigration.setRemainingHours(new BigDecimal("15.00"));
		dbMigration.setStartDate(LocalDate.now().plusDays(5));
		dbMigration.setDueDate(LocalDate.now().plusDays(15));
		final CActivityStatus notStartedStatus = activityStatusService.findByNameAndProject("Not Started", project).orElseThrow();
		dbMigration.setStatus(notStartedStatus);
		dbMigration.setProgressPercentage(0);
		// Set missing fields
		dbMigration.setCreatedBy(userService.getRandom());
		activityService.save(dbMigration);
		commentService.createComment("Database migration plan prepared", dbMigration, admin);
		commentService.createComment("Waiting for backend API completion", dbMigration, manager);
	}

	/** Creates additional activities for Infrastructure Modernization project. */
	private void createAdditionalInfrastructureActivities(final CProject project) {
		// Security Audit Activity
		final CActivity securityAudit = new CActivity("Security Audit", project);
		final CActivityType researchType = activityTypeService.findByNameAndProject("Research", project).orElseThrow();
		securityAudit.setActivityType(researchType);
		securityAudit.setDescription("Comprehensive security assessment and vulnerability analysis");
		final CUser admin = userService.findByLogin("admin");
		final CUser manager = userService.findByLogin("mkaradeniz");
		securityAudit.setAssignedTo(admin);
		securityAudit.setCreatedBy(manager);
		securityAudit.setEstimatedHours(new BigDecimal("25.00"));
		securityAudit.setActualHours(new BigDecimal("0.00"));
		securityAudit.setRemainingHours(new BigDecimal("25.00"));
		securityAudit.setStartDate(LocalDate.now().plusDays(10));
		securityAudit.setDueDate(LocalDate.now().plusDays(18));
		final CActivityStatus notStartedStatus = activityStatusService.findByNameAndProject("Not Started", project).orElseThrow();
		securityAudit.setStatus(notStartedStatus);
		securityAudit.setProgressPercentage(0);
		// Set missing fields
		securityAudit.setCreatedBy(userService.getRandom());
		activityService.save(securityAudit);
		commentService.createComment("Security audit requirements defined", securityAudit, admin);
		commentService.createComment("External security firm selected for audit", securityAudit, manager);
		// Server Migration Activity
		final CActivity serverMigration = new CActivity("Server Migration", project);
		final CActivityType developmentType = activityTypeService.findByNameAndProject("Development", project).orElseThrow();
		serverMigration.setActivityType(developmentType);
		serverMigration.setDescription("Migrate applications to new server infrastructure");
		final CUser dev1 = userService.findByLogin("bozkan");
		serverMigration.setAssignedTo(dev1);
		serverMigration.setCreatedBy(admin);
		serverMigration.setEstimatedHours(new BigDecimal("35.00"));
		serverMigration.setActualHours(new BigDecimal("20.00"));
		serverMigration.setRemainingHours(new BigDecimal("15.00"));
		serverMigration.setStartDate(LocalDate.now().minusDays(8));
		serverMigration.setDueDate(LocalDate.now().plusDays(12));
		final CActivityStatus onHoldStatus = activityStatusService.findByNameAndProject("On Hold", project).orElseThrow();
		serverMigration.setStatus(onHoldStatus);
		serverMigration.setProgressPercentage(55);
		serverMigration.setParent(securityAudit); // Set parent-child relationship
		// Set missing fields
		serverMigration.setCreatedBy(userService.getRandom());
		activityService.save(serverMigration);
		commentService.createComment("Server migration plan created", serverMigration, dev1);
		commentService.createComment("Testing environment successfully migrated", serverMigration, dev1);
		commentService.createComment("Production migration on hold pending approval", serverMigration, admin);
	}

	/** Creates additional activities for Product Development Phase 2 project. */
	private void createAdditionalProductDevelopmentActivities(final CProject project) {
		// Code Review Activity
		final CActivity codeReview = new CActivity("Code Review Process", project);
		final CActivityType testingType = activityTypeService.findByNameAndProject("Testing", project).orElseThrow();
		codeReview.setActivityType(testingType);
		codeReview.setDescription("Comprehensive code review and quality assurance");
		final CUser analyst = userService.findByLogin("ademir");
		final CUser manager = userService.findByLogin("mkaradeniz");
		codeReview.setAssignedTo(analyst);
		codeReview.setCreatedBy(manager);
		codeReview.setEstimatedHours(new BigDecimal("12.00"));
		codeReview.setActualHours(new BigDecimal("12.00"));
		codeReview.setRemainingHours(new BigDecimal("0.00"));
		codeReview.setStartDate(LocalDate.now().minusDays(3));
		codeReview.setDueDate(LocalDate.now().minusDays(1));
		codeReview.setCompletionDate(LocalDate.now().minusDays(1));
		final CActivityStatus completedStatus = activityStatusService.findByNameAndProject("Completed", project).orElseThrow();
		codeReview.setStatus(completedStatus);
		codeReview.setProgressPercentage(100);
		// Set missing fields
		codeReview.setCreatedBy(userService.getRandom());
		activityService.save(codeReview);
		commentService.createComment("Code review process initiated", codeReview, manager);
		commentService.createComment("Found minor issues, created fix recommendations", codeReview, analyst);
		commentService.createComment("All issues resolved, code approved", codeReview, analyst);
		// Performance Testing Activity
		final CActivity perfTesting = new CActivity("Performance Testing", project);
		perfTesting.setActivityType(testingType);
		perfTesting.setDescription("Load testing and performance optimization");
		final CUser dev2 = userService.findByLogin("bozkan");
		perfTesting.setAssignedTo(dev2);
		perfTesting.setCreatedBy(manager);
		perfTesting.setEstimatedHours(new BigDecimal("18.00"));
		perfTesting.setActualHours(new BigDecimal("10.00"));
		perfTesting.setRemainingHours(new BigDecimal("8.00"));
		perfTesting.setStartDate(LocalDate.now().minusDays(5));
		perfTesting.setDueDate(LocalDate.now().plusDays(2));
		final CActivityStatus inProgressStatus = activityStatusService.findByNameAndProject("In Progress", project).orElseThrow();
		perfTesting.setStatus(inProgressStatus);
		perfTesting.setProgressPercentage(60);
		perfTesting.setParent(codeReview); // Set parent-child relationship
		// Set missing fields
		perfTesting.setCreatedBy(userService.getRandom());
		activityService.save(perfTesting);
		commentService.createComment("Performance testing framework setup", perfTesting, dev2);
		commentService.createComment("Baseline performance metrics collected", perfTesting, dev2);
	}

	/** Creates system administrator user. */
	private void createAdminUser() {
		final CUser admin = userService.createLoginUser(USER_ADMIN, STANDARD_PASSWORD, "Ahmet", "admin@of.gov.tr", "ADMIN,USER");
		// Set user profile directly on entity
		final String profilePictureFile = PROFILE_PICTURE_MAPPING.get(USER_ADMIN);
		final byte[] profilePictureBytes = loadProfilePictureData(profilePictureFile);
		admin.setLastname("Yılmaz");
		admin.setPhone("+90-462-751-1001");
		admin.setProfilePictureData(profilePictureBytes);
		userService.save(admin);
		// Associate admin with first company (typically the tech company)
		final CCompany company = companyService.getRandom();
		Check.notNull(company, "At least one company must exist to assign to admin user");
		// Find a suitable role for the company
		final List<tech.derbent.api.roles.domain.CUserCompanyRole> companyRoles = userCompanyRoleService.findAll().stream()
				.filter(role -> role.getCompany() != null && role.getCompany().getId().equals(company.getId())).filter(role -> !role.isGuest())
				.collect(java.util.stream.Collectors.toList());
		final tech.derbent.api.roles.domain.CUserCompanyRole role = companyRoles.isEmpty() ? null : companyRoles.get(0);
		userCompanySettingsService.addUserToCompany(admin, company, "OWNER", role);
	}

	private void createApprovalStatus(final String name, final CProject project, final String description, final String color, final boolean isFinal,
			final int sortOrder) {
		final tech.derbent.orders.domain.CApprovalStatus status = new tech.derbent.orders.domain.CApprovalStatus(name, project);
		status.setDescription(description);
		status.setColor(color);
		status.setSortOrder(sortOrder);
		CSpringContext.getBean(tech.derbent.orders.service.CApprovalStatusService.class).save(status);
	}

	/** Creates backend development activity. */
	private void createBackendDevActivity(final CProject project) {
		// Create the activity using new auxiliary methods
		final CActivity backendDev = new CActivity("Backend API Development", project);
		// Find and set the activity type
		final CActivityType developmentType = activityTypeService.findByNameAndProject("Development", project).orElseThrow();
		Check.notNull(developmentType, "Development activity type not found for project");
		// Set activity type and description using auxiliary method
		backendDev.setActivityType(developmentType);
		backendDev.setDescription("Develop REST API endpoints for user management and authentication");
		// Set assigned users using auxiliary method
		final CUser manager = userService.findByLogin("mkaradeniz");
		final CUser admin = userService.findByLogin("admin");
		backendDev.setAssignedTo(manager);
		backendDev.setCreatedBy(admin);
		// Set time tracking using entity methods
		backendDev.setEstimatedHours(new BigDecimal("40.00"));
		backendDev.setActualHours(new BigDecimal("35.50"));
		backendDev.setRemainingHours(new BigDecimal("4.50"));
		// Set date information using auxiliary method
		backendDev.setStartDate(LocalDate.now().minusDays(10));
		backendDev.setDueDate(LocalDate.now().plusDays(5));
		// Set status and priority using auxiliary method
		final CActivityStatus inProgressStatus = activityStatusService.findByNameAndProject("In Progress", project).orElseThrow();
		backendDev.setStatus(inProgressStatus);
		backendDev.setProgressPercentage(75);
		// Set missing fields
		backendDev.setCreatedBy(userService.getRandom());
		activityService.save(backendDev);
		// Create comments
		commentService.createComment("Initial backend API development started", backendDev, admin);
		commentService.createComment("API endpoints for user registration and login implemented", backendDev, admin);
		commentService.createComment("Working on authentication and authorization features", backendDev, manager);
	}

	private void createCommentPriority(CProject project, final String name, final String description, final String color, final Integer priorityLevel,
			final boolean isDefault, final int sortOrder) {
		final CCommentPriority priority = new CCommentPriority(name, project, color, sortOrder);
		priority.setDescription(description);
		priority.setPriorityLevel(priorityLevel);
		priority.setDefault(isDefault);
		commentPriorityService.save(priority);
	}

	/** Creates consulting company. */
	private void createConsultingCompany() {
		final CCompany consulting = new CCompany(COMPANY_OF_DANISMANLIK);
		consulting.setDescription("Yönetim danışmanlığı ve stratejik planlama hizmetleri");
		consulting.setAddress("Merkez Mahallesi, Gülbahar Sokağı No:7, Of/Trabzon");
		consulting.setPhone("+90-462-751-0303");
		consulting.setEmail("merhaba@ofdanismanlik.com.tr");
		consulting.setWebsite("https://www.ofdanismanlik.com.tr");
		consulting.setTaxNumber("TR-456789123");
		consulting.setEnabled(true);
		// Company Configuration Settings
		consulting.setCompanyTheme("lumo-light");
		consulting.setCompanyLogoUrl("/assets/logos/consulting-logo.svg");
		consulting.setPrimaryColor("#4caf50");
		consulting.setWorkingHoursStart("08:30");
		consulting.setWorkingHoursEnd("17:30");
		consulting.setCompanyTimezone("Europe/Istanbul");
		consulting.setDefaultLanguage("tr");
		consulting.setEnableNotifications(true);
		consulting.setNotificationEmail("bildirim@ofdanismanlik.com.tr");
		companyService.save(consulting);
	}

	/** Create a currency entity with proper validation. */
	private void createCurrency(final CProject project, final String code, final String name, final String symbol) {
		try {
			final CCurrency currency = new CCurrency(project, name);
			currency.setCurrencyCode(code);
			currency.setCurrencySymbol(symbol);
			// Add business description
			currency.setDescription(String.format("%s (%s) - International currency for business transactions", name, code));
			currencyService.save(currency);
		} catch (final Exception e) {
			LOGGER.error("Error creating currency: {} ({})", name, code, e);
			throw new RuntimeException("Failed to create currency: " + code, e);
		}
	}

	private void createDecisionStatus(final String name, final CProject project, final String description, final String color, final boolean isFinal,
			final int sortOrder) {
		final CDecisionStatus status = new CDecisionStatus(name, project);
		status.setDescription(description);
		status.setColor(color);
		status.setFinal(isFinal);
		status.setSortOrder(sortOrder);
		decisionStatusService.save(status);
	}

	/** Creates healthcare company. */
	private void createHealthcareCompany() {
		final CCompany healthcare = new CCompany(COMPANY_OF_SAGLIK);
		healthcare.setDescription("İleri tıp teknolojisi ve sağlık çözümleri");
		healthcare.setAddress("Yeni Mahalle, Sağlık Sokağı No:21, Of/Trabzon");
		healthcare.setPhone("+90-462-751-0404");
		healthcare.setEmail("iletisim@ofsaglik.com.tr");
		healthcare.setWebsite("https://www.ofsaglik.com.tr");
		healthcare.setTaxNumber("TR-789123456");
		healthcare.setEnabled(true);
		// Company Configuration Settings
		healthcare.setCompanyTheme("lumo-light");
		healthcare.setCompanyLogoUrl("/assets/logos/healthcare-logo.svg");
		healthcare.setPrimaryColor("#f44336");
		healthcare.setWorkingHoursStart("07:00");
		healthcare.setWorkingHoursEnd("19:00");
		healthcare.setCompanyTimezone("Europe/Istanbul");
		healthcare.setDefaultLanguage("tr");
		healthcare.setEnableNotifications(true);
		healthcare.setNotificationEmail("acil@ofsaglik.com.tr");
		companyService.save(healthcare);
	}

	/** Creates manufacturing company. */
	private void createManufacturingCompany() {
		final CCompany manufacturing = new CCompany(COMPANY_OF_ENDUSTRI);
		manufacturing.setDescription("Hassas mühendislik bileşenlerinde lider üretici");
		manufacturing.setAddress("Sanayi Mahallesi, İstiklal Caddesi No:42, Of/Trabzon");
		manufacturing.setPhone("+90-462-751-0202");
		manufacturing.setEmail("bilgi@ofendüstri.com.tr");
		manufacturing.setWebsite("https://www.ofendüstri.com.tr");
		manufacturing.setTaxNumber("TR-987654321");
		manufacturing.setEnabled(true);
		// Company Configuration Settings
		manufacturing.setCompanyTheme("lumo-dark");
		manufacturing.setCompanyLogoUrl("/assets/logos/manufacturing-logo.svg");
		manufacturing.setPrimaryColor("#ff9800");
		manufacturing.setWorkingHoursStart("06:00");
		manufacturing.setWorkingHoursEnd("18:00");
		manufacturing.setCompanyTimezone("Europe/Istanbul");
		manufacturing.setDefaultLanguage("tr");
		manufacturing.setEnableNotifications(true);
		manufacturing.setNotificationEmail("operasyon@ofendüstri.com.tr");
		companyService.save(manufacturing);
	}

	private void createMeetingStatus(final String name, final CProject project, final String description, final String color, final boolean isFinal,
			final int sortOrder) {
		final CMeetingStatus status = new CMeetingStatus(name, project);
		status.setDescription(description);
		status.setColor(color);
		status.setFinalStatus(isFinal);
		status.setSortOrder(sortOrder);
		meetingStatusService.save(status);
	}

	/** Create order status entity. */
	private void createOrderStatus(final String name, final CProject project, final String description, final String color, final boolean isFinal,
			final int sortOrder) {
		try {
			final COrderStatus status = new COrderStatus(name, project);
			status.setDescription(description);
			status.setColor(color);
			status.setSortOrder(sortOrder);
			// Note: isFinal parameter is not used as COrderStatus doesn't have this field
			orderStatusService.save(status);
		} catch (final Exception e) {
			LOGGER.error("Error creating order status: {} for project: {}", name, project.getName(), e);
			throw new RuntimeException("Failed to create order status: " + name, e);
		}
	}

	private void createProjectDigitalTransformation() {
		final CProject project = new CProject("Digital Transformation Initiative");
		project.setDescription("Comprehensive digital transformation for enhanced customer experience");
		project.setIsActive(true);
		// Assign to technology company
		final CCompany company = companyService.findByName(COMPANY_OF_TEKNOLOJI).orElse(null);
		if (company != null) {
			project.setCompany(company);
		}
		projectService.save(project);
	}

	private void createProjectInfrastructureUpgrade() {
		final CProject project = new CProject("Infrastructure Upgrade Project");
		project.setDescription("Upgrading IT infrastructure for improved performance and scalability");
		// Assign to consulting company
		final CCompany company = companyService.findByName(COMPANY_OF_DANISMANLIK).orElse(null);
		if (company != null) {
			project.setCompany(company);
		}
		projectService.save(project);
	}

	/** Creates project manager user. */
	private void createProjectManagerUser() {
		LOGGER.info("createProjectManagerUser called - creating project manager");
		final CUser manager =
				userService.createLoginUser(USER_MANAGER, STANDARD_PASSWORD, "Mehmet Emin", "mehmet.karadeniz@ofteknoloji.com.tr", "MANAGER,USER");
		// Set user profile directly on entity
		final String profilePictureFile = PROFILE_PICTURE_MAPPING.get(USER_MANAGER);
		final byte[] profilePictureBytes = loadProfilePictureData(profilePictureFile);
		manager.setLastname("Karadeniz");
		manager.setPhone("+90-462-751-1002");
		manager.setProfilePictureData(profilePictureBytes);
		userService.save(manager);
		// Associate with company (Of Teknoloji Çözümleri based on email domain)
		final CCompany company = companyService.findByName(COMPANY_OF_TEKNOLOJI).orElse(null);
		if (company != null) {
			// Find a suitable role for the company
			final List<tech.derbent.api.roles.domain.CUserCompanyRole> companyRoles = userCompanyRoleService.findAll().stream()
					.filter(role -> role.getCompany() != null && role.getCompany().getId().equals(company.getId())).filter(role -> !role.isGuest())
					.collect(java.util.stream.Collectors.toList());
			final tech.derbent.api.roles.domain.CUserCompanyRole role = companyRoles.isEmpty() ? null : companyRoles.get(0);
			userCompanySettingsService.addUserToCompany(manager, company, "MANAGER", role);
		}
	}

	private void createProjectProductDevelopment() {
		final CProject project = new CProject("New Product Development");
		project.setDescription("Development of innovative products to expand market reach");
		// Assign to healthcare company
		final CCompany company = companyService.findByName(COMPANY_OF_SAGLIK).orElse(null);
		if (company != null) {
			project.setCompany(company);
		}
		projectService.save(project);
	}

	private void createRiskStatus(final String name, final CProject project, final String description, final String color, final boolean isFinal,
			final int sortOrder) {
		final tech.derbent.risks.domain.CRiskStatus status = new tech.derbent.risks.domain.CRiskStatus(name, project);
		status.setDescription(description);
		status.setColor(color);
		status.setSortOrder(sortOrder);
		riskStatusService.save(status);
	}

	/** Create a sample budget decision. */
	private void createSampleBudgetDecision(final CProject project) {
		final CDecision decision = new CDecision("Additional Development Resources", project);
		decision.setDescription("Budget allocation for additional development resources to meet project deadlines");
		decision.setDecisionType(decisionTypeService.getRandom(project));
		decision.setDecisionStatus(decisionStatusService.getRandom(project));
		decision.setReviewDate(LocalDateTime.now().plusDays(3).withHour(16).withMinute(0));
		decision.setEstimatedCost(new BigDecimal("25000.00"));
		decision.setAccountableUser(userService.getRandom());
		// Set missing fields
		decision.setCreatedBy(userService.getRandom());
		decisionService.save(decision);
	}

	private void createSampleHardwareOrder(CProject project) {
		final COrder order = new COrder("Laptop Procurement", project);
		order.setDescription("Procurement of high-performance laptops for development team");
		// Set order type
		final COrderType hardwareType = orderTypeService.getRandom(project);
		order.setOrderType(hardwareType);
		// Set order status
		final COrderStatus processingStatus = orderStatusService.getRandom(project);
		order.setStatus(processingStatus);
		// Set assigned user
		order.setCreatedBy(userService.getRandom());
		order.setAssignedTo(userService.getRandom());
		order.setRequestor(userService.getRandom());
		// Set financial details
		final CCurrency tryCurrency = currencyService.getRandom(project);
		order.setCurrency(tryCurrency);
		// Set date information
		order.setOrderDate(LocalDate.now().minusDays(10));
		order.setDeliveryDate(LocalDate.now().plusDays(5));
		order.setProviderCompanyName("asfdsafsaf");
		orderService.save(order);
	}

	/** Create a sample operational decision. */
	private void createSampleOperationalDecision(final CProject project) {
		final CDecision decision = new CDecision("Daily Standup Meeting Time Change", project);
		decision.setDescription("Change daily standup meeting time from 9:00 AM to 10:00 AM to accommodate remote team members");
		decision.setDecisionType(decisionTypeService.getRandom(project));
		decision.setDecisionStatus(decisionStatusService.getRandom(project));
		decision.setImplementationDate(LocalDateTime.now().minusDays(2).withHour(10).withMinute(0));
		decision.setAccountableUser(userService.getRandom());
		// Set missing fields
		decision.setCreatedBy(userService.getRandom());
		decisionService.save(decision);
	}

	/** Creates sample planning meeting. */
	private void createSamplePlanningMeeting(final CProject project) {
		final CMeeting meeting = new CMeeting("Sprint Planning - Q1 2024", project);
		meeting.setDescription("Planning for next sprint with story estimation and task assignment");
		meeting.setMeetingDate(LocalDateTime.now().plusDays(3).withHour(14).withMinute(0));
		meeting.setEndDate(LocalDateTime.now().plusDays(3).withHour(16).withMinute(0));
		meeting.setLocation("Meeting Room B");
		// Add participants
		final Set<CUser> participants = new HashSet<>();
		final CUser manager = userService.getRandom();
		final CUser analyst = userService.getRandom();
		if (manager != null) {
			participants.add(manager);
		}
		if (analyst != null) {
			participants.add(analyst);
		}
		meeting.setParticipants(participants);
		// Set missing fields
		meeting.setMeetingType(meetingTypeService.getRandom(project));
		meeting.setStatus(meetingStatusService.getRandom(project));
		meeting.setResponsible(userService.getRandom());
		meeting.setCreatedBy(userService.getRandom());
		meetingService.save(meeting);
	}

	/** Creates sample project meeting using auxiliary service methods. Demonstrates the use of auxiliary meeting service methods. */
	private void createSampleProjectMeeting(final CProject project) {
		// Create the meeting using new auxiliary methods
		final CMeeting meeting = new CMeeting("Weekly Project Status Meeting", project);
		meeting.setDescription("Weekly status update on project progress, blockers discussion, and next steps planning");
		// Set meeting details using entity methods
		meeting.setMeetingDate(LocalDateTime.now().plusDays(1).withHour(14).withMinute(0));
		meeting.setEndDate(LocalDateTime.now().plusDays(1).withHour(15).withMinute(0));
		meeting.setLocation("Conference Room A");
		// Set meeting content using entity methods
		final CUser responsible = userService.findByLogin("mkaradeniz");
		meeting.setAgenda("Weekly status update on project progress, blockers discussion, and next steps planning");
		meeting.setResponsible(responsible);
		// Set participants using auxiliary method
		final Set<CUser> participants = new HashSet<>();
		participants.add(userService.getRandom());
		participants.add(userService.getRandom());
		participants.add(userService.getRandom());
		participants.add(userService.getRandom());
		meeting.setParticipants(participants);
		// Set meeting status using proper status entity
		final CMeetingStatus scheduledStatus = meetingStatusService.findByNameAndProject("Scheduled", project).orElseThrow();
		meeting.setStatus(scheduledStatus);
		meeting.setMinutes("Meeting agenda prepared");
		meeting.setLinkedElement("Project management system");
		// Set missing fields
		meeting.setMeetingType(meetingTypeService.getRandom(project));
		meeting.setCreatedBy(userService.getRandom());
		meetingService.save(meeting);
	}

	/** Creates sample retrospective meeting. */
	private void createSampleRetrospectiveMeeting(final CProject project) {
		final CMeeting meeting = new CMeeting("Sprint Retrospective", project);
		meeting.setDescription("Team reflection on what went well, what could be improved, and action items");
		meeting.setMeetingDate(LocalDateTime.now().minusDays(7).withHour(15).withMinute(0));
		meeting.setEndDate(LocalDateTime.now().minusDays(7).withHour(16).withMinute(0));
		meeting.setLocation("Conference Room C");
		// Set proper status for completed meeting
		final CMeetingStatus completedStatus = meetingStatusService.findByNameAndProject("Completed", project).orElseThrow();
		meeting.setStatus(completedStatus);
		// Add participants and attendees
		final Set<CUser> participants = new HashSet<>();
		final Set<CUser> attendees = new HashSet<>();
		participants.add(userService.getRandom());
		attendees.add(userService.getRandom());
		participants.add(userService.getRandom());
		attendees.add(userService.getRandom());
		participants.add(userService.getRandom());
		meeting.setParticipants(participants);
		meeting.setAttendees(attendees);
		// Set missing fields
		meeting.setMeetingType(meetingTypeService.getRandom(project));
		meeting.setResponsible(userService.getRandom());
		meeting.setCreatedBy(userService.getRandom());
		meetingService.save(meeting);
	}

	/** Creates sample review meeting. */
	private void createSampleReviewMeeting(final CProject project) {
		final CMeeting meeting = new CMeeting("Code Review Session", project);
		meeting.setDescription("Review of architectural changes and code quality improvements");
		meeting.setMeetingDate(LocalDateTime.now().minusDays(2).withHour(10).withMinute(0));
		meeting.setEndDate(LocalDateTime.now().minusDays(2).withHour(11).withMinute(30));
		meeting.setLocation("Virtual - Zoom");
		// Add participants
		final Set<CUser> participants = new HashSet<>();
		final CUser manager = userService.getRandom();
		final CUser dev = userService.getRandom();
		if (manager != null) {
			participants.add(manager);
		}
		if (dev != null) {
			participants.add(dev);
		}
		meeting.setParticipants(participants);
		// Set missing fields
		meeting.setMeetingType(meetingTypeService.getRandom(project));
		meeting.setStatus(meetingStatusService.getRandom(project));
		meeting.setResponsible(userService.getRandom());
		meeting.setCreatedBy(userService.getRandom());
		meetingService.save(meeting);
	}

	private void createSampleSoftwareOrder(CProject project) {
		final COrder order = new COrder("Cloud Service Subscription", project);
		order.setDescription("Subscription to cloud services for hosting and scalability");
		// Set order type
		final COrderType softwareType = orderTypeService.getRandom(project);
		order.setOrderType(softwareType);
		// Set order status
		final COrderStatus submittedStatus = orderStatusService.getRandom(project);
		order.setStatus(submittedStatus);
		// Set assigned user
		order.setCreatedBy(userService.getRandom());
		order.setAssignedTo(userService.getRandom());
		order.setRequestor(userService.getRandom());
		// Set financial details
		final CCurrency usdCurrency = currencyService.getRandom(project);
		order.setCurrency(usdCurrency);
		// Set date information
		order.setOrderDate(LocalDate.now().minusDays(3));
		order.setDeliveryDate(LocalDate.now().plusDays(7));
		order.setProviderCompanyName("poiopiopoipiopi");
		orderService.save(order);
	}

	/** Creates sample standup meeting. */
	private void createSampleStandupMeeting(final CProject project) {
		final CMeeting meeting = new CMeeting("Daily Standup - Sprint 3", project);
		meeting.setDescription("Daily progress sync and impediment discussion");
		meeting.setMeetingDate(LocalDateTime.now().plusDays(1).withHour(9).withMinute(0));
		meeting.setEndDate(LocalDateTime.now().plusDays(1).withHour(9).withMinute(30));
		meeting.setLocation("Conference Room A");
		// Set proper status
		meeting.setStatus(meetingStatusService.getRandom(project));
		// Add participants
		final Set<CUser> participants = new HashSet<>();
		final CUser manager = userService.getRandom();
		final CUser dev1 = userService.getRandom();
		final CUser dev2 = userService.getRandom();
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
		// Set missing fields
		meeting.setMeetingType(meetingTypeService.getRandom(project));
		meeting.setResponsible(userService.getRandom());
		meeting.setCreatedBy(userService.getRandom());
		meetingService.save(meeting);
	}

	/** Create a sample strategic decision. */
	private void createSampleStrategicDecision(final CProject project) {
		// Create the decision
		final CDecision decision = new CDecision("Technology Stack Selection", project);
		decision.setDescription("Decision on the primary technology stack for the digital transformation initiative");
		decision.setDecisionType(decisionTypeService.getRandom(project));
		decision.setDecisionStatus(decisionStatusService.getRandom(project));
		decision.setReviewDate(LocalDateTime.now().plusDays(7).withHour(14).withMinute(0));
		// Set accountable user
		decision.setAccountableUser(userService.getRandom());
		// Set missing fields
		decision.setCreatedBy(userService.getRandom());
		decisionService.save(decision);
	}

	/** Create a sample technical decision. */
	private void createSampleTechnicalDecision(final CProject project) {
		final CDecision decision = new CDecision("Database Migration Strategy", project);
		decision.setDescription("Technical approach for migrating legacy database to modern architecture");
		decision.setDecisionType(decisionTypeService.getRandom(project));
		decision.setDecisionStatus(decisionStatusService.getRandom(project));
		decision.setImplementationDate(LocalDateTime.now().minusDays(5).withHour(10).withMinute(0));
		decision.setAccountableUser(userService.getRandom());
		// Set missing fields
		decision.setCreatedBy(userService.getRandom());
		decisionService.save(decision);
	}

	/** Creates system architecture design activity. */
	private void createSystemArchitectureActivity(final CProject project) {
		// Create the activity using new auxiliary methods
		final CActivity archDesign = new CActivity("System Architecture Design", project);
		archDesign.setActivityType(activityTypeService.getRandom(project));
		archDesign.setDescription("Design scalable system architecture for infrastructure modernization");
		archDesign.setAssignedTo(userService.getRandom());
		archDesign.setCreatedBy(userService.getRandom());
		// Set time tracking using entity methods
		archDesign.setEstimatedHours(new BigDecimal("60.00"));
		archDesign.setActualHours(new BigDecimal("45.00"));
		archDesign.setRemainingHours(new BigDecimal("15.00"));
		// Set date information using auxiliary method
		archDesign.setStartDate(LocalDate.now().minusDays(15));
		archDesign.setDueDate(LocalDate.now().plusDays(10));
		// Set status and priority using auxiliary method
		archDesign.setStatus(activityStatusService.getRandom(project));
		archDesign.setProgressPercentage(65);
		activityService.save(archDesign);
		// Create comments
		commentService.createComment("Initial system architecture design phase started", archDesign, userService.getRandom());
		commentService.createComment("Completed high-level architecture diagrams and component definitions", archDesign, userService.getRandom());
		commentService.createComment("Reviewed architecture with team and incorporated feedback", archDesign, userService.getRandom());
		commentService.createComment("Activity on hold pending stakeholder approval of design changes", archDesign, userService.getRandom());
	}

	/** Creates team member Alice Davis. */
	private void createTeamMemberAlice() {
		final CUser analyst = userService.createLoginUser(USER_MEMBER_AYSE, STANDARD_PASSWORD, "Ayşe", "ayse.demir@ofsaglik.com.tr", "USER");
		// Set user profile directly on entity
		final String profilePictureFile = PROFILE_PICTURE_MAPPING.get(USER_MEMBER_AYSE);
		final byte[] profilePictureBytes = loadProfilePictureData(profilePictureFile);
		analyst.setLastname("Demir");
		analyst.setPhone("+90-462-751-1005");
		analyst.setProfilePictureData(profilePictureBytes);
		userService.save(analyst);
		// Associate with company (Of Sağlık Teknolojileri based on email domain)
		final CCompany company = companyService.findByName(COMPANY_OF_SAGLIK).orElse(null);
		if (company != null) {
			// Find a suitable role for the company
			final List<tech.derbent.api.roles.domain.CUserCompanyRole> companyRoles = userCompanyRoleService.findAll().stream()
					.filter(role -> role.getCompany() != null && role.getCompany().getId().equals(company.getId())).filter(role -> !role.isGuest())
					.collect(java.util.stream.Collectors.toList());
			final tech.derbent.api.roles.domain.CUserCompanyRole role = companyRoles.isEmpty() ? null : companyRoles.get(0);
			userCompanySettingsService.addUserToCompany(analyst, company, "MEMBER", role);
		}
	}

	/** Creates team member Bob Wilson. */
	private void createTeamMemberBob() {
		final CUser developer =
				userService.createLoginUser(USER_MEMBER_BURAK, STANDARD_PASSWORD, "Burak", "burak.ozkan@ofdanismanlik.com.tr", "USER");
		// Set user profile directly on entity
		final String profilePictureFile = PROFILE_PICTURE_MAPPING.get(USER_MEMBER_BURAK);
		final byte[] profilePictureBytes = loadProfilePictureData(profilePictureFile);
		developer.setLastname("Özkan");
		developer.setPhone("+90-462-751-0404");
		developer.setProfilePictureData(profilePictureBytes);
		userService.save(developer);
		// Associate with company (Of Stratejik Danışmanlık based on email domain)
		final CCompany company = companyService.findByName(COMPANY_OF_DANISMANLIK).orElse(null);
		if (company != null) {
			// Find a suitable role for the company
			final List<tech.derbent.api.roles.domain.CUserCompanyRole> companyRoles = userCompanyRoleService.findAll().stream()
					.filter(role -> role.getCompany() != null && role.getCompany().getId().equals(company.getId())).filter(role -> !role.isGuest())
					.collect(java.util.stream.Collectors.toList());
			final tech.derbent.api.roles.domain.CUserCompanyRole role = companyRoles.isEmpty() ? null : companyRoles.get(0);
			userCompanySettingsService.addUserToCompany(developer, company, "MEMBER", role);
		}
	}

	/** Creates team member Mary Johnson. */
	private void createTeamMemberMary() {
		final CUser teamMember = userService.createLoginUser(USER_MEMBER_MERVE, STANDARD_PASSWORD, "Merve", "merve.sahin@ofendüstri.com.tr", "USER");
		// Set user profile directly on entity
		final String profilePictureFile = PROFILE_PICTURE_MAPPING.get(USER_MEMBER_MERVE);
		final byte[] profilePictureBytes = loadProfilePictureData(profilePictureFile);
		teamMember.setLastname("Şahin");
		teamMember.setPhone("+90-462-751-1003");
		teamMember.setProfilePictureData(profilePictureBytes);
		userService.save(teamMember);
		// Associate with company (Of Endüstri Dinamikleri based on email domain)
		final CCompany company = companyService.findByName(COMPANY_OF_ENDUSTRI).orElse(null);
		if (company != null) {
			// Find a suitable role for the company
			final List<tech.derbent.api.roles.domain.CUserCompanyRole> companyRoles = userCompanyRoleService.findAll().stream()
					.filter(role -> role.getCompany() != null && role.getCompany().getId().equals(company.getId())).filter(role -> !role.isGuest())
					.collect(java.util.stream.Collectors.toList());
			final tech.derbent.api.roles.domain.CUserCompanyRole role = companyRoles.isEmpty() ? null : companyRoles.get(0);
			userCompanySettingsService.addUserToCompany(teamMember, company, "MEMBER", role);
		}
	}

	/** Creates team member users across different companies. */
	private void createTeamMemberUsers() {
		createTeamMemberMary();
		createTeamMemberBob();
		createTeamMemberAlice();
	}

	/** Creates technology startup company. */
	private void createTechCompany() {
		final CCompany techStartup = new CCompany(COMPANY_OF_TEKNOLOJI);
		techStartup.setDescription("Dijital dönüşüm için yenilikçi teknoloji çözümleri");
		techStartup.setAddress("Cumhuriyet Mahallesi, Atatürk Caddesi No:15, Of/Trabzon");
		techStartup.setPhone("+90-462-751-0101");
		techStartup.setEmail("iletisim@ofteknoloji.com.tr");
		techStartup.setWebsite("https://www.ofteknoloji.com.tr");
		techStartup.setTaxNumber("TR-123456789");
		techStartup.setEnabled(true);
		// Company Configuration Settings
		techStartup.setCompanyTheme("lumo-dark");
		techStartup.setCompanyLogoUrl("/assets/logos/tech-logo.svg");
		techStartup.setPrimaryColor("#1976d2");
		techStartup.setWorkingHoursStart("09:00");
		techStartup.setWorkingHoursEnd("18:00");
		techStartup.setCompanyTimezone("Europe/Istanbul");
		techStartup.setDefaultLanguage("tr");
		techStartup.setEnableNotifications(true);
		techStartup.setNotificationEmail("bildirim@ofteknoloji.com.tr");
		companyService.save(techStartup);
	}

	/** Creates technical documentation activity. */
	private void createTechnicalDocumentationActivity(final CProject project) {
		// Create the activity using new auxiliary methods
		final CActivity techDoc = new CActivity("Technical Documentation Update", project);
		techDoc.setActivityType(activityTypeService.getRandom(project));
		techDoc.setDescription("Update and enhance technical documentation for customer experience features");
		// Set assigned users using auxiliary method
		techDoc.setAssignedTo(userService.getRandom());
		techDoc.setCreatedBy(userService.getRandom());
		// Set time tracking using entity methods (completed activity)
		techDoc.setEstimatedHours(new BigDecimal("16.00"));
		techDoc.setActualHours(new BigDecimal("16.00"));
		techDoc.setRemainingHours(new BigDecimal("0.00"));
		// Set date information using entity methods (completed activity)
		techDoc.setStartDate(LocalDate.now().minusDays(5));
		techDoc.setDueDate(LocalDate.now().minusDays(1));
		techDoc.setCompletionDate(LocalDate.now().minusDays(1));
		// Set status and priority using auxiliary method (completed activity)
		techDoc.setStatus(activityStatusService.getRandom(project));
		techDoc.setProgressPercentage(100);
		activityService.save(techDoc);
		// Create comments
		commentService.createComment("Initial technical documentation review and updates started", techDoc, userService.getRandom());
		commentService.createComment("Completed updates to user guides and API documentation", techDoc, userService.getRandom());
		commentService.createComment("Reviewed documentation with team and incorporated feedback", techDoc, userService.getRandom());
		commentService.createComment("Documentation successfully updated and approved by stakeholders", techDoc, userService.getRandom());
	}

	/** Creates UI testing activity. */
	private void createUITestingActivity(final CProject project) {
		// Create the activity using new auxiliary methods
		final CActivity uiTesting = new CActivity("User Interface Testing", project);
		// Find and set the activity type
		final CActivityType testingType = activityTypeService.findByNameAndProject("Testing", project).orElseThrow();
		if (testingType == null) {
			LOGGER.warn("Testing activity type not found for project, using null");
		}
		// Set activity type and description using auxiliary method
		uiTesting.setActivityType(testingType);
		uiTesting.setDescription("Comprehensive testing of user interface components and workflows");
		uiTesting.setAssignedTo(userService.getRandom());
		uiTesting.setCreatedBy(userService.getRandom());
		// Set time tracking using entity methods
		uiTesting.setEstimatedHours(new BigDecimal("24.00"));
		uiTesting.setActualHours(new BigDecimal("20.00"));
		uiTesting.setRemainingHours(new BigDecimal("4.00"));
		// Set date information using auxiliary method
		uiTesting.setStartDate(LocalDate.now().minusDays(7));
		uiTesting.setDueDate(LocalDate.now().plusDays(3));
		// Set status and priority using auxiliary method
		final CActivityStatus inProgressStatus = activityStatusService.findByNameAndProject("In Progress", project).orElseThrow();
		uiTesting.setStatus(inProgressStatus);
		uiTesting.setProgressPercentage(85);
		// Set missing fields
		uiTesting.setCreatedBy(userService.getRandom());
		activityService.save(uiTesting);
		// Create comments
		commentService.createComment("UI testing activity initiated with comprehensive test plan", uiTesting, userService.getRandom());
		commentService.createComment("Completed responsive design testing across multiple devices", uiTesting, userService.getRandom());
		commentService.createComment("Working on accessibility testing and user experience validation", uiTesting, userService.getRandom());
	}

	private void createUserCompanySetting(final CUser user, final CCompany company, final String roleName, final String ownershipLevel) {
		try {
			// Check if relationship already exists
			if (!userCompanySettingsService.relationshipExists(user.getId(), company.getId())) {
				// Find a non-guest role for the company - prefer a role with matching name or use first available
				List<tech.derbent.api.roles.domain.CUserCompanyRole> companyRoles = userCompanyRoleService.findAll().stream()
						.filter(role -> role.getCompany() != null && role.getCompany().getId().equals(company.getId()))
						.filter(role -> !role.isGuest()) // Exclude guest roles
						.collect(java.util.stream.Collectors.toList());
				tech.derbent.api.roles.domain.CUserCompanyRole roleToAssign = null;
				if (!companyRoles.isEmpty()) {
					// Try to find a role with matching name (case insensitive)
					roleToAssign = companyRoles.stream().filter(role -> role.getName().toLowerCase().contains(roleName.toLowerCase())
							|| roleName.toLowerCase().contains(role.getName().toLowerCase())).findFirst().orElse(companyRoles.get(0)); // Use first
																																		// available
																																		// if no match
				}
				userCompanySettingsService.addUserToCompany(user, company, ownershipLevel, roleToAssign);
				LOGGER.debug("Created user company setting: {} -> {} (role: {})", user.getLogin(), company.getName(),
						roleToAssign != null ? roleToAssign.getName() : "none");
			} else {
				LOGGER.debug("User company relationship already exists: {} -> {}", user.getLogin(), company.getName());
			}
		} catch (final Exception e) {
			LOGGER.warn("Failed to create user company setting for {} -> {}: {}", user.getLogin(), company.getName(), e.getMessage());
		}
	}

	/** Helper method to create user project settings safely.
	 * @param user       the user to assign
	 * @param project    the project to assign to
	 * @param role       the role name
	 * @param permission the permission level */
	private void createUserProjectSetting(final CUser user, final CProject project, final String role, final String permission) {
		try {
			// Check if relationship already exists
			if (!userProjectSettingsService.relationshipExists(user.getId(), project.getId())) {
				LOGGER.debug("Created user project setting: {} -> {} ({})", user.getLogin(), project.getName(), role);
			} else {
				LOGGER.debug("User project relationship already exists: {} -> {}", user.getLogin(), project.getName());
			}
		} catch (final Exception e) {
			LOGGER.warn("Failed to create user project setting for {} -> {}: {}", user.getLogin(), project.getName(), e.getMessage());
		}
	}

	/** Initializes comprehensive activity data with available fields populated. */
	private void initializeSampleActivities(final CProject project) {
		try {
			// Create at least 3 activities per project
			createBackendDevActivity(project);
			createUITestingActivity(project);
			createSystemArchitectureActivity(project);
			createTechnicalDocumentationActivity(project);
			// Additional activities to meet 3+ per project requirement
			createAdditionalInfrastructureActivities(project);
			createAdditionalDigitalTransformationActivities(project);
			createAdditionalProductDevelopmentActivities(project);
			createAdditionalCustomerExperienceActivities(project);
			// LOGGER.info("Successfully created comprehensive activity samples");
		} catch (final Exception e) {
			LOGGER.error("Error creating sample activities", e);
			throw new RuntimeException("Failed to initialize activities", e);
		}
	}

	private void initializeSampleActivityStatuses(final CProject project) {
		try {
			createActivityStatus(STATUS_NOT_STARTED, project, "Activity has not been started yet", "#95a5a6", false, 1);
			createActivityStatus(STATUS_IN_PROGRESS, project, "Activity is currently in progress", "#3498db", false, 2);
			createActivityStatus(STATUS_ON_HOLD, project, "Activity is temporarily on hold", "#f39c12", false, 3);
			createActivityStatus(STATUS_COMPLETED, project, "Activity has been completed", "#27ae60", true, 4);
			createActivityStatus(STATUS_CANCELLED, project, "Activity has been cancelled", "#e74c3c", true, 5);
		} catch (final Exception e) {
			LOGGER.error("Error initializing activity statuses for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize activity statuses for project: " + project.getName(), e);
		}
	}

	private void initializeSampleActivityTypes(final CProject project) {
		try {
			final String[][] activityTypes = {
					{
							ACTIVITY_TYPE_DEVELOPMENT, "Software development and coding tasks"
					}, {
							ACTIVITY_TYPE_TESTING, "Quality assurance and testing activities"
					}, {
							ACTIVITY_TYPE_DESIGN, "UI/UX design and system architecture"
					}, {
							ACTIVITY_TYPE_DOCUMENTATION, "Technical writing and documentation"
					}, {
							ACTIVITY_TYPE_RESEARCH, "Research and analysis activities"
					}
			};
			for (final String[] typeData : activityTypes) {
				final CActivityType item = activityTypeService.createEntity(typeData[0], project);
				item.setDescription(typeData[1]);
				item.setColor(CColorUtils.getRandomColor(true));
			}
		} catch (final Exception e) {
			LOGGER.error("Error creating activity types", e);
			throw new RuntimeException("Failed to initialize activity types", e);
		}
	}

	private void initializeSampleApprovalStatuses(final CProject project) {
		try {
			createApprovalStatus("Draft", project, "Approval is in draft state", CColorUtils.getRandomColor(true), false, 1);
			createApprovalStatus("Submitted", project, "Approval has been submitted", CColorUtils.getRandomColor(true), false, 2);
			createApprovalStatus("Approved", project, "Approval has been approved", CColorUtils.getRandomColor(true), true, 3);
			createApprovalStatus("Rejected", project, "Approval has been rejected", CColorUtils.getRandomColor(true), true, 4);
		} catch (final Exception e) {
			LOGGER.error("Error initializing approval statuses for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize approval statuses for project: " + project.getName(), e);
		}
	}

	private void initializeSampleCommentPriorities(final CProject project) {
		try {
			createCommentPriority(project, "Low", "Low priority comment", CColorUtils.getRandomColor(true), 1, false, 1);
			createCommentPriority(project, "Medium", "Medium priority comment", CColorUtils.getRandomColor(true), 2, true, 2);
			createCommentPriority(project, "High", "High priority comment", CColorUtils.getRandomColor(true), 3, false, 3);
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample comment priorities for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize sample comment priorities for project: " + project.getName(), e);
		}
	}

	private void initializeSampleCompanyRoles() {
		try {
			final List<CCompany> companies = companyService.findAll();
			final String[][] companyRoles = {
					{
							"Company Admin", "Administrative role with full company access", "true", "true", "false"
					}, {
							"Company Manager", "Company management role with write access", "false", "true", "false"
					}, {
							"Employee", "Standard employee role", "false", "true", "false"
					}, {
							"Company Guest", "Guest role with limited access", "false", "false", "true"
					}
			};
			for (final CCompany company : companies) {
				for (final String[] roleData : companyRoles) {
					final tech.derbent.api.roles.domain.CUserCompanyRole role =
							new tech.derbent.api.roles.domain.CUserCompanyRole(roleData[0], company);
					role.setDescription(roleData[1]);
					role.setIsAdmin(Boolean.parseBoolean(roleData[2]));
					role.setIsUser(Boolean.parseBoolean(roleData[3]));
					role.setIsGuest(Boolean.parseBoolean(roleData[4]));
					role.setColor(CColorUtils.getRandomColor(true));
					// Add appropriate page access based on role type
					if (role.isAdmin()) {
						role.addWriteAccess("CompanySettings");
						role.addWriteAccess("UserManagement");
						role.addWriteAccess("CompanyReports");
					}
					if (role.isUser()) {
						role.addReadAccess("Dashboard");
						role.addReadAccess("Tasks");
						role.addWriteAccess("Profile");
					}
					if (role.isGuest()) {
						role.addReadAccess("Dashboard");
						role.addReadAccess("PublicInfo");
					}
					userCompanyRoleService.save(role);
				}
			}
		} catch (final Exception e) {
			LOGGER.error("Error creating company roles: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to initialize company roles", e);
		}
	}

	private void initializeSampleCurrencies(final CProject project) {
		try {
			createCurrency(project, "USD", "US Dollar", "$ ");
			createCurrency(project, "EUR", "Euro", "€");
			createCurrency(project, "TRY", "Turkish Lira", "₺");
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample currencies for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize sample currencies for project: " + project.getName(), e);
		}
	}

	private void initializeSampleDecisions(final CProject project) {
		try {
			createSampleStrategicDecision(project);
			createSampleTechnicalDecision(project);
			createSampleBudgetDecision(project);
			createSampleOperationalDecision(project);
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample decisions for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize sample decisions for project: " + project.getName(), e);
		}
	}

	private void initializeSampleDecisionStatuses(final CProject project) {
		try {
			createDecisionStatus("Draft", project, "Decision is in draft state", CColorUtils.getRandomColor(true), false, 1);
			createDecisionStatus("Under Review", project, "Decision is under review", CColorUtils.getRandomColor(true), false, 2);
			createDecisionStatus("Approved", project, "Decision has been approved", CColorUtils.getRandomColor(true), true, 3);
			createDecisionStatus("Implemented", project, "Decision has been implemented", CColorUtils.getRandomColor(true), true, 4);
			createDecisionStatus("Rejected", project, "Decision has been rejected", CColorUtils.getRandomColor(true), true, 5);
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample decision statuses for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize sample decision statuses for project: " + project.getName(), e);
		}
	}

	private void initializeSampleDecisionTypes(final CProject project) {
		try {
			final String[][] decisionTypes = {
					{
							"Strategic", "High-level strategic decisions affecting organization direction"
					}, {
							"Tactical", "Mid-level tactical decisions for project execution"
					}, {
							"Operational", "Day-to-day operational decisions"
					}, {
							"Technical", "Technology and implementation related decisions"
					}, {
							"Budget", "Financial and budgeting decisions"
					}, {
							"Resource", "Human resource and allocation decisions"
					}, {
							"Timeline", "Schedule and milestone related decisions"
					}, {
							"Quality", "Quality assurance and standards decisions"
					}
			};
			for (final String[] typeData : decisionTypes) {
				final CDecisionType item = decisionTypeService.createEntity(typeData[0], project);
				item.setDescription(typeData[1]);
				item.setColor(CColorUtils.getRandomColor(true));
			}
		} catch (final Exception e) {
			LOGGER.error("Error creating decision types for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize decision types for project: " + project.getName(), e);
		}
	}

	private void initializeSampleMeetings(final CProject project) {
		try {
			createSamplePlanningMeeting(project);
			createSampleProjectMeeting(project);
			createSampleRetrospectiveMeeting(project);
			createSampleReviewMeeting(project);
			createSampleStandupMeeting(project);
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample meetings for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize sample meetings for project: " + project.getName(), e);
		}
	}

	private void initializeSampleMeetingStatuses(final CProject project) {
		try {
			createMeetingStatus("Scheduled", project, "Meeting is scheduled but not yet started", "#3498db", false, 1);
			createMeetingStatus("In Progress", project, "Meeting is currently in progress", "#f39c12", false, 2);
			createMeetingStatus("Completed", project, "Meeting has been completed successfully", "#27ae60", true, 3);
			createMeetingStatus("Cancelled", project, "Meeting has been cancelled", "#e74c3c", true, 4);
			createMeetingStatus("Postponed", project, "Meeting has been postponed to a later date", "#9b59b6", false, 5);
		} catch (final Exception e) {
			LOGGER.error("Error initializing meeting statuses for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize meeting statuses for project: " + project.getName(), e);
		}
	}

	private void initializeSampleMeetingTypes(final CProject project) {
		try {
			final String[][] meetingTypes = {
					{
							"Daily Standup", "Daily team synchronization meetings"
					}, {
							"Sprint Planning", "Sprint planning and estimation meetings"
					}, {
							"Sprint Review", "Sprint review and demonstration meetings"
					}, {
							"Sprint Retrospective", "Sprint retrospective and improvement meetings"
					}, {
							"Project Review", "Project review and status meetings"
					}, {
							"Technical Review", "Technical design and code review meetings"
					}, {
							"Stakeholder Meeting", "Meetings with project stakeholders"
					}, {
							"Training Session", "Training and knowledge sharing sessions"
					}
			};
			for (final String[] typeData : meetingTypes) {
				final CMeetingType meetingType = meetingTypeService.createEntity(typeData[0], project);
				meetingType.setDescription(typeData[1]);
				meetingType.setColor(CColorUtils.getRandomColor(true));
				meetingTypeService.save(meetingType);
			}
		} catch (final Exception e) {
			LOGGER.error("Error creating meeting types for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize meeting types for project: " + project.getName(), e);
		}
	}

	private void initializeSampleOrders(CProject project) {
		try {
			createSampleHardwareOrder(project);
			createSampleSoftwareOrder(project);
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample orders for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize sample orders for project: " + project.getName(), e);
		}
	}

	private void initializeSampleOrderStatuses(final CProject project) {
		try {
			createOrderStatus("Draft", project, "Order is in draft state", "#95a5a6", false, 1);
			createOrderStatus("Submitted", project, "Order has been submitted for approval", "#3498db", false, 2);
			createOrderStatus("Approved", project, "Order has been approved", "#27ae60", false, 3);
			createOrderStatus("Processing", project, "Order is being processed", "#f39c12", false, 4);
			createOrderStatus("Delivered", project, "Order has been delivered", "#2ecc71", true, 5);
			createOrderStatus("Cancelled", project, "Order has been cancelled", "#e74c3c", true, 6);
		} catch (final Exception e) {
			LOGGER.error("Error initializing order statuses for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize order statuses for project: " + project.getName(), e);
		}
	}

	private void initializeSampleOrderTypes(final CProject project) {
		try {
			final String[][] orderTypes = {
					{
							"Hardware", "Hardware procurement orders"
					}, {
							"Software", "Software licensing and subscription orders"
					}, {
							"Service", "Professional services and consulting orders"
					}, {
							"Training", "Training and certification orders"
					}, {
							"Maintenance", "Maintenance and support service orders"
					}, {
							"Infrastructure", "Infrastructure and hosting service orders"
					}, {
							"Equipment", "Equipment rental and leasing orders"
					}, {
							"Supplies", "Office supplies and materials orders"
					}
			};
			for (final String[] typeData : orderTypes) {
				final COrderType orderType = orderTypeService.createEntity(typeData[0], project);
				orderType.setDescription(typeData[1]);
				orderType.setColor(CColorUtils.getRandomColor(true));
				orderTypeService.save(orderType);
			}
		} catch (final Exception e) {
			LOGGER.error("Error creating order types for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize order types for project: " + project.getName(), e);
		}
	}

	private void initializeSampleProjectRoles(final CProject project) {
		try {
			final String[][] projectRoles = {
					{
							"Project Admin", "Administrative role with full project access", "true", "true", "false"
					}, {
							"Project Manager", "Project management role with write access", "false", "true", "false"
					}, {
							"Team Member", "Standard team member role", "false", "true", "false"
					}, {
							"Project Guest", "Guest role with limited access", "false", "false", "true"
					}
			};
			for (final String[] roleData : projectRoles) {
				final CUserProjectRole role = new CUserProjectRole(roleData[0], project);
				role.setDescription(roleData[1]);
				role.setIsAdmin(Boolean.parseBoolean(roleData[2]));
				role.setIsUser(Boolean.parseBoolean(roleData[3]));
				role.setIsGuest(Boolean.parseBoolean(roleData[4]));
				role.setColor(CColorUtils.getRandomColor(true));
				// Add appropriate page access based on role type
				if (role.isAdmin()) {
					role.addWriteAccess("ProjectSettings");
					role.addWriteAccess("UserManagement");
					role.addWriteAccess("ProjectReports");
				}
				if (role.isUser()) {
					role.addReadAccess("Dashboard");
					role.addReadAccess("Tasks");
					role.addWriteAccess("Profile");
				}
				if (role.isGuest()) {
					role.addReadAccess("Dashboard");
					role.addReadAccess("PublicInfo");
				}
				userProjectRoleService.save(role);
			}
		} catch (final Exception e) {
			LOGGER.error("Error creating project roles for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize project roles for project: " + project.getName(), e);
		}
	}

	/** Creates high priority technical risk. */
	private void initializeSampleRisks(CProject project) {
		CRisk risk = new CRisk("Legacy System Integration Challenges", project);
		risk.setRiskSeverity(ERiskSeverity.HIGH);
		risk.setDescription("Integration with legacy systems may cause compatibility issues and performance bottlenecks");
		risk.setStatus(riskStatusService.getRandom(project));
		// Set missing fields
		risk.setCreatedBy(userService.getRandom());
		risk.setAssignedTo(userService.getRandom());
		riskService.save(risk);
		risk = new CRisk("Team Member Vacation Scheduling Conflicts", project);
		risk.setRiskSeverity(ERiskSeverity.LOW);
		risk.setDescription("Overlapping vacation schedules may temporarily reduce team capacity");
		// Set missing fields
		risk.setStatus(riskStatusService.getRandom(project));
		risk.setCreatedBy(userService.getRandom());
		risk.setAssignedTo(userService.getRandom());
		riskService.save(risk);
		risk = new CRisk("Minor Delays in Third-Party Integrations", project);
		risk.setRiskSeverity(ERiskSeverity.LOW);
		risk.setDescription("External vendor may experience minor delays in API delivery");
		// Set missing fields
		risk.setStatus(riskStatusService.getRandom(project));
		risk.setCreatedBy(userService.getRandom());
		risk.setAssignedTo(userService.getRandom());
		riskService.save(risk);
	}

	private void initializeSampleRiskStatuses(final CProject project) {
		try {
			createRiskStatus("Identified", project, "Risk has been identified", CColorUtils.getRandomColor(true), false, 1);
			createRiskStatus("Assessed", project, "Risk has been assessed", CColorUtils.getRandomColor(true), false, 2);
			createRiskStatus("Mitigated", project, "Risk mitigation actions taken", CColorUtils.getRandomColor(true), false, 3);
			createRiskStatus("Closed", project, "Risk is closed", CColorUtils.getRandomColor(true), true, 4);
		} catch (final Exception e) {
			LOGGER.error("Error initializing risk statuses for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize risk statuses for project: " + project.getName(), e);
		}
	}

	/** Initialize sample user company settings to demonstrate the CComponentCompanyUserSettings pattern. This creates realistic user-company
	 * relationships following the established pattern. */
	private void initializeSampleUserCompanySettings() {
		try {
			final List<CCompany> companies = companyService.list(Pageable.unpaged()).getContent();
			// Get sample users by login for consistent assignment
			final CUser admin = userService.findByLogin(USER_ADMIN);
			final CUser manager = userService.findByLogin(USER_MANAGER);
			final CUser mary = userService.findByLogin("mary");
			final CUser bob = userService.findByLogin("bob");
			final CUser alice = userService.findByLogin("alice");
			for (final CCompany company : companies) {
				// Assign users to different companies with different roles
				if ("Tech Solutions Inc.".equals(company.getName())) {
					if (admin != null) {
						createUserCompanySetting(admin, company, "CEO", "Owner");
					}
					if (manager != null) {
						createUserCompanySetting(manager, company, "CTO", "Technology");
					}
					if (mary != null) {
						createUserCompanySetting(mary, company, "Senior Developer", "Employee");
					}
				} else if ("Global Consulting Partners".equals(company.getName())) {
					if (manager != null) {
						createUserCompanySetting(manager, company, "Senior Consultant", "Employee");
					}
					if (alice != null) {
						createUserCompanySetting(alice, company, "Business Analyst", "Employee");
					}
					if (bob != null) {
						createUserCompanySetting(bob, company, "Technical Consultant", "Contractor");
					}
				} else if ("HealthCare Systems".equals(company.getName())) {
					if (alice != null) {
						createUserCompanySetting(alice, company, "Product Manager", "Employee");
					}
					if (mary != null) {
						createUserCompanySetting(mary, company, "Lead Developer", "Employee");
					}
				} else if ("Manufacturing Corp".equals(company.getName())) {
					if (bob != null) {
						createUserCompanySetting(bob, company, "Systems Engineer", "Employee");
					}
				}
			}
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample user company settings: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to initialize sample user company settings", e);
		}
	}

	/** Initialize sample user project settings to demonstrate the CComponentProjectUserSettings pattern. This creates realistic user-project
	 * relationships following the established pattern. */
	private void initializeSampleUserProjectSettings() {
		try {
			LOGGER.info("Initializing sample user project settings");
			final List<CProject> projects = projectService.list(Pageable.unpaged()).getContent();
			// Get sample users by login for consistent assignment
			final CUser admin = userService.findByLogin(USER_ADMIN);
			final CUser manager = userService.findByLogin(USER_MANAGER);
			final CUser mary = userService.findByLogin("mary");
			final CUser bob = userService.findByLogin("bob");
			final CUser alice = userService.findByLogin("alice");
			for (final CProject project : projects) {
				// Admin gets full access to all projects
				if (admin != null) {
					createUserProjectSetting(admin, project, "Admin", "FULL_ACCESS");
				}
				// Manager gets management access to all projects
				if (manager != null) {
					createUserProjectSetting(manager, project, "Project Manager", "WRITE_ACCESS");
				}
				// Assign team members to different projects for variety
				if ("Digital Transformation Initiative".equals(project.getName())) {
					if (mary != null) {
						createUserProjectSetting(mary, project, "Developer", "WRITE_ACCESS");
					}
					if (alice != null) {
						createUserProjectSetting(alice, project, "Analyst", "READ_ACCESS");
					}
				} else if ("Infrastructure Upgrade Project".equals(project.getName())) {
					if (bob != null) {
						createUserProjectSetting(bob, project, "DevOps Engineer", "WRITE_ACCESS");
					}
					if (mary != null) {
						createUserProjectSetting(mary, project, "Technical Lead", "WRITE_ACCESS");
					}
				} else if ("New Product Development".equals(project.getName())) {
					if (alice != null) {
						createUserProjectSetting(alice, project, "Product Owner", "WRITE_ACCESS");
					}
					if (bob != null) {
						createUserProjectSetting(bob, project, "Developer", "WRITE_ACCESS");
					}
				}
			}
			LOGGER.info("Successfully initialized sample user project settings for {} projects", projects.size());
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample user project settings: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to initialize sample user project settings", e);
		}
	}

	private void initializeSampleUserTypes(final CProject project) {
		try {
			final String[][] userTypes = {
					{
							"Administrator", "System administrators with full access"
					}, {
							"Project Manager", "Project managers responsible for project coordination"
					}, {
							"Team Lead", "Team leaders responsible for team management"
					}, {
							"Developer", "Software developers and engineers"
					}, {
							"Analyst", "Business and system analysts"
					}, {
							"Tester", "Quality assurance and testing specialists"
					}, {
							"Designer", "UI/UX designers and architects"
					}, {
							"Stakeholder", "Project stakeholders and decision makers"
					}
			};
			for (final String[] typeData : userTypes) {
				final CUserType userType = userTypeService.createEntity(typeData[0], project);
				userType.setDescription(typeData[1]);
				userType.setColor(CColorUtils.getRandomColor(true));
				userTypeService.save(userType);
			}
		} catch (final Exception e) {
			LOGGER.error("Error creating user types for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize user types for project: " + project.getName(), e);
		}
		// for each user distribute random user types
		final List<CUser> users = userService.list(Pageable.unpaged()).getContent();
		for (final CUser user : users) {
			user.setUserType(userTypeService.getRandom());
			userService.save(user);
		}
	}

	public boolean isDatabaseEmpty() {
		final long cnt = userService.count();
		LOGGER.info("User count = {}", cnt);
		return cnt == 0;
	}

	/** Loads profile picture data from the profile-pictures directory.
	 * @param filename The SVG filename to load
	 * @return byte array of the SVG content, or null if not found */
	private byte[] loadProfilePictureData(final String filename) {
		try {
			// Try direct file path first (since profile-pictures is in project root)
			final Path filePath = java.nio.file.Paths.get("profile-pictures", filename);
			Check.isTrue(!filename.contains(".."), "Invalid filename: " + filename); // Prevent path traversal
			if (Files.exists(filePath)) {
				return Files.readAllBytes(filePath);
			}
			// Fallback: Load from classpath resources
			final var resource = getClass().getClassLoader().getResourceAsStream("profile-pictures/" + filename);
			Check.notNull(resource, "Profile picture resource not found in classpath: " + filename);
			return resource.readAllBytes();
		} catch (final Exception e) {
			LOGGER.error("Error loading profile picture: {}", filename, e);
			return null;
		}
	}

	public void loadSampleData() {
		try {
			// ========== NON-PROJECT RELATED INITIALIZATION PHASE ==========
			// **** CREATE COMPANY SAMPLES ****//
			createTechCompany();
			createConsultingCompany();
			createHealthcareCompany();
			createManufacturingCompany();
			// Create sample users across different companies
			createAdminUser();
			createProjectManagerUser();
			createTeamMemberUsers();
			/* create sample projects */
			createProjectDigitalTransformation();
			createProjectInfrastructureUpgrade();
			createProjectProductDevelopment();
			/* create sample user project relationships */
			initializeSampleUserProjectSettings();
			/* create sample user company relationships */
			initializeSampleUserCompanySettings();
			// ========== PROJECT-SPECIFIC INITIALIZATION PHASE ==========
			final List<CProject> projects = projectService.list(Pageable.unpaged()).getContent();
			for (final CProject project : projects) {
				CSystemSettingsInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, true);
				// Core system entities required for project operation
				CActivityInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, true);
				CUserInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, true);
				CCompanyInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
				CDecisionInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
				CMeetingInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, true);
				COrderInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
				CProjectInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, true);
				CRiskInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
				CUserProjectRoleInitizerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
				CUserCompanyRoleInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
				// Type/Status InitializerServices
				CActivityStatusInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
				CActivityTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
				CApprovalStatusInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
				CCommentPriorityInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
				CCurrencyInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
				CDecisionStatusInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
				CDecisionTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
				CMeetingStatusInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
				CMeetingTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
				COrderStatusInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
				COrderTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
				CRiskStatusInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
				CUserTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
				// TODO: Add similar calls for all other InitializerServices (user types, priorities, etc.)
				// Project-specific type and configuration entities
				initializeSampleMeetingStatuses(project);
				initializeSampleActivityStatuses(project);
				initializeSampleOrderStatuses(project);
				initializeSampleApprovalStatuses(project);
				initializeSampleRiskStatuses(project);
				// types
				initializeSampleUserTypes(project);
				initializeSampleProjectRoles(project);
				initializeSampleMeetingTypes(project);
				initializeSampleDecisionTypes(project);
				initializeSampleOrderTypes(project);
				initializeSampleActivityTypes(project);
				initializeSampleRisks(project);
				// Sample data entities for demonstration
				initializeSampleActivities(project);
				initializeSampleDecisionStatuses(project);
				initializeSampleCommentPriorities(project);
				initializeSampleCurrencies(project);
				initializeSampleMeetings(project);
				initializeSampleDecisions(project);
				initializeSampleOrders(project);
			}
			// Initialize company roles (non-project specific)
			initializeSampleCompanyRoles();
			// createSampleOrders(); // Temporarily disabled due to missing dependencies
			LOGGER.info("Sample data initialization completed successfully");
		} catch (final Exception e) {
			LOGGER.error("Error loading sample data", e);
			throw new RuntimeException("Failed to load sample data", e);
		}
	}

	public void reloadForced() {
		LOGGER.info("Sample data reload (forced) started");
		clearSampleData(); // <<<<< ÖNCE TEMİZLE
		loadSampleData(); // <<<<< SONRA YENİDEN OLUŞTUR
		LOGGER.info("Sample data reload (forced) finished");
	}
	// ========================================================================
	// SYSTEM INITIALIZATION METHODS - Base entities required for operation
	// ========================================================================
}
