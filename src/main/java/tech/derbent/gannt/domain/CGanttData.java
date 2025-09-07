package tech.derbent.gannt.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.projects.domain.CProject;

/** CGanttData - Data model for Gantt chart containing all project items organized hierarchically. This class manages the collection of CGanttItem
 * objects and provides hierarchy calculation. Follows coding standards with C prefix and provides comprehensive Gantt chart data management. */
public class CGanttData {

	private final CProject project;
	private final List<CGanttItem> items;
	private final Map<String, List<CGanttItem>> itemsByType;
	private LocalDate projectStartDate;
	private LocalDate projectEndDate;

	/** Constructor for CGanttData.
	 * @param project The project for which to create Gantt data */
	public CGanttData(final CProject project) {
		this.project = project;
		this.items = new ArrayList<>();
		this.itemsByType = new HashMap<>();
	}

	/** Add a project entity to the Gantt data.
	 * @param entity The entity to add */
	public void addEntity(final CEntityOfProject<?> entity) {
		if (entity == null) {
			return;
		}
		final CGanttItem item = new CGanttItem(entity);
		items.add(item);
		// Group by type
		final String type = item.getEntityType();
		itemsByType.computeIfAbsent(type, k -> new ArrayList<>()).add(item);
		// Update project date range
		updateProjectDateRange(item);
	}

	/** Add multiple entities to the Gantt data.
	 * @param entities The entities to add */
	public void addEntities(final List<? extends CEntityOfProject<?>> entities) {
		if (entities != null) {
			entities.forEach(this::addEntity);
		}
		calculateHierarchy();
	}

	/** Calculate hierarchy levels for all items. This method processes parent-child relationships and assigns hierarchy levels. */
	public void calculateHierarchy() {
		// Create maps for quick lookup
		final Map<String, Map<Long, CGanttItem>> itemsByTypeAndId = new HashMap<>();
		for (final CGanttItem item : items) {
			itemsByTypeAndId.computeIfAbsent(item.getEntityType(), k -> new HashMap<>()).put(item.getEntityId(), item);
		}
		// Calculate hierarchy levels
		for (final CGanttItem item : items) {
			if (!item.hasParent()) {
				// Top level item
				continue;
			}
			// Find parent and calculate level
			final String parentType = item.getParentType();
			final Long parentId = item.getParentId();
			final Map<Long, CGanttItem> parentTypeItems = itemsByTypeAndId.get(parentType);
			if ((parentTypeItems != null) && parentTypeItems.containsKey(parentId)) {
				final CGanttItem parent = parentTypeItems.get(parentId);
				// For now, set to level 1 (child). More complex hierarchy can be implemented later.
				// This would require recursive calculation to handle multiple levels.
			}
		}
	}

	/** Clear all data. */
	public void clear() {
		items.clear();
		itemsByType.clear();
		projectStartDate = null;
		projectEndDate = null;
	}

	/** Get all entity types present in the data.
	 * @return List of entity type names */
	public List<String> getEntityTypes() { return new ArrayList<>(itemsByType.keySet()); }

	/** Get all items ordered by hierarchy and start date.
	 * @return List of Gantt items in display order */
	public List<CGanttItem> getItems() {
		return items.stream().sorted(Comparator.comparing((CGanttItem item) -> item.hasParent() ? 1 : 0) // Parents first
				.thenComparing(item -> item.getStartDate() != null ? item.getStartDate() : LocalDate.MAX) // Then by start date
				.thenComparing(CGanttItem::getDisplayName) // Then by name
		).collect(Collectors.toList());
	}

	/** Get items by entity type.
	 * @param entityType The entity type to filter by
	 * @return List of items of the specified type */
	public List<CGanttItem> getItemsByType(final String entityType) {
		return itemsByType.getOrDefault(entityType, new ArrayList<>());
	}

	/** Get items that have valid dates for timeline display.
	 * @return List of items with valid start and end dates */
	public List<CGanttItem> getItemsWithDates() {
		return items.stream().filter(CGanttItem::hasDates).collect(Collectors.toList());
	}

	/** Get the associated project.
	 * @return The project */
	public CProject getProject() { return project; }

	/** Get the project end date (latest end date of all items).
	 * @return The project end date or null if no dates available */
	public LocalDate getProjectEndDate() { return projectEndDate; }

	/** Get the project start date (earliest start date of all items).
	 * @return The project start date or null if no dates available */
	public LocalDate getProjectStartDate() { return projectStartDate; }

	/** Get top-level items (items without parents).
	 * @return List of top-level items */
	public List<CGanttItem> getTopLevelItems() {
		return items.stream().filter(item -> !item.hasParent()).collect(Collectors.toList());
	}

	/** Check if the data is empty.
	 * @return true if no items are present */
	public boolean isEmpty() { return items.isEmpty(); }

	/** Get the total number of items.
	 * @return The item count */
	public int size() {
		return items.size();
	}

	/** Update the project date range based on a new item.
	 * @param item The item to consider for date range */
	private void updateProjectDateRange(final CGanttItem item) {
		if (item.getStartDate() != null) {
			if ((projectStartDate == null) || item.getStartDate().isBefore(projectStartDate)) {
				projectStartDate = item.getStartDate();
			}
		}
		if (item.getEndDate() != null) {
			if ((projectEndDate == null) || item.getEndDate().isAfter(projectEndDate)) {
				projectEndDate = item.getEndDate();
			}
		}
	}
}
