package tech.derbent.activities.service;

import java.time.Clock;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.projects.domain.CProject;

/**
 * Service layer for CActivity entity management.
 * Implements MVC architecture principles - handles business logic and data operations.
 * Layer: Service (MVC)
 */
@Service
@PreAuthorize("isAuthenticated()")
public class CActivityService extends CAbstractService<CActivity> {

	CActivityService(final CActivityRepository repository, final Clock clock) {
		super(repository, clock);
	}

	/**
	 * Creates a new activity entity with the given name.
	 * @param name The name of the activity to create
	 * @throws RuntimeException if name is "fail" (for testing error handling)
	 */
	@Transactional
	public void createEntity(final String name) {
		if ("fail".equals(name)) {
			throw new RuntimeException("This is for testing the error handler");
		}
		final var entity = new CActivity();
		entity.setName(name);
		repository.saveAndFlush(entity);
	}

	/**
	 * Finds all activities associated with the given project.
	 * Uses JOIN FETCH to eagerly load project associations and avoid lazy loading issues.
	 * @param project The project to filter activities by
	 * @return List of activities belonging to the specified project
	 */
	@Transactional
	public List<CActivity> findByProject(final CProject project) {
		return ((CActivityRepository) repository).findByProject(project);
	}

	/**
	 * Gets paginated list of activities for the specified project.
	 * Uses JOIN FETCH to eagerly load project associations and avoid lazy loading issues.
	 * @param project The project to filter activities by
	 * @param pageable Pagination information
	 * @return Page of activities belonging to the specified project
	 */
	@Transactional
	public Page<CActivity> listByProject(final CProject project, final Pageable pageable) {
		return ((CActivityRepository) repository).findByProject(project, pageable);
	}
}
