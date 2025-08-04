package tech.derbent.activities.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.projects.domain.CProject;

/**
 * CActivityStatusService - Service class for managing CActivityStatus entities. Layer:
 * Service (MVC) Provides business logic for activity status management including CRUD
 * operations, validation, and workflow management.
 */
@Service
@Transactional
public class CActivityStatusService extends CEntityOfProjectService<CActivityStatus> {

	private static final Logger LOGGER =
		LoggerFactory.getLogger(CActivityStatusService.class);

	private final CActivityStatusRepository activityStatusRepository;

	@Autowired
	public CActivityStatusService(
		final CActivityStatusRepository activityStatusRepository, final Clock clock) {
		super(activityStatusRepository, clock);

		if (activityStatusRepository == null) {
			LOGGER.error(
				"CActivityStatusService constructor - Repository parameter is null");
			throw new IllegalArgumentException(
				"Activity status repository cannot be null");
		}
		this.activityStatusRepository = activityStatusRepository;
	}

	/**
	 * Create default activity statuses if they don't exist. This method should be called
	 * during application startup.
	 */
	public void createDefaultStatusesIfNotExist() {
		LOGGER.debug(
			"createDefaultStatusesIfNotExist() - Creating default activity statuses");
		// TODO implement default statuses creation logic
	}

	/**
	 * Delete an activity status by ID.
	 * @param id the status ID - must not be null
	 * @throws IllegalArgumentException if the ID is null
	 */
	public void deleteById(final Long id) {
		LOGGER.debug("deleteById(id={}) - Deleting activity status", id);

		if (id == null) {
			LOGGER.error("deleteById(id=null) - ID parameter is null");
			throw new IllegalArgumentException("Activity status ID cannot be null");
		}
		final Optional<CActivityStatus> existing = activityStatusRepository.findById(id);

		if (!existing.isPresent()) {
			LOGGER.warn("deleteById(id={}) - Activity status not found", id);
			return;
		}

		try {
			activityStatusRepository.deleteById(id);
			LOGGER.debug("deleteById(id={}) - Successfully deleted activity status", id);
		} catch (final Exception e) {
			LOGGER.error("deleteById(id={}) - Error deleting activity status: {}", id,
				e.getMessage(), e);
			throw new RuntimeException("Failed to delete activity status", e);
		}
	}

	/**
	 * Check if an activity status name exists (case-insensitive).
	 * @param name the name to check - must not be null
	 * @return true if the name exists, false otherwise
	 */
	@Override
	@Transactional (readOnly = true)
	public boolean existsByName(final String name) {
		LOGGER.debug("existsByName(name={}) - Checking if activity status name exists",
			name);

		if ((name == null) || name.trim().isEmpty()) {
			LOGGER.warn("existsByName(name={}) - Name parameter is null or empty", name);
			return false;
		}
		final boolean exists =
			activityStatusRepository.existsByNameIgnoreCase(name.trim());
		LOGGER.debug("existsByName(name={}) - Name exists: {}", name, exists);
		return exists;
	}

	/**
	 * Find all active (non-final) statuses for a specific project. This replaces the
	 * problematic findAllActiveStatuses() method that didn't require project.
	 * @param project the project to find statuses for
	 * @return List of active statuses for the project
	 */
	@Transactional (readOnly = true)
	public List<CActivityStatus> findAllActiveStatusesByProject(final CProject project) {
		LOGGER.debug(
			"findAllActiveStatusesByProject() - Finding all active activity statuses for project: {}",
			project != null ? project.getName() : "null");

		if (project == null) {
			LOGGER.warn("findAllActiveStatusesByProject called with null project");
			return List.of();
		}
		// Use the inherited method and filter for active statuses
		return findAllByProject(project).stream().filter(status -> !status.isFinal())
			.toList();
	}

