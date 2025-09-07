package tech.derbent.gannt.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.gannt.domain.CGanttData;
import tech.derbent.meetings.service.CMeetingService;
import tech.derbent.projects.domain.CProject;

/** CGanttDataService - Service for loading and managing Gantt chart data. This service coordinates with various entity services to collect project
 * items and organize them into a unified Gantt data structure. Follows coding standards with C prefix and provides comprehensive data loading. */
@Service
public class CGanttDataService {

	@Autowired
	private CActivityService activityService;
	@Autowired
	private CMeetingService meetingService;

	/** Load all project items for Gantt chart display.
	 * @param project The project to load items for
	 * @return CGanttData containing all project items */
	public CGanttData loadGanttDataForProject(final CProject project) {
		if (project == null) {
			return new CGanttData(null);
		}
		final CGanttData ganttData = new CGanttData(project);
		// Load activities
		loadActivities(ganttData, project);
		// Load meetings
		loadMeetings(ganttData, project);
		// Additional entity types can be added here as needed
		// loadRisks(ganttData, project);
		// loadDecisions(ganttData, project);
		return ganttData;
	}

	/** Load activities for the project.
	 * @param ganttData The Gantt data to add to
	 * @param project   The project to load from */
	private void loadActivities(final CGanttData ganttData, final CProject project) {
		try {
			final List<?> activities = activityService.listByProject(project);
			for (final Object activity : activities) {
				if (activity instanceof CEntityOfProject) {
					ganttData.addEntity((CEntityOfProject<?>) activity);
				}
			}
		} catch (final Exception e) {
			// Log error but continue with other entity types
			System.err.println("Error loading activities for Gantt chart: " + e.getMessage());
		}
	}

	/** Load meetings for the project.
	 * @param ganttData The Gantt data to add to
	 * @param project   The project to load from */
	private void loadMeetings(final CGanttData ganttData, final CProject project) {
		try {
			final List<?> meetings = meetingService.listByProject(project);
			for (final Object meeting : meetings) {
				if (meeting instanceof CEntityOfProject) {
					ganttData.addEntity((CEntityOfProject<?>) meeting);
				}
			}
		} catch (final Exception e) {
			// Log error but continue with other entity types
			System.err.println("Error loading meetings for Gantt chart: " + e.getMessage());
		}
	}
}
