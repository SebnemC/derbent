package tech.derbent.session.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.security.AuthenticationContext;

import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserService;

/**
 * Service to manage user session state including active user and active project.
 * Layer: Service (MVC)
 * 
 * This service handles session management for project-based filtering and user context.
 * It provides methods to get/set active project and user, with automatic fallbacks
 * and proper session management. Uses Vaadin session storage for persistence.
 * 
 * Key responsibilities:
 * - Manage active project selection with automatic fallback to first available project
 * - Handle user session context with authentication integration
 * - Trigger UI refresh events when project changes
 * - Provide session cleanup capabilities
 */
@Service
public class SessionService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SessionService.class);
	private static final String ACTIVE_PROJECT_KEY = "activeProject";
	private static final String ACTIVE_USER_KEY = "activeUser";

	private final AuthenticationContext authenticationContext;
	private final CUserService userService;
	private final CProjectService projectService;

	/**
	 * Constructor with dependency injection.
	 * @param authenticationContext Spring Security authentication context
	 * @param userService Service for user operations
	 * @param projectService Service for project operations
	 */
	public SessionService(final AuthenticationContext authenticationContext, 
	                     final CUserService userService, 
	                     final CProjectService projectService) {
		this.authenticationContext = authenticationContext;
		this.userService = userService;
		this.projectService = projectService;
	}

	/**
	 * Gets the currently active project from the session.
	 * If no project is set, automatically sets and returns the first available project.
	 * This ensures there's always an active project when projects exist.
	 * @return Optional containing the active project, or empty if no projects available
	 */
	public Optional<CProject> getActiveProject() {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session == null) {
			LOGGER.warn("No Vaadin session available - cannot get active project");
			return Optional.empty();
		}

		CProject activeProject = (CProject) session.getAttribute(ACTIVE_PROJECT_KEY);
		if (activeProject == null) {
			// If no active project is set, try to set the first available project
			final List<CProject> availableProjects = getAvailableProjects();
			if (!availableProjects.isEmpty()) {
				activeProject = availableProjects.get(0);
				setActiveProject(activeProject);
				LOGGER.debug("No active project found, automatically set to: {}", activeProject.getName());
			}
		}
		return Optional.ofNullable(activeProject);
	}

	/**
	 * Sets the active project in the session and triggers UI refresh.
	 * This method broadcasts a project change event to all project-aware components.
	 * @param project The project to set as active, or null to clear
	 */
	public void setActiveProject(final CProject project) {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session != null) {
			session.setAttribute(ACTIVE_PROJECT_KEY, project);
			LOGGER.info("Active project set to: {}", project != null ? project.getName() : "null");
			
			// Trigger UI refresh for all project-aware components
			refreshProjectAwareComponents();
		} else {
			LOGGER.warn("No Vaadin session available - cannot set active project");
		}
	}

	/**
	 * Gets the currently active user from the session.
	 * If no user is cached, attempts to load from authentication context.
	 * @return Optional containing the active user, or empty if not authenticated
	 */
	public Optional<CUser> getActiveUser() {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session == null) {
			LOGGER.warn("No Vaadin session available - cannot get active user");
			return Optional.empty();
		}

		CUser activeUser = (CUser) session.getAttribute(ACTIVE_USER_KEY);
		if (activeUser == null) {
			// Try to load user from authentication context
			final Optional<org.springframework.security.core.userdetails.User> authenticatedUser = 
				authenticationContext.getAuthenticatedUser(org.springframework.security.core.userdetails.User.class);
			
			if (authenticatedUser.isPresent()) {
				final String username = authenticatedUser.get().getUsername();
				activeUser = userService.findByLogin(username);
				if (activeUser != null) {
					setActiveUser(activeUser);
					LOGGER.debug("Loaded user from authentication context: {}", username);
				}
			}
		}
		return Optional.ofNullable(activeUser);
	}

	/**
	 * Sets the active user in the session.
	 * @param user The user to set as active, or null to clear
	 */
	public void setActiveUser(final CUser user) {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session != null) {
			session.setAttribute(ACTIVE_USER_KEY, user);
			LOGGER.info("Active user set to: {}", user != null ? user.getLogin() : "null");
		} else {
			LOGGER.warn("No Vaadin session available - cannot set active user");
		}
	}

	/**
	 * Gets all available projects for the current user.
	 * Currently returns all projects - can be enhanced to filter by user permissions.
	 * @return List of all available projects
	 */
	public List<CProject> getAvailableProjects() {
		return projectService.findAll();
	}

	/**
	 * Triggers refresh of project-aware components when project changes.
	 * Sets a timestamp in session attributes that project-aware views can monitor
	 * to detect project changes and refresh their data accordingly.
	 */
	private void refreshProjectAwareComponents() {
		final UI ui = UI.getCurrent();
		if (ui != null) {
			ui.access(() -> {
				// Broadcast a project change event that project-aware components can listen to
				ui.getSession().setAttribute("projectChanged", System.currentTimeMillis());
				LOGGER.debug("Project change event broadcasted at: {}", System.currentTimeMillis());
			});
		} else {
			LOGGER.warn("No UI available - cannot broadcast project change event");
		}
	}

	/**
	 * Clears all session data on logout.
	 * Removes both active project and active user from session storage.
	 */
	public void clearSession() {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session != null) {
			session.setAttribute(ACTIVE_PROJECT_KEY, null);
			session.setAttribute(ACTIVE_USER_KEY, null);
			LOGGER.info("Session data cleared");
		} else {
			LOGGER.warn("No Vaadin session available - cannot clear session data");
		}
	}
}