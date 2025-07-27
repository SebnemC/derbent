package tech.derbent.activities.service;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CAbstractNamedEntityService;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.activities.domain.CActivityType;
import tech.derbent.projects.domain.CProject;

@Service
@PreAuthorize ("isAuthenticated()")
public class CActivityService extends CAbstractNamedEntityService<CActivity> {

	CActivityService(final CActivityRepository repository, final Clock clock) {
		super(repository, clock);
	}

	/**
	 * Counts the number of activities for a specific project.
	 * @param project the project
	 * @return count of activities for the project
	 */
	@PreAuthorize ("permitAll()")
	public long countByProject(final CProject project) {
		LOGGER.info("Counting activities for project: {}", project.getName());
		return ((CActivityRepository) repository).countByProject(project);
	}
	// Now using the inherited createEntity(String name) method from
	// CAbstractNamedEntityService The original createEntity method is replaced by the
	// parent class implementation

	@Override
	protected CActivity createNewEntityInstance() {
		return new CActivity();
	}

	/**
	 * Helper method to create a placeholder CActivityStatus for activities without a
	 * status.
	 * @return a CActivityStatus instance representing "No Status"
	 */
	private CActivityStatus createNoStatusInstance() {
		final CActivityStatus noStatus = new CActivityStatus();
		noStatus.setName("No Status");
		noStatus.setDescription("Activities without an assigned status");
		return noStatus;
	}

	/**
	 * Helper method to create a placeholder CActivityType for activities without a type.
	 * @return a CActivityType instance representing "No Type"
	 */
	private CActivityType createNoTypeInstance() {
		final CActivityType noType = new CActivityType();
		noType.setName("No Type");
		noType.setDescription("Activities without an assigned type");
		return noType;
	}

	/**
	 * Finds activities by project.
	 */
	public List<CActivity> findByProject(final CProject project) {
		return ((CActivityRepository) repository).findByProject(project);
	}

	/**
	 * Overrides the base get method to eagerly load CActivityType, CActivityStatus,
	 * and CProject relationships. This prevents LazyInitializationException when the
	 * entity is used in UI contexts.
	 * @param id the activity ID
	 * @return optional CActivity with loaded activityType, activityStatus, and project
	 */
	@Override
	@Transactional (readOnly = true)
	public Optional<CActivity> get(final Long id) {
		LOGGER.debug(
			"Getting CActivity with ID {} (overridden to eagerly load activityType, activityStatus, and project)",
			id);
		final Optional<CActivity> entity =
			((CActivityRepository) repository).findByIdWithActivityTypeStatusAndProject(id);
		// Initialize lazy fields if entity is present (for any other potential lazy
		// relationships)
		entity.ifPresent(this::initializeLazyFields);
		return entity;
	}

	/**
	 * Gets all activities for a project grouped by activity status. Activities without a
	 * status are grouped under a "No Status" key.
	 * @param project the project to get activities for
	 * @return map of activity status to list of activities
	 */
	@Transactional (readOnly = true)
	public Map<CActivityStatus, List<CActivity>>
		getActivitiesGroupedByStatus(final CProject project) {
		LOGGER.debug("Getting activities grouped by status for project: {}",
			project.getName());
		// Get all activities for the project with type, status, and project loaded
		final List<CActivity> activities =
			((CActivityRepository) repository).findByProjectWithTypeAndStatus(project);
		// Group by activity status, handling null statuses
		return activities.stream()
			.collect(Collectors.groupingBy(activity -> activity.getStatus() != null
				? activity.getStatus() : createNoStatusInstance(), Collectors.toList()));
	}

	/**
	 * Gets all activities for a project grouped by activity type. Activities without a
	 * type are grouped under a "No Type" key.
	 * @param project the project to get activities for
	 * @return map of activity type to list of activities
	 */
	@Transactional (readOnly = true)
	public Map<CActivityType, List<CActivity>>
		getActivitiesGroupedByType(final CProject project) {
		LOGGER.debug("Getting activities grouped by type for project: {}",
			project.getName());
		// Get all activities for the project with type, status, and project loaded
		final List<CActivity> activities =
			((CActivityRepository) repository).findByProjectWithTypeAndStatus(project);
		// Group by activity type, handling null types
		return activities.stream()
			.collect(Collectors.groupingBy(
				activity -> activity.getActivityType() != null
					? activity.getActivityType() : createNoTypeInstance(),
				Collectors.toList()));
	}

	/**
	 * Gets an activity by ID with eagerly loaded CActivityType relationship. This method
	 * should be used in UI contexts to prevent LazyInitializationException.
	 * @param id the activity ID
	 * @return optional CActivity with loaded activityType
	 */
	@Transactional (readOnly = true)
	public Optional<CActivity> getWithActivityType(final Long id) {
		LOGGER.debug("Getting CActivity with ID {} and eagerly loading CActivityType",
			id);
		return ((CActivityRepository) repository).findByIdWithActivityType(id);
	}

	/**
	 * Gets an activity by ID with eagerly loaded CActivityType, CActivityStatus,
	 * and CProject relationships. This method should be used in UI contexts to prevent
	 * LazyInitializationException.
	 * @param id the activity ID
	 * @return optional CActivity with loaded activityType, activityStatus, and project
	 */
	@Transactional (readOnly = true)
	public Optional<CActivity> getWithActivityTypeAndStatus(final Long id) {
		LOGGER.debug(
			"Getting CActivity with ID {} and eagerly loading CActivityType, CActivityStatus, and CProject",
			id);
		return ((CActivityRepository) repository).findByIdWithActivityTypeStatusAndProject(id);
	}

	/**
	 * Initializes lazy fields for CActivity entity to prevent
	 * LazyInitializationException. Specifically handles the lazy-loaded CActivityType,
	 * CActivityStatus, and CProject relationships.
	 * @param entity the CActivity entity to initialize
	 */
	@Override
	protected void initializeLazyFields(final CActivity entity) {

		if (entity == null) {
			return;
		}
		LOGGER.debug("Initializing lazy fields for CActivity with ID: {}",
			entity.getId());

		try {
			// Initialize the entity itself first
			super.initializeLazyFields(entity);
			// Initialize the lazy-loaded CActivityType relationship
			initializeLazyRelationship(entity.getActivityType(), "CActivityType");
			// Initialize the lazy-loaded CActivityStatus relationship
			initializeLazyRelationship(entity.getStatus(), "CActivityStatus");
			// Initialize the lazy-loaded CProject relationship
			initializeLazyRelationship(entity.getProject(), "CProject");
		} catch (final Exception e) {
			LOGGER.warn("Error initializing lazy fields for CActivity with ID: {}",
				entity.getId(), e);
		}
	}

	/**
	 * Gets paginated activities by project with eagerly loaded relationships including
	 * project, activityType, and status.
	 */
	@Transactional (readOnly = true)
	public Page<CActivity> listByProject(final CProject project,
		final Pageable pageable) {
		LOGGER.debug(
			"Getting paginated activities for project {} with eager loading of project, type, and status relationships",
			project.getName());
		return ((CActivityRepository) repository).findByProjectWithTypeAndStatus(project,
			pageable);
	}
}
