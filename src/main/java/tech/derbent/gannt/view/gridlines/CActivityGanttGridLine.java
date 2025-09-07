package tech.derbent.gannt.view.gridlines;

import com.vaadin.flow.component.html.Span;
import tech.derbent.gannt.domain.CGanttItem;

/** CActivityGanttGridLine - Gantt grid line for Activity entities. Provides activity-specific display customizations for Gantt chart rows. Shows
 * activity progress, priority, and status information. Follows coding standards with C prefix. */
public class CActivityGanttGridLine extends CAbstractGanttGridLine {

	private static final long serialVersionUID = 1L;

	/** Constructor for CActivityGanttGridLine.
	 * @param ganttItem The activity Gantt item to display */
	public CActivityGanttGridLine(final CGanttItem ganttItem) {
		super(ganttItem);
	}

	/** Create the description cell with activity-specific information. Shows description along with progress percentage if available. */
	@Override
	protected void createDescriptionCell() {
		super.createDescriptionCell();
		// Add progress information if available
		try {
			final Object entity = ganttItem.getEntity();
			final java.lang.reflect.Method progressMethod = entity.getClass().getMethod("getProgressPercentage");
			final Integer progress = (Integer) progressMethod.invoke(entity);
			if (progress != null) {
				final Span progressSpan = new Span(" (" + progress + "% complete)");
				progressSpan.addClassName("gantt-progress-text");
				progressSpan.getStyle().set("font-size", "0.8em");
				progressSpan.getStyle().set("color", "#666");
				descriptionCell.add(progressSpan);
			}
		} catch (final Exception e) {
			// Progress not available, ignore
		}
	}

	/** Create the timeline bar with activity-specific styling. Shows completion progress as a gradient or fill pattern. */
	@Override
	protected void createTimelineBar() {
		super.createTimelineBar();
		if (ganttItem.hasDates()) {
			try {
				final Object entity = ganttItem.getEntity();
				final java.lang.reflect.Method progressMethod = entity.getClass().getMethod("getProgressPercentage");
				final Integer progress = (Integer) progressMethod.invoke(entity);
				if ((progress != null) && (progress > 0)) {
					// Find the bar element and add progress styling
					timelineBar.getChildren().findFirst().ifPresent(bar -> {
						if (progress >= 100) {
							bar.getElement().getStyle().set("background",
									"linear-gradient(90deg, " + ganttItem.getColorCode() + " 100%, " + ganttItem.getColorCode() + " 100%)");
							bar.getElement().getStyle().set("border", "2px solid #28a745");
						} else {
							final String progressColor = ganttItem.getColorCode();
							final String remainingColor = adjustColorOpacity(progressColor, 0.3);
							bar.getElement().getStyle().set("background",
									"linear-gradient(90deg, " + progressColor + " " + progress + "%, " + remainingColor + " " + progress + "%)");
						}
					});
				}
			} catch (final Exception e) {
				// Progress not available, use default styling
			}
		}
	}

	/** Apply activity-specific styling. */
	@Override
	protected void styleGridLine() {
		super.styleGridLine();
		addClassName("gantt-activity");
		// Add status-based styling if available
		try {
			final Object entity = ganttItem.getEntity();
			final java.lang.reflect.Method statusMethod = entity.getClass().getMethod("getStatus");
			final Object status = statusMethod.invoke(entity);
			if (status != null) {
				// Check if it's a final status
				final java.lang.reflect.Method finalStatusMethod = status.getClass().getMethod("getFinalStatus");
				final Boolean isFinal = (Boolean) finalStatusMethod.invoke(status);
				if ((isFinal != null) && isFinal) {
					addClassName("gantt-activity-completed");
					getStyle().set("opacity", "0.8");
				}
			}
		} catch (final Exception e) {
			// Status not available, ignore
		}
		// Add overdue styling if applicable
		try {
			final Object entity = ganttItem.getEntity();
			final java.lang.reflect.Method overdueMethod = entity.getClass().getMethod("isOverdue");
			final Boolean isOverdue = (Boolean) overdueMethod.invoke(entity);
			if ((isOverdue != null) && isOverdue) {
				addClassName("gantt-activity-overdue");
				getStyle().set("border-left", "4px solid #dc3545");
			}
		} catch (final Exception e) {
			// Overdue check not available, ignore
		}
	}

	/** Adjust color opacity for progress display.
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
