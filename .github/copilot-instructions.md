# Derbent Project Management Application
Derbent is a Java Spring Boot + Vaadin collaborative project management application inspired by Jira and ProjeQtOr, targeting small to medium-sized offices. Built with Java 17, Spring Boot 3.5, Vaadin 24.8, and comprehensive testing infrastructure.

**ALWAYS reference these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the info here.**

## Working Effectively

### Bootstrap, Build, and Test the Repository
```bash
# Prerequisites: Java 17+ and Maven 3.9+ are required
java -version    # Should show Java 17+
mvn -version     # Should show Maven 3.9+

# Clean and compile (NEVER CANCEL: takes 15+ seconds after first build)
mvn clean compile
# TIMEOUT: Set 10+ minutes. Expected time: 12-15 seconds for incremental builds

# Test compilation (NEVER CANCEL: takes 10+ seconds)  
mvn test-compile
# TIMEOUT: Set 5+ minutes. Expected time: 10-15 seconds

# Apply code formatting (required before commits)
mvn spotless:apply
# TIMEOUT: Set 2+ minutes. Expected time: 5 seconds

# Check code formatting
mvn spotless:check
# TIMEOUT: Set 2+ minutes. Expected time: 2-3 seconds
```

### Run the Application
```bash
# ALWAYS apply formatting first before running
mvn spotless:apply

# Start the application (NEVER CANCEL: takes 15+ seconds)
# NOTE: The default configuration uses PostgreSQL. For development without PostgreSQL:
mvn spring-boot:run -Ph2-local-development -Dspring-boot.run.profiles=h2
# TIMEOUT: Set 5+ minutes. Expected time: 12-15 seconds to start
# Application will be available at http://localhost:8080

# Alternative: Use Maven profile for H2 database
mvn spring-boot:run -Ph2-local-development

# Stop the application
# Use Ctrl+C or: pkill -f "spring-boot:run"
```

### Testing Infrastructure  
```bash
# Run unit tests (NEVER CANCEL: takes 90+ seconds, some failures expected)
mvn test -Dtest="**/unit_tests/**/*Test"
# TIMEOUT: Set 10+ minutes. Expected time: 1-2 minutes
# Note: Some test failures are expected in development environment

# Run Playwright UI automation tests with screenshots (NEVER CANCEL: takes 37+ seconds)
./run-playwright-tests.sh mock
# TIMEOUT: Set 5+ minutes. Expected time: 37-40 seconds
# Generates 84 screenshots in target/screenshots/

# Run comprehensive Playwright tests (NEVER CANCEL: takes 2+ minutes)
./run-playwright-tests.sh comprehensive
# TIMEOUT: Set 10+ minutes. Expected time: 2-5 minutes

# Run specific Playwright test categories
./run-playwright-tests.sh status-types    # Status and type views
./run-playwright-tests.sh main-views      # Main business views  
./run-playwright-tests.sh admin-views     # Administrative views
./run-playwright-tests.sh kanban-views    # Kanban board views

# Clean test artifacts
./run-playwright-tests.sh clean
```

## Validation

### ALWAYS run through complete validation scenarios after making changes:

#### 1. Build and Format Validation
```bash
# CRITICAL: Always run these in sequence before committing
mvn spotless:apply                    # Fix formatting issues
mvn spotless:check                    # Verify formatting is correct
mvn clean compile                     # Full build (NEVER CANCEL: 12-15 seconds)
```

#### 2. Application Startup Validation  
```bash
# Start application and verify it loads (use H2 profile for development)
mvn spring-boot:run -Ph2-local-development -Dspring-boot.run.profiles=h2 &
APP_PID=$!

# Wait for startup (12-15 seconds)
sleep 20

# Test application is responding  
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/
# Expected: 302 (redirect to login page)

# Stop application
kill $APP_PID
```

#### 3. UI Automation Validation
```bash
# ALWAYS test UI changes with Playwright screenshots
./run-playwright-tests.sh mock
# Expected: 84 screenshots generated successfully in 37-40 seconds
# Check target/screenshots/ for generated images

# For specific UI changes, run targeted tests
./run-playwright-tests.sh [category]  # See categories above
```

