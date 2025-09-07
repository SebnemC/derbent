package tech.derbent.gannt.view.test;

import java.time.LocalDate;
import java.util.Arrays;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.gannt.domain.CGanttData;
import tech.derbent.gannt.domain.CGanttItem;
import tech.derbent.gannt.view.components.CGanttChart;
import tech.derbent.gannt.view.components.CGanttTimeline;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.projects.domain.CProject;

/** CGanttTestView - Test view for Gantt chart components. This view demonstrates the Gantt chart functionality with sample data. Accessible via
 * /gantt-test route for testing purposes. */
@Route ("gantt-test")
@PageTitle ("Gantt Chart Test")
public class CGanttTestView extends VerticalLayout {

	private static final long serialVersionUID = 1L;

	public CGanttTestView() {
		setSizeFull();
		setPadding(true);
		setSpacing(true);
		// Create test data
		createTestGanttChart();
	}

	private void createTestGanttChart() {
		// Create a test project
		final CProject testProject = new CProject("Test Project for Gantt Chart");
		// Create test activities
		final CActivity activity1 = new CActivity("Design Phase", testProject);
		activity1.setStartDate(LocalDate.now());
		activity1.setDueDate(LocalDate.now().plusDays(10));
		activity1.setDescription("Create initial design documents and wireframes");
		final CActivity activity2 = new CActivity("Development Phase", testProject);
		activity2.setStartDate(LocalDate.now().plusDays(5));
		activity2.setDueDate(LocalDate.now().plusDays(20));
		activity2.setDescription("Implement core functionality and features");
		final CActivity activity3 = new CActivity("Testing Phase", testProject);
		activity3.setStartDate(LocalDate.now().plusDays(15));
		activity3.setDueDate(LocalDate.now().plusDays(25));
		activity3.setDescription("Comprehensive testing and bug fixes");
		// Create test meetings
		final CMeeting meeting1 = new CMeeting("Project Kickoff", testProject);
		meeting1.setMeetingDate(LocalDate.now().atTime(9, 0));
		meeting1.setEndDate(LocalDate.now().atTime(10, 0));
		meeting1.setDescription("Initial project planning and team alignment");
		final CMeeting meeting2 = new CMeeting("Design Review", testProject);
		meeting2.setMeetingDate(LocalDate.now().plusDays(8).atTime(14, 0));
		meeting2.setEndDate(LocalDate.now().plusDays(8).atTime(15, 0));
		meeting2.setDescription("Review design documents and approve approach");
		// Create Gantt data
		final CGanttData ganttData = new CGanttData(testProject);
		ganttData.addEntities(Arrays.asList(activity1, activity2, activity3, meeting1, meeting2));
		// Create Gantt chart
		final CGanttChart ganttChart = new CGanttChart();
		ganttChart.setGanttData(ganttData);
		// Create timeline separately for demonstration
		final CGanttTimeline timeline = new CGanttTimeline();
		timeline.setDateRange(LocalDate.now(), LocalDate.now().plusDays(30));
		// Add components to view
		add(new Div("Gantt Chart Test View"));
		add(new Div("Timeline Component:"));
		add(timeline);
		add(new Div("Complete Gantt Chart:"));
		add(ganttChart);
		// Add sample Gantt items for demonstration
		final Div sampleItemsDiv = new Div();
		sampleItemsDiv.add(new com.vaadin.flow.component.html.H3("Sample Gantt Items:"));
		for (final CGanttItem item : ganttData.getItems()) {
			final Div itemDiv = new Div();
			itemDiv.getStyle().set("border", "1px solid #ccc");
			itemDiv.getStyle().set("padding", "10px");
			itemDiv.getStyle().set("margin", "5px");
			itemDiv.getStyle().set("background-color", "#f9f9f9");
			final com.vaadin.flow.component.html.Span itemInfo = new com.vaadin.flow.component.html.Span(String.format("%s (%s): %s - %s (%s)",
					item.getDisplayName(), item.getEntityType(), item.getStartDate() != null ? item.getStartDate().toString() : "No start",
					item.getEndDate() != null ? item.getEndDate().toString() : "No end", item.getResponsibleName()));
			itemDiv.add(itemInfo);
			sampleItemsDiv.add(itemDiv);
		}
		add(sampleItemsDiv);
	}
}
