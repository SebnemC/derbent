# Copilot Agent Guideline for Vaadin Java Projects (MVC Design)

## Project Overview

This is a comprehensive **project management and collaboration platform** built with **Java 17, Spring Boot 3.5, and Vaadin Flow 24.8**. The application provides workflows, task management, resource planning, and team collaboration features inspired by Atlassian Jira and ProjeQtOr.

### Key Implemented Features
- **Enhanced Activity Management**: 25+ field activity tracking with status workflows, priorities, time/cost tracking, parent-child relationships
- **User & Company Management**: Multi-tenant user system with role-based access control, company associations
- **Project Management**: Project lifecycle management with resource allocation and timeline tracking
- **Meeting Management**: Comprehensive meeting scheduling with participants, types, and agenda management
- **Risk Management**: Project risk identification, assessment, and mitigation tracking
- **Dashboard & Analytics**: KPI tracking, progress visualization, and performance metrics
- **Hierarchical Navigation**: Multi-level side menu system with context-aware navigation
- **Advanced UI Components**: Custom dialog system, accordion layouts, responsive grid components

### Core Domain Model Examples
```java
// Actual domain classes from the project:
CUser extends CEntityNamed        // User management with company associations
CProject extends CEntityNamed     // Project lifecycle management
CActivity extends CEntityOfProject // 25+ field activity tracking system
CActivityStatus                   // Workflow management (TODO, IN_PROGRESS, REVIEW, etc.)
CActivityPriority                 // Priority system (CRITICAL, HIGH, MEDIUM, LOW, LOWEST)
CMeeting extends CEntityOfProject // Meeting management system
CRisk extends CEntityOfProject    // Risk management system
```

Check [project_design_description.md](./project_design_description.md) file for complete project scope and architectural details.

