package tech.derbent.activities.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.derbent.abstracts.services.CAbstractRepository;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.projects.domain.CProject;

/**
 * Repository for CActivity entities with project-based filtering.
 * Implements lazy loading fix with JOIN FETCH queries to avoid LazyInitializationException.
 */
public interface CActivityRepository extends CAbstractRepository<CActivity> {
	
	/**
	 * Finds all activities by project with eager loading of project association.
	 * Using JOIN FETCH to avoid lazy loading issues when accessing project properties.
	 */
	@Query("SELECT a FROM CActivity a JOIN FETCH a.project p WHERE p = :project")
	List<CActivity> findByProject(@Param("project") CProject project);
	
	/**
	 * Finds paginated activities by project with eager loading of project association.
	 * Using JOIN FETCH to avoid lazy loading issues when accessing project properties.
	 */
	@Query("SELECT a FROM CActivity a JOIN FETCH a.project p WHERE p = :project")
	Page<CActivity> findByProject(@Param("project") CProject project, Pageable pageable);
}
