package tech.derbent.users.service;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.derbent.abstracts.services.CAbstractRepository;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

public interface CUserRepository extends CAbstractRepository<CUser> {

	/**
	 * Finds all enabled login users. Useful for administration and user management.
	 * @param enabled true to find enabled users, false for disabled users
	 * @return List of CUser entities matching the enabled status
	 */
	@Query("SELECT u FROM CUser u WHERE u.enabled = :enabled")
	java.util.List<CUser> findByEnabled(@Param("enabled") boolean enabled);

	@Query("SELECT u FROM CUser u LEFT JOIN FETCH u.projectSettings WHERE u.id = :id")
	Optional<CUser> findByIdWithProjects(@Param("id") Long id);

	/**
	 * Finds a login user by their CUser (login field). This method is crucial for
	 * the authentication flow. Authentication Flow Usage: 1. User enters username
	 * in login form 2. Spring Security calls
	 * UserDetailsService.loadUserByUsername() 3. CUser uses this method to find the
	 * user 4. If found, password is verified and user is authenticated
	 * @param username the username to search for (corresponds to login field in
	 *                 CUser)
	 * @return Optional containing the CUser if found, empty otherwise
	 */
	@Query("SELECT u FROM CUser u WHERE u.login = :username")
	Optional<CUser> findByUsername(@Param("username") String username);

	/**
	 * Finds a login user by their username and loads associated project settings. Useful
	 * for getting complete user data including project assignments.
	 * @param username the username to search for
	 * @return Optional containing the CUser with project settings if found, empty otherwise
	 */
	@Query("SELECT u FROM CUser u LEFT JOIN FETCH u.projectSettings WHERE u.login = :username")
	Optional<CUser> findByUsernameWithProjects(@Param("username") String username);

	/**
	 * Finds projects accessible to a user based on their project settings.
	 * @param userId the user ID
	 * @return list of accessible projects
	 */
	@Query("SELECT p FROM CProject p WHERE p.id IN (SELECT ups.projectId FROM CUserProjectSettings ups WHERE ups.user.id = :userId)")
	java.util.List<CProject> findAccessibleProjectsByUserId(@Param("userId") Long userId);
}
