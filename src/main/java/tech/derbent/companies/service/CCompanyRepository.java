package tech.derbent.companies.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.services.CAbstractNamedRepository;
import tech.derbent.companies.domain.CCompany;

/** CCompanyRepository - Data access layer for CCompany entities Layer: Service (MVC) - Repository interface Extends CAbstractRepository to provide
 * standard CRUD operations */
public interface CCompanyRepository extends CAbstractNamedRepository<CCompany> {

	/** Finds all enabled companies using generic pattern */
	@Query ("SELECT c FROM #{#entityName} c WHERE c.enabled = :enabled ORDER BY c.name")
	List<CCompany> findByEnabled(@Param ("enabled") boolean enabled);
	/** Finds companies by name containing the search term (case-insensitive) using generic pattern */
	@Query ("SELECT c FROM #{#entityName} c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY c.name")
	List<CCompany> findByNameContainingIgnoreCase(@Param ("name") String name);
	/** Finds companies by tax number using generic pattern */
	@Query ("SELECT c FROM #{#entityName} c WHERE c.taxNumber = :taxNumber")
	Optional<CCompany> findByTaxNumber(@Param ("taxNumber") String taxNumber);
	/** Finds all companies ordered by name using generic pattern */
	@Query ("SELECT c FROM #{#entityName} c ORDER BY c.name")
	List<CCompany> findAllOrderByName();
}
