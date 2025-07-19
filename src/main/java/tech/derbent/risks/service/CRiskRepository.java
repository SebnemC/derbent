package tech.derbent.risks.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.derbent.abstracts.services.CAbstractRepository;
import tech.derbent.projects.domain.CProject;
import tech.derbent.risks.domain.CRisk;

/**
 * Repository for CRisk entities with project-based filtering.
 * Implements lazy loading fix with JOIN FETCH queries to avoid LazyInitializationException.
 */
public interface CRiskRepository extends CAbstractRepository<CRisk> {
	
	/**
	 * Finds all risks by project with eager loading of project association.
	 * Using JOIN FETCH to avoid lazy loading issues when accessing project properties.
	 */
	@Query("SELECT r FROM CRisk r JOIN FETCH r.project p WHERE p = :project")
	List<CRisk> findByProject(@Param("project") CProject project);
	
	/**
	 * Finds paginated risks by project with eager loading of project association.
	 * Using JOIN FETCH to avoid lazy loading issues when accessing project properties.
	 */
	@Query("SELECT r FROM CRisk r JOIN FETCH r.project p WHERE p = :project")
	Page<CRisk> findByProject(@Param("project") CProject project, Pageable pageable);
}