#### 4. Manual Validation Scenarios
When making UI or business logic changes, ALWAYS manually test these workflows:

**Login Flow:**
1. Start application: `mvn spring-boot:run -Ph2-local-development -Dspring-boot.run.profiles=h2`
2. Navigate to http://localhost:8080
3. Verify login page displays
4. Test login with sample data (users created automatically)

**Core Navigation:**
1. Test main menu navigation between views
2. Verify project selection works
3. Test creating/editing activities, meetings, users
4. Verify kanban board functionality

**Data Operations:**
1. Create new project
2. Add activities to project  
3. Assign users to activities
4. Test time tracking functionality
5. Verify all changes persist

## Build Configuration and Timing

### Maven Build Phases and Expected Times:
- **clean**: 2-5 seconds
- **compile**: 12-15 seconds (incremental builds after first compile)
- **test-compile**: 10-15 seconds
- **test**: 1-2 minutes (with failures expected)
- **spotless:apply**: ~5 seconds
- **spotless:check**: ~2-3 seconds
- **spring-boot:run**: 12-15 seconds to start

### CRITICAL Timeout Settings:
- **Build commands**: MINIMUM 5 minutes timeout
- **Test commands**: MINIMUM 10 minutes timeout  
- **Playwright tests**: MINIMUM 5 minutes timeout
- **Application startup**: MINIMUM 2 minutes timeout

### NEVER CANCEL warnings:
- **mvn clean compile** - Takes 12-15 seconds for incremental builds, DO NOT CANCEL
- **mvn test** - Takes 90+ seconds with expected failures, DO NOT CANCEL  
- **./run-playwright-tests.sh** - Takes 37+ seconds minimum, DO NOT CANCEL
- **mvn spring-boot:run** - Takes 12-15 seconds to start, DO NOT CANCEL

## Key Development Patterns

### Project Structure
```
src/main/java/tech/derbent/
├── abstracts/           # Base classes, annotations, utilities
├── activities/          # Activity management (CRITICAL feature)
├── companies/           # Company management  
├── meetings/            # Meeting management
├── projects/            # Project management (CRITICAL feature)
├── users/              # User management (CRITICAL feature)
├── setup/              # System settings
└── administration/     # Company settings

docs/                   # Comprehensive documentation
├── architecture/       # Design patterns and coding standards
├── guides/            # Component and development guides  
├── implementation/    # Feature implementation patterns
└── testing/           # Testing strategies
```

### Coding Standards (CRITICAL - Follow Strictly)
- **ALL domain classes MUST be prefixed with "C"** (e.g., CActivity, CUser, CProject)
- **Follow MVC pattern**: Model (domain), View (UI), Controller (service)
- **Always use CAbstractService** as base for service classes
- **Entity classes extend CEntityDB<T>**
- **Repository classes extend CAbstractRepository<T>**
- **View classes extend appropriate CAbstract*Page**

### Database Configuration
- **Development**: H2 in-memory database (requires both Maven profile and Spring profile)
  ```bash
  mvn spring-boot:run -Ph2-local-development -Dspring-boot.run.profiles=h2
  ```
- **Production**: PostgreSQL (requires manual setup and database server)
- **Schema**: Hibernate auto-creates tables with sample data
- **Sample Data**: Automatically loaded on startup via CSampleDataInitializer

### Testing Strategy
- **Unit Tests**: Located in `src/test/java/unit_tests/`
- **UI Tests**: Playwright automation in `src/test/java/automated_tests/`
- **Integration Tests**: Spring Boot with TestContainers
- **Manual Tests**: Documented scenarios in test classes

## Common Tasks

### Adding New Entities
1. Create domain class extending `CEntityDB<T>` in appropriate package
2. Create repository interface extending `CAbstractRepository<T>`
3. Create service class extending `CAbstractService<T>`
4. Create view class extending appropriate `CAbstract*Page`
5. Add navigation entry in `MainLayout.java`
6. Create unit tests following existing patterns
7. Add Playwright tests for UI validation

