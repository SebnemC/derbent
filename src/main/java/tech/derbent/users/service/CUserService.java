package tech.derbent.users.service;

import java.time.Clock;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import tech.derbent.abstracts.annotations.CSpringAuxillaries;
import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true) // Default to read-only transactions for better
									// performance
public class CUserService extends CAbstractService<CUser> implements UserDetailsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CUserService.class);

	private final PasswordEncoder passwordEncoder;

	CUserService(final CUserRepository repository, final Clock clock) {
		super(repository, clock);
		this.passwordEncoder = new BCryptPasswordEncoder(); // BCrypt for secure password
															// hashing
		final CharSequence newPlainPassword = "test123";
		final String encodedPassword = passwordEncoder.encode(newPlainPassword);
		LOGGER.info("Encoded password for '{}': {}", newPlainPassword, encodedPassword);
	}

	/**
	 * Counts the number of users assigned to a specific project.
	 * @param projectId the project ID
	 * @return count of users assigned to the project
	 */
	@PreAuthorize ("permitAll()")
	public long countUsersByProjectId(final Long projectId) {
		LOGGER.info("Counting users for project ID: {}", projectId);
		return ((CUserRepository) repository).countUsersByProjectId(projectId);
	}

	@Transactional
	public void createEntity(final String name) {

		if ("fail".equals(name)) {
			throw new RuntimeException("This is for testing the error handler");
		}
		final var entity = new CUser();
		entity.setName(name);
		repository.saveAndFlush(entity);
	}

	/**
	 * Creates a new login user with encoded password. This method handles password
	 * encoding automatically.
	 * @param username      the username for login
	 * @param plainPassword the plain text password (will be encoded)
	 * @param name          the user's first name
	 * @param email         the user's email
	 * @param roles         comma-separated roles (e.g., "USER,ADMIN")
	 * @return the created and saved CUser
	 */
	@Transactional // Write operation requires writable transaction
	public CUser createLoginUser(final String username, final String plainPassword,
		final String name, final String email, final String roles) {
		LOGGER.info("Creating new login user with username: {}", username);

		// Check if username already exists
		if (((CUserRepository) repository).findByUsername(username).isPresent()) {
			throw new IllegalArgumentException("Username already exists: " + username);
		}
		// Encode the password
		final String encodedPassword = passwordEncoder.encode(plainPassword);
		// Create new login user
		final CUser loginUser = new CUser(username, encodedPassword, name, email);
		loginUser.setRoles(roles != null ? roles : "USER");
		loginUser.setEnabled(true);
		// Save to database
		final CUser savedUser = repository.saveAndFlush(loginUser);
		LOGGER.info("Successfully created login user with ID: {} and username: {}",
			savedUser.getId(), username);
		return savedUser;
	}

	/**
	 * Finds a user by login username.
	 * @param login the login username
	 * @return the CUser if found, null otherwise
	 */
	public CUser findByLogin(final String login) {
		LOGGER.debug("Finding user by login: {}", login);
		return ((CUserRepository) repository).findByUsername(login).orElse(null);
	}

	/**
	 * Overrides the base get method to eagerly load CUserType and company relationships.
	 * This prevents LazyInitializationException when the entity is used in UI contexts,
	 * particularly when company information is used in comboboxes.
	 * @param id the user ID
	 * @return optional CUser with loaded userType and company
	 */
	@Override
	@Transactional (readOnly = true)
	public Optional<CUser> get(final Long id) {
		LOGGER.debug(
			"Getting CUser with ID {} (overridden to eagerly load userType and company)",
			id);
		final Optional<CUser> entity =
			((CUserRepository) repository).findByIdWithAllRelationships(id);
		// Initialize lazy fields if entity is present (for any other potential lazy
		// relationships)
		entity.ifPresent(this::initializeLazyFields);
		return entity;
	}

	/**
	 * Converts comma-separated role string to Spring Security authorities. Roles are
	 * prefixed with "ROLE_" as per Spring Security convention.
	 * @param rolesString comma-separated roles (e.g., "USER,ADMIN")
	 * @return Collection of GrantedAuthority objects
	 */
	private Collection<GrantedAuthority> getAuthorities(final String rolesString) {

		if ((rolesString == null) || rolesString.trim().isEmpty()) {
			LOGGER.warn("User has no roles assigned, defaulting to ROLE_USER");
			return Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
		}
		// Split roles by comma and convert to authorities
		final Collection<GrantedAuthority> authorities =
			Arrays.stream(rolesString.split(",")).map(String::trim)
				.filter(role -> !role.isEmpty())
				.map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role) // Add
																				// ROLE_
																				// prefix
																				// if not
																				// present
				.map(SimpleGrantedAuthority::new).collect(Collectors.toList());
		LOGGER.debug("Converted roles '{}' to authorities: {}", rolesString, authorities);
		return authorities;
	}

	/**
	 * Gets the password encoder used by this service. Useful for external password
	 * operations.
	 * @return the PasswordEncoder instance
	 */
	public PasswordEncoder getPasswordEncoder() { return passwordEncoder; }

	/**
	 * Gets a user with lazy-loaded project settings initialized.
	 * @param id the user ID
	 * @return the user with project settings loaded
	 * @throws EntityNotFoundException if user not found
	 */
	@Transactional (readOnly = true)
	public CUser getUserWithProjects(final Long id) {
		LOGGER.debug("Getting user with projects for ID: {}", id);
		final CUser user =
			((CUserRepository) repository).findByIdWithProjects(id).orElseThrow(
				() -> new EntityNotFoundException("User not found with ID: " + id));
		// Ensure lazy fields are properly initialized
		initializeLazyFields(user);
		return user;
	}

	/**
	 * Initializes lazy fields for a user entity to prevent LazyInitializationException.
	 * Specifically initializes user type, company, and project settings.
	 * @param user the user entity to initialize
	 */
	@Override
	protected void initializeLazyFields(final CUser user) {

		if (user == null) {
			return;
		}

		try {
			super.initializeLazyFields(user);
			initializeLazyRelationship(user.getUserType());
			initializeLazyRelationship(user.getCompany());
			initializeLazyRelationship(user.getProjectSettings());
		} catch (final Exception e) {
			LOGGER.warn("Error initializing lazy fields for user with ID: {}",
				CSpringAuxillaries.safeGetId(user), e);
		}
	}

	/**
	 * Implementation of UserDetailsService.loadUserByUsername(). This is the core method
	 * called by Spring Security during authentication. Authentication Flow Step: 1.
	 * Spring Security calls this method with username from login form 2. Query database
	 * to find CUser by username 3. If not found, throw UsernameNotFoundException 4. If
	 * found, convert CUser to Spring Security UserDetails 5. Return UserDetails with
	 * username, password, and authorities 6. Spring Security uses returned UserDetails to
	 * verify password
	 * @param username the username from the login form
	 * @return UserDetails object containing user authentication info
	 * @throws UsernameNotFoundException if user not found in database
	 */
	@Override
	public UserDetails loadUserByUsername(final String username)
		throws UsernameNotFoundException {
		LOGGER.debug("Attempting to load user by username: {}", username);
		// Step 1: Query database for user by username
		final CUser loginUser =
			((CUserRepository) repository).findByUsername(username).orElseThrow(() -> {
				LOGGER.warn("User not found with username: {}", username);
				return new UsernameNotFoundException(
					"User not found with username: " + username);
			});
		LOGGER.debug("User found: {} with roles: {}", username, loginUser.getRoles());
		// Step 2: Convert user roles to Spring Security authorities
		final Collection<GrantedAuthority> authorities =
			getAuthorities(loginUser.getRoles());
		// Step 3: Create and return Spring Security UserDetails The password will be
		// compared by Spring Security using the configured PasswordEncoder
		return User.builder().username(loginUser.getUsername())
			.password(loginUser.getPassword()) // Already encoded password from database
			.authorities(authorities).accountExpired(false).accountLocked(false)
			.credentialsExpired(false).disabled(!loginUser.isEnabled()) // Convert enabled
																		// flag to
																		// disabled flag
			.build();
	}

	/**
	 * Removes a project setting for a user.
	 * @param userId    the user ID
	 * @param projectId the project ID
	 */
	@Transactional
	public void removeUserProjectSetting(final Long userId, final Long projectId) {
		final CUser user = getUserWithProjects(userId);

		if (user.getProjectSettings() != null) {
			user.getProjectSettings()
				.removeIf(setting -> setting.getProjectId().equals(projectId));
			repository.saveAndFlush(user);
		}
	}

	/**
	 * Adds or updates a project setting for a user.
	 * @param userProjectSetting the project setting to save
	 * @return the saved project setting
	 */
	@Transactional
	public CUserProjectSettings
		saveUserProjectSetting(final CUserProjectSettings userProjectSetting) {
		LOGGER.info("Saving user project setting for user ID: {} and project ID: {}",
			userProjectSetting.getUser().getId(), userProjectSetting.getProjectId());
		// Ensure the user exists and reload with project settings
		final CUser user = getUserWithProjects(userProjectSetting.getUser().getId());

		// Initialize project settings list if null
		if (user.getProjectSettings() == null) {
			user.setProjectSettings(new java.util.ArrayList<>());
		}
		// Check if this setting already exists (update case)
		boolean updated = false;

		for (final CUserProjectSettings existing : user.getProjectSettings()) {

			if (existing.getProjectId().equals(userProjectSetting.getProjectId())) {
				existing.setRole(userProjectSetting.getRole());
				existing.setPermission(userProjectSetting.getPermission());
				updated = true;
				break;
			}
		}

		if (!updated) {
			userProjectSetting.setUser(user);
			user.getProjectSettings().add(userProjectSetting);
		}
		repository.saveAndFlush(user);
		// Return the saved project setting
		return userProjectSetting;
	}

	/**
	 * Updates user password with proper encoding.
	 * @param username         the username to update
	 * @param newPlainPassword the new plain text password
	 * @throws UsernameNotFoundException if user not found
	 */
	@Transactional
	public void updatePassword(final String username, final String newPlainPassword) {
		LOGGER.info("Updating password for user: {}", username);
		final CUser loginUser =
			((CUserRepository) repository).findByUsername(username).orElseThrow(
				() -> new UsernameNotFoundException("User not found: " + username));
		final String encodedPassword = passwordEncoder.encode(newPlainPassword);
		loginUser.setPassword(encodedPassword);
		repository.saveAndFlush(loginUser);
		LOGGER.info("Password updated successfully for user: {}", username);
	}

	// Auxiliary methods for sample data initialization and user setup

	/**
	 * Auxiliary method to set user profile information.
	 * Following coding guidelines to use service layer methods instead of direct field setting.
	 * @param user the user to configure
	 * @param lastname the user's last name
	 * @param phone the user's phone number
	 * @param profilePictureData profile picture data
	 * @return the configured user
	 */
	@Transactional
	public CUser setUserProfile(final CUser user, final String lastname, final String phone, 
		final byte[] profilePictureData) {
		LOGGER.info("setUserProfile called for user: {} with lastname: {}, phone: {}",
			user != null ? user.getName() : "null", lastname, phone);

		if (user == null) {
			LOGGER.warn("User is null, cannot set user profile");
			return null;
		}

		if (lastname != null && !lastname.isEmpty()) {
			user.setLastname(lastname);
		}
		if (phone != null && !phone.isEmpty()) {
			user.setPhone(phone);
		}
		if (profilePictureData != null) {
			user.setProfilePictureData(profilePictureData);
		}

		return save(user);
	}

	/**
	 * Auxiliary method to set company association for a user.
	 * @param user the user to configure
	 * @param company the company to associate with the user
	 * @return the configured user
	 */
	@Transactional
	public CUser setCompanyAssociation(final CUser user, final tech.derbent.companies.domain.CCompany company) {
		LOGGER.info("setCompanyAssociation called for user: {} with company: {}",
			user != null ? user.getName() : "null",
			company != null ? company.getName() : "null");

		if (user == null) {
			LOGGER.warn("User is null, cannot set company association");
			return null;
		}

		if (company != null) {
			user.setCompany(company);
		}

		return save(user);
	}

	/**
	 * Auxiliary method to set user role and permissions.
	 * @param user the user to configure
	 * @param userRole the user's role enum
	 * @param roles comma-separated role string for Spring Security
	 * @return the configured user
	 */
	@Transactional
	public CUser setUserRole(final CUser user, final tech.derbent.users.domain.CUserRole userRole, final String roles) {
		LOGGER.info("setUserRole called for user: {} with role: {}, security roles: {}",
			user != null ? user.getName() : "null", userRole, roles);

		if (user == null) {
			LOGGER.warn("User is null, cannot set user role");
			return null;
		}

		if (userRole != null) {
			user.setUserRole(userRole);
		}
		if (roles != null && !roles.isEmpty()) {
			user.setRoles(roles);
		}

		return save(user);
	}

	@Override
	protected void validateEntity(final CUser user) {
		super.validateEntity(user);

		// Additional validation for user entities
		if ((user.getLogin() == null) || user.getLogin().trim().isEmpty()) {
			throw new IllegalArgumentException("User login cannot be null or empty");
		}

		if ((user.getName() == null) || user.getName().trim().isEmpty()) {
			throw new IllegalArgumentException("User name cannot be null or empty");
		}
	}
}
