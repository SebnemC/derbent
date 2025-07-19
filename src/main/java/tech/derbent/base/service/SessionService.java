package tech.derbent.base.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.vaadin.flow.spring.annotation.VaadinSessionScope;

import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserService;

/**
 * Session service to manage user session state including current user and selected projects.
 * Uses Vaadin session scope to maintain state per user session.
 */
@Service
@VaadinSessionScope
public class SessionService {

	private static final Logger logger = LoggerFactory.getLogger(SessionService.class);
	
	private final CUserService userService;
	private CUser currentUser;
	private final Set<Long> selectedProjectIds = ConcurrentHashMap.newKeySet();
	private List<CProject> accessibleProjects = new ArrayList<>();
	
	public SessionService(final CUserService userService) {
		this.userService = userService;
		logger.debug("SessionService initialized");
	}
	
	/**
	 * Gets the current authenticated user, loading it if not already cached.
	 * @return the current CUser or null if not authenticated
	 */
	public CUser getCurrentUser() {
		if (currentUser == null) {
			loadCurrentUser();
		}
		return currentUser;
	}
	
	/**
	 * Gets the list of projects accessible to the current user.
	 * @return list of accessible projects
	 */
	public List<CProject> getAccessibleProjects() {
		if (accessibleProjects.isEmpty() && getCurrentUser() != null) {
			loadAccessibleProjects();
		}
		return new ArrayList<>(accessibleProjects);
	}
	
	/**
	 * Gets the currently selected project IDs.
	 * @return set of selected project IDs
	 */
	public Set<Long> getSelectedProjectIds() {
		return Set.copyOf(selectedProjectIds);
	}
	
	/**
	 * Sets the selected project IDs and notifies listeners.
	 * @param projectIds the project IDs to select
	 */
	public void setSelectedProjectIds(final Set<Long> projectIds) {
		logger.debug("Setting selected project IDs: {}", projectIds);
		selectedProjectIds.clear();
		if (projectIds != null) {
			selectedProjectIds.addAll(projectIds);
		}
		// TODO: Notify listeners about project selection change
		logger.info("Selected projects changed to: {}", selectedProjectIds);
	}
	
	/**
	 * Checks if a project is currently selected.
	 * @param projectId the project ID to check
	 * @return true if project is selected
	 */
	public boolean isProjectSelected(final Long projectId) {
		return selectedProjectIds.contains(projectId);
	}
	
	/**
	 * Clears the session state (useful for logout).
	 */
	public void clearSession() {
		logger.debug("Clearing session state");
		currentUser = null;
		selectedProjectIds.clear();
		accessibleProjects.clear();
	}
	
	/**
	 * Loads the current user from the security context.
	 */
	private void loadCurrentUser() {
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
			try {
				// Load the user with their project settings
				final String username = authentication.getName();
				logger.debug("Loading current user: {}", username);
				currentUser = userService.getUserByUsername(username);
				logger.info("Loaded current user: {} with ID: {}", username, currentUser != null ? currentUser.getId() : "null");
			} catch (final Exception e) {
				logger.warn("Failed to load current user: {}", e.getMessage());
				currentUser = null;
			}
		}
	}
	
	/**
	 * Loads the projects accessible to the current user.
	 */
	private void loadAccessibleProjects() {
		if (currentUser != null) {
			try {
				logger.debug("Loading accessible projects for user: {}", currentUser.getLogin());
				accessibleProjects = userService.getUserAccessibleProjects(currentUser.getId());
				logger.info("Loaded {} accessible projects for user: {}", accessibleProjects.size(), currentUser.getLogin());
			} catch (final Exception e) {
				logger.warn("Failed to load accessible projects for user {}: {}", currentUser.getLogin(), e.getMessage());
				accessibleProjects = new ArrayList<>();
			}
		}
	}
}