package tech.derbent.gannt.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.users.domain.CUser;

/** CGanttItem - Data transfer object for Gantt chart representation of project entities. This class wraps project entities to provide a unified
 * interface for Gantt chart display. Follows coding standards with C prefix and provides standardized access to entity properties. */
public class CGanttItem {

	private final CEntityOfProject<?> entity;
	private final String entityType;
	private final LocalDate startDate;
	private final LocalDate endDate;
	private final Long parentId;
	private final String parentType;
	private final int hierarchyLevel;

	/** Constructor for CGanttItem.
	 * @param entity The project entity to wrap */
	public CGanttItem(final CEntityOfProject<?> entity) {
		this.entity = entity;
		this.entityType = entity.getClass().getSimpleName();
		this.startDate = extractStartDate(entity);
		this.endDate = extractEndDate(entity);
		this.parentId = extractParentId(entity);
		this.parentType = extractParentType(entity);
		this.hierarchyLevel = 0; // Will be calculated by hierarchy service
	}

	/** Constructor with hierarchy level.
	 * @param entity         The project entity to wrap
	 * @param hierarchyLevel The level in the hierarchy (0 = top level) */
	public CGanttItem(final CEntityOfProject<?> entity, final int hierarchyLevel) {
		this.entity = entity;
		this.entityType = entity.getClass().getSimpleName();
		this.startDate = extractStartDate(entity);
		this.endDate = extractEndDate(entity);
		this.parentId = extractParentId(entity);
		this.parentType = extractParentType(entity);
		this.hierarchyLevel = hierarchyLevel;
	}

	/** Get the entity color code for visual representation.
	 * @return The color code string */
	public String getColorCode() {
		try {
			final java.lang.reflect.Method method = entity.getClass().getMethod("getEntityColorCode");
			return (String) method.invoke(null);
		} catch (final Exception e) {
			return "#6c757d"; // Default gray color
		}
	}

	/** Get the entity description.
	 * @return The description or empty string */
	public String getDescription() {
		if (entity.getDescription() != null) {
			return entity.getDescription();
		}
		return "";
	}

	/** Get the display name for the entity.
	 * @return The display name */
	public String getDisplayName() {
		if (entity.getName() != null) {
			return entity.getName();
		}
		return "Unnamed " + entityType;
	}

	/** Get the duration in days.
	 * @return The duration in days, or 1 if dates are not available */
	public long getDurationDays() {
		if ((startDate != null) && (endDate != null)) {
			return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
		}
		return 1; // Default duration
	}

	/** Get the end date.
	 * @return The end date */
	public LocalDate getEndDate() { return endDate; }

	/** Get the wrapped entity.
	 * @return The project entity */
	public CEntityOfProject<?> getEntity() { return entity; }

	/** Get the entity ID.
	 * @return The entity ID */
	public Long getEntityId() { return entity.getId(); }

	/** Get the entity type name.
	 * @return The entity type */
	public String getEntityType() { return entityType; }

	/** Get the hierarchy level.
	 * @return The hierarchy level */
	public int getHierarchyLevel() { return hierarchyLevel; }

	/** Get the icon filename for the entity.
	 * @return The icon filename */
	public String getIconFilename() {
		try {
			final java.lang.reflect.Method method = entity.getClass().getMethod("getIconFilename");
			return (String) method.invoke(null);
		} catch (final Exception e) {
			return "vaadin:question"; // Default icon
		}
	}

	/** Get the parent entity ID.
	 * @return The parent ID or null */
	public Long getParentId() { return parentId; }

	/** Get the parent entity type.
	 * @return The parent type or null */
	public String getParentType() { return parentType; }

	/** Get the responsible user (assigned to).
	 * @return The responsible user or null */
	public CUser getResponsible() {
		if (entity.getAssignedTo() != null) {
			return entity.getAssignedTo();
		}
		return null;
	}

	/** Get the responsible user name.
	 * @return The responsible user name or "Unassigned" */
	public String getResponsibleName() {
		final CUser responsible = getResponsible();
		if (responsible != null) {
			return responsible.getName();
		}
		return "Unassigned";
	}

	/** Get the start date.
	 * @return The start date */
	public LocalDate getStartDate() { return startDate; }

	/** Check if this item has a parent.
	 * @return true if parent exists */
	public boolean hasParent() {
		return (parentId != null) && (parentType != null);
	}

	/** Check if dates are available for timeline display.
	 * @return true if both start and end dates are available */
	public boolean hasDates() {
		return (startDate != null) && (endDate != null);
	}

	/** Extract end date from entity using reflection.
	 * @param entity The entity to extract from
	 * @return The end date or null */
	private LocalDate extractEndDate(final CEntityOfProject<?> entity) {
		try {
			// Try dueDate first (for activities)
			try {
				final java.lang.reflect.Method method = entity.getClass().getMethod("getDueDate");
				final LocalDate dueDate = (LocalDate) method.invoke(entity);
				if (dueDate != null) {
					return dueDate;
				}
			} catch (final Exception ignored) {
				// Try endDate (for meetings)
			}
			try {
				final java.lang.reflect.Method method = entity.getClass().getMethod("getEndDate");
				final Object endDate = method.invoke(entity);
				if (endDate instanceof LocalDate) {
					return (LocalDate) endDate;
				} else if (endDate instanceof LocalDateTime) {
					return ((LocalDateTime) endDate).toLocalDate();
				}
			} catch (final Exception ignored) {
				// No end date available
			}
		} catch (final Exception e) {
			// Ignore reflection errors
		}
		return null;
	}

	/** Extract parent ID from entity.
	 * @param entity The entity to extract from
	 * @return The parent ID or null */
	private Long extractParentId(final CEntityOfProject<?> entity) {
		try {
			final java.lang.reflect.Method method = entity.getClass().getMethod("getParentId");
			return (Long) method.invoke(entity);
		} catch (final Exception e) {
			return null;
		}
	}

	/** Extract parent type from entity.
	 * @param entity The entity to extract from
	 * @return The parent type or null */
	private String extractParentType(final CEntityOfProject<?> entity) {
		try {
			final java.lang.reflect.Method method = entity.getClass().getMethod("getParentType");
			return (String) method.invoke(entity);
		} catch (final Exception e) {
			return null;
		}
	}

	/** Extract start date from entity using reflection.
	 * @param entity The entity to extract from
	 * @return The start date or null */
	private LocalDate extractStartDate(final CEntityOfProject<?> entity) {
		try {
			// Try startDate first (for activities)
			try {
				final java.lang.reflect.Method method = entity.getClass().getMethod("getStartDate");
				final LocalDate startDate = (LocalDate) method.invoke(entity);
				if (startDate != null) {
					return startDate;
				}
			} catch (final Exception ignored) {
				// Try meetingDate (for meetings)
			}
			try {
				final java.lang.reflect.Method method = entity.getClass().getMethod("getMeetingDate");
				final Object meetingDate = method.invoke(entity);
				if (meetingDate instanceof LocalDate) {
					return (LocalDate) meetingDate;
				} else if (meetingDate instanceof LocalDateTime) {
					return ((LocalDateTime) meetingDate).toLocalDate();
				}
			} catch (final Exception ignored) {
				// No start date available
			}
		} catch (final Exception e) {
			// Ignore reflection errors
		}
		return null;
	}
}
