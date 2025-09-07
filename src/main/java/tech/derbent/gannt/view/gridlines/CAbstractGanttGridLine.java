package tech.derbent.gannt.view.gridlines;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import tech.derbent.gannt.domain.CGanttItem;

/** CAbstractGanttGridLine - Abstract base class for Gantt chart grid lines. This class provides common functionality for displaying project entities
 * as Gantt chart rows. Each entity type will have its own concrete implementation extending this class. Follows coding standards with C prefix and
 * provides template method pattern. */
public abstract class CAbstractGanttGridLine extends Div {

	private static final long serialVersionUID = 1L;
	protected final CGanttItem ganttItem;
	protected Div nameCell;
	protected Div responsibleCell;
	protected Div descriptionCell;
	protected Div timelineBar;

	/** Constructor for CAbstractGanttGridLine.
	 * @param ganttItem The Gantt item to display */
	protected CAbstractGanttGridLine(final CGanttItem ganttItem) {
		this.ganttItem = ganttItem;
		initializeLayout();
		createCells();
		styleGridLine();
	}

	/** Create the description cell. Subclasses can override to customize description display. */
	protected void createDescriptionCell() {
		descriptionCell = new Div();
		descriptionCell.addClassName("gantt-description-cell");
		descriptionCell.setWidth("200px");
		final String description = ganttItem.getDescription();
		if ((description != null) && !description.isEmpty()) {
			final Span descSpan = new Span(description.length() > 50 ? description.substring(0, 47) + "..." : description);
			descSpan.setTitle(description); // Full description on hover
			descriptionCell.add(descSpan);
		} else {
			descriptionCell.add(new Span("No description"));
		}
	}

	/** Create the name cell with icon. Subclasses can override to customize name display. */
	protected void createNameCell() {
		nameCell = new Div();
		nameCell.addClassName("gantt-name-cell");
		nameCell.setWidth("250px");
		final HorizontalLayout nameLayout = new HorizontalLayout();
		nameLayout.setSpacing(true);
		nameLayout.setDefaultVerticalComponentAlignment(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
		// Add hierarchy indentation
		final int level = ganttItem.getHierarchyLevel();
		if (level > 0) {
			final Div indent = new Div();
			indent.setWidth((level * 20) + "px");
			nameLayout.add(indent);
		}
		// Add entity icon
		final Icon icon = createEntityIcon();
		nameLayout.add(icon);
		// Add entity name
		final Span nameSpan = new Span(ganttItem.getDisplayName());
		nameSpan.addClassName("gantt-name-text");
		nameLayout.add(nameSpan);
		nameCell.add(nameLayout);
	}

	/** Create the responsible cell. Subclasses can override to customize responsible display. */
	protected void createResponsibleCell() {
		responsibleCell = new Div();
		responsibleCell.addClassName("gantt-responsible-cell");
		responsibleCell.setWidth("150px");
		final Span responsibleSpan = new Span(ganttItem.getResponsibleName());
		responsibleCell.add(responsibleSpan);
	}

	/** Create the timeline bar. Subclasses can override to customize timeline display. */
	protected void createTimelineBar() {
		timelineBar = new Div();
		timelineBar.addClassName("gantt-timeline-bar");
		timelineBar.setHeight("30px");
		if (ganttItem.hasDates()) {
			final Div bar = new Div();
			bar.addClassName("gantt-bar");
			bar.getStyle().set("background-color", ganttItem.getColorCode());
			bar.getStyle().set("height", "20px");
			bar.getStyle().set("border-radius", "4px");
			bar.getStyle().set("margin", "5px 0");
			bar.setTitle(createBarTooltip());
			// Calculate bar width and position based on project timeline
			// This will be implemented when integrating with timeline component
			bar.setWidth("100px"); // Placeholder width
			timelineBar.add(bar);
		} else {
			final Span noDateSpan = new Span("No dates");
			noDateSpan.addClassName("gantt-no-dates");
			timelineBar.add(noDateSpan);
		}
	}

	/** Get the Gantt item associated with this grid line.
	 * @return The Gantt item */
	public CGanttItem getGanttItem() { return ganttItem; }

	/** Get the entity color code for this grid line. Subclasses can override to provide custom colors.
	 * @return The color code */
	protected String getEntityColorCode() { return ganttItem.getColorCode(); }

	/** Get the entity icon for this grid line. Subclasses can override to provide custom icons.
	 * @return The Vaadin icon */
	protected VaadinIcon getEntityIcon() {
		final String iconName = ganttItem.getIconFilename();
		if ((iconName != null) && iconName.startsWith("vaadin:")) {
			try {
				final String enumName = iconName.substring(7).toUpperCase().replace("-", "_");
				return VaadinIcon.valueOf(enumName);
			} catch (final IllegalArgumentException e) {
				return VaadinIcon.QUESTION;
			}
		}
		return VaadinIcon.QUESTION;
	}

	/** Create tooltip text for the timeline bar.
	 * @return Tooltip text */
	private String createBarTooltip() {
		final StringBuilder tooltip = new StringBuilder();
		tooltip.append(ganttItem.getDisplayName()).append("\n");
		tooltip.append("Type: ").append(ganttItem.getEntityType()).append("\n");
		tooltip.append("Responsible: ").append(ganttItem.getResponsibleName()).append("\n");
		if (ganttItem.hasDates()) {
			tooltip.append("Start: ").append(ganttItem.getStartDate()).append("\n");
			tooltip.append("End: ").append(ganttItem.getEndDate()).append("\n");
			tooltip.append("Duration: ").append(ganttItem.getDurationDays()).append(" days");
		}
		return tooltip.toString();
	}

	/** Create all grid cells. */
	private void createCells() {
		createNameCell();
		createResponsibleCell();
		createDescriptionCell();
		createTimelineBar();
		add(nameCell, responsibleCell, descriptionCell, timelineBar);
	}

	/** Create the entity icon component.
	 * @return The icon component */
	private Icon createEntityIcon() {
		final Icon icon = new Icon(getEntityIcon());
		icon.setSize("16px");
		icon.getStyle().set("color", getEntityColorCode());
		return icon;
	}

	/** Initialize the layout structure. */
	private void initializeLayout() {
		addClassName("gantt-grid-line");
		getStyle().set("display", "flex");
		getStyle().set("align-items", "center");
		getStyle().set("border-bottom", "1px solid #e0e0e0");
		getStyle().set("min-height", "40px");
		getStyle().set("padding", "5px");
	}

	/** Apply styling to the grid line. Subclasses can override to provide custom styling. */
	protected void styleGridLine() {
		// Add entity-specific styling
		addClassName("gantt-" + ganttItem.getEntityType().toLowerCase());
		// Add hierarchy level styling
		if (ganttItem.getHierarchyLevel() > 0) {
			addClassName("gantt-child-item");
		} else {
			addClassName("gantt-parent-item");
		}
	}
}
