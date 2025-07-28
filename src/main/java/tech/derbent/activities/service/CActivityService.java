package tech.derbent.activities.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
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
import tech.derbent.activities.domain.CActivityPriority;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.activities.domain.CActivityType;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

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
		LOGGER.info("countByProject called with project: {}", project);

		if (project == null) {
			return 0L;
		}
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
	 * @param project the project to search for
	 * @return list of activities for the project
	 */
	public List<CActivity> findByProject(final CProject project) {
		LOGGER.info("findByProject called with project: {}", project);

		if (project == null) {
			return List.of();
		}
		return ((CActivityRepository) repository).findByProject(project);
	}

	/**
	 * Overrides the base get method to eagerly load CActivityType and CActivityStatus
	 * relationships. This prevents LazyInitializationException when the entity is used in
	 * UI contexts.
	 * @param id the activity ID
	 * @return optional CActivity with loaded activityType and activityStatus
	 */
	@Override
	@Transactional (readOnly = true)
	public Optional<CActivity> get(final Long id) {
		LOGGER.info("get called with id: {}", id);

		if (id == null) {
			return Optional.empty();
		}
		final Optional<CActivity> entity = ((CActivityRepository) repository)
			.findByIdWithActivityTypeStatusAndProject(id);
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
		// Get all activities for the project with type and status loaded
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
		// Get all activities for the project with type and status loaded
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
	 * Gets an activity by ID with eagerly loaded CActivityType and CActivityStatus
	 * relationships. This method should be used in UI contexts to prevent
	 * LazyInitializationException.
	 * @param id the activity ID
	 * @return optional CActivity with loaded activityType and activityStatus
	 */
	@Transactional (readOnly = true)
	public Optional<CActivity> getWithActivityTypeAndStatus(final Long id) {
		LOGGER.debug(
			"Getting CActivity with ID {} and eagerly loading CActivityType and CActivityStatus",
			id);
		return ((CActivityRepository) repository)
			.findByIdWithActivityTypeStatusAndProject(id);
	}

	/**
	 * Initializes lazy fields for CActivity entity to prevent
	 * LazyInitializationException. Specifically handles the lazy-loaded CActivityType and
	 * CActivityStatus relationships.
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
			super.initializeLazyFields(entity);
			initializeLazyRelationship(entity.getActivityType());
			initializeLazyRelationship(entity.getAssignedTo());
			initializeLazyRelationship(entity.getCreatedBy());
			initializeLazyRelationship(entity.getStatus());
			initializeLazyRelationship(entity.getPriority());
			initializeLazyRelationship(entity.getParentActivity());
			initializeLazyRelationship(entity.getProject());
		} catch (final Exception e) {
			LOGGER.warn("Error initializing lazy fields for CActivity with ID: {}",
				entity.getId(), e);
		}
	}

	/**
	 * Gets paginated activities by project with eagerly loaded relationships.
	 */
	@Transactional (readOnly = true)
	public Page<CActivity> listByProject(final CProject project,
		final Pageable pageable) {
		LOGGER.debug("Getting paginated activities for project {} with eager loading",
			project.getName());
		return ((CActivityRepository) repository).findByProjectWithTypeAndStatus(project,
			pageable);
	}

	// Auxiliary methods for sample data initialization and activity setup

	/**
	 * Auxiliary method to set activity type and basic properties for an activity.
	 * Following coding guidelines to use service layer methods instead of direct field setting.
	 * @param activity the activity to configure
	 * @param activityType the activity type to set
	 * @param description the description to set
	 * @return the configured activity
	 */
	@Transactional
	public CActivity setActivityType(final CActivity activity, final CActivityType activityType, final String description) {
		LOGGER.info("setActivityType called for activity: {} with type: {}", 
			activity != null ? activity.getName() : "null", 
			activityType != null ? activityType.getName() : "null");

		if (activity == null) {
			LOGGER.warn("Activity is null, cannot set activity type");
			return null;
		}

		activity.setActivityType(activityType);
		if (description != null && !description.isEmpty()) {
			activity.setDescription(description);
		}

		return save(activity);
	}

	/**
	 * Auxiliary method to set time tracking information for an activity.
	 * @param activity the activity to configure
	 * @param estimatedHours estimated hours for completion
	 * @param actualHours actual hours spent
	 * @param remainingHours remaining hours to complete
	 * @return the configured activity
	 */
	@Transactional
	public CActivity setTimeTracking(final CActivity activity, final BigDecimal estimatedHours, 
		final BigDecimal actualHours, final BigDecimal remainingHours) {
		LOGGER.info("setTimeTracking called for activity: {} with estimated: {}, actual: {}, remaining: {}",
			activity != null ? activity.getName() : "null", estimatedHours, actualHours, remainingHours);

		if (activity == null) {
			LOGGER.warn("Activity is null, cannot set time tracking");
			return null;
		}

		if (estimatedHours != null) {
			activity.setEstimatedHours(estimatedHours);
		}
		if (actualHours != null) {
			activity.setActualHours(actualHours);
		}
		if (remainingHours != null) {
			activity.setRemainingHours(remainingHours);
		}

		return save(activity);
	}

	/**
	 * Auxiliary method to set assigned users and creator for an activity.
	 * @param activity the activity to configure
	 * @param assignedTo the user assigned to the activity
	 * @param createdBy the user who created the activity
	 * @return the configured activity
	 */
	@Transactional
	public CActivity setAssignedUsers(final CActivity activity, final CUser assignedTo, final CUser createdBy) {
		LOGGER.info("setAssignedUsers called for activity: {} with assignedTo: {}, createdBy: {}",
			activity != null ? activity.getName() : "null",
			assignedTo != null ? assignedTo.getName() : "null",
			createdBy != null ? createdBy.getName() : "null");

		if (activity == null) {
			LOGGER.warn("Activity is null, cannot set assigned users");
			return null;
		}

		if (assignedTo != null) {
			activity.setAssignedTo(assignedTo);
		}
		if (createdBy != null) {
			activity.setCreatedBy(createdBy);
		}

		return save(activity);
	}

	/**
	 * Auxiliary method to set budget information for an activity.
	 * @param activity the activity to configure
	 * @param estimatedCost estimated cost for completion
	 * @param actualCost actual cost spent
	 * @param hourlyRate hourly rate for cost calculations
	 * @return the configured activity
	 */
	@Transactional
	public CActivity setBudgetInfo(final CActivity activity, final BigDecimal estimatedCost, 
		final BigDecimal actualCost, final BigDecimal hourlyRate) {
		LOGGER.info("setBudgetInfo called for activity: {} with estimated cost: {}, actual cost: {}, hourly rate: {}",
			activity != null ? activity.getName() : "null", estimatedCost, actualCost, hourlyRate);

		if (activity == null) {
			LOGGER.warn("Activity is null, cannot set budget info");
			return null;
		}

		if (estimatedCost != null) {
			activity.setEstimatedCost(estimatedCost);
		}
		if (actualCost != null) {
			activity.setActualCost(actualCost);
		}
		if (hourlyRate != null) {
			activity.setHourlyRate(hourlyRate);
		}

		return save(activity);
	}

	/**
	 * Auxiliary method to set status, priority and progress for an activity.
	 * @param activity the activity to configure
	 * @param status the activity status
	 * @param priority the activity priority
	 * @param progressPercentage completion percentage (0-100)
	 * @return the configured activity
	 */
	@Transactional
	public CActivity setStatusAndPriority(final CActivity activity, final CActivityStatus status, 
		final CActivityPriority priority, final Integer progressPercentage) {
		LOGGER.info("setStatusAndPriority called for activity: {} with status: {}, priority: {}, progress: {}%",
			activity != null ? activity.getName() : "null",
			status != null ? status.getName() : "null",
			priority != null ? priority.getName() : "null",
			progressPercentage);

		if (activity == null) {
			LOGGER.warn("Activity is null, cannot set status and priority");
			return null;
		}

		if (status != null) {
			activity.setStatus(status);
		}
		if (priority != null) {
			activity.setPriority(priority);
		}
		if (progressPercentage != null) {
			activity.setProgressPercentage(progressPercentage);
		}

		return save(activity);
	}

	/**
	 * Auxiliary method to set date information for an activity.
	 * @param activity the activity to configure
	 * @param startDate planned start date
	 * @param dueDate expected completion date
	 * @param completionDate actual completion date (optional)
	 * @return the configured activity
	 */
	@Transactional
	public CActivity setDateInfo(final CActivity activity, final LocalDate startDate, 
		final LocalDate dueDate, final LocalDate completionDate) {
		LOGGER.info("setDateInfo called for activity: {} with start: {}, due: {}, completion: {}",
			activity != null ? activity.getName() : "null", startDate, dueDate, completionDate);

		if (activity == null) {
			LOGGER.warn("Activity is null, cannot set date info");
			return null;
		}

		if (startDate != null) {
			activity.setStartDate(startDate);
		}
		if (dueDate != null) {
			activity.setDueDate(dueDate);
		}
		if (completionDate != null) {
			activity.setCompletionDate(completionDate);
		}

		return save(activity);
	}

	/**
	 * Auxiliary method to set additional information for an activity.
	 * @param activity the activity to configure
	 * @param acceptanceCriteria criteria for completion
	 * @param notes additional notes
	 * @param parentActivity parent activity for hierarchical structure
	 * @return the configured activity
	 */
	@Transactional
	public CActivity setAdditionalInfo(final CActivity activity, final String acceptanceCriteria, 
		final String notes, final CActivity parentActivity) {
		LOGGER.info("setAdditionalInfo called for activity: {} with parent: {}",
			activity != null ? activity.getName() : "null",
			parentActivity != null ? parentActivity.getName() : "null");

		if (activity == null) {
			LOGGER.warn("Activity is null, cannot set additional info");
			return null;
		}

		if (acceptanceCriteria != null && !acceptanceCriteria.isEmpty()) {
			activity.setAcceptanceCriteria(acceptanceCriteria);
		}
		if (notes != null && !notes.isEmpty()) {
			activity.setNotes(notes);
		}
		if (parentActivity != null) {
			activity.setParentActivity(parentActivity);
		}

		return save(activity);
	}
}
