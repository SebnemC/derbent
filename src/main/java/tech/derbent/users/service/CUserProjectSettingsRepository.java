package tech.derbent.users.service;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.derbent.abstracts.services.CAbstractRepository;
import tech.derbent.users.domain.CUserProjectSettings;

/**
 * Repository interface for CUserProjectSettings entity.
 * Provides database access methods for user-project settings management.
 */
public interface CUserProjectSettingsRepository extends CAbstractRepository<CUserProjectSettings> {

	/**
	 * Finds all user project settings for a specific user.
	 * @param userId the ID of the user
	 * @return List of CUserProjectSettings for the user
	 */
	@Query("SELECT ups FROM CUserProjectSettings ups WHERE ups.user.id = :userId")
	List<CUserProjectSettings> findByUserId(@Param("userId") Long userId);

	/**
	 * Finds all user project settings for a specific project.
	 * @param projectId the ID of the project
	 * @return List of CUserProjectSettings for the project
	 */
	@Query("SELECT ups FROM CUserProjectSettings ups WHERE ups.project.id = :projectId")
	List<CUserProjectSettings> findByProjectId(@Param("projectId") Long projectId);

	/**
	 * Finds user project settings by user ID and project ID.
	 * @param userId the ID of the user
	 * @param projectId the ID of the project
	 * @return CUserProjectSettings if found, null otherwise
	 */
	@Query("SELECT ups FROM CUserProjectSettings ups WHERE ups.user.id = :userId AND ups.project.id = :projectId")
	CUserProjectSettings findByUserIdAndProjectId(@Param("userId") Long userId, @Param("projectId") Long projectId);

	/**
	 * Finds all user project settings by role.
	 * @param role the role to search for
	 * @return List of CUserProjectSettings with the specified role
	 */
	@Query("SELECT ups FROM CUserProjectSettings ups WHERE ups.role = :role")
	List<CUserProjectSettings> findByRole(@Param("role") String role);
}