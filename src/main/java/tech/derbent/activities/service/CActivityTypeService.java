package tech.derbent.activities.service;

import java.time.Clock;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.activities.domain.CActivityType;

/**
 * CActivityTypeService - Service layer for CActivityType entity. Layer: Service (MVC)
 * Handles business logic for project-aware activity type operations.
 */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CActivityTypeService extends CEntityOfProjectService<CActivityType> {

	private static final Logger LOGGER =
		LoggerFactory.getLogger(CActivityTypeService.class);

	public CActivityTypeService(final CActivityTypeRepository repository,
		final Clock clock) {
		super(repository, clock);
	}

	@Override
	@Transactional (readOnly = true)
	public Optional<CActivityType> get(final Long id) {

		if (id == null) {
			LOGGER.debug("Getting CActivityType with null ID - returning empty");
			return Optional.empty();
		}
		LOGGER.debug("Getting CActivityType with ID {} (with eager loading)", id);
		final Optional<CActivityType> entity =
			((CActivityTypeRepository) repository).findByIdWithRelationships(id);
		entity.ifPresent(this::initializeLazyFields);
		return entity;
	}

	@Override
	protected Class<CActivityType> getEntityClass() { return CActivityType.class; }
}