package tech.derbent.users.service;

import java.time.Clock;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import tech.derbent.api.roles.service.CUserProjectRoleService;
import tech.derbent.api.services.CAbstractNamedEntityService;
import tech.derbent.api.utils.Check;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserProjectSettingsService;
import tech.derbent.users.view.CUserProjectSettingsComponent;
import tech.derbent.users.view.CUserProjectSettingsDialog;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CUserService extends CAbstractNamedEntityService<CUser> implements UserDetailsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CUserService.class);
	private final PasswordEncoder passwordEncoder;
	@Autowired
	private CProjectService projectService;
	@Autowired
	private CUserProjectRoleService roleService;
	@Autowired
	private CUserProjectSettingsService userProjectSettingsService;

	public CUserService(final CUserRepository repository, final Clock clock) {
		super(repository, clock);
		this.passwordEncoder = new BCryptPasswordEncoder(); // BCrypt for secure password
															// hashing
		@SuppressWarnings ("unused")
		final CharSequence newPlainPassword = "test123";
		// final String encodedPassword = passwordEncoder.encode(newPlainPassword);
	}

	/** Counts the number of users assigned to a specific project.
	 * @param projectId the project ID
	 * @return count of users assigned to the project */
	@PreAuthorize ("permitAll()")
	public long countUsersByProjectId(final Long projectId) {
		return ((CUserRepository) repository).countByProjectId(projectId);
	}

	@Transactional // Write operation requires writable transaction
	public CUser createLoginUser(final String username, final String plainPassword, final String name, final String email, final String roles) {
		// Check if username already exists
		if (((CUserRepository) repository).findByUsername(username).isPresent()) {
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

	/** Finds a user by login username.
	 * @param login the login username
	 * @return the CUser if found, null otherwise */
	public CUser findByLogin(final String login) {
		return ((CUserRepository) repository).findByUsername(login).orElse(null);
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

	@Override
	protected Class<CUser> getEntityClass() { return CUser.class; }

	/** Gets the password encoder used by this service. Useful for external password operations.
	 * @return the PasswordEncoder instance */
	public PasswordEncoder getPasswordEncoder() { return passwordEncoder; }

	@Override
	public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
		LOGGER.debug("Attempting to load user by username: {}", username);
		// Step 1: Query database for user by username
		final CUser loginUser = ((CUserRepository) repository).findByUsername(username).orElseThrow(() -> {
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

	/** Lists users by project using the CUserProjectSettings relationship. This method allows CUserService to work with dynamic pages that expect
	 * project filtering.
	 * @param project the project to filter users by
	 * @return list of users associated with the project */
	@Transactional (readOnly = true)
	public List<CUser> listByProject(final CProject project) {
		Check.notNull(project, "Project cannot be null");
		try {
			// Find users through the project settings relationship
			return repository.findAll().stream()
					.filter(user -> user.getProjectSettings() != null && user.getProjectSettings().stream()
							.anyMatch(settings -> settings.getProject() != null && projectsMatch(settings.getProject(), project)))
					.collect(Collectors.toList());
		} catch (final Exception e) {
			LOGGER.error("Error listing users by project '{}': {}", project.getName(), e.getMessage(), e);
			throw new RuntimeException("Failed to list users by project", e);
		}
	}

	/** Helper method to safely compare projects, handling cases where IDs might be null */
	private boolean projectsMatch(CProject project1, CProject project2) {
		if (project1 == null || project2 == null) {
			return false;
		}
		// If both have IDs, compare by ID
		if (project1.getId() != null && project2.getId() != null) {
			return project1.getId().equals(project2.getId());
		}
		// If IDs are null, compare by name as fallback
		return project1.getName() != null && project1.getName().equals(project2.getName());
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

	/** Override the default list method to filter users by active project when used in dynamic pages. This allows CUserService to work with dynamic
	 * pages without needing to implement CEntityOfProjectService. If no active project is available, returns all users (preserves existing
	 * behavior). */
	@Override
	@Transactional (readOnly = true)
	public Page<CUser> list(final Pageable pageable) {
		return super.list(pageable);
	}

	@Override
	public boolean onBeforeSaveEvent(final CUser entity) {
		if (super.onBeforeSaveEvent(entity) == false) {
			return false;
		}
		return true;
	}

	@Transactional
	public void updatePassword(final String username, final String newPlainPassword) {
		final CUser loginUser = ((CUserRepository) repository).findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
		final String encodedPassword = passwordEncoder.encode(newPlainPassword);
		loginUser.setPassword(encodedPassword);
		repository.saveAndFlush(loginUser);
	}

	@Override
	protected void validateEntity(final CUser user) {
		super.validateEntity(user);
		Check.notBlank(user.getLogin(), "User login cannot be null or empty");
		Check.notBlank(user.getName(), "User name cannot be null or empty");
	}

	@Transactional (readOnly = true)
	@PreAuthorize ("permitAll()")
	public List<CUser> getAvailableUsersForProject(final Long projectId) {
		Check.notNull(projectId, "User ID must not be null");
		return ((CUserRepository) repository).findUsersNotAssignedToProject(projectId);
	}

	public Component createUserProjectSettingsComponent() {
		LOGGER.debug("Creating enhanced user project settings component");
		try {
			// Create the enhanced component with proper service dependencies
			CUserProjectSettingsComponent component =
					new CUserProjectSettingsComponent(this, projectService, roleService, userProjectSettingsService);
			LOGGER.debug("Successfully created CUserProjectSettingsComponent");
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
}
