package tech.derbent.meetings.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.views.CAccordionDescription;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.service.CMeetingService;
import tech.derbent.meetings.service.CMeetingTypeService;
import tech.derbent.session.service.SessionService;
import tech.derbent.users.service.CUserService;

@Route ("meetings/:meeting_id?/:action?(edit)")
@PageTitle ("Meeting Master Detail")
@Menu (order = 3, icon = "vaadin:group", title = "Project.Meetings")
@PermitAll // When security is enabled, allow all authenticated users
public class CMeetingsView extends CProjectAwareMDPage<CMeeting> {

	private static final long serialVersionUID = 1L;

	private final String ENTITY_ID_FIELD = "meeting_id";

	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "meetings/%s/edit";

	private final CMeetingTypeService meetingTypeService;

	private final CUserService userService;

	public CMeetingsView(final CMeetingService entityService,
		final SessionService sessionService, final CMeetingTypeService meetingTypeService,
		final CUserService userService) {
		super(CMeeting.class, entityService, sessionService);
		addClassNames("meetings-view");
		this.meetingTypeService = meetingTypeService;
		this.userService = userService;
		// createDetailsLayout();
	}

	@Override
	protected void createDetailsLayout() {
		CAccordionDescription<CMeeting> panel;
		panel = new CPanelMeetingBasicInfo(getCurrentEntity(), getBinder(),
			(CMeetingService) entityService, meetingTypeService);
		addAccordionPanel(panel);
		panel = new CPanelMeetingParticipants(getCurrentEntity(), getBinder(),
			(CMeetingService) entityService, meetingTypeService, userService);
		addAccordionPanel(panel);
		panel = new CPanelMeetingSchedule(getCurrentEntity(), getBinder(),
			(CMeetingService) entityService);
		addAccordionPanel(panel);
	}

	@Override
	protected void createGridForEntity() {
		LOGGER.info(
			"Creating enhanced grid for meetings with project and participant details");
		// Project Name - Important for context
		grid.addColumn(meeting -> meeting.getProject() != null
			? meeting.getProject().getName() : "No Project").setAutoWidth(true)
			.setHeader("Project").setSortable(true).setFlexGrow(0);
		// Meeting Name
		grid.addColumn(meeting -> meeting.getName()).setAutoWidth(true)
			.setHeader("Meeting Name").setSortable(true).setFlexGrow(1);
		// Meeting Type
		grid.addColumn(meeting -> meeting.getMeetingType() != null
			? meeting.getMeetingType().getName() : "No Type").setAutoWidth(true)
			.setHeader("Type").setSortable(true).setFlexGrow(0);
		// Start Time with proper formatting
		grid.addColumn(meeting -> {

			if (meeting.getMeetingDate() != null) {
				return meeting.getMeetingDate().format(
					java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
			}
			return "Not set";
		}).setAutoWidth(true).setHeader("Start Time").setSortable(true).setFlexGrow(0);
		// End Time with proper formatting
		grid.addColumn(meeting -> {

			if (meeting.getEndDate() != null) {
				return meeting.getEndDate().format(
					java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
			}
			return "Not set";
		}).setAutoWidth(true).setHeader("End Time").setSortable(true).setFlexGrow(0);
		// Participants with names instead of just count
		grid.addColumn(meeting -> {

			if (meeting.getParticipants().isEmpty()) {
				return "No participants";
			}
			return meeting.getParticipants().stream().map(
				user -> user.getName() != null ? user.getName() : "User #" + user.getId())
				.collect(java.util.stream.Collectors.joining(", "));
		}).setAutoWidth(true).setHeader("Participants").setSortable(false).setFlexGrow(1);
		// Description - shortened for grid display
		grid.addColumn(meeting -> {

			if ((meeting.getDescription() == null)
				|| meeting.getDescription().trim().isEmpty()) {
				return "No description";
			}
			final String desc = meeting.getDescription().trim();
			return desc.length() > 50 ? desc.substring(0, 47) + "..." : desc;
		}).setAutoWidth(true).setHeader("Description").setSortable(false).setFlexGrow(1);
	}

	@Override
	protected CMeeting createNewEntityInstance() {
		return new CMeeting();
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected String getEntityRouteTemplateEdit() { return ENTITY_ROUTE_TEMPLATE_EDIT; }

	@Override
	protected java.util.List<CMeeting> getProjectFilteredData(
		final tech.derbent.projects.domain.CProject project,
		final org.springframework.data.domain.Pageable pageable) {
		return ((CMeetingService) entityService).listByProject(project, pageable)
			.getContent();
	}

	@Override
	protected CMeeting newEntity() {
		return super.newEntity(); // Uses the project-aware implementation from parent
	}

	@Override
	protected void setProjectForEntity(final CMeeting entity,
		final tech.derbent.projects.domain.CProject project) {
		entity.setProject(project);
	}

	@Override
	protected void setupToolbar() {
		// TODO: Implement toolbar setup if needed
	}
}