package tech.derbent.gannt.view.components;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

/** CGanttTimeline - Timeline component for Gantt chart showing date ranges. This component displays the time scale at the top of the Gantt chart.
 * Shows months, weeks, and days based on the project date range. Follows coding standards with C prefix. */
public class CGanttTimeline extends Div {

	private static final long serialVersionUID = 1L;
	private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy");
	private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("dd");
	private LocalDate startDate;
	private LocalDate endDate;
	private int totalDays;
	private final List<TimelineColumn> columns;

	/** Constructor for CGanttTimeline. */
	public CGanttTimeline() {
		this.columns = new ArrayList<>();
		initializeTimeline();
	}

	/** Set the date range for the timeline.
	 * @param startDate The start date
	 * @param endDate   The end date */
	public void setDateRange(final LocalDate startDate, final LocalDate endDate) {
		if ((startDate == null) || (endDate == null) || startDate.isAfter(endDate)) {
			throw new IllegalArgumentException("Invalid date range");
		}
		this.startDate = startDate;
		this.endDate = endDate;
		this.totalDays = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
		createTimeline();
	}

	/** Get the number of timeline columns.
	 * @return The column count */
	public int getColumnCount() { return columns.size(); }

	/** Get the width of a single day column in pixels.
	 * @return The day column width */
	public int getDayColumnWidth() {
		if (totalDays <= 30) {
			return 30; // 30px per day for short projects
		} else if (totalDays <= 90) {
			return 20; // 20px per day for medium projects
		} else {
			return 10; // 10px per day for long projects
		}
	}

	/** Get the position (in pixels) for a specific date.
	 * @param date The date to get position for
	 * @return The position in pixels from the start */
	public int getPositionForDate(final LocalDate date) {
		if ((date == null) || date.isBefore(startDate) || date.isAfter(endDate)) {
			return 0;
		}
		final long daysDiff = ChronoUnit.DAYS.between(startDate, date);
		return (int) (daysDiff * getDayColumnWidth());
	}

	/** Get the width (in pixels) for a date range.
	 * @param start The start date
	 * @param end   The end date
	 * @return The width in pixels */
	public int getWidthForDateRange(final LocalDate start, final LocalDate end) {
		if ((start == null) || (end == null) || start.isAfter(end)) {
			return getDayColumnWidth(); // Minimum width
		}
		// Clamp dates to timeline range
		final LocalDate clampedStart = start.isBefore(startDate) ? startDate : start;
		final LocalDate clampedEnd = end.isAfter(endDate) ? endDate : end;
		final long days = ChronoUnit.DAYS.between(clampedStart, clampedEnd) + 1;
		return (int) (days * getDayColumnWidth());
	}

	/** Create the timeline display. */
	private void createTimeline() {
		removeAll();
		columns.clear();
		if ((startDate == null) || (endDate == null)) {
			return;
		}
		// Create header structure
		final Div monthHeader = createMonthHeader();
		final Div dayHeader = createDayHeader();
		add(monthHeader, dayHeader);
		// Set total width
		final int totalWidth = totalDays * getDayColumnWidth();
		setWidth(totalWidth + "px");
	}

	/** Create the day header row.
	 * @return The day header component */
	private Div createDayHeader() {
		final Div dayHeader = new Div();
		dayHeader.addClassName("gantt-timeline-days");
		dayHeader.getStyle().set("display", "flex");
		dayHeader.getStyle().set("border-bottom", "1px solid #ddd");
		dayHeader.getStyle().set("background-color", "#f8f9fa");
		LocalDate currentDate = startDate;
		while (!currentDate.isAfter(endDate)) {
			final Div dayColumn = new Div();
			dayColumn.addClassName("gantt-day-column");
			dayColumn.setWidth(getDayColumnWidth() + "px");
			dayColumn.getStyle().set("text-align", "center");
			dayColumn.getStyle().set("padding", "2px");
			dayColumn.getStyle().set("border-right", "1px solid #eee");
			dayColumn.getStyle().set("font-size", "11px");
			final Span dayLabel = new Span(currentDate.format(DAY_FORMATTER));
			dayColumn.add(dayLabel);
			// Highlight weekends
			final int dayOfWeek = currentDate.getDayOfWeek().getValue();
			if ((dayOfWeek == 6) || (dayOfWeek == 7)) { // Saturday or Sunday
				dayColumn.getStyle().set("background-color", "#e9ecef");
			}
			dayHeader.add(dayColumn);
			columns.add(new TimelineColumn(currentDate, getDayColumnWidth()));
			currentDate = currentDate.plusDays(1);
		}
		return dayHeader;
	}

	/** Create the month header row.
	 * @return The month header component */
	private Div createMonthHeader() {
		final Div monthHeader = new Div();
		monthHeader.addClassName("gantt-timeline-months");
		monthHeader.getStyle().set("display", "flex");
		monthHeader.getStyle().set("border-bottom", "2px solid #ccc");
		monthHeader.getStyle().set("background-color", "#e9ecef");
		LocalDate currentDate = startDate;
		while (!currentDate.isAfter(endDate)) {
			final LocalDate monthStart = currentDate.withDayOfMonth(1);
			LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
			if (monthEnd.isAfter(endDate)) {
				monthEnd = endDate;
			}
			final long monthDays = ChronoUnit.DAYS.between(currentDate.isBefore(monthStart) ? currentDate : monthStart,
					monthEnd.isAfter(endDate) ? endDate : monthEnd) + 1;
			final Div monthColumn = new Div();
			monthColumn.addClassName("gantt-month-column");
			monthColumn.setWidth((monthDays * getDayColumnWidth()) + "px");
			monthColumn.getStyle().set("text-align", "center");
			monthColumn.getStyle().set("padding", "8px");
			monthColumn.getStyle().set("border-right", "2px solid #ccc");
			monthColumn.getStyle().set("font-weight", "bold");
			monthColumn.getStyle().set("font-size", "12px");
			final Span monthLabel = new Span(currentDate.format(MONTH_FORMATTER));
			monthColumn.add(monthLabel);
			monthHeader.add(monthColumn);
			// Move to next month
			currentDate = monthStart.plusMonths(1);
		}
		return monthHeader;
	}

	/** Initialize the timeline component. */
	private void initializeTimeline() {
		addClassName("gantt-timeline");
		getStyle().set("border", "1px solid #ddd");
		getStyle().set("border-radius", "4px");
		getStyle().set("overflow-x", "auto");
		getStyle().set("background-color", "white");
		// Default empty state
		final Span emptyLabel = new Span("No date range set");
		emptyLabel.getStyle().set("padding", "20px");
		emptyLabel.getStyle().set("color", "#666");
		add(emptyLabel);
	}

	/** Inner class representing a timeline column. */
	public static class TimelineColumn {

		private final LocalDate date;
		private final int width;

		public TimelineColumn(final LocalDate date, final int width) {
			this.date = date;
			this.width = width;
		}

		public LocalDate getDate() { return date; }

		public int getWidth() { return width; }
	}
}
