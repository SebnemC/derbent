package tech.derbent.users.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.users.domain.CUserProjectSettings;

/**
 * Service class for managing CUserProjectSettings entities.
 * Provides business logic for user-project settings operations.
 */
@Service
@PreAuthorize("isAuthenticated()")
@Transactional(readOnly = true) // Default to read-only transactions for better performance
public class CUserProjectSettingsService extends CAbstractService<CUserProjectSettings> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CUserProjectSettingsService.class);

	private final CUserProjectSettingsRepository repository;

	public CUserProjectSettingsService(final CUserProjectSettingsRepository repository, final Clock clock) {
		super(repository, clock);
		this.repository = repository;
		LOGGER.info("CUserProjectSettingsService initialized");
	}

	@Override
	protected Class<CUserProjectSettings> getEntityClass() {
		return CUserProjectSettings.class;
	}

	/**
	 * Finds all user project settings for a specific user.
	 * @param userId the ID of the user
	 * @return List of CUserProjectSettings for the user
	 */
	public List<CUserProjectSettings> findByUserId(final Long userId) {
		LOGGER.debug("Finding user project settings for user ID: {}", userId);
		return repository.findByUserId(userId);
	}

	/**
	 * Finds all user project settings for a specific project.
	 * @param projectId the ID of the project
	 * @return List of CUserProjectSettings for the project
	 */
	public List<CUserProjectSettings> findByProjectId(final Long projectId) {
		LOGGER.debug("Finding user project settings for project ID: {}", projectId);
		return repository.findByProjectId(projectId);
	}

	/**
	 * Finds user project settings by user ID and project ID.
	 * @param userId the ID of the user
	 * @param projectId the ID of the project
	 * @return Optional containing CUserProjectSettings if found
	 */
	public Optional<CUserProjectSettings> findByUserIdAndProjectId(final Long userId, final Long projectId) {
		LOGGER.debug("Finding user project settings for user ID: {} and project ID: {}", userId, projectId);
		final CUserProjectSettings settings = repository.findByUserIdAndProjectId(userId, projectId);
		return Optional.ofNullable(settings);
	}

	/**
	 * Finds all user project settings by role.
	 * @param role the role to search for
	 * @return List of CUserProjectSettings with the specified role
	 */
	public List<CUserProjectSettings> findByRole(final String role) {
		LOGGER.debug("Finding user project settings for role: {}", role);
		return repository.findByRole(role);
	}

	/**
	 * Creates or updates user project settings.
	 * @param settings the CUserProjectSettings to save
	 * @return the saved CUserProjectSettings
	 */
	@Transactional
	public CUserProjectSettings saveUserProjectSettings(final CUserProjectSettings settings) {
		LOGGER.debug("Saving user project settings for user: {} and project: {}",
			settings.getUser() != null ? settings.getUser().getName() : "null",
			settings.getProject() != null ? settings.getProject().getName() : "null");
		
		validateUserProjectSettings(settings);
		return save(settings);
	}

	/**
	 * Validates user project settings before saving.
	 * @param settings the settings to validate
	 * @throws IllegalArgumentException if validation fails
	 */
	private void validateUserProjectSettings(final CUserProjectSettings settings) {
		if (settings == null) {
			throw new IllegalArgumentException("UserProjectSettings cannot be null");
		}
		if (settings.getUser() == null) {
			throw new IllegalArgumentException("User cannot be null");
		}
		if (settings.getProject() == null) {
			throw new IllegalArgumentException("Project cannot be null");
		}
		
		// Check for duplicate assignment
		if (settings.getId() == null) { // Only check for new entities
			final Optional<CUserProjectSettings> existing = findByUserIdAndProjectId(
				settings.getUser().getId(), settings.getProject().getId());
			if (existing.isPresent()) {
				throw new IllegalArgumentException(
					"User is already assigned to this project. Use update instead.");
			}
		}
	}

	/**
	 * Removes user from project by deleting the settings.
	 * @param userId the user ID
	 * @param projectId the project ID
	 * @return true if settings were found and deleted
	 */
	@Transactional
	public boolean removeUserFromProject(final Long userId, final Long projectId) {
		LOGGER.debug("Removing user {} from project {}", userId, projectId);
		final Optional<CUserProjectSettings> settings = findByUserIdAndProjectId(userId, projectId);
		if (settings.isPresent()) {
			delete(settings.get());
			return true;
		}
		return false;
	}
}