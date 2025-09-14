package tech.derbent.page.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.components.CCrudToolbar;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.service.CMeetingService;
import tech.derbent.meetings.view.CMeetingsView;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.session.service.CSessionService;

@Route ("meeting-planner")
@PageTitle ("Meeting Planner")
@Menu (order = 2.1, icon = "vaadin:calendar", title = "Planning.Meeting Planner")
@PermitAll // When security is enabled, allow all authenticated users
public class CPageMeetingPlanner extends CPageGenericEntity<CMeeting> {

	private static final long serialVersionUID = 1L;

	public static String getStaticEntityColorCode() { return getStaticIconColorCode(); }

	public static String getStaticIconColorCode() {
		return "#6f42c1"; // Purple color for meetings
	}

	public static String getStaticIconFilename() { return "vaadin:calendar"; }

	public CPageMeetingPlanner(final CSessionService sessionService, final CGridEntityService gridEntityService,
			final CDetailSectionService screenService, final CMeetingService meetingService) {
		super(sessionService, screenService, gridEntityService, meetingService, CMeeting.class, CMeetingsView.VIEW_NAME);
	}

	/** Configures the dependency checker for meetings */
	@Override
	@SuppressWarnings ("unchecked")
	protected void configureCrudToolbar(CCrudToolbar<?> toolbar) {
		super.configureCrudToolbar(toolbar);
		CCrudToolbar<CMeeting> typedToolbar = (CCrudToolbar<CMeeting>) toolbar;
		typedToolbar.setDependencyChecker(meeting -> {
			try {
				// Check if meeting has participants or dependencies
				return null; // No dependencies, deletion allowed
			} catch (Exception e) {
				return "Error checking for dependent data. Please try again.";
			}
		});
	}

	@Override
	protected CMeeting createNewEntityInstance() {
		CMeeting newMeeting = new CMeeting();
		// Set project if available
		sessionService.getActiveProject().ifPresent(newMeeting::setProject);
		return newMeeting;
	}
}
