package tech.derbent.activities.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.derbent.abstracts.services.CAbstractRepository;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.projects.domain.CProject;

public interface CActivityRepository extends CAbstractRepository<CActivity> {

    List<CActivity> findByProject(CProject project);

    Page<CActivity> findByProject(CProject project, Pageable pageable);

    /**
     * Finds activities by project with eagerly loaded CActivityType and CActivityStatus to prevent LazyInitializationException.
     * 
     * @param project the project
     * @param pageable pagination information
     * @return page of CActivity with loaded activityType and activityStatus
     */
    @Query("SELECT a FROM CActivity a LEFT JOIN FETCH a.activityType LEFT JOIN FETCH a.activityStatus WHERE a.project = :project")
    Page<CActivity> findByProjectWithTypeAndStatus(@Param("project") CProject project, Pageable pageable);

    /**
     * Finds an activity by ID with eagerly loaded CActivityType to prevent LazyInitializationException.
     * 
     * @param id
     *            the activity ID
     * @return optional CActivity with loaded activityType
     */
    @Query("SELECT a FROM CActivity a LEFT JOIN FETCH a.activityType WHERE a.id = :id")
    Optional<CActivity> findByIdWithActivityType(@Param("id") Long id);

    /**
     * Finds an activity by ID with eagerly loaded CActivityType and CActivityStatus to prevent LazyInitializationException.
     * 
     * @param id
     *            the activity ID
     * @return optional CActivity with loaded activityType and activityStatus
     */
    @Query("SELECT a FROM CActivity a LEFT JOIN FETCH a.activityType LEFT JOIN FETCH a.activityStatus WHERE a.id = :id")
    Optional<CActivity> findByIdWithActivityTypeAndStatus(@Param("id") Long id);
}
