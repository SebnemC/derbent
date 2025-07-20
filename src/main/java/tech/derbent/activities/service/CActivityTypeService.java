package tech.derbent.activities.service;

import java.time.Clock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.activities.domain.CActivityType;

/**
 * CActivityTypeService - Service layer for CActivityType entity.
 * Layer: Service (MVC)
 * Handles business logic for activity type operations.
 */
@Service
@PreAuthorize("isAuthenticated()")
@Transactional(readOnly = true)
public class CActivityTypeService extends CAbstractService<CActivityType> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CActivityTypeService.class);

    /**
     * Constructor for CActivityTypeService.
     * @param repository the CActivityTypeRepository to use for data access
     * @param clock the Clock instance for time-related operations
     */
    CActivityTypeService(final CActivityTypeRepository repository, final Clock clock) {
        super(repository, clock);
        LOGGER.info("CActivityTypeService initialized");
    }

    /**
     * Creates a new activity type entity.
     * @param name the name of the activity type
     * @param description the description of the activity type
     */
    @Transactional
    public void createEntity(final String name, final String description) {
        LOGGER.info("Creating new activity type: {}", name);
        final var entity = new CActivityType(name, description);
        repository.saveAndFlush(entity);
    }
}