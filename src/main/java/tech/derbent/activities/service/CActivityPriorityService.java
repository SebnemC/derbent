package tech.derbent.activities.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.activities.domain.CActivityPriority;

/**
 * CActivityPriorityService - Service class for managing CActivityPriority entities.
 * Layer: Service (MVC) Provides business logic for activity priority management including
 * CRUD operations, validation, and priority level management.
 */
@Service
@Transactional
public class CActivityPriorityService {

	private static final Logger LOGGER =
		LoggerFactory.getLogger(CActivityPriorityService.class);

	private final CActivityPriorityRepository activityPriorityRepository;

	@Autowired
	public CActivityPriorityService(
		final CActivityPriorityRepository activityPriorityRepository) {

		if (activityPriorityRepository == null) {
			LOGGER.error(
				"CActivityPriorityService constructor - Repository parameter is null");
			throw new IllegalArgumentException(
				"Activity priority repository cannot be null");
		}
		this.activityPriorityRepository = activityPriorityRepository;
	}

	/**
	 * Create default activity priorities if they don't exist. This method should be
	 * called during application startup.
	 */
	public void createDefaultPrioritiesIfNotExist() {
		LOGGER.debug(
			"createDefaultPrioritiesIfNotExist() - Creating default activity priorities");
		final String[][] defaultPriorities = {
			{
				"CRITICAL", "1", "Critical priority - immediate attention required",
				"#FF0000", "false" },
			{
				"HIGH", "2", "High priority - important task", "#FF8800", "false" },
			{
				"MEDIUM", "3", "Medium priority - normal task", "#FFA500", "true" },
			{
				"LOW", "4", "Low priority - can be deferred", "#00AA00", "false" },
			{
				"LOWEST", "5", "Lowest priority - nice to have", "#808080", "false" } };

		for (final String[] priorityData : defaultPriorities) {
			final String name = priorityData[0];
			final Integer level = Integer.parseInt(priorityData[1]);

			if (!existsByName(name) && !existsByPriorityLevel(level)) {
				LOGGER.debug(
					"createDefaultPrioritiesIfNotExist() - Creating default priority: {}",
					name);
				final CActivityPriority priority =
					new CActivityPriority(name, level, priorityData[2], priorityData[3],
						Boolean.parseBoolean(priorityData[4]));
				save(priority);
			}
			else {
				LOGGER.debug(
					"createDefaultPrioritiesIfNotExist() - Priority '{}' or level {} already exists",
					name, level);
			}
		}
		LOGGER.debug(
			"createDefaultPrioritiesIfNotExist() - Completed creating default activity priorities");
	}

	/**
	 * Delete an activity priority by ID.
	 * @param id the priority ID - must not be null
	 * @throws IllegalArgumentException if the ID is null
	 */
	public void deleteById(final Long id) {
		LOGGER.debug("deleteById(id={}) - Deleting activity priority", id);

		if (id == null) {
			LOGGER.error("deleteById(id=null) - ID parameter is null");
			throw new IllegalArgumentException("Activity priority ID cannot be null");
		}
		final Optional<CActivityPriority> existing =
			activityPriorityRepository.findById(id);

		if (!existing.isPresent()) {
			LOGGER.warn("deleteById(id={}) - Activity priority not found", id);
			return;
		}

		try {
			activityPriorityRepository.deleteById(id);
			LOGGER.debug("deleteById(id={}) - Successfully deleted activity priority",
				id);
		} catch (final Exception e) {
			LOGGER.error("deleteById(id={}) - Error deleting activity priority: {}", id,
				e.getMessage(), e);
			throw new RuntimeException("Failed to delete activity priority", e);
		}
	}

	/**
	 * Check if an activity priority name exists (case-insensitive).
	 * @param name the name to check - must not be null
	 * @return true if the name exists, false otherwise
	 */
	@Transactional (readOnly = true)
	public boolean existsByName(final String name) {
		LOGGER.debug("existsByName(name={}) - Checking if activity priority name exists",
			name);

		if ((name == null) || name.trim().isEmpty()) {
			LOGGER.warn("existsByName(name={}) - Name parameter is null or empty", name);
			return false;
		}
		final boolean exists =
			activityPriorityRepository.existsByNameIgnoreCase(name.trim());
		LOGGER.debug("existsByName(name={}) - Name exists: {}", name, exists);
		return exists;
	}

