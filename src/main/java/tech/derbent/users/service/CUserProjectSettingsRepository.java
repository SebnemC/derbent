package tech.derbent.users.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.services.CAbstractRepository;
import tech.derbent.users.domain.CUserProjectSettings;

/** Repository interface for CUserProjectSettings entity. Provides data access methods for user-project relationships. */
@Repository
public interface CUserProjectSettingsRepository extends CAbstractRepository<CUserProjectSettings> {

	/** Find all user project settings for a specific user */
	@Query ("SELECT ups FROM #{#entityName} ups LEFT JOIN FETCH ups.project LEFT JOIN FETCH ups.user WHERE ups.user.id = :userId")
	List<CUserProjectSettings> findByUserId(@Param ("userId") Long userId);
	/** Find all user project settings for a specific project */
	@Query ("SELECT ups FROM #{#entityName} ups LEFT JOIN FETCH ups.project LEFT JOIN FETCH ups.user WHERE ups.project.id = :projectId")
	List<CUserProjectSettings> findByProjectId(@Param ("projectId") Long projectId);
	/** Find a specific user project setting by user and project */
	@Query (
		"SELECT ups FROM #{#entityName} ups LEFT JOIN FETCH ups.project LEFT JOIN FETCH ups.user WHERE ups.user.id = :userId AND ups.project.id = :projectId"
	)
	Optional<CUserProjectSettings> findByUserIdAndProjectId(@Param ("userId") Long userId, @Param ("projectId") Long projectId);
	/** Check if a relationship exists between user and project */
	boolean existsByUserIdAndProjectId(Long userId, Long projectId);
	/** Delete a specific user-project relationship by user and project IDs. */
	@Modifying
	@Transactional
	@Query ("DELETE FROM #{#entityName} ups WHERE ups.user.id = :userId AND ups.project.id = :projectId")
	void deleteByUserIdProjectId(@Param ("userId") Long userId, @Param ("projectId") Long projectId);
}
