package tech.derbent.users.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.roles.domain.CUserProjectRole;
import tech.derbent.api.services.CAbstractEntityRelationService;
import tech.derbent.api.utils.Check;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.CSessionService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;

/** Service class for managing user-project relationships. Handles CRUD operations for CUserProjectSettings entities. */
@Service
@Transactional (readOnly = true)
public class CUserProjectSettingsService extends CAbstractEntityRelationService<CUserProjectSettings> {

	private final CUserProjectSettingsRepository repository;

	@Autowired
	public CUserProjectSettingsService(final CUserProjectSettingsRepository repository, final Clock clock, final CSessionService sessionService) {
		super(repository, clock, sessionService);
		this.repository = repository;
	}

	/** Add user to project with specific role and permissions */
	@Transactional
	public CUserProjectSettings addUserToProject(final CUser user, final CProject project, final CUserProjectRole role, final String permission) {
		LOGGER.debug("Adding user {} to project {} with role {} and permission {}", user, project, role, permission);
		Check.notNull(user, "User must not be null");
		Check.notNull(project, "Project must not be null");
		if ((user.getId() == null) || (project.getId() == null)) {
			throw new IllegalArgumentException("User and project must have valid IDs");
		}
		if (relationshipExists(user.getId(), project.getId())) {
			throw new IllegalArgumentException("User is already assigned to this project");
		}
		final CUserProjectSettings settings = new CUserProjectSettings();
		settings.setUser(user);
		settings.setProject(project);
		// TODO: Update to handle CUserProjectRole instead of String
		// settings.setRole(role);
		settings.setPermission(permission);
		validateRelationship(settings);
		// Save the entity first
		final CUserProjectSettings savedSettings = save(settings);
		// Maintain bidirectional relationships
		user.addProjectSettings(savedSettings);
		project.addUserSettings(savedSettings);
		return savedSettings;
	}

	@Override
	protected CUserProjectSettings createRelationshipInstance(final Long userId, final Long projectId) {
		// Note: In a real implementation, you would fetch the actual entities from their
		// services This method should not be used directly - instead use the service
		// methods that accept entities
		throw new UnsupportedOperationException("Use addUserToProject(CUser, CProject, String, String) method instead");
	}

	@Override
	@Transactional (readOnly = true)
	public List<CUserProjectSettings> findByChildEntityId(final Long projectId) {
		LOGGER.debug("Finding user project settings for project ID: {}", projectId);
		return repository.findByProjectId(projectId);
	}

	@Override
	@Transactional (readOnly = true)
	public List<CUserProjectSettings> findByParentEntityId(final Long userId) {
		return repository.findByUserId(userId);
	}

	/** Find user project settings by project */
	@Transactional (readOnly = true)
	public List<CUserProjectSettings> findByProject(final CProject project) {
		Check.notNull(project, "Project cannot be null");
		return findByChildEntityId(project.getId());
	}

	/** Find user project settings by user */
	@Transactional (readOnly = true)
	public List<CUserProjectSettings> findByUser(final CUser user) {
		Check.notNull(user, "User cannot be null");
		return findByParentEntityId(user.getId());
	}

	@Override
	@Transactional (readOnly = true)
	public Optional<CUserProjectSettings> findRelationship(final Long userId, final Long projectId) {
		return repository.findByUserIdAndProjectId(userId, projectId);
	}

	@Override
	protected Class<CUserProjectSettings> getEntityClass() { return CUserProjectSettings.class; }

	@Override
	@Transactional (readOnly = true)
	public boolean relationshipExists(final Long userId, final Long projectId) {
		return repository.existsByUserIdAndProjectId(userId, projectId);
	}

	/** Remove user from project */
	@Transactional
	public void removeUserFromProject(final CUser user, final CProject project) {
		LOGGER.debug("Removing user {} from project {}", user, project);
		Check.notNull(user, "User cannot be null");
		Check.notNull(project, "Project cannot be null");
		Check.notNull(user.getId(), "User must have a valid ID");
		Check.notNull(project.getId(), "Project must have a valid ID");
		// Find the relationship first to maintain bidirectional collections
		final Optional<CUserProjectSettings> settingsOpt = findRelationship(user.getId(), project.getId());
		if (settingsOpt.isEmpty()) {
			LOGGER.warn("No relationship found between user {} and project {}", user.getId(), project.getId());
			return;
		}
		final CUserProjectSettings settings = settingsOpt.get();
		// Remove from bidirectional collections first to maintain consistency
		user.removeProjectSettings(settings);
		project.removeUserSettings(settings);
		// Delete the relationship using the parent method that handles ID checking
		deleteRelationship(user.getId(), project.getId());
		LOGGER.debug("Successfully removed user {} from project {}", user.getId(), project.getId());
	}

	/** Refresh user project settings from database - useful after deletion to ensure collections are synchronized */
	@Transactional (readOnly = true)
	public void refreshUserProjectCollections(final CUser user, final CProject project) {
		if (user != null && user.getId() != null) {
			final List<CUserProjectSettings> userSettings = findByParentEntityId(user.getId());
			user.setProjectSettings(userSettings);
		}
		if (project != null && project.getId() != null) {
			final List<CUserProjectSettings> projectSettings = findByChildEntityId(project.getId());
			project.getUserSettings().clear();
			project.getUserSettings().addAll(projectSettings);
		}
	}

	/** Update user role and permissions for a project */
	@Transactional
	public CUserProjectSettings updateUserProjectRole(final CUser user, final CProject project, final String role, final String permission) {
		LOGGER.debug("Updating user {} project {} role to {} and permission to {}", user, project, role, permission);
		final Optional<CUserProjectSettings> settingsOpt = findRelationship(user.getId(), project.getId());
		if (settingsOpt.isEmpty()) {
			throw new IllegalArgumentException("User is not assigned to this project");
		}
		final CUserProjectSettings settings = settingsOpt.get();
		// TODO: Update to handle CUserProjectRole instead of String
		// settings.setRole(role);
		settings.setPermission(permission);
		return updateRelationship(settings);
	}

	@Override
	protected void validateRelationship(final CUserProjectSettings relationship) {
		super.validateRelationship(relationship);
		Check.notNull(relationship, "Relationship cannot be null");
		Check.notNull(relationship.getUser(), "User cannot be null");
		Check.notNull(relationship.getProject(), "Project cannot be null");
		// Role can be null according to entity definition (nullable = true)
		// Check.notNull(relationship.getRole(), "User role cannot be null");
		// Permission can also be null according to entity definition
		// Check.notNull(relationship.getPermission(), "Permission cannot be null");
	}
}
