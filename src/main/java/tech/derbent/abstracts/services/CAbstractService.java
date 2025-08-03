package tech.derbent.abstracts.services;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.annotations.CSpringAuxillaries;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.abstracts.utils.PageableUtils;

/**
 * CAbstractService - Abstract base service class for entity operations. Layer: Service (MVC) Provides common CRUD
 * operations and lazy loading support for all entity types.
 */
public abstract class CAbstractService<EntityClass extends CEntityDB<EntityClass>> {

    protected final Clock clock;

    protected final CAbstractRepository<EntityClass> repository;

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    public CAbstractService(final CAbstractRepository<EntityClass> repository, final Clock clock) {
        this.clock = clock;
        this.repository = repository;
    }

    public int count() {
        // LOGGER.debug("Counting entities in {}", getClass().getSimpleName());
        return (int) repository.count();
    }

    @Transactional
    public EntityClass createEntity() {

        try {
            final EntityClass entity = newEntity();
            repository.saveAndFlush(entity);
            return entity;
        } catch (final Exception e) {
            throw new RuntimeException("Failed to create instance of " + getEntityClass().getName(), e);
        }
    }

    public void delete(final EntityClass entity) {
        repository.delete(entity);
    }

    public void delete(final Long id) {
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<EntityClass> get(final Long id) {
        final Optional<EntityClass> entity = repository.findById(id);
        // Initialize lazy fields if entity is present
        entity.ifPresent(this::initializeLazyFields);
        return entity;
    }

    protected abstract Class<EntityClass> getEntityClass();

    /**
     * Initializes lazy fields for an entity to prevent LazyInitializationException. Subclasses can override this method
     * to specify which fields need initialization. This base implementation automatically handles CEntityOfProject
     * entities.
     * 
     * @param entity
     *            the entity to initialize
     */
    protected void initializeLazyFields(final EntityClass entity) {

        if (entity == null) {
            return;
        }

        try {
            CSpringAuxillaries.initializeLazily(entity);

            // Automatically handle CEntityOfProject's lazy project relationship
            if (entity instanceof CEntityOfProject) {
                @SuppressWarnings("unchecked")
                final CEntityOfProject<EntityClass> projectEntity = (CEntityOfProject<EntityClass>) entity;
                initializeLazyRelationship(projectEntity.getProject());
            }
        } catch (final Exception e) {
            LOGGER.warn("Error initializing lazy fields for entity: {}", CSpringAuxillaries.safeToString(entity), e);
        }
    }

    /**
     * Helper method to safely initialize a specific lazy relationship.
     * 
     * @param relationshipEntity
     *            the related entity to initialize
     * @param relationshipName
     *            the name of the relationship (for logging)
     */
    protected void initializeLazyRelationship(final Object relationshipEntity) {

        if (relationshipEntity == null) {
            return;
        }
        final boolean success = CSpringAuxillaries.initializeLazily(relationshipEntity);

        if (!success) {
            LOGGER.warn("Failed to initialize lazy relationship: {}",
                    CSpringAuxillaries.safeToString(relationshipEntity));
        }
    }

    @Transactional(readOnly = true)
    public List<EntityClass> list(final Pageable pageable) {
        // Validate and fix pageable to prevent "max-results cannot be negative" error
        final Pageable safePage = PageableUtils.validateAndFix(pageable);
        // LOGGER.debug("Listing entities with pageable: {}", safePage);
        final List<EntityClass> entities = repository.findAllBy(safePage).toList();
        // Initialize lazy fields for all entities
        entities.forEach(this::initializeLazyFields);
        return entities;
    }

    @Transactional(readOnly = true)
    public Page<EntityClass> list(final Pageable pageable, final Specification<EntityClass> filter) {
        // Validate and fix pageable to prevent "max-results cannot be negative" error
        final Pageable safePage = PageableUtils.validateAndFix(pageable);
        // LOGGER.debug("Listing entities with filter and pageable");
        final Page<EntityClass> page = repository.findAll(filter, safePage);
        // Initialize lazy fields for all entities in the page
        page.getContent().forEach(this::initializeLazyFields);
        return page;
    }

    public EntityClass newEntity() {

        try {
            // Get constructor that takes a String parameter and invoke it with the name
            final Object instance = getEntityClass().getDeclaredConstructor().newInstance();

            if (!getEntityClass().isInstance(instance)) {
                throw new IllegalStateException("Created object is not instance of T");
            }
            final EntityClass entity = ((EntityClass) instance);
            return entity;
        } catch (final Exception e) {
            throw new RuntimeException("Failed to create instance of " + getEntityClass().getName(), e);
        }
    }

    @Transactional
    public EntityClass save(final EntityClass entity) {
        // LOGGER.info("Saving entity: {}", CSpringAuxillaries.safeToString(entity));

        try {
            final EntityClass savedEntity = repository.save(entity);
            return savedEntity;
        } catch (final Exception e) {
            LOGGER.error("Error saving entity: {}", CSpringAuxillaries.safeToString(entity), e);
            throw e;
        }
    }

    /**
     * Validates an entity before saving. Subclasses can override this method to add custom validation logic.
     * 
     * @param entity
     *            the entity to validate
     * @throws IllegalArgumentException
     *             if validation fails
     */
    protected void validateEntity(final EntityClass entity) {

        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }
        // Add more validation logic in subclasses if needed
    }

    /**
     * Generic search method using reflection for any field. This provides common search functionality for all entity
     * types.
     * 
     * @param fieldName
     *            the name of the field to search by
     * @param fieldValue
     *            the value to search for
     * @param pageable
     *            pagination information
     * @return list of entities matching the search criteria
     */
    @Transactional(readOnly = true)
    public List<EntityClass> findByField(final String fieldName, final Object fieldValue, final Pageable pageable) {
        return CGenericSearchService.findByField(this, fieldName, fieldValue, pageable);
    }

    /**
     * Generic search method using reflection for any field with default pagination.
     * 
     * @param fieldName
     *            the name of the field to search by
     * @param fieldValue
     *            the value to search for
     * @return list of entities matching the search criteria
     */
    @Transactional(readOnly = true)
    public List<EntityClass> findByField(final String fieldName, final Object fieldValue) {
        return findByField(fieldName, fieldValue, Pageable.unpaged());
    }

    /**
     * Gets all searchable field names for this entity type.
     * 
     * @return list of searchable field names
     */
    public List<String> getSearchableFields() {
        return CGenericSearchService.getSearchableFields(getEntityClass());
    }

    /**
     * Checks if this entity type has a specific field.
     * 
     * @param fieldName
     *            the field name to check
     * @return true if the field exists, false otherwise
     */
    public boolean hasField(final String fieldName) {
        return CGenericSearchService.hasField(getEntityClass(), fieldName);
    }
}