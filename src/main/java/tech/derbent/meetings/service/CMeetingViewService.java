package tech.derbent.meetings.service;

import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.service.CDetailLinesService;

public class CMeetingViewService {

	public static final String BASE_VIEW_NAME = "Meetings View";
	public static final String BASE_PANEL_NAME = "Meetings Information";

	public static CDetailSection createBasicView(final CProject project) {
		try {
			final CDetailSection scr = new CDetailSection();
			final Class<?> clazz = CMeeting.class;
			final String entityType = clazz.getSimpleName().replaceFirst("^C", "");
			scr.setProject(project);
			scr.setEntityType(clazz.getSimpleName());
			scr.setHeaderText(entityType + " View");
			scr.setIsActive(Boolean.TRUE);
			scr.setScreenTitle(entityType + " View");
			scr.setName(BASE_VIEW_NAME);
			scr.setDescription(entityType + " View Details");
			// create screen lines
			scr.addScreenLine(CDetailLinesService.createSection(CMeetingViewService.BASE_PANEL_NAME));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "meetingType"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			scr.addScreenLine(CDetailLinesService.createSection("Schedule"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "meetingDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "endDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			scr.addScreenLine(CDetailLinesService.createSection("Participants"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "attendees"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "participants"));
			scr.addScreenLine(CDetailLinesService.createSection("Agenda & Location"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "agenda"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "location"));
			scr.addScreenLine(CDetailLinesService.createSection("Status & Follow-up"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "status"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "relatedActivity"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "assignedTo"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "responsible"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "minutes"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "linkedElement"));
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public static void createDefaultViews(final CProject project) {}
}
