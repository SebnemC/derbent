package tech.derbent.abstracts.services;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

/**
 * CGenericSearchService - Utility class providing generic search methods using reflection. Layer: Service (MVC) -
 * Utility Provides common search operations for entities with standard fields like name, project, assignedTo,
 * createdBy.
 */
public final class CGenericSearchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CGenericSearchService.class);

    private CGenericSearchService() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a specification for searching by field value using reflection.
     * 
     * @param fieldName
     *            the name of the field to search by
     * @param fieldValue
     *            the value to search for
     * @param <T>
     *            the entity type
     * @return specification for the search criteria
     */
    public static <T extends CEntityDB<T>> Specification<T> createFieldSpec(final String fieldName,
            final Object fieldValue) {
        return (root, query, criteriaBuilder) -> {
            if (fieldValue == null) {
                return criteriaBuilder.isNull(root.get(fieldName));
            }
            return criteriaBuilder.equal(root.get(fieldName), fieldValue);
        };
    }

    /**
     * Creates a specification for case-insensitive name search.
     * 
     * @param name
     *            the name to search for
     * @param <T>
     *            the entity type extending CEntityNamed
     * @return specification for name search
     */
    public static <T extends CEntityNamed<T>> Specification<T> createNameSpec(final String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.trim().isEmpty()) {
                return criteriaBuilder.conjunction(); // Return all if name is empty
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase().trim() + "%");
        };
    }

    /**
     * Creates a specification for project search.
     * 
     * @param project
     *            the project to search for
     * @param <T>
     *            the entity type extending CEntityOfProject
     * @return specification for project search
     */
    public static <T extends CEntityOfProject<T>> Specification<T> createProjectSpec(final CProject project) {
        return createFieldSpec("project", project);
    }

    /**
     * Creates a specification for assignedTo user search.
     * 
     * @param user
     *            the assigned user to search for
     * @param <T>
     *            the entity type extending CEntityOfProject
     * @return specification for assignedTo search
     */
    public static <T extends CEntityOfProject<T>> Specification<T> createAssignedToSpec(final CUser user) {
        return createFieldSpec("assignedTo", user);
    }

    /**
     * Creates a specification for createdBy user search.
     * 
     * @param user
     *            the creator user to search for
     * @param <T>
     *            the entity type extending CEntityOfProject
     * @return specification for createdBy search
     */
    public static <T extends CEntityOfProject<T>> Specification<T> createCreatedBySpec(final CUser user) {
        return createFieldSpec("createdBy", user);
    }

    /**
     * Dynamically searches for entities by any field using reflection.
     * 
     * @param service
     *            the service instance to use for searching
     * @param fieldName
     *            the name of the field to search by
     * @param fieldValue
     *            the value to search for
     * @param pageable
     *            pagination information
     * @param <T>
     *            the entity type
     * @return list of entities matching the search criteria
     */
    @Transactional(readOnly = true)
    public static <T extends CEntityDB<T>> List<T> findByField(final CAbstractService<T> service,
            final String fieldName, final Object fieldValue, final Pageable pageable) {

        LOGGER.debug("Generic search by field '{}' with value '{}' for {}", fieldName, fieldValue,
                service.getClass().getSimpleName());

        try {
            // Verify the field exists in the entity class
            if (!hasField(service.getEntityClass(), fieldName)) {
                LOGGER.warn("Field '{}' not found in entity class {}", fieldName,
                        service.getEntityClass().getSimpleName());
                return new ArrayList<>();
            }

            final Specification<T> spec = createFieldSpec(fieldName, fieldValue);
            return service.list(pageable, spec).getContent();
        } catch (final Exception e) {
            LOGGER.error("Error in generic search by field '{}' for {}: {}", fieldName,
                    service.getClass().getSimpleName(), e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Checks if an entity class has a specific field (including inherited fields).
     * 
     * @param entityClass
     *            the entity class to check
     * @param fieldName
     *            the field name to look for
     * @return true if the field exists, false otherwise
     */
    public static boolean hasField(final Class<?> entityClass, final String fieldName) {
        try {
            Class<?> currentClass = entityClass;
            while (currentClass != null && currentClass != Object.class) {
                try {
                    currentClass.getDeclaredField(fieldName);
                    return true; // Field found
                } catch (final NoSuchFieldException e) {
                    // Field not found in current class, check parent class
                    currentClass = currentClass.getSuperclass();
                }
            }
            return false; // Field not found in any class in the hierarchy
        } catch (final Exception e) {
            LOGGER.warn("Error checking field existence for '{}' in class {}: {}", fieldName,
                    entityClass.getSimpleName(), e.getMessage());
            return false;
        }
    }

    /**
     * Gets all searchable field names for an entity class using reflection.
     * 
     * @param entityClass
     *            the entity class to analyze
     * @return list of searchable field names
     */
    public static List<String> getSearchableFields(final Class<?> entityClass) {
        final List<String> fields = new ArrayList<>();

        try {
            Class<?> currentClass = entityClass;
            while (currentClass != null && currentClass != Object.class) {
                for (final Field field : currentClass.getDeclaredFields()) {
                    final String fieldName = field.getName();
                    // Skip certain fields that shouldn't be searched
                    if (!fieldName.equals("id") && !fieldName.equals("version") && !fieldName.equals("serialVersionUID")
                            && !fieldName.startsWith("LOGGER")) {
                        fields.add(fieldName);
                    }
                }
                currentClass = currentClass.getSuperclass();
            }
        } catch (final Exception e) {
            LOGGER.warn("Error getting searchable fields for class {}: {}", entityClass.getSimpleName(),
                    e.getMessage());
        }

        return fields;
    }
}