package tech.derbent.gannt.view.components;

import java.time.LocalDate;
import java.util.List;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import tech.derbent.gannt.domain.CGanttData;
import tech.derbent.gannt.domain.CGanttItem;
import tech.derbent.gannt.view.gridlines.CAbstractGanttGridLine;
import tech.derbent.gannt.view.gridlines.CActivityGanttGridLine;
import tech.derbent.gannt.view.gridlines.CMeetingGanttGridLine;

/** CGanttChart - Comprehensive Gantt chart component. This component combines the timeline, grid lines, and data management into a complete Gantt
 * chart. Supports multiple entity types with proper hierarchy display and date-based timeline visualization. Follows coding standards with C
 * prefix. */
public class CGanttChart extends VerticalLayout {

	private static final long serialVersionUID = 1L;
	private CGanttData ganttData;
	private CGanttTimeline timeline;
	private Div headerRow;
	private Div gridContainer;
	private HorizontalLayout chartLayout;

	/** Constructor for CGanttChart. */
	public CGanttChart() {
		initializeChart();
	}

	/** Set the Gantt data to display.
	 * @param ganttData The Gantt data containing project items */
	public void setGanttData(final CGanttData ganttData) {
		this.ganttData = ganttData;
		refreshChart();
	}

	/** Refresh the chart display with current data. */
	public void refreshChart() {
		if (ganttData == null) {
			showEmptyState();
			return;
		}
		if (ganttData.isEmpty()) {
			showEmptyState();
			return;
		}
		createTimeline();
		createHeader();
		createGridRows();
		updateLayout();
	}

	/** Create the timeline component. */
	private void createTimeline() {
		if (timeline == null) {
			timeline = new CGanttTimeline();
		}
		final LocalDate startDate = ganttData.getProjectStartDate();
		final LocalDate endDate = ganttData.getProjectEndDate();
		if ((startDate != null) && (endDate != null)) {
			timeline.setDateRange(startDate, endDate);
		}
	}