### Debugging Issues
```bash
# Check application logs during startup (use H2 profile for development)
mvn spring-boot:run -Ph2-local-development -Dspring-boot.run.profiles=h2 | grep -E "(ERROR|WARN|DEBUG)"

# Validate database connectivity - H2 console available at:
# http://localhost:8080/h2-console (when using H2 profile)
# URL: jdbc:h2:mem:testdb, User: sa, Password: (empty)

# Check test failures
mvn test 2>&1 | grep -A 5 -B 5 "FAILURE\|ERROR"

# Generate test reports
mvn test jacoco:report
# Reports in target/site/jacoco/
```

### Code Quality Checks
```bash
# ALWAYS run before committing:
mvn spotless:apply      # Fix formatting
mvn spotless:check      # Verify formatting
mvn clean compile      # Full build verification (NEVER CANCEL: 12-15 seconds)

# Optional quality checks:
mvn test               # Run tests (some failures expected)
./run-playwright-tests.sh mock  # UI validation (37-40 seconds)
```

## Technology Stack Reference

### Core Technologies
- **Java 17** - Programming language
- **Spring Boot 3.5** - Application framework  
- **Vaadin Flow 24.8** - UI framework
- **Hibernate/JPA** - Data persistence
- **H2 Database** - Development database
- **PostgreSQL** - Production database
- **Maven 3.9+** - Build tool

### Testing Technologies
- **JUnit 5** - Unit testing framework
- **Mockito** - Mocking framework
- **TestContainers** - Integration testing
- **Playwright** - Browser automation testing
- **Selenium WebDriver** - Alternative browser testing

### Development Tools
- **Spotless** - Code formatting (Eclipse formatter)
- **Spring DevTools** - Hot reload in development
- **Vaadin Control Center** - Development tools
- **Jacoco** - Code coverage reporting

## Common Failures and Solutions

### Build Failures
- **"Unable to determine Dialect"**: Database configuration issue, ensure H2 profile is active
- **Spotless violations**: Run `mvn spotless:apply` to fix formatting
- **Compilation errors**: Usually due to missing imports or incorrect generics

### Test Failures  
- **Spring context failures**: Expected in development, focus on new test failures only
- **Playwright browser issues**: Use `./run-playwright-tests.sh mock` for screenshot testing
- **Timeout errors**: Increase timeout values, builds take 12-15 seconds (incremental)

### Application Startup Issues
- **"Unable to determine Dialect"**: Database configuration issue, use H2 profile: `mvn spring-boot:run -Ph2-local-development -Dspring-boot.run.profiles=h2`
- **Port 8080 in use**: Kill existing process with `pkill -f spring-boot:run`
- **Database errors**: For development, use H2 profile to avoid PostgreSQL dependency
- **Vaadin compilation**: Delete `target/` and rebuild with `mvn clean compile`

## File Locations Reference

### Key Configuration Files
- `pom.xml` - Maven configuration and dependencies
- `src/main/resources/application.properties` - Application configuration
- `src/main/resources/application-h2.properties` - H2 database configuration
- `eclipse-formatter.xml` - Code formatting configuration
- `.prettierrc.json` - TypeScript/JavaScript formatting

### Important Documentation  
- `docs/architecture/coding-standards.md` - CRITICAL coding guidelines
- `docs/testing/comprehensive-testing-guide.md` - Testing strategies
- `docs/guides/enhanced-binder-guide.md` - Form binding patterns
- `README.md` - Project overview and quick start

### Build and Test Scripts
- `run-playwright-tests.sh` - Playwright UI automation
- `run-ui-tests.sh` - Dual framework UI testing  
- `generate-test-report.sh` - Test report generation
- `mvnw` / `mvnw.cmd` - Maven wrapper scripts

---

**Remember**: Always follow the coding standards in `docs/architecture/coding-standards.md` and validate ALL changes with the build and test procedures documented above. When in doubt, refer to existing patterns in the codebase and comprehensive documentation in the `docs/` directory.
