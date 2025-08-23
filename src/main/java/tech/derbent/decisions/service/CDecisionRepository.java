package tech.derbent.decisions.service;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.abstracts.services.CEntityOfProjectRepository;
import tech.derbent.decisions.domain.CDecision;
import tech.derbent.projects.domain.CProject;

public interface CDecisionRepository extends CEntityOfProjectRepository<CDecision> {

	@Query (
		"SELECT d FROM CDecision d " + "LEFT JOIN FETCH d.project " + "LEFT JOIN FETCH d.assignedTo " + "LEFT JOIN FETCH d.createdBy "
				+ "LEFT JOIN FETCH d.decisionType " + "LEFT JOIN FETCH d.decisionStatus " + "LEFT JOIN FETCH d.accountableUser " + "WHERE d.id = :id"
	)
	Optional<CDecision> findByIdWithAllRelationships(@Param ("id") Long id);
	@Query (
		"SELECT d FROM CDecision d " + "LEFT JOIN FETCH d.project " + "LEFT JOIN FETCH d.assignedTo " + "LEFT JOIN FETCH d.createdBy "
				+ "LEFT JOIN FETCH d.decisionType " + "LEFT JOIN FETCH d.decisionStatus " + "LEFT JOIN FETCH d.accountableUser "
				+ "WHERE d.project = :project"
	)
	Page<CDecision> findByProjectWithAllRelationships(@Param ("project") CProject project, Pageable pageable);
}
