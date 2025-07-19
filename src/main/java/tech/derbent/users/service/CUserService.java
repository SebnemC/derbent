package tech.derbent.users.service;

import java.time.Clock;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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
import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

@Service
@PreAuthorize("isAuthenticated()")
@Transactional(readOnly = true) // Default to read-only transactions for better performance
public class CUserService extends CAbstractService<CUser> implements UserDetailsService {

	private static final Logger logger = LoggerFactory.getLogger(CUserService.class);
	private final PasswordEncoder passwordEncoder;

	CUserService(final CUserRepository repository, final Clock clock) {
		super(repository, clock);
		this.passwordEncoder = new BCryptPasswordEncoder(); // BCrypt for secure password hashing
		logger.info("CLoginUserService initialized with database authentication");
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
	public CUser createLoginUser(final String username, final String plainPassword, final String name, final String email, final String roles) {
		logger.info("Creating new login user with username: {}", username);
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
		logger.info("Successfully created login user with ID: {} and username: {}", savedUser.getId(), username);
		return savedUser;
	}

	/**
	 * Converts comma-separated role string to Spring Security authorities. Roles
	 * are prefixed with "ROLE_" as per Spring Security convention.
	 * @param rolesString comma-separated roles (e.g., "USER,ADMIN")
	 * @return Collection of GrantedAuthority objects
	 */
	private Collection<GrantedAuthority> getAuthorities(final String rolesString) {
		if ((rolesString == null) || rolesString.trim().isEmpty()) {
			logger.warn("User has no roles assigned, defaulting to ROLE_USER");
			return Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
		}
		// Split roles by comma and convert to authorities
		final Collection<GrantedAuthority> authorities = Arrays.stream(rolesString.split(",")).map(String::trim).filter(role -> !role.isEmpty())
			.map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role) // Add ROLE_ prefix if not present
			.map(SimpleGrantedAuthority::new).collect(Collectors.toList());
		logger.debug("Converted roles '{}' to authorities: {}", rolesString, authorities);
		return authorities;
	}

	/**
	 * Gets the password encoder used by this service. Useful for external password
	 * operations.
	 * @return the PasswordEncoder instance
	 */
	public PasswordEncoder getPasswordEncoder() { return passwordEncoder; }

	public CUser getUserWithProjects(final Long id) {
		return ((CUserRepository) repository).findByIdWithProjects(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
	}

	/**
	 * Implementation of UserDetailsService.loadUserByUsername(). This is the core
	 * method called by Spring Security during authentication. Authentication Flow
	 * Step: 1. Spring Security calls this method with username from login form 2.
	 * Query database to find CUser by username 3. If not found, throw
	 * UsernameNotFoundException 4. If found, convert CUser to Spring Security
	 * UserDetails 5. Return UserDetails with username, password, and authorities 6.
	 * Spring Security uses returned UserDetails to verify password
	 * @param username the username from the login form
	 * @return UserDetails object containing user authentication info
	 * @throws UsernameNotFoundException if user not found in database
	 */
	public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
		logger.debug("Attempting to load user by username: {}", username);
		// Step 1: Query database for user by username
		final CUser loginUser = ((CUserRepository) repository).findByUsername(username).orElseThrow(() -> {
			logger.warn("User not found with username: {}", username);
			return new UsernameNotFoundException("User not found with username: " + username);
		});
		logger.debug("User found: {} with roles: {}", username, loginUser.getRoles());
		// Step 2: Convert user roles to Spring Security authorities
		final Collection<GrantedAuthority> authorities = getAuthorities(loginUser.getRoles());
		// Step 3: Create and return Spring Security UserDetails The password will be
		// compared by Spring Security using the configured PasswordEncoder
		return User.builder().username(loginUser.getUsername()).password(loginUser.getPassword()) // Already encoded password from database
			.authorities(authorities).accountExpired(false).accountLocked(false).credentialsExpired(false).disabled(!loginUser.isEnabled()) // Convert enabled flag to disabled flag
			.build();
	}

	/**
	 * Gets a user by username.
	 * @param username the username to search for
	 * @return the CUser if found
	 * @throws UsernameNotFoundException if user not found
	 */
	public CUser getUserByUsername(final String username) throws UsernameNotFoundException {
		logger.debug("Getting user by username: {}", username);
		return ((CUserRepository) repository).findByUsername(username)
			.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
	}

	/**
	 * Gets the projects accessible to a user based on their project settings.
	 * @param userId the user ID
	 * @return list of accessible projects
	 */
	public List<CProject> getUserAccessibleProjects(final Long userId) {
		logger.debug("Getting accessible projects for user ID: {}", userId);
		final CUser user = getUserWithProjects(userId);
		return ((CUserRepository) repository).findAccessibleProjectsByUserId(userId);
	}

	/**
	 * Updates user password with proper encoding.
	 * @param username         the username to update
	 * @param newPlainPassword the new plain text password
	 * @throws UsernameNotFoundException if user not found
	 */
	@Transactional
	public void updatePassword(final String username, final String newPlainPassword) {
		logger.info("Updating password for user: {}", username);
		final CUser loginUser = ((CUserRepository) repository).findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
		final String encodedPassword = passwordEncoder.encode(newPlainPassword);
		loginUser.setPassword(encodedPassword);
		repository.saveAndFlush(loginUser);
		logger.info("Password updated successfully for user: {}", username);
	}
}
