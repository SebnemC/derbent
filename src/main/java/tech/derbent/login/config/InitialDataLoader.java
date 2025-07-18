package tech.derbent.login.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import tech.derbent.login.service.CLoginUserService;

/**
 * Data initialization component that creates initial login users on application startup.
 * This component implements CommandLineRunner to run after the application context is fully loaded.
 * 
 * Purpose:
 * - Creates initial admin and test users for the application
 * - Only creates users if they don't already exist in the database
 * - Provides a reliable way to bootstrap the authentication system
 * 
 * Authentication Flow Bootstrap:
 * 1. Application starts up
 * 2. This component runs after all services are initialized
 * 3. Checks if initial users exist in database
 * 4. Creates admin, user, and demo accounts if they don't exist
 * 5. Users can immediately log in with these credentials
 */
@Component
public class InitialDataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(InitialDataLoader.class);

    private final CLoginUserService loginUserService;

    /**
     * Constructor injection of the login user service.
     * 
     * @param loginUserService service for managing login users
     */
    public InitialDataLoader(CLoginUserService loginUserService) {
        this.loginUserService = loginUserService;
    }

    /**
     * Runs after application startup to create initial users.
     * This method is called by Spring Boot's CommandLineRunner interface.
     * 
     * Creates three initial users:
     * - admin/admin with ADMIN,USER roles
     * - user/user with USER role  
     * - demo/test123 with USER role
     * 
     * @param args command line arguments (not used)
     */
    @Override
    public void run(String... args) {
        logger.info("Initializing database with default login users...");

        try {
            // Create admin user if it doesn't exist
            createUserIfNotExists("admin", "admin", "System", "Administrator", 
                               "admin@derbent.tech", "+90-555-000-0001", "ADMIN,USER");

            // Create regular user if it doesn't exist  
            createUserIfNotExists("user", "user", "Test", "User",
                               "user@derbent.tech", "+90-555-000-0002", "USER");

            // Create demo user if it doesn't exist
            createUserIfNotExists("demo", "test123", "Demo", "User", 
                               "demo@derbent.tech", "+90-555-000-0003", "USER");

            logger.info("Initial user creation completed successfully");

        } catch (Exception e) {
            logger.error("Error creating initial users: {}", e.getMessage(), e);
        }
    }

    /**
     * Creates a user if it doesn't already exist in the database.
     * Uses the CLoginUserService to handle user creation with proper password encoding.
     * 
     * @param username the login username
     * @param password the plain text password (will be encoded)
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param email the user's email address
     * @param phone the user's phone number
     * @param roles comma-separated roles for the user
     */
    private void createUserIfNotExists(String username, String password, String firstName, 
                                     String lastName, String email, String phone, String roles) {
        try {
            // Try to load user by username to check if it exists
            loginUserService.loadUserByUsername(username);
            logger.debug("User '{}' already exists, skipping creation", username);
            
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            // User doesn't exist, create it
            logger.info("Creating user: {}", username);
            
            var loginUser = loginUserService.createLoginUser(username, password, firstName, email, roles);
            loginUser.setLastname(lastName);
            loginUser.setPhone(phone);
            
            // Note: The service automatically saves the user, but we could save again if we modified additional fields
            logger.info("Successfully created user '{}' with ID: {}", username, loginUser.getId());
        }
    }
}