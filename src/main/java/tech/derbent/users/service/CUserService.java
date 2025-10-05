package tech.derbent.users.service;

import java.time.Clock;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import tech.derbent.api.services.CAbstractNamedEntityService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.components.CComponentSingleCompanyUserSetting;
import tech.derbent.api.views.components.CComponentUserProjectSettings;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CUserService extends CAbstractNamedEntityService<CUser> implements UserDetailsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CUserService.class);
	@Autowired
	private ApplicationContext applicationContext;
	private final PasswordEncoder passwordEncoder;

	public CUserService(final IUserRepository repository, final Clock clock) {
		super(repository, clock);
		passwordEncoder = new BCryptPasswordEncoder(); // BCrypt for secure password
														// hashing
		@SuppressWarnings ("unused")
		final CharSequence newPlainPassword = "test123";
		// final String encodedPassword = passwordEncoder.encode(newPlainPassword);
	}

	@PreAuthorize ("permitAll()")
	public long countUsersByProjectId(final Long projectId) {
		return ((IUserRepository) repository).countByProjectId(projectId);
	}

	@Transactional // Write operation requires writable transaction
	public CUser createLoginUser(final String username, final String plainPassword, final String name, final String email, final String roles) {
		// Check if username already exists
		if (((IUserRepository) repository).findByUsername(username).isPresent()) {
			LOGGER.warn("Username already exists: {}", username);
			throw new IllegalArgumentException("Username already exists: " + username);
		}
		// Encode the password
		final String encodedPassword = passwordEncoder.encode(plainPassword);
		// Create new login user
		final CUser loginUser = new CUser(username, encodedPassword, name, email);
		loginUser.setEnabled(true);
		// Save to database
		final CUser savedUser = repository.saveAndFlush(loginUser);
		return savedUser;
	}

	public Component createSingleCompanyUserSettingComponent() {
		LOGGER.debug("Creating single company user setting component");
		try {
			CComponentSingleCompanyUserSetting component = new CComponentSingleCompanyUserSetting(this, applicationContext);
			return component;
		} catch (Exception e) {
			LOGGER.error("Failed to create single company user setting component: {}", e.getMessage(), e);
			// Fallback to simple div with error message
			final Div errorDiv = new Div();
			errorDiv.setText("Error loading single company user setting component: " + e.getMessage());
			errorDiv.addClassName("error-message");
			return errorDiv;
		}
	}

	public Component createUserProjectSettingsComponent() {
		LOGGER.debug("Creating enhanced user project settings component");
		try {
			CComponentUserProjectSettings component = new CComponentUserProjectSettings(this, applicationContext);
			return component;
		} catch (Exception e) {
			LOGGER.error("Failed to create user project settings component: {}", e.getMessage(), e);
			// Fallback to simple div with error message
			final Div errorDiv = new Div();
			errorDiv.setText("Error loading user project settings component: " + e.getMessage());
			errorDiv.addClassName("error-message");
			return errorDiv;
		}
	}

	/** Finds a user by login username.
	 * @param login the login username
	 * @return the CUser if found, null otherwise */
	public CUser findByLogin(final String login) {
		return ((IUserRepository) repository).findByUsername(login).orElse(null);
	}

	/** Converts comma-separated role string to Spring Security authorities. Roles are prefixed with "ROLE_" as per Spring Security convention.
	 * @param rolesString comma-separated roles (e.g., "USER,ADMIN")
	 * @return Collection of GrantedAuthority objects */
	private Collection<GrantedAuthority> getAuthorities(final String rolesString) {
		if ((rolesString == null) || rolesString.trim().isEmpty()) {
			LOGGER.warn("User has no roles assigned, defaulting to ROLE_USER");
			return Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
		}
		// Split roles by comma and convert to authorities
		final Collection<GrantedAuthority> authorities = Arrays.stream(rolesString.split(",")).map(String::trim).filter(role -> !role.isEmpty())
				.map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role) // Add
				// ROLE_ prefix if not present
				.map(SimpleGrantedAuthority::new).collect(Collectors.toList());
		LOGGER.debug("Converted roles '{}' to authorities: {}", rolesString, authorities);
		return authorities;
	}

	@Transactional (readOnly = true)
	@PreAuthorize ("permitAll()")
	public List<CUser> getAvailableUsersForCompany(final Long companyId) {
		Check.notNull(companyId, "ID must not be null");
		return ((IUserRepository) repository).findUsersNotAssignedToCompany(companyId);
	}

	@Transactional (readOnly = true)
	@PreAuthorize ("permitAll()")
	public List<CUser> getAvailableUsersForProject(final Long projectId) {
		Check.notNull(projectId, "User ID must not be null");
		return ((IUserRepository) repository).findUsersNotAssignedToProject(projectId);
	}

	/** Find user by ID with company setting eagerly loaded to avoid LazyInitializationException in UI. This method should be used when the UI needs
	 * to access user's company settings and company data.
	 * @param userId the user ID
	 * @return Optional containing the user with company setting loaded, or empty if not found */
	@Transactional (readOnly = true)
	@PreAuthorize ("permitAll()")
	public Optional<CUser> findByIdWithCompanySetting(final Long userId) {
		Check.notNull(userId, "User ID must not be null");
		return ((IUserRepository) repository).findByIdWithCompanySetting(userId);
	}

	@Override
	protected Class<CUser> getEntityClass() { return CUser.class; }

	/** Override the default list method to filter users by active project when used in dynamic pages. This allows CUserService to work with dynamic
	 * pages without needing to implement CEntityOfProjectService. If no active project is available, returns all users (preserves existing
	 * behavior). */
	@Override
	@Transactional (readOnly = true)
	public Page<CUser> list(final Pageable pageable) {
		return ((IUserRepository) repository).list(pageable);
		// return super.list(pageable);
	}

	/** Lists users by project using the CUserProjectSettings relationship. This method allows CUserService to work with dynamic pages that expect
	 * project filtering.
	 * @param project the project to filter users by
	 * @return list of users associated with the project */
	@Transactional (readOnly = true)
	public List<CUser> listByProject(final CProject project) {
		Check.notNull(project, "Project cannot be null");
		try {
			// Find users through the project settings relationship
			return ((IUserRepository) repository).findByProject(project.getId());
		} catch (final Exception e) {
			LOGGER.error("Error listing users by project '{}': {}", project.getName(), e.getMessage(), e);
			throw new RuntimeException("Failed to list users by project", e);
		}
	}

	/** Lists users by project with pagination. This method allows CUserService to work with dynamic pages that expect project filtering.
	 * @param project  the project to filter users by
	 * @param pageable pagination information
	 * @return page of users associated with the project */
	@Transactional (readOnly = true)
	public Page<CUser> listByProject(final CProject project, final Pageable pageable) {
		Check.notNull(project, "Project cannot be null");
		Check.notNull(pageable, "Pageable cannot be null");
		try {
			List<CUser> allUsers = listByProject(project);
			// Apply pagination
			int start = (int) Math.min(pageable.getOffset(), allUsers.size());
			int end = Math.min(start + pageable.getPageSize(), allUsers.size());
			List<CUser> content = allUsers.subList(start, end);
			return new PageImpl<>(content, pageable, allUsers.size());
		} catch (final Exception e) {
			LOGGER.error("Error listing users by project '{}' with pagination: {}", project.getName(), e.getMessage(), e);
			throw new RuntimeException("Failed to list users by project with pagination", e);
		}
	}

	@Override
	public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
		LOGGER.debug("Attempting to load user by username: {}", username);
		// Step 1: Query database for user by username
		final CUser loginUser = ((IUserRepository) repository).findByUsername(username).orElseThrow(() -> {
			LOGGER.warn("User not found with username: {}", username);
			return new UsernameNotFoundException("User not found with username: " + username);
		});
		// Step 2: Convert user roles to Spring Security authorities
		// fix this next line!!!!!
		final Collection<GrantedAuthority> authorities = getAuthorities("ADMIN,USER"); // Example roles, replace with actual user roles if available
		// Step 3: Create and return Spring Security UserDetails
		return User.builder().username(loginUser.getUsername()).password(loginUser.getPassword()) // Already encoded password from database
				.authorities(authorities).accountExpired(false).accountLocked(false).credentialsExpired(false).disabled(!loginUser.isEnabled()) // Convert
																																				// enabled
				// flag to disabled flag
				.build();
	}

	@Override
	public boolean onBeforeSaveEvent(final CUser entity) {
		if (super.onBeforeSaveEvent(entity) == false) {
			return false;
		}
		return true;
	}

	/** Override getRandom to fail safely when no users with company associations exist. This ensures sample data initialization fails early if
	 * dependencies are missing. */
	@Override
	public CUser getRandom() {
		final CUser randomUser = super.getRandom();
		if (randomUser == null) {
			LOGGER.warn("No users found in database");
			return null;
		}
		// Optionally validate that user has company association
		// For now, return the random user if found
		return randomUser;
	}

	@Override
	protected void validateEntity(final CUser user) {
		super.validateEntity(user);
		Check.notBlank(user.getLogin(), "User login cannot be null or empty");
		Check.notBlank(user.getName(), "User name cannot be null or empty");
	}
}