	/** Create the header row with column titles. */
	private void createHeader() {
		if (headerRow == null) {
			headerRow = new Div();
			headerRow.addClassName("gantt-header-row");
		}
		headerRow.removeAll();
		// Create header layout
		final HorizontalLayout headerLayout = new HorizontalLayout();
		headerLayout.setSpacing(false);
		headerLayout.setPadding(false);
		headerLayout.setDefaultVerticalComponentAlignment(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
		// ID column header
		final Div idHeader = createHeaderCell("ID", "70px");
		idHeader.getStyle().set("font-weight", "bold");
		// Name column header
		final Div nameHeader = createHeaderCell("Name", "200px");
		nameHeader.getStyle().set("font-weight", "bold");
		// Responsible column header
		final Div responsibleHeader = createHeaderCell("Responsible", "140px");
		responsibleHeader.getStyle().set("font-weight", "bold");
		// Start Date column header
		final Div startDateHeader = createHeaderCell("Start Date", "100px");
		startDateHeader.getStyle().set("font-weight", "bold");
		// Due Date column header
		final Div dueDateHeader = createHeaderCell("Due Date", "100px");
		dueDateHeader.getStyle().set("font-weight", "bold");
		// Timeline header
		final Div timelineHeader = new Div(new Span("Timeline"));
		timelineHeader.addClassName("gantt-timeline-header");
		timelineHeader.getStyle().set("font-weight", "bold");
		timelineHeader.getStyle().set("padding", "10px");
		timelineHeader.getStyle().set("background-color", "#f8f9fa");
		timelineHeader.getStyle().set("border", "1px solid #ddd");
		timelineHeader.getStyle().set("flex-grow", "1");
		headerLayout.add(idHeader, nameHeader, responsibleHeader, startDateHeader, dueDateHeader, timelineHeader);
		headerRow.add(headerLayout);
	}

	/** Create a header cell.
	 * @param title The header title
	 * @param width The header width
	 * @return The header cell */
	private Div createHeaderCell(final String title, final String width) {
		final Div header = new Div(new Span(title));
		header.addClassName("gantt-header-cell");
		header.setWidth(width);
		header.getStyle().set("padding", "10px");
		header.getStyle().set("background-color", "#f8f9fa");
		header.getStyle().set("border", "1px solid #ddd");
		header.getStyle().set("text-align", "center");
		return header;
	}

	/** Create grid rows for all Gantt items. */
	private void createGridRows() {
		if (gridContainer == null) {
			gridContainer = new Div();
			gridContainer.addClassName("gantt-grid-container");
		}
		gridContainer.removeAll();
		final List<CGanttItem> items = ganttData.getItems();
		for (final CGanttItem item : items) {
			final CAbstractGanttGridLine gridLine = createGridLineForItem(item);
			if (gridLine != null) {
				updateGridLineTimeline(gridLine, item);
				gridContainer.add(gridLine);
			}
		}
	}

	/** Create a grid line for a specific Gantt item.
	 * @param item The Gantt item
	 * @return The appropriate grid line component */
	private CAbstractGanttGridLine createGridLineForItem(final CGanttItem item) {
		final String entityType = item.getEntityType();
		switch (entityType) {
		case "CActivity":
			return new CActivityGanttGridLine(item);
		case "CMeeting":
			return new CMeetingGanttGridLine(item);
		default:
			// For other entity types, use the base implementation
			return new CAbstractGanttGridLine(item) {

				private static final long serialVersionUID = 1L;
				// Default implementation for unknown entity types
			};
		}
	}

	/** Initialize the chart layout and styling. */
	private void initializeChart() {
		addClassName("gantt-chart");
		setSpacing(false);
		setPadding(false);
		setSizeFull();
		// Initialize empty state
		showEmptyState();
	}

	/** Show empty state when no data is available. */
	private void showEmptyState() {
		removeAll();
		final Div emptyState = new Div();
		emptyState.addClassName("gantt-empty-state");
		emptyState.getStyle().set("text-align", "center");
		emptyState.getStyle().set("padding", "40px");
		emptyState.getStyle().set("color", "#666");
		final Span emptyMessage = new Span("No project items to display in Gantt chart");
		emptyMessage.getStyle().set("font-size", "18px");
		emptyState.add(emptyMessage);
		final Span emptySubMessage = new Span("Add activities, meetings, or other project items to see them here");
		emptySubMessage.getStyle().set("font-size", "14px");
		emptySubMessage.getStyle().set("color", "#999");
		emptyState.add(new Div(), emptySubMessage);
		add(emptyState);
	}

	/** Update the timeline positioning for a grid line.
	 * @param gridLine The grid line to update
	 * @param item     The associated Gantt item */
	private void updateGridLineTimeline(final CAbstractGanttGridLine gridLine, final CGanttItem item) {
		if ((timeline == null) || !item.hasDates()) {
			return;
		}
		// Get the timeline bar from the grid line
		final com.vaadin.flow.component.Component timelineBar =
				gridLine.getChildren().filter(child -> child.getElement().getClassList().contains("gantt-timeline-bar")).findFirst().orElse(null);
		if (timelineBar != null) {
			// Calculate position and width based on timeline
			final int position = timeline.getPositionForDate(item.getStartDate());
			final int width = timeline.getWidthForDateRange(item.getStartDate(), item.getEndDate());
			// Update the timeline bar styling
			timelineBar.getElement().getStyle().set("position", "relative");
			timelineBar.getElement().getStyle().set("width", timeline.getColumnCount() * timeline.getDayColumnWidth() + "px");
			// Find the actual bar element inside and position it
			timelineBar.getChildren().forEach(bar -> {
				if (bar.getElement().getClassList().contains("gantt-bar")) {
					bar.getElement().getStyle().set("margin-left", position + "px");
					bar.getElement().getStyle().set("width", width + "px");
				}
			});
		}
	}

	/** Update the overall layout after creating all components. */
	private void updateLayout() {
		removeAll();
		if ((timeline == null) || (headerRow == null) || (gridContainer == null)) {
			showEmptyState();
			return;
		}
		// Create chart layout with fixed columns and scrollable timeline
		chartLayout = new HorizontalLayout();
		chartLayout.setSpacing(false);
		chartLayout.setPadding(false);
		chartLayout.setSizeFull();
		// Left panel with fixed columns
		final VerticalLayout leftPanel = new VerticalLayout();
		leftPanel.setSpacing(false);
		leftPanel.setPadding(false);
		leftPanel.setWidth("610px"); // Fixed width for ID, name, responsible, start date, due date columns
		// Right panel with timeline
		final VerticalLayout rightPanel = new VerticalLayout();
		rightPanel.setSpacing(false);
		rightPanel.setPadding(false);
		rightPanel.getStyle().set("overflow-x", "auto");
		// Add timeline
		add(timeline);
		// Add header
		add(headerRow);
		// Add grid container
		add(gridContainer);
		// Set scrollable container
		getStyle().set("overflow", "auto");
		setHeightFull();
	}
}