	/**
	 * Check if a priority level exists.
	 * @param priorityLevel the priority level to check - must not be null
	 * @return true if the level exists, false otherwise
	 */
	@Transactional (readOnly = true)
	public boolean existsByPriorityLevel(final Integer priorityLevel) {
		LOGGER.debug(
			"existsByPriorityLevel(priorityLevel={}) - Checking if priority level exists",
			priorityLevel);

		if (priorityLevel == null) {
			LOGGER.warn(
				"existsByPriorityLevel(priorityLevel=null) - Priority level parameter is null");
			return false;
		}
		final boolean exists =
			activityPriorityRepository.existsByPriorityLevel(priorityLevel);
		LOGGER.debug("existsByPriorityLevel(priorityLevel={}) - Level exists: {}",
			priorityLevel, exists);
		return exists;
	}

	/**
	 * Find all activity priorities ordered by priority level.
	 * @return List of all activity priorities
	 */
	@Transactional (readOnly = true)
	public List<CActivityPriority> findAll() {
		LOGGER.debug("findAll() - Finding all activity priorities");
		final List<CActivityPriority> priorities =
			activityPriorityRepository.findAllOrderedByPriorityLevel();
		LOGGER.debug("findAll() - Found {} activity priorities", priorities.size());
		return priorities;
	}

	/**
	 * Find all high priority levels (level 1 or 2).
	 * @return List of high priority types
	 */
	@Transactional (readOnly = true)
	public List<CActivityPriority> findAllHighPriorities() {
		LOGGER.debug("findAllHighPriorities() - Finding all high activity priorities");
		final List<CActivityPriority> priorities =
			activityPriorityRepository.findAllHighPriorities();
		LOGGER.debug("findAllHighPriorities() - Found {} high priorities",
			priorities.size());
		return priorities;
	}

	/**
	 * Find all low priority levels (level 4 or 5).
	 * @return List of low priority types
	 */
	@Transactional (readOnly = true)
	public List<CActivityPriority> findAllLowPriorities() {
		LOGGER.debug("findAllLowPriorities() - Finding all low activity priorities");
		final List<CActivityPriority> priorities =
			activityPriorityRepository.findAllLowPriorities();
		LOGGER.debug("findAllLowPriorities() - Found {} low priorities",
			priorities.size());
		return priorities;
	}

	/**
	 * Find activity priority by ID.
	 * @param id the priority ID - must not be null
	 * @return Optional containing the priority if found, empty otherwise
	 */
	@Transactional (readOnly = true)
	public Optional<CActivityPriority> findById(final Long id) {
		LOGGER.debug("findById(id={}) - Finding activity priority by ID", id);

		if (id == null) {
			LOGGER.warn("findById(id=null) - ID parameter is null");
			return Optional.empty();
		}
		final Optional<CActivityPriority> priority =
			activityPriorityRepository.findById(id);
		LOGGER.debug("findById(id={}) - Found priority: {}", id, priority.isPresent());
		return priority;
	}

	/**
	 * Find activity priority by name (case-insensitive).
	 * @param name the priority name - must not be null or empty
	 * @return Optional containing the priority if found, empty otherwise
	 */
	@Transactional (readOnly = true)
	public Optional<CActivityPriority> findByName(final String name) {
		LOGGER.debug("findByName(name={}) - Finding activity priority by name", name);

		if ((name == null) || name.trim().isEmpty()) {
			LOGGER.warn("findByName(name={}) - Name parameter is null or empty", name);
			return Optional.empty();
		}
		final Optional<CActivityPriority> priority =
			activityPriorityRepository.findByNameIgnoreCase(name.trim());
		LOGGER.debug("findByName(name={}) - Found priority: {}", name,
			priority.isPresent());
		return priority;
	}

