package tech.derbent.abstracts.annotations;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.router.Route;

import tech.derbent.abstracts.domains.CEntityDB;

/**
 * CSpringAuxillaries - Utility class for Spring and Hibernate operations. Layer: Utility (MVC) Provides helper methods
 * for handling lazy loading, reflection, and other common operations.
 */
public final class CSpringAuxillaries {

    private static final Logger LOGGER = LoggerFactory.getLogger(CSpringAuxillaries.class);

    /**
     * Gets all fields with MetaData annotations from an entity class.
     * 
     * @param entityClass
     *            the entity class to analyze
     * @return list of fields with MetaData annotations, sorted by order
     */
    public static List<Field> getMetaDataFields(final Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> !java.lang.reflect.Modifier.isStatic(field.getModifiers()))
                .filter(field -> field.getAnnotation(MetaData.class) != null)
                .filter(field -> !field.getAnnotation(MetaData.class).hidden()).sorted((f1, f2) -> {
                    final MetaData meta1 = f1.getAnnotation(MetaData.class);
                    final MetaData meta2 = f2.getAnnotation(MetaData.class);
                    return Integer.compare(meta1.order(), meta2.order());
                }).collect(Collectors.toList());
    }

    /**
     * Retrieves the @Route path value of a given class, if present.
     * 
     * @param clazz
     *            The view class to inspect.
     * @return The route path, or empty string if not annotated.
     */
    public static String getRoutePath(final Class<?> clazz) {
        // Check if the @Route annotation is present on the class
        final Route routeAnnotation = clazz.getAnnotation(Route.class);

        if (routeAnnotation != null) {
            // Return the path value from the annotation
            return routeAnnotation.value();
        }
        return ""; // No annotation found
    }

    /**
     * Safely initializes a lazy-loaded entity to avoid LazyInitializationException.
     * 
     * @param entity
     *            the entity to initialize
     * @return true if initialization was successful, false otherwise
     */
    public static boolean initializeLazily(final Object entity) {

        if (entity == null) {
            return false;
        }

        try {
            Hibernate.initialize(entity);
            return true;
        } catch (final Exception e) {
            LOGGER.warn("Failed to initialize lazy entity: {}", entity.getClass().getSimpleName(), e);
            return false;
        }
    }

    /**
     * Checks if an entity is loaded (not a proxy).
     * 
     * @param entity
     *            the entity to check
     * @return true if loaded, false if proxy or null
     */
    public static boolean isLoaded(final Object entity) {

        if (entity == null) {
            return false;
        }

        try {
            return Hibernate.isInitialized(entity);
        } catch (final Exception e) {
            LOGGER.warn("Error checking if entity is loaded: {}", entity.getClass().getSimpleName(), e);
            return false;
        }
    }

    /**
     * Safely gets the ID of an entity, handling null and lazy loading.
     * 
     * @param entity
     *            the entity (must extend CEntityDB)
     * @return the ID or null if not available
     */
    public static Long safeGetId(final CEntityDB<?> entity) {

        if (entity == null) {
            return null;
        }

        try {
            return entity.getId();
        } catch (final Exception e) {
            LOGGER.warn("Error getting ID from entity: {}", entity.getClass().getSimpleName(), e);
            return null;
        }
    }

    /**
     * Safely gets a string representation of an entity, handling null and lazy loading.
     * 
     * @param entity
     *            the entity
     * @return string representation or "N/A" if null or not loaded
     */
    public static String safeToString(final Object entity) {

        if (entity == null) {
            return "N/A";
        }

        try {

            if (!isLoaded(entity)) {
                return entity.getClass().getSimpleName() + "[Proxy]";
            }
            return entity.toString();
        } catch (final Exception e) {
            LOGGER.warn("Error getting string representation of entity", e);
            return entity.getClass().getSimpleName() + "[Error]";
        }
    }

    private CSpringAuxillaries() {
        // Utility class - prevent instantiation
    }
}