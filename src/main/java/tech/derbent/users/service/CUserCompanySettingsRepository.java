package tech.derbent.users.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.derbent.api.services.CUserRelationshipRepository;
import tech.derbent.users.domain.CUserCompanySettings;

/** Repository interface for CUserCompanySettings entity. Provides data access methods for user-company relationships. */
@Repository
public interface CUserCompanySettingsRepository extends CUserRelationshipRepository<CUserCompanySettings> {

	/** Find all user company settings for a specific company with eager loading */
	@Query ("SELECT r FROM #{#entityName} r LEFT JOIN FETCH r.company LEFT JOIN FETCH r.user WHERE r.company.id = :companyId")
	List<CUserCompanySettings> findByCompanyId(@Param ("companyId") Long companyId);
	/** Find a specific user company setting by user and company using generic pattern */
	@Query ("SELECT r FROM #{#entityName} r LEFT JOIN FETCH r.company LEFT JOIN FETCH r.user WHERE r.user.id = :userId AND r.company.id = :companyId")
	Optional<CUserCompanySettings> findByUserIdAndCompanyId(@Param ("userId") Long userId, @Param ("companyId") Long companyId);

	/** Check if a relationship exists between user and company */
	@Override
	default boolean existsByUserIdAndEntityId(Long userId, Long entityId) {
		return existsByUserIdAndCompanyId(userId, entityId);
	}

	/** Check if a relationship exists between user and company - concrete implementation */
	boolean existsByUserIdAndCompanyId(Long userId, Long companyId);

	/** Find relationship by user and entity IDs - concrete implementation */
	@Override
	default Optional<CUserCompanySettings> findByUserIdAndEntityId(Long userId, Long entityId) {
		return findByUserIdAndCompanyId(userId, entityId);
	}

	/** Find all settings by ownership level using generic pattern */
	@Query ("SELECT r FROM #{#entityName} r LEFT JOIN FETCH r.company LEFT JOIN FETCH r.user WHERE r.ownershipLevel = :ownershipLevel")
	List<CUserCompanySettings> findByOwnershipLevel(@Param ("ownershipLevel") String ownershipLevel);
	/** Find primary company for a user using generic pattern */
	@Query ("SELECT r FROM #{#entityName} r LEFT JOIN FETCH r.company WHERE r.user.id = :userId AND r.isPrimaryCompany = true")
	Optional<CUserCompanySettings> findPrimaryCompanyByUserId(@Param ("userId") Long userId);
	/** Find all company admins for a company using generic pattern */
	@Query (
		"SELECT r FROM #{#entityName} r LEFT JOIN FETCH r.user WHERE r.company.id = :companyId AND (r.ownershipLevel = 'OWNER' OR r.ownershipLevel = 'ADMIN') AND r.isActive = true"
	)
	List<CUserCompanySettings> findCompanyAdminsByCompanyId(@Param ("companyId") Long companyId);
	/** Count users for a specific company using generic pattern */
	@Query ("SELECT COUNT(r) FROM #{#entityName} r WHERE r.company.id = :companyId")
	long countByCompanyId(@Param ("companyId") Long companyId);
	/** Find users by role in a company using generic pattern */
	@Query ("SELECT r FROM #{#entityName} r LEFT JOIN FETCH r.user WHERE r.company.id = :companyId AND r.role = :role AND r.isActive = true")
	List<CUserCompanySettings> findByCompanyIdAndRole(@Param ("companyId") Long companyId, @Param ("role") String role);
}