## Table of Contents
- [1. MVC Architecture Principles](#1-mvc-architecture-principles)
- [2. Development Environment Setup](#2-development-environment-setup)
- [3. Commenting Standards](#3-commenting-standards)
- [4. Security and Validation Standards](#4-security-and-validation-standards)
- [5. Comprehensive Testing Requirements](#5-comprehensive-testing-requirements)
- [6. Java Build and Quality Checks Before Submission](#6-java-build-and-quality-checks-before-submission)
- [7. Pull Request Checklist](#7-pull-request-checklist)
- [8. Copilot Review Protocol](#8-copilot-review-protocol)
- [9. Coding Styles & Best Practices](#9-coding-styles--best-practices)
- [10. Strict Prohibitions](#10-strict-prohibitions)
- [11. Additional Best Practices](#11-additional-best-practices)
- [Related Documentation](#related-documentation)

---

## 1. MVC Architecture Principles

- **Domain:**  
  Represents business logic and data. Use POJOs, Entities, and Service classes.
- **View:**  
  UI components using Vaadin's framework. Avoid embedding business logic in views.
- **Controller/Service:**  
  Handles user input, updates models, and refreshes views. Typically, Vaadin views act as controllers.

**Example directory structure:**
```
src/main/java/tech/derbent/
├── abstracts/           # Base classes and annotations
│   ├── annotations/     # MetaData and other annotations
│   ├── domains/         # Base domain entities (CEntityNamed, CEntityOfProject)
│   ├── services/        # Base service classes
│   └── views/           # Base UI components (CButton, CDialog, CGrid)
├── activities/          # Activity management module
│   ├── domain/         # CActivity, CActivityStatus, CActivityPriority
│   ├── service/        # Activity business logic
│   └── view/           # Activity UI components
├── users/              # User management module
│   ├── domain/         # CUser, CUserRole, CUserType
│   ├── service/        # User business logic
│   └── view/           # User UI components
├── projects/           # Project management module
├── meetings/           # Meeting management module
├── companies/          # Company management module
└── base/               # Core infrastructure
    └── ui/dialogs/     # Standard dialogs (CWarningDialog, CInformationDialog)
```

## 2. Development Environment Setup

**Current Tech Stack:**
- **Java 17+** with Spring Boot 3.5.0
- **Vaadin Flow 24.8.3** for server-side UI rendering
- **PostgreSQL** as the primary database (avoid H2 in production)
- **Maven** build system with provided pom.xml configuration
- **Spring Boot DevTools** for hot reloading during development

**Key Dependencies:**
- Spring Data JPA for database operations
- Spring Security for authentication/authorization
- Vaadin Charts for data visualization
- H2 for testing only (PostgreSQL for production)
- JUnit 5 and TestContainers for testing

**Code Quality Tools:**
- Follow the `eclipse-formatter.xml` code formatting rules
- Use static analysis tools (CheckStyle, PMD, SonarLint)
- Maintain test coverage above 80% for critical business logic

**Database Configuration:**
- Always ensure **PostgreSQL-only** configuration for production
- Use `spring.jpa.defer-datasource-initialization=true`
- Update `data.sql` with correct sample and initial database values after any database change
- Password is always **test123** for all users with hash: `$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu`

---

## 3. Commenting Standards

- **Every class:** Document its role in MVC.
- **Every method:** Describe parameters, return value, and side effects.
- **Complex logic:** Inline comments explaining tricky parts.
- **Copilot suggestions:** Review and expand comments for clarity.

---


## 4. Security and Validation Standards

- Always validate all user inputs in controllers and services
- Use Spring Security for authentication and authorization
- Implement proper RBAC (Role-Based Access Control)
- Never store sensitive data in plain text
- Use parameterized queries to prevent SQL injection
- Implement CSRF protection for all forms


## 5. Comprehensive Testing Requirements

**Mandatory Testing Standards:**
- **Unit Tests**: Write unit tests for ALL business logic and service methods
- **Integration Tests**: Use TestContainers for integration testing with PostgreSQL
- **Field Naming Tests**: ALWAYS verify field naming conventions match repository method expectations
- **Repository Tests**: Test all `findBy` methods to ensure they work with actual field names
- **Form Generation Tests**: Test `@MetaData` annotations generate forms correctly
- **Validation Tests**: Test all validation scenarios and edge cases
- **Exception Handling Tests**: Test graceful exception handling in UI components
- **Null Safety Tests**: Test all methods with null inputs to prevent `NullPointerException`

**Testing Coverage Requirements:**
- Maintain test coverage above **80%** for critical business logic
- Test all CRUD operations for entities
- Test all form field bindings and validation
- Test all ComboBox data providers work correctly
- Test lazy loading scenarios and `@Transactional` boundaries
- Test user authorization and access control

**Testing Patterns:**
```java
// Example: Field naming compatibility test
@Test
void testRepositoryMethodMatchesFieldName() {
    // Given: An entity with camelCase field naming
    CCommentPriority priority = new CCommentPriority();
    priority.setPriorityLevel(1);
    
    // When: Saving and querying by field name
    priorityRepository.save(priority);
    Optional<CCommentPriority> found = priorityRepository.findByPriorityLevel(1);
    
    // Then: Repository method should work without Hibernate errors
    assertTrue(found.isPresent());
    assertEquals(1, found.get().getPriorityLevel());
}

// Example: Form generation test
@Test
void testMetaDataFormGeneration() {
    // Test that @MetaData annotations properly generate form fields
    Component form = CEntityFormBuilder.buildForm(CActivity.class, binder, fieldList);
    assertNotNull(form);
    // Verify form contains expected fields with correct labels
}
```

**Manual Testing Requirements:**
- Test all UI workflows end-to-end after any field or entity changes
- Verify ComboBox selections work correctly with data providers
- Test form validation messages display appropriately
- Verify all dialog interactions work as expected
- Test accessibility and responsive design
- Mock external dependencies appropriately
- Include manual verification tests for complex UI interactions

---

## 6. Java Build and Quality Checks Before Submission

**Mandatory Pre-Submission Checks:**
- Compile using `mvn clean install` and ensure no build errors
- Run static analysis: CheckStyle, PMD, or SonarLint
- Verify **ALL field names follow camelCase convention** to prevent Hibernate query errors
- Run unit and integration tests with coverage above 80%
- Confirm no business logic is present in View classes (proper MVC separation)
- Check for unused imports and variables
- Validate all forms of input in controllers and services
- Test all repository methods work with actual field names (no `findByPriorityLevel` vs `PriorityLevel` mismatches)

## 7. Pull Request Checklist

**Code Quality and Architecture:**
- [ ] Code follows MVC separation principles
- [ ] All field names use proper camelCase convention (critical for Hibernate compatibility)
- [ ] Methods and classes are properly commented with JavaDoc
- [ ] All Copilot-generated code has been reviewed and tested
- [ ] All Java quality checks passed: compilation, static analysis, testing
- [ ] No hardcoded values in controllers or views
- [ ] UI logic (Vaadin) does not leak into model/service layers
- [ ] Database changes include proper migrations and sample data

**Testing Requirements:**
- [ ] Unit tests written for all business logic and service methods
- [ ] Repository method naming verified against actual field names
- [ ] Form generation tests pass with `@MetaData` annotations
- [ ] Integration tests pass with TestContainers and PostgreSQL
- [ ] Test coverage above 80% for critical business logic
- [ ] All validation scenarios and edge cases tested

## 8. Copilot Review Protocol

After accepting Copilot suggestions, manually review for:
- **Field naming compliance**: Ensure all fields follow camelCase to prevent repository method failures
- Correct MVC placement and architecture compliance
- Security considerations (input validation, access control)
- Performance and scalability implications
- Proper exception handling and error messages
- Code quality and maintainability standards
- Repository method compatibility with actual field names

---

## 9. Coding Styles & Best Practices

**Field and Variable Naming Conventions:**
- **CRITICAL**: All private fields MUST follow Java camelCase naming conventions
- Field names MUST start with a lowercase letter (e.g., `priorityLevel`, NOT `PriorityLevel`)
- Use descriptive field names that clearly indicate purpose (e.g., `entityFields`, NOT `EntityFields`)
- Constants should be UPPER_CASE with underscores (e.g., `MAX_FILE_SIZE`, `LOGGER`)
- Repository method names must match field names exactly (e.g., `findByPriorityLevel()` requires field `priorityLevel`)
- **Before submitting ANY code**: verify all field names follow camelCase convention to prevent Hibernate query failures

**General Principles:**
- Use `final` keyword wherever possible (variables, parameters, methods, classes)
- Favor abstraction: if two or more features are similar, create an abstract base class with abstract fields and methods
- Always start class names with a capital "C" (e.g., `CUser`, `CSettings`). Do not use standard Java class naming for domain classes
- Check for lazy loading issues using best practices (e.g., `@Transactional`, `fetch = FetchType.LAZY`). Add comments explaining lazy loading risks or solutions
- Always check for `null` values and possible `NullPointerException` in every function. If necessary, also check for empty strings
- Always prefer using base classes to avoid code duplication
- Every important function must start with a logger statement detailing function name and parameter values (do not log at function end)
- Always catch exceptions in Vaadin UI and handle them gracefully
- Use Vaadin UI components to display exceptions to users where possible
- Always use `MetaData.java` annotation system to bind and generate forms for entities when possible
- Extend base classes or add new ones to increase modularity and maintainability
- Always use appropriate icons for views and UI components
- For delete operations, always require explicit user approval (e.g., confirmation dialog)
- All selective ComboBoxes must be **selection only** (user must not be able to type arbitrary text)
- Keep project documentation updated in the `docs` folder; create separate documents for each design concept. If implementing complex solutions (e.g., Spring technologies), create a step-by-step solution document

**Concrete Implementation Examples:**

**Domain Class Pattern:**
```java
@Entity
@Table(name = "cactivity")
@AttributeOverride(name = "id", column = @Column(name = "activity_id"))
public class CActivity extends CEntityOfProject {
    
    @MetaData(
        displayName = "Activity Type", required = false, readOnly = false,
        description = "Type category of the activity", hidden = false, order = 2,
        dataProviderBean = "CActivityTypeService"
    )
    private CActivityType activityType;
    // ... more fields with MetaData annotations
}
```

**Service Class Pattern:**
```java
public class CActivityService extends CEntityOfProjectService<CActivity> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CActivityService.class);
    
    public CActivity createActivity(final CActivity activity) {
        LOGGER.info("createActivity called with activity: {}", activity);
        // Always log method entry with parameters
        // ... implementation
    }
}
```

**UI Component Pattern:**
```java
// Always use base classes from abstracts/views package
public class CActivityView extends CProjectAwareMDPage<CActivity> {
    
    @Override
    protected void setupButtons() {
        // Never use standard Vaadin Button - always use CButton
        final CButton saveButton = CButton.createPrimary("Save", this::saveActivity);
        final CButton cancelButton = CButton.createSecondary("Cancel", this::cancel);
    }
}
```

- Use the `abstracts/views` package for project-wide UI components (e.g., `CButton`, `CGrid`, `CDialog`). Always use these superclasses and extend them as needed; add new UI classes here
- Whenever a button action is not applicable (e.g., user not selected but project add requested), do not silently return. Show a user warning explaining the situation. Use new dialog classes:
  - `CWarningDialog` (located at `src/main/java/tech/derbent/base/ui/dialogs/CWarningDialog.java`)
  - `CInformationDialog` (located at `src/main/java/tech/derbent/base/ui/dialogs/CInformationDialog.java`)
  - `CExceptionDialog` (located at `src/main/java/tech/derbent/base/ui/dialogs/CExceptionDialog.java`)
  - These dialogs must be simple, visually appealing, and inherit from the common `CDialog` superclass
- Never add loggers at the end of functions. Always log at the start with full detail about the function and parameters
- All code must follow Java naming conventions for variables and methods, except for class names which must start with "C"

## 9.1. MetaData Annotation System

This project uses a sophisticated **@MetaData** annotation system for automatic form generation and field configuration. This is a key architectural pattern that must be followed:

**MetaData Annotation Pattern:**
```java
@MetaData(
    displayName = "User Name",           // UI label
    required = true,                     // Validation requirement
    readOnly = false,                    // Field editability
    defaultValue = "",                   // Default field value
    description = "User's first name",   // Tooltip/help text
    hidden = false,                      // Visibility control
    order = 1,                          // Display order in forms
    maxLength = CEntityConstants.MAX_LENGTH_NAME,        // Field length constraints
    dataProviderBean = "CUserService"   // For ComboBox data source
)
private String name;
```

**Real Examples from the Codebase:**
```java
// From CActivity.java - Complex relationship with data provider
@MetaData(
    displayName = "Activity Type", required = false, readOnly = false,
    description = "Type category of the activity", hidden = false, order = 2,
    dataProviderBean = "CActivityTypeService"
)
private CActivityType activityType;

// From CUser.java - Simple string field with validation
@MetaData(
    displayName = "User Name", required = true, readOnly = false,
    defaultValue = "", description = "User's first name", hidden = false,
    order = 1, maxLength = CEntityConstants.MAX_LENGTH_NAME
)
private String name;
```

**Benefits of MetaData System:**
- Automatic form generation using `CEntityFormBuilder`
- Consistent validation across the application
- Centralized field configuration
- Automatic ComboBox data provider binding
- Consistent UI labeling and ordering

**Usage Guidelines:**
- Always use MetaData for all entity fields that appear in UI forms
- Specify appropriate `dataProviderBean` for relationship fields
- Use meaningful `displayName` values for user-friendly labels
- Set proper `order` values to control form field sequence
- Include helpful `description` text for complex fields

## 9.2. CSS Guidelines

**File Organization:**
- Main styles: `src/main/frontend/themes/default/styles.css`
- Login styles: `src/main/frontend/themes/default/dev-login.css`
- Background styles: `src/main/frontend/themes/default/login-background.css`

**CSS Best Practices:**
- Always use very simple CSS. Don't use JavaScript in Java or CSS
- Use CSS built-in simple functions and Vaadin Lumo design tokens
- Update CSS class names based on component class names, update CSS file accordingly
- Use Vaadin Shadow DOM parts for component styling:
  ```css
  #vaadinLoginOverlayWrapper::part(overlay) {
      background-image: url('./images/background1.png');
  }
  ```

**Real CSS Examples from the Project:**
```css
/* Dashboard view styling */
.cdashboard-view {
    width: 100%;
    padding: var(--lumo-space-m);
    background: var(--lumo-base-color);
}

/* Detailed tab layout with background */
.details-tab-layout {
    background-color: #fff7e9;
    width: 99%;
    border-radius: 12px;
    border: 1px solid var(--lumo-contrast-10pct);
    font-size: 1.6em;
    font-weight: bold;
}

/* Accordion styling */
vaadin-accordion-heading {
    font-size: var(--lumo-font-size-m);
    font-weight: 600;
    color: #5d6069;
    font-size: 1.3em;
    border-bottom: 1px solid var(--lumo-contrast-10pct);
}
```

**Entity Panel Pattern Implementation:**
- Follow the pattern of `CPanelActivityDescription` and its superclasses
- Use this pattern to group each entity's fields according to related business topics
- Don't leave any field out - create comprehensive panels for all entity aspects
- Create necessary classes and base classes with consistent naming conventions
- Only open the first panel by default (call `openPanel()` in constructor)

**Example Panel Implementation:**
```java
public class CPanelActivityDescription extends CPanelActivityBase {
    public CPanelActivityDescription(CActivity currentEntity,
        BeanValidationBinder<CActivity> binder, CActivityService service) {
        super("Basic Information", currentEntity, binder, service);
        openPanel(); // Only open this panel by default
    }
    
    @Override
    protected void updatePanelEntityFields() {
        // Group related fields logically
        setEntityFields(List.of("name", "description", "activityType", "project"));
    }
}
```

**Panel Organization Guidelines:**
- Create separate panels for different business aspects:
  - `CPanelActivityDescription` - Basic information
  - `CPanelActivityStatusPriority` - Status and priority management
  - `CPanelActivityTimeTracking` - Time and progress tracking
  - `CPanelActivityBudgetManagement` - Cost and budget information
  - `CPanelActivityResourceManagement` - User assignments and resources

## 9.5. Architecture Patterns and Best Practices

**Entity Hierarchy Pattern:**
The project uses a sophisticated inheritance hierarchy for domain entities:
```java
CEntityBase (id, createdBy, createdDate, lastModified)
├── CEntityNamed (name, description) 
│   ├── CUser, CProject, CActivityType, CActivityStatus
│   └── CCompany, CMeetingType, CUserType
└── CEntityOfProject (extends CEntityBase + project relationship)
    ├── CActivity (comprehensive activity management)
    ├── CMeeting (meeting management)
    └── CRisk (risk management)
```

**Service Layer Pattern:**
```java
// Base service with CRUD operations
public abstract class CEntityService<T extends CEntityBase> {
    protected abstract CEntityRepository<T> getRepository();
    // Common CRUD methods with logging
}

// Project-aware service for entities linked to projects
public abstract class CEntityOfProjectService<T extends CEntityOfProject> 
    extends CEntityService<T> {
    // Project-specific operations
}

// Concrete implementation
public class CActivityService extends CEntityOfProjectService<CActivity> {
    // Activity-specific business logic
}
```

**UI Component Hierarchy:**
```java
CAbstractPage (base page functionality)
├── CAbstractMDPage (metadata-driven pages)
│   ├── CProjectAwareMDPage (project context awareness)
│   │   └── CActivitiesView, CMeetingsView
│   └── CCustomizedMDPage (custom behavior)
└── CDialog (base dialog functionality)
    └── CWarningDialog, CInformationDialog, CExceptionDialog
```

**Form Building Pattern:**
- Use `@MetaData` annotations on entity fields
- Automatic form generation via reflection
- Consistent validation and data binding
- ComboBox data providers via service beans
- Panel-based organization for complex entities

**Logging Standards:**
```java
public class CActivityService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CActivityService.class);
    
    public CActivity save(CActivity activity) {
        LOGGER.info("save called with activity: {}", activity);
        // Implementation follows
    }
}
```

**Security and Validation:**
- Always validate inputs in service layer
- Use Spring Security for authentication/authorization
- Implement proper null checking in all methods
- Use `@Transactional` for data consistency
- Handle exceptions gracefully with user-friendly dialogs

**Testing Standards:**
The project includes **41 comprehensive test classes** covering:
```java
// Unit tests for domain logic
class CActivityCardTest {
    @Test
    void testActivityCardCreation() {
        final CProject project = new CProject();
        project.setName("Test Project");
        final CActivity activity = new CActivity("Test Activity", project);
        
        final CActivityCard card = new CActivityCard(activity);
        
        assertNotNull(card);
        assertEquals(activity, card.getActivity());
    }
}

// Integration tests for services
@SpringBootTest
class SessionServiceProjectChangeTest {
    // Test service layer integration
}

// Manual verification tests for complex UI components
class ManualVerificationTest {
    // UI component validation tests
}
```

**Testing Guidelines:**
- Write unit tests for all business logic and service methods
- Use TestContainers for integration testing with PostgreSQL
- Maintain test coverage above 80% for critical business logic
- Test all validation scenarios and edge cases
- Mock external dependencies appropriately
- Include manual verification tests for complex UI interactions
## 9.6. Database Rules and Sample Data

- Password is always test123 for all users with hash code '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu'
- Every entity should have an example in data.sql for per project, per company per user per activity etc...
- Always ensure **PostgreSQL-only** configuration. Update `data.sql` with correct sample and initial database values after any database change
- Keep spring.jpa.defer-datasource-initialization=true
- Don't use memory database H2 in production
- Always delete tables at top of data.sql before you insert values into it. Check them before they exist
- Delete constraints etc. if there is change in the DB structure
- Always reset sequences in the format and escape characters below with a conditional to check if they exist first:
  ```sql
  DO '
  BEGIN
      IF EXISTS (
          SELECT 1
          FROM pg_class c
          JOIN pg_namespace n ON n.oid = c.relnamespace
          WHERE c.relkind = ''S'' AND c.relname = ''ctask_task_id_seq''
      ) THEN
          EXECUTE ''SELECT setval(''''ctask_task_id_seq'''', 1, false)'';
      END IF;
  END;
  ';
  ```

## 9.7. Documentation & Modularity
- Update the `docs` folder for every significant change:
  - Add new documentation for each project design concept (one file per concept)
  - For complex or Spring-based solutions, create a step-by-step solution file
- All new UI components, dialogs, and base classes must be documented
- Maintain consistency with existing documentation patterns

### Available Documentation:
- [Project Design Description](./project_design_description.md) - Overall project scope and requirements
- [Enhanced Activity Management](./enhanced-activity-management-implementation.md) - Activity system implementation
- [CSS Guidelines](./CSS_GUIDELINES.md) - Styling standards and best practices
- [CPanelActivityDescription Implementation](./CPANEL_ACTIVITY_DESCRIPTION_IMPLEMENTATION.md) - Entity panel patterns
- [User Profile Dialog Implementation](./user-profile-dialog-implementation.md) - Dialog implementation patterns
- [Annotation-Based ComboBox Data Providers](./ANNOTATION_BASED_COMBOBOX_DATA_PROVIDERS.md) - Form building patterns
- [Hierarchical Side Menu Implementation](./HIERARCHICAL_SIDE_MENU_IMPLEMENTATION.md) - Navigation patterns


## 10. Strict Prohibitions

- **Never use standard Vaadin `Button` directly!**  
  Immediately refactor to use `CButton` (or its improved/extended version).  
  If `CButton` can be improved, extend it so all buttons share consistent behavior and appearance
- Never bypass base classes for UI components. Always use or extend base classes from `abstracts/view`
- Never silently fail on user actions. Always notify the user with an appropriate dialog
- Never allow editable text in selection-only ComboBoxes
- Never use H2 database in production environments
- Never log sensitive information (passwords, tokens, personal data)
- Never commit hardcoded configuration values or secrets
## 11. Additional Best Practices
- Use dependency injection wherever possible
- All domain classes should be immutable where practical
- Always write unit tests for new features or bug fixes
- Always use proper JavaDoc for public classes and methods
- Ensure all database changes are backward compatible and thoroughly tested
- Use enums for all constant lists and types
- Ensure all UI components are accessible (a11y) and responsive
- Follow SOLID principles in class design
- Implement proper logging with appropriate log levels
- Use transactions appropriately for data consistency

---

## Related Documentation

For comprehensive development guidance, refer to these additional documents:

### Available Documentation in `/docs` folder:
- [Auxiliary Service Methods Usage](./docs/auxiliary-service-methods-usage.md) - Helper methods and utilities

### Documentation to be created as needed:
- Project Design Description - Complete project requirements and architecture
- Enhanced Activity Management Requirements - Activity system specifications  
- CSS Guidelines - Complete styling standards and Vaadin theming
- Entity Panel Implementation Patterns - Panel organization patterns
- Dialog Implementation Patterns - Standard dialog patterns
- Navigation Implementation Guide - Menu and routing patterns
- Form Building with Annotations - MetaData annotation usage
- JPA Performance Optimization - Lazy loading best practices
- Multi-tenant Patterns - Company association patterns
- Activity Domain Implementation - Activity enhancement patterns

**Note**: Create documentation files in the `/docs` folder as you implement new features or fix complex issues. Each major architectural pattern should have its own documentation file.

---

> **How to use with GitHub Copilot and PRs:**  
> - This file exists in your `docs` folder and should also be copied to project root.  
> - Link to this file in your README and in your PR/issue templates.
> - Remind contributors (and Copilot) to always follow these rules in every code review and commit.
> - Reference the related documentation above for specific implementation patterns.