	/**
	 * Find activity priority by priority level.
	 * @param priorityLevel the numeric priority level - must not be null
	 * @return Optional containing the priority if found, empty otherwise
	 */
	@Transactional (readOnly = true)
	public Optional<CActivityPriority> findByPriorityLevel(final Integer priorityLevel) {
		LOGGER.debug(
			"findByPriorityLevel(priorityLevel={}) - Finding activity priority by level",
			priorityLevel);

		if (priorityLevel == null) {
			LOGGER.warn(
				"findByPriorityLevel(priorityLevel=null) - Priority level parameter is null");
			return Optional.empty();
		}
		final Optional<CActivityPriority> priority =
			activityPriorityRepository.findByPriorityLevel(priorityLevel);
		LOGGER.debug("findByPriorityLevel(priorityLevel={}) - Found priority: {}",
			priorityLevel, priority.isPresent());
		return priority;
	}

	/**
	 * Find the default priority for new activities.
	 * @return Optional containing the default priority if found
	 */
	@Transactional (readOnly = true)
	public Optional<CActivityPriority> findDefaultPriority() {
		LOGGER.debug("findDefaultPriority() - Finding default activity priority");
		final Optional<CActivityPriority> priority =
			activityPriorityRepository.findDefaultPriority();
		LOGGER.debug("findDefaultPriority() - Found default priority: {}",
			priority.isPresent());
		return priority;
	}

	/**
	 * Save or update an activity priority.
	 * @param activityPriority the priority to save - must not be null
	 * @return the saved priority
	 * @throws IllegalArgumentException if the priority is null or invalid
	 */
	public CActivityPriority save(final CActivityPriority activityPriority) {
		LOGGER.debug("save(activityPriority={}) - Saving activity priority",
			activityPriority != null ? activityPriority.getName() : "null");

		if (activityPriority == null) {
			LOGGER.error(
				"save(activityPriority=null) - Activity priority parameter is null");
			throw new IllegalArgumentException("Activity priority cannot be null");
		}

		if ((activityPriority.getName() == null)
			|| activityPriority.getName().trim().isEmpty()) {
			LOGGER.error(
				"save() - Activity priority name is null or empty for priority id={}",
				activityPriority.getId());
			throw new IllegalArgumentException(
				"Activity priority name cannot be null or empty");
		}

		if ((activityPriority.getPriorityLevel() == null)
			|| (activityPriority.getPriorityLevel() < 1)
			|| (activityPriority.getPriorityLevel() > 5)) {
			LOGGER.error("save() - Invalid priority level {} for priority id={}",
				activityPriority.getPriorityLevel(), activityPriority.getId());
			throw new IllegalArgumentException("Priority level must be between 1 and 5");
		}
		// Check for duplicate names (excluding self for updates)
		final String trimmedName = activityPriority.getName().trim();
		final Optional<CActivityPriority> existingByName =
			activityPriorityRepository.findByNameIgnoreCase(trimmedName);

		if (existingByName.isPresent()
			&& !existingByName.get().getId().equals(activityPriority.getId())) {
			LOGGER.error("save() - Activity priority name '{}' already exists",
				trimmedName);
			throw new IllegalArgumentException(
				"Activity priority name '" + trimmedName + "' already exists");
		}
		// Check for duplicate priority levels (excluding self for updates)
		final Optional<CActivityPriority> existingByLevel = activityPriorityRepository
			.findByPriorityLevel(activityPriority.getPriorityLevel());

		if (existingByLevel.isPresent()
			&& !existingByLevel.get().getId().equals(activityPriority.getId())) {
			LOGGER.error("save() - Priority level {} already exists",
				activityPriority.getPriorityLevel());
			throw new IllegalArgumentException("Priority level "
				+ activityPriority.getPriorityLevel() + " already exists");
		}

		try {
			final CActivityPriority savedPriority =
				activityPriorityRepository.save(activityPriority);
			LOGGER.debug("save() - Successfully saved activity priority with id={}",
				savedPriority.getId());
			return savedPriority;
		} catch (final Exception e) {
			LOGGER.error("save() - Error saving activity priority: {}", e.getMessage(),
				e);
			throw new RuntimeException("Failed to save activity priority", e);
		}
	}
}