	/**
	 * Find all activity statuses for a specific project ordered by sort order. This
	 * replaces the problematic findAll() method that didn't require project.
	 * @param project the project to find statuses for
	 * @return List of activity statuses for the project
	 */
	@Override
	@Transactional (readOnly = true)
	public List<CActivityStatus> findAllByProject(final CProject project) {
		LOGGER.debug("findAllByProject() - Finding all activity statuses for project: {}",
			project != null ? project.getName() : "null");

		if (project == null) {
			LOGGER.warn("findAllByProject called with null project");
			return List.of();
		}
		// Use the inherited method from CEntityOfProjectService
		return super.findAllByProject(project);
	}

	/**
	 * Find all final statuses (completed/cancelled states) for a specific project. This
	 * replaces the problematic findAllFinalStatuses() method that didn't require project.
	 * @param project the project to find statuses for
	 * @return List of final statuses for the project
	 */
	@Transactional (readOnly = true)
	public List<CActivityStatus> findAllFinalStatusesByProject(final CProject project) {
		LOGGER.debug(
			"findAllFinalStatusesByProject() - Finding all final activity statuses for project: {}",
			project != null ? project.getName() : "null");

		if (project == null) {
			LOGGER.warn("findAllFinalStatusesByProject called with null project");
			return List.of();
		}
		// Use the inherited method and filter for final statuses
		return findAllByProject(project).stream().filter(status -> status.isFinal())
			.toList();
	}

	/**
	 * Find the default status for new activities.
	 * @return Optional containing the default status if found
	 */
	@Transactional (readOnly = true)
	public Optional<CActivityStatus> findDefaultStatus(final CProject project) {
		LOGGER.debug("findDefaultStatus() - Finding default activity status");
		final Optional<CActivityStatus> status =
			activityStatusRepository.findDefaultStatus(project);
		LOGGER.debug("findDefaultStatus() - Found default status: {}",
			status.isPresent());
		return status;
	}

	/**
	 * Override get() method to eagerly load project relationship and prevent
	 * LazyInitializationException.
	 * @param id the activity status ID
	 * @return optional CActivityStatus with all relationships loaded
	 */
	@Override
	@Transactional (readOnly = true)
	public Optional<CActivityStatus> getById(final Long id) {

		if (id == null) {
			return Optional.empty();
		}
		final Optional<CActivityStatus> entity = activityStatusRepository.findById(id);
		entity.ifPresent(this::initializeLazyFields);
		return entity;
	}

	@Override
	protected Class<CActivityStatus> getEntityClass() { return CActivityStatus.class; }

	/**
	 * Save or update an activity status.
	 * @param status the status to save - must not be null
	 * @return the saved status
	 * @throws IllegalArgumentException if the status is null or invalid
	 */
	@Override
	public CActivityStatus save(final CActivityStatus status) {
		LOGGER.debug("save(status={}) - Saving activity status",
			status != null ? status.getName() : "null");

		if (status == null) {
			LOGGER.error("save(activityStatus=null) - Activity status parameter is null");
			throw new IllegalArgumentException("Activity status cannot be null");
		}

		if ((status.getName() == null) || status.getName().trim().isEmpty()) {
			LOGGER.error(
				"save() - Activity status name is null or empty for status id={}",
				status.getId());
			throw new IllegalArgumentException(
				"Activity status name cannot be null or empty");
		}
		// Check for duplicate names (excluding self for updates)
		final String trimmedName = status.getName().trim();
		// search with same name and same project
		final Optional<CActivityStatus> existing = activityStatusRepository
			.findByNameAndProject(trimmedName, status.getProject());

		if (existing.isPresent()) {
			LOGGER.error("save() - Activity status name '{}' already exists",
				trimmedName);
			throw new IllegalArgumentException(
				"Activity status name '" + trimmedName + "' already exists");
		}

		try {
			final CActivityStatus savedStatus = activityStatusRepository.save(status);
			return savedStatus;
		} catch (final Exception e) {
			LOGGER.error("save() - Error saving activity status: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to save activity status", e);
		}
	}
}