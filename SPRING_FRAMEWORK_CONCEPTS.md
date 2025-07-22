# Spring Framework Concepts in Derbent Project

This document provides a comprehensive guide to the Spring Framework concepts used in the Derbent project, with real code examples from the codebase.

## Table of Contents
- [Bean Annotations](#bean-annotations)
- [Services](#services) 
- [Configuration](#configuration)
- [Entities](#entities)
- [Repositories](#repositories)
- [Dependency Injection](#dependency-injection)
- [Security](#security)
- [Transaction Management](#transaction-management)
- [Application Context](#application-context)
- [Data Provider System](#data-provider-system)

---

## Bean Annotations

Spring uses annotations to mark classes as managed components (beans) that are automatically detected and registered in the application context.

### @Service
Marks service layer components that contain business logic.

```java
// From: CActivityTypeService.java
@Service
@PreAuthorize("isAuthenticated()")
@Transactional(readOnly = true)
public class CActivityTypeService extends CAbstractService<CActivityType> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CActivityTypeService.class);

    /**
     * Constructor injection - Spring automatically injects dependencies
     */
    CActivityTypeService(final CActivityTypeRepository repository, final Clock clock) {
        super(repository, clock);
        LOGGER.info("CActivityTypeService initialized");
    }
}
```

### @Configuration
Marks classes that define bean configurations and application setup.

```java
// From: CSecurityConfig.java
@EnableWebSecurity
@Configuration
class CSecurityConfig extends VaadinWebSecurity {
    
    private final CUserService loginUserService;

    public CSecurityConfig(final CUserService loginUserService) {
        this.loginUserService = loginUserService;
    }
    
    /**
     * Bean definition method - Spring manages this as a singleton
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### @Bean
Used in configuration classes to explicitly define beans that Spring should manage.

```java
// From: Application.java
@SpringBootApplication
@Theme("default")
public class Application implements AppShellConfigurator {
    
    /**
     * Clock bean for consistent time handling across the application
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    /**
     * Application initialization bean that runs after startup
     */
    @Bean
    public ApplicationRunner dataInitializer(final JdbcTemplate jdbcTemplate) {
        return args -> {
            final Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM cuser", Integer.class);
            if ((count != null) && (count == 0)) {
                final String sql = StreamUtils.copyToString(
                    new ClassPathResource("data.sql").getInputStream(),
                    StandardCharsets.UTF_8);
                jdbcTemplate.execute(sql);
            }
        };
    }
}
```

---

## Services

Services encapsulate business logic and serve as the intermediary between controllers and repositories.

### Service Layer Architecture

```java
// From: CActivityService.java
@Service
@PreAuthorize("isAuthenticated()")  // Security at method level
public class CActivityService extends CAbstractService<CActivity> {

    /**
     * Constructor injection - no @Autowired needed in modern Spring
     */
    CActivityService(final CActivityRepository repository, final Clock clock) {
        super(repository, clock);
    }

    /**
     * Business logic method with transaction management
     */
    @Transactional
    public void createEntity(final String name) {
        if ("fail".equals(name)) {
            throw new RuntimeException("This is for testing the error handler");
        }
        final var entity = new CActivity();
        entity.setName(name);
        repository.saveAndFlush(entity);
    }

    /**
     * Custom query method delegating to repository
     */
    public List<CActivity> findByProject(final CProject project) {
        return ((CActivityRepository) repository).findByProject(project);
    }
}
```

### Abstract Service Pattern

```java
// From: CAbstractService.java
public abstract class CAbstractService<EntityClass extends CEntityDB> {

    protected final Clock clock;
    protected final CAbstractRepository<EntityClass> repository;
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    /**
     * Common service functionality shared across all entity services
     */
    public CAbstractService(final CAbstractRepository<EntityClass> repository, final Clock clock) {
        this.clock = clock;
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<EntityClass> get(final Long id) {
        LOGGER.debug("Getting entity by ID: {}", id);
        final Optional<EntityClass> entity = repository.findById(id);
        entity.ifPresent(this::initializeLazyFields);
        return entity;
    }
}
```

---

## Configuration

Configuration classes set up application-wide settings, security, and custom beans.

### Security Configuration

```java
// From: CSecurityConfig.java
@EnableWebSecurity
@Configuration
class CSecurityConfig extends VaadinWebSecurity {

    private final CUserService loginUserService;

    /**
     * Constructor injection of services needed for security
     */
    public CSecurityConfig(final CUserService loginUserService) {
        this.loginUserService = loginUserService;
    }

    /**
     * HTTP security configuration
     */
    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        super.configure(http);  // Apply Vaadin defaults
        setLoginView(http, CLoginView.class);
        http.userDetailsService(loginUserService);
    }

    /**
     * Password encoder bean for secure password hashing
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Expose UserDetailsService as a bean
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return loginUserService;
    }
}
```

### Main Application Configuration

```java
// From: Application.java
@SpringBootApplication  // Combines @Configuration, @EnableAutoConfiguration, @ComponentScan
@Theme("default")
public class Application implements AppShellConfigurator {
    
    public static void main(final String[] args) {
        final SpringApplication app = new SpringApplication(Application.class);
        app.run(args);
    }
}
```

---

## Entities

JPA entities represent database tables and use Spring Data JPA for persistence.

### Entity Definition

```java
// From: CActivity.java
@Entity
@Table(name = "cactivity")
@AttributeOverride(name = "id", column = @Column(name = "activity_id"))
public class CActivity extends CEntityOfProject {

    /**
     * Many-to-one relationship with lazy loading
     * Enhanced with metadata annotation for form generation
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cactivitytype_id", nullable = true)
    @MetaData(
        displayName = "Activity Type", 
        required = false, 
        readOnly = false, 
        description = "Type category of the activity", 
        order = 2,
        dataProviderBean = "CActivityTypeService"  // Spring integration
    )
    private CActivityType activityType;
}
```

### Entity Inheritance Hierarchy

```java
// From: CEntityDB.java - Base entity class
@MappedSuperclass
public abstract class CEntityDB extends CEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Proper equals implementation for JPA entities
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CEntityDB)) return false;
        
        final CEntityDB other = (CEntityDB) obj;
        final Long id = getId();
        return (id != null) && id.equals(other.getId());
    }
}
```

### Type Entity with Validation

```java
// From: CActivityType.java
@Entity
@Table(name = "cactivitytype")
@AttributeOverride(name = "id", column = @Column(name = "cactivitytype_id"))
public class CActivityType extends CTypeEntity {

    @Column(name = "name", nullable = false, length = MAX_LENGTH_NAME, unique = true)
    @Size(max = MAX_LENGTH_NAME)  // Bean Validation
    @MetaData(
        displayName = "Type Name", 
        required = true, 
        order = 1, 
        maxLength = MAX_LENGTH_NAME
    )
    private String name;

    @Column(name = "description", nullable = true, length = MAX_LENGTH_DESCRIPTION)
    @Size(max = MAX_LENGTH_DESCRIPTION)
    @MetaData(
        displayName = "Description", 
        required = false, 
        order = 2, 
        maxLength = MAX_LENGTH_DESCRIPTION
    )
    private String description;
}
```

---

## Repositories

Spring Data JPA repositories provide data access operations with minimal code.

### Repository Interface

```java
// From: CActivityTypeRepository.java
public interface CActivityTypeRepository extends CAbstractRepository<CActivityType> {
    // Spring Data JPA automatically implements basic CRUD operations
    // Additional custom queries can be added here if needed
}
```

### Abstract Repository Pattern

```java
// From: CAbstractRepository.java (referenced in services)
// This would typically extend JpaRepository<T, Long>
public interface CAbstractRepository<T extends CEntityDB> extends JpaRepository<T, Long> {
    // Common repository methods available to all entities
    // Spring Data JPA provides automatic implementation
}
```

### Custom Query Methods

```java
// From: CActivityRepository.java (inferred from service usage)
public interface CActivityRepository extends CAbstractRepository<CActivity> {
    
    /**
     * Custom query method - Spring generates implementation
     */
    List<CActivity> findByProject(CProject project);
    
    /**
     * Paginated version
     */
    Page<CActivity> findByProject(CProject project, Pageable pageable);
    
    /**
     * Custom query with eager loading to prevent LazyInitializationException
     */
    @Query("SELECT a FROM CActivity a LEFT JOIN FETCH a.activityType WHERE a.id = :id")
    Optional<CActivity> findByIdWithActivityType(@Param("id") Long id);
}
```

---

## Dependency Injection

Spring's IoC container manages object dependencies automatically.

### Constructor Injection (Recommended)

```java
// From: CDataProviderResolver.java
@Service
public final class CDataProviderResolver {
    
    private final ApplicationContext applicationContext;

    /**
     * Constructor injection - @Autowired is optional in single constructor scenarios
     */
    @Autowired
    public CDataProviderResolver(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
```

### Field Injection (Legacy Style)

```java
// Example of field injection (not recommended for new code)
@Service
public class ExampleService {
    
    @Autowired
    private SomeRepository repository;  // Field injection
    
    // Less testable and creates tight coupling
}
```

### Method Injection

```java
// Example of method injection in configuration
@Configuration
public class ExampleConfig {
    
    @Bean
    public SomeService someService(@Autowired SomeDependency dependency) {
        return new SomeService(dependency);
    }
}
```

---

## Security

Spring Security provides authentication, authorization, and security features.

### Method-Level Security

```java
// From: CActivityService.java
@Service
@PreAuthorize("isAuthenticated()")  // All methods require authentication
public class CActivityService extends CAbstractService<CActivity> {
    
    /**
     * Additional method-level security can be applied
     */
    @PreAuthorize("hasRole('ADMIN')")
    public void sensitiveOperation() {
        // Only users with ADMIN role can execute this
    }
}
```

### Security Configuration Flow

```java
// From: CSecurityConfig.java
@Configuration
@EnableWebSecurity
public class CSecurityConfig extends VaadinWebSecurity {
    
    /**
     * 1. User accesses protected resource
     * 2. Spring Security redirects to login if not authenticated
     * 3. CUserService validates credentials against database
     * 4. BCrypt encoder verifies password hash
     * 5. User is granted access based on roles
     */
    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        super.configure(http);
        setLoginView(http, CLoginView.class);
        http.userDetailsService(loginUserService);  // Custom user lookup
    }
}
```

---

## Transaction Management

Spring provides declarative transaction management with annotations.

### Service-Level Transactions

```java
// From: CActivityService.java
@Service
@Transactional(readOnly = true)  // Default read-only for all methods
public class CActivityService extends CAbstractService<CActivity> {

    /**
     * Write transaction overrides class-level read-only setting
     */
    @Transactional  // Read-write transaction
    public void createEntity(final String name) {
        final var entity = new CActivity();
        entity.setName(name);
        repository.saveAndFlush(entity);  // Flushes within transaction
    }

    /**
     * Explicit read-only transaction for complex queries
     */
    @Transactional(readOnly = true)
    public Optional<CActivity> getWithActivityType(final Long id) {
        return ((CActivityRepository) repository).findByIdWithActivityType(id);
    }
}
```

### Transaction Propagation

```java
// Examples of different transaction behaviors
@Service
public class TransactionExampleService {
    
    @Transactional(propagation = Propagation.REQUIRED)  // Default
    public void requiredTransaction() {
        // Joins existing transaction or creates new one
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void newTransaction() {
        // Always creates new transaction, suspending current one
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void rollbackOnAnyException() {
        // Rolls back on any exception, not just RuntimeException
    }
}
```

---

## Application Context

Spring's ApplicationContext manages beans and provides lookup capabilities.

### Dynamic Bean Resolution

```java
// From: CDataProviderResolver.java
@Service
public final class CDataProviderResolver {
    
    private final ApplicationContext applicationContext;

    public CDataProviderResolver(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Dynamic bean lookup by name
     */
    private <T> List<T> resolveDataFromBean(final Class<T> entityType, 
            final String beanName, final String methodName) {
        
        // Check if bean exists
        if (applicationContext.containsBean(beanName)) {
            // Get bean from context
            final Object serviceBean = applicationContext.getBean(beanName);
            return callDataMethod(serviceBean, methodName, entityType);
        }
        return Collections.emptyList();
    }

    /**
     * Dynamic bean lookup by type
     */
    private <T> List<T> resolveDataFromClass(final Class<T> entityType, 
            final Class<?> serviceClass, final String methodName) {
        
        try {
            // Get bean by type from Spring context
            final Object serviceBean = applicationContext.getBean(serviceClass);
            return callDataMethod(serviceBean, methodName, entityType);
        } catch (final Exception e) {
            LOGGER.warn("Bean of type '{}' not found", serviceClass.getSimpleName());
            return Collections.emptyList();
        }
    }
}
```

### Context Events

```java
// From: Application.java - Listening to application events
@SpringBootApplication
public class Application implements AppShellConfigurator {
    
    public static void main(final String[] args) {
        final SpringApplication app = new SpringApplication(Application.class);
        
        // Add event listener
        app.addListeners((final ApplicationReadyEvent event) -> {
            final long endTime = System.nanoTime();
            LOGGER.info("Application started in {} ms", 
                (endTime - startTime) / 1_000_000);
        });
        
        app.run(args);
    }
}
```

---

## Data Provider System

A sophisticated example of Spring integration for dynamic data loading.

### Annotation-Based Configuration

```java
// From: MetaData.java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MetaData {
    
    /**
     * Spring bean name for data provider
     */
    String dataProviderBean() default "";
    
    /**
     * Method name to call on the provider bean
     */
    String dataProviderMethod() default "list";
    
    /**
     * Spring bean class type for data provider
     */
    Class<?> dataProviderClass() default Object.class;
}
```

### Dynamic Service Resolution

```java
// From: CDataProviderResolver.java
@Service
public final class CDataProviderResolver {
    
    /**
     * Resolves data provider based on annotation configuration
     */
    public <T extends CEntityDB> List<T> resolveData(
            final Class<T> entityType, final MetaData metaData) {
        
        // Strategy 1: Use specified bean name
        if (!metaData.dataProviderBean().trim().isEmpty()) {
            return resolveDataFromBean(entityType, 
                metaData.dataProviderBean(), metaData.dataProviderMethod());
        }

        // Strategy 2: Use specified bean class
        if (metaData.dataProviderClass() != Object.class) {
            return resolveDataFromClass(entityType, 
                metaData.dataProviderClass(), metaData.dataProviderMethod());
        }

        // Strategy 3: Automatic resolution by naming convention
        return resolveDataAutomatically(entityType, metaData.dataProviderMethod());
    }

    /**
     * Smart method resolution with multiple signatures
     */
    private <T> List<T> callDataMethod(final Object serviceBean, 
            final String methodName, final Class<T> entityType) {
        
        // Try different method signatures in order of preference:
        // 1. methodName(Pageable) - returns Page<T> or List<T>
        // 2. methodName() - returns List<T>
        // 3. "list"(Pageable) - standard pagination
        // 4. "list"() - simple list
        // 5. "findAll"() - JPA repository method
        
        final String[] methodsToTry = {methodName, "list", "findAll"};
        
        for (final String currentMethodName : methodsToTry) {
            // Try with Pageable parameter first
            List<T> result = tryMethodWithPageable(serviceBean, currentMethodName, entityType);
            if (result != null) return result;
            
            // Try without parameters
            result = tryMethodWithoutParams(serviceBean, currentMethodName, entityType);
            if (result != null) return result;
        }
        
        return Collections.emptyList();
    }
}
```

### Usage in Entity Definitions

```java
// From: CActivity.java
@Entity
@Table(name = "cactivity")
public class CActivity extends CEntityOfProject {

    /**
     * Annotation configures Spring-based data provider
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cactivitytype_id", nullable = true)
    @MetaData(
        displayName = "Activity Type", 
        description = "Type category of the activity", 
        order = 2,
        dataProviderBean = "CActivityTypeService"  // Spring bean name
    )
    private CActivityType activityType;
}
```

---

## Best Practices Summary

1. **Use Constructor Injection**: Preferred over field injection for better testability
2. **Service Layer**: Encapsulate business logic in @Service classes
3. **Transaction Boundaries**: Use @Transactional at service methods, not repositories
4. **Security**: Apply @PreAuthorize at service level for business rule enforcement
5. **Configuration**: Centralize configuration in @Configuration classes
6. **Entity Design**: Use proper JPA annotations and inheritance hierarchies
7. **Repository Pattern**: Keep repositories simple, put logic in services
8. **Bean Management**: Let Spring manage object lifecycle through annotations
9. **ApplicationContext**: Use for dynamic bean resolution when needed
10. **Documentation**: Annotate code clearly for maintenance and understanding

This Spring Framework integration provides a robust, scalable foundation for the Derbent project, following modern Java enterprise development practices.