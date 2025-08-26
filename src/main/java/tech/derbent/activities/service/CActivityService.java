package tech.derbent.activities.service;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.abstracts.domains.CProjectItemService;
import tech.derbent.abstracts.interfaces.CKanbanService;
import tech.derbent.abstracts.services.CEntityOfProjectRepository;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.activities.domain.CActivityType;
import tech.derbent.projects.domain.CProject;

@Service
@PreAuthorize ("isAuthenticated()")
public class CActivityService extends CProjectItemService<CActivity> implements CKanbanService<CActivity, CActivityStatus> {
	public CActivityService(final CActivityRepository repository, final Clock clock) {
		super(repository, clock);
	}

	/** Helper method to create a placeholder CActivityStatus for activities without a status.
	 * @param project
	 * @return a CActivityStatus instance representing "No Status" */
	private CActivityStatus createNoStatusInstance(final CProject project) {
		final CActivityStatus noStatus = new CActivityStatus("No Status", project);
		noStatus.setDescription("Activities without an assigned status");
		return noStatus;
	}

	/** Helper method to create a placeholder CActivityType for activities without a type.
	 * @return a CActivityType instance representing "No Type" */
	private CActivityType createNoTypeInstance(final CProject project) {
		final CActivityType noType = new CActivityType("No Type", project);
		noType.setDescription("Activities without an assigned type");
		return noType;
	}

	@Transactional (readOnly = true)
	public Map<CActivityStatus, List<CActivity>> getActivitiesGroupedByStatus(final CProject project) {
		// Get all activities for the project with type and status loaded
		final List<CActivity> activities = ((CEntityOfProjectRepository<CActivity>) repository).findByProject(project);
		// Group by activity status, handling null statuses
		return activities.stream().collect(Collectors
				.groupingBy(activity -> activity.getStatus() != null ? activity.getStatus() : createNoStatusInstance(project), Collectors.toList()));
	}

	@Transactional (readOnly = true)
	public Map<CActivityType, List<CActivity>> getActivitiesGroupedByType(final CProject project) {
		LOGGER.debug("Getting activities grouped by type for project: {}", project.getName());
		// Get all activities for the project with type and status loaded
		final List<CActivity> activities = ((CEntityOfProjectRepository<CActivity>) repository).findByProject(project);
		// Group by activity type, handling null types
		return activities.stream().collect(Collectors.groupingBy(
				activity -> activity.getActivityType() != null ? activity.getActivityType() : createNoTypeInstance(project), Collectors.toList()));
	}

	@Override
	public List<CActivityStatus> getAllStatuses() {
		// This would need to be implemented by calling the status service For minimal
		// changes, returning empty list for now
		return List.of();
	}

	// CKanbanService implementation methods
	@Override
	public Map<CActivityStatus, List<CActivity>> getEntitiesGroupedByStatus(final Long projectId) {
		// Find project by ID For now, we'll use the existing method that takes CProject
		// In a real implementation, you'd want to fetch the project by ID This is a
		// simplification for the minimal change approach
		return Map.of(); // This would need proper implementation
	}

	@Override
	protected Class<CActivity> getEntityClass() { return CActivity.class; }

	@Override
	public CActivity updateEntityStatus(final CActivity entity, final CActivityStatus newStatus) {
		Check.notNull(entity, "Entity cannot be null");
		Check.notNull(newStatus, "New status cannot be null");
		entity.setStatus(newStatus);
		return save(entity);
	}
}
