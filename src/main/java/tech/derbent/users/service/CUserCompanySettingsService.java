package tech.derbent.users.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.services.CAbstractEntityRelationService;
import tech.derbent.api.utils.Check;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.session.service.CSessionService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserCompanySetting;

/** Service class for managing user-company relationships with ownership capabilities. Handles CRUD operations for CUserCompanySetting entities and
 * provides business logic for company membership management. */
@Service
@Transactional (readOnly = true)
public class CUserCompanySettingsService extends CAbstractEntityRelationService<CUserCompanySetting> {

	private final IUserCompanySettingsRepository repository;

	@Autowired
	public CUserCompanySettingsService(final IUserCompanySettingsRepository repository, final Clock clock, final CSessionService sessionService) {
		super(repository, clock, sessionService);
		this.repository = repository;
	}

	/** Add user to company with full configuration */
	@Transactional
	public CUserCompanySetting addUserToCompany(final CUser user, final CCompany company, final String ownershipLevel, final String role) {
		LOGGER.debug("Adding user {} to company {} with ownership level {} and role {}", user, company, ownershipLevel, role);
		Check.notNull(user, "User must not be null");
		Check.notNull(company, "Company must not be null");
		if ((user.getId() == null) || (company.getId() == null)) {
			throw new IllegalArgumentException("User and company must have valid IDs");
		}
		if (relationshipExists(user.getId(), company.getId())) {
			throw new IllegalArgumentException("User is already a member of this company");
		}
		final CUserCompanySetting settings = new CUserCompanySetting(user, company);
		settings.setOwnershipLevel(ownershipLevel != null ? ownershipLevel : "MEMBER");
		settings.setRole(role);
		validateRelationship(settings);
		// Save the entity first
		final CUserCompanySetting savedSettings = save(settings);
		// Maintain bidirectional relationships
		if (company.getUsers() != null && !company.getUsers().contains(user)) {
			company.getUsers().add(user);
		}
		return savedSettings;
	}

	// Implementation of abstract methods
	@Override
	protected CUserCompanySetting createRelationshipInstance(final Long userId, final Long companyId) {
		throw new UnsupportedOperationException("Use addUserToCompany(CUser, CCompany, String, String) method instead");
	}

	public void deleteByUserCompany(CUser user, CCompany company) {
		Check.notNull(user, "User cannot be null");
		Check.notNull(company, "Company cannot be null");
		Check.notNull(user.getId(), "User must have a valid ID");
		Check.notNull(company.getId(), "Company must have a valid ID");
		repository.deleteByUserIdAndCompanyId(user.getId(), company.getId());
	}

	/** Find all companies where user has admin privileges */
	@Transactional (readOnly = true)
	public List<CUserCompanySetting> findAdminCompanies(final CUser user) {
		Check.notNull(user, "User cannot be null");
		List<CUserCompanySetting> allSettings = findByUser(user);
		return allSettings.stream().filter(CUserCompanySetting::isCompanyAdmin).toList();
	}

	@Override
	@Transactional (readOnly = true)
	public List<CUserCompanySetting> findByChildEntityId(final Long companyId) {
		LOGGER.debug("Finding user company settings for company ID: {}", companyId);
		return repository.findByCompanyId(companyId);
	}

	/** Find user company settings by company */
	@Transactional (readOnly = true)
	public List<CUserCompanySetting> findByCompany(final CCompany company) {
		Check.notNull(company, "Company cannot be null");
		return findByChildEntityId(company.getId());
	}

	@Override
	@Transactional (readOnly = true)
	public List<CUserCompanySetting> findByParentEntityId(final Long userId) {
		return repository.findByUserId(userId);
	}

	/** Find user company settings by user */
	@Transactional (readOnly = true)
	public List<CUserCompanySetting> findByUser(final CUser user) {
		Check.notNull(user, "User cannot be null");
		return findByParentEntityId(user.getId());
	}

	@Override
	@Transactional (readOnly = true)
	public Optional<CUserCompanySetting> findRelationship(final Long userId, final Long companyId) {
		return repository.findByUserIdAndCompanyId(userId, companyId);
	}

	@Override
	protected Class<CUserCompanySetting> getEntityClass() { return CUserCompanySetting.class; }

	@Override
	@Transactional (readOnly = true)
	public boolean relationshipExists(final Long userId, final Long companyId) {
		return repository.existsByUserIdAndCompanyId(userId, companyId);
	}

	/** Remove user from company */
	@Transactional
	public void removeUserFromCompany(final CUser user, final CCompany company) {
		LOGGER.debug("Removing user {} from company {}", user, company);
		if ((user == null) || (company == null)) {
			throw new IllegalArgumentException("User and company cannot be null");
		}
		if ((user.getId() == null) || (company.getId() == null)) {
			throw new IllegalArgumentException("User and company must have valid IDs");
		}
		// Find the relationship first to maintain bidirectional collections
		final Optional<CUserCompanySetting> settingsOpt = findRelationship(user.getId(), company.getId());
		if (settingsOpt.isPresent()) {
			// Remove from bidirectional collections
			if (company.getUsers() != null) {
				company.getUsers().remove(user);
			}
		}
		// Delete the relationship using the parent method
		deleteRelationship(user.getId(), company.getId());
	}

	/** Set a company as the user's primary company */
	@Transactional
	public void setPrimaryCompany(final CUser user, final CCompany company) {
		LOGGER.debug("Setting company {} as primary for user {}", company, user);
		Check.notNull(user, "User cannot be null");
		Check.notNull(company, "Company cannot be null");
		// First, remove primary status from all other companies
		List<CUserCompanySetting> userCompanies = findByUser(user);
		for (CUserCompanySetting settings : userCompanies) {
			save(settings);
		}
	}

	/** Update user's role and ownership in a company */
	@Transactional
	public CUserCompanySetting updateUserCompanyRole(final CUser user, final CCompany company, final String ownershipLevel, final String role) {
		LOGGER.debug("Updating user {} company {} ownership to {} and role to {}", user, company, ownershipLevel, role);
		final Optional<CUserCompanySetting> settingsOpt = findRelationship(user.getId(), company.getId());
		if (settingsOpt.isEmpty()) {
			throw new IllegalArgumentException("User is not a member of this company");
		}
		final CUserCompanySetting settings = settingsOpt.get();
		if (ownershipLevel != null) {
			settings.setOwnershipLevel(ownershipLevel);
		}
		if (role != null) {
			settings.setRole(role);
		}
		return updateRelationship(settings);
	}

	@Override
	protected void validateRelationship(final CUserCompanySetting relationship) {
		super.validateRelationship(relationship);
		Check.notNull(relationship, "Relationship cannot be null");
		Check.notNull(relationship.getUser(), "User cannot be null");
		Check.notNull(relationship.getCompany(), "Company cannot be null");
		Check.notNull(relationship.getOwnershipLevel(), "Ownership level cannot be null");
	}
}
