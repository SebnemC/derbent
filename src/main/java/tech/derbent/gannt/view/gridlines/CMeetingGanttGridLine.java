package tech.derbent.gannt.view.gridlines;

import com.vaadin.flow.component.html.Span;
import tech.derbent.gannt.domain.CGanttItem;

/** CMeetingGanttGridLine - Gantt grid line for Meeting entities. Provides meeting-specific display customizations for Gantt chart rows. Shows meeting
 * location, agenda, and attendee information. Follows coding standards with C prefix. */
public class CMeetingGanttGridLine extends CAbstractGanttGridLine {

	private static final long serialVersionUID = 1L;

	/** Constructor for CMeetingGanttGridLine.
	 * @param ganttItem The meeting Gantt item to display */
	public CMeetingGanttGridLine(final CGanttItem ganttItem) {
		super(ganttItem);
	}

	/** Create the responsible cell with meeting organizer information. Shows the meeting responsible person. */
	@Override
	protected void createResponsibleCell() {
		responsibleCell = new com.vaadin.flow.component.html.Div();
		responsibleCell.addClassName("gantt-responsible-cell");
		responsibleCell.setWidth("140px");
		// Try to get meeting-specific responsible person first
		String responsibleName = ganttItem.getResponsibleName();
		try {
			final Object entity = ganttItem.getEntity();
			final java.lang.reflect.Method responsibleMethod = entity.getClass().getMethod("getResponsible");
			final Object responsible = responsibleMethod.invoke(entity);
			if (responsible != null) {
				final java.lang.reflect.Method nameMethod = responsible.getClass().getMethod("getName");
				responsibleName = (String) nameMethod.invoke(responsible);
			}
		} catch (final Exception e) {
			// Use default responsible from assignedTo
		}
		final Span responsibleSpan = new Span(responsibleName != null ? responsibleName : "No organizer");
		responsibleCell.add(responsibleSpan);
	}

	/** Create the due date cell with meeting-specific information. Shows end time along with location if available. */
	@Override
	protected void createDueDateCell() {
		super.createDueDateCell();
		// Add location information if available
		try {
			final Object entity = ganttItem.getEntity();
			final java.lang.reflect.Method locationMethod = entity.getClass().getMethod("getLocation");
			final String location = (String) locationMethod.invoke(entity);
			if ((location != null) && !location.isEmpty()) {
				final Span locationSpan = new Span(" @ " + location);
				locationSpan.addClassName("gantt-location-text");
				locationSpan.getStyle().set("font-size", "0.8em");
				locationSpan.getStyle().set("color", "#666");
				locationSpan.getStyle().set("font-style", "italic");
				dueDateCell.add(locationSpan);
			}
		} catch (final Exception e) {
			// Location not available, ignore
		}
	}

	/** Create the timeline bar with meeting-specific styling. Shows meetings as shorter bars since they're typically time-bound events. */
	@Override
	protected void createTimelineBar() {
		super.createTimelineBar();
		if (ganttItem.hasDates()) {
			// Find the bar element and style as meeting
			timelineBar.getChildren().findFirst().ifPresent(bar -> {
				bar.getElement().getStyle().set("height", "24px");
				bar.getElement().getStyle().set("border", "2px solid " + ganttItem.getColorCode());
				bar.getElement().getStyle().set("background", adjustColorOpacity(ganttItem.getColorCode(), 0.6));
				bar.getElement().getStyle().set("border-radius", "8px");
				// Add meeting-specific icon overlay
				bar.getElement().setProperty("innerHTML",
						"<div style='text-align: center; line-height: 20px; color: white; font-size: 12px; font-weight: bold;'>M</div>");
			});
		}
	}

	/** Apply meeting-specific styling. */
	@Override
	protected void styleGridLine() {
		super.styleGridLine();
		addClassName("gantt-meeting");
		// Add status-based styling if available
		try {
			final Object entity = ganttItem.getEntity();
			final java.lang.reflect.Method statusMethod = entity.getClass().getMethod("getStatus");
			final Object status = statusMethod.invoke(entity);
			if (status != null) {
				final String statusName = status.toString().toLowerCase();
				addClassName("gantt-meeting-" + statusName.replace(" ", "-"));
				if (statusName.contains("completed") || statusName.contains("done")) {
					getStyle().set("opacity", "0.8");
				} else if (statusName.contains("cancelled")) {
					getStyle().set("text-decoration", "line-through");
					getStyle().set("opacity", "0.6");
				}
			}
		} catch (final Exception e) {
			// Status not available, ignore
		}
		// Add time-based styling for meetings
		getStyle().set("border-left", "3px solid " + ganttItem.getColorCode());
	}

	/** Adjust color opacity for meeting display.
	 * @param color   Original color code
	 * @param opacity Opacity value (0.0 to 1.0)
	 * @return Color with adjusted opacity */
	private String adjustColorOpacity(final String color, final double opacity) {
		if (color.startsWith("#") && (color.length() == 7)) {
			final int r = Integer.parseInt(color.substring(1, 3), 16);
			final int g = Integer.parseInt(color.substring(3, 5), 16);
			final int b = Integer.parseInt(color.substring(5, 7), 16);
			return String.format("rgba(%d, %d, %d, %.2f)", r, g, b, opacity);
		}
		return color;
	}
}
