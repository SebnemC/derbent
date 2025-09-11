package tech.derbent.users.service;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.service.CDetailLinesSampleBase;
import tech.derbent.screens.service.CDetailLinesService;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.users.domain.CUser;

public class CUserViewService extends CDetailLinesSampleBase {

	public static final String BASE_VIEW_NAME = "User View";
	public static final String BASE_PANEL_NAME = "User Information";
	static EntityFieldInfo info;
	static Map<String, EntityFieldInfo> fields;
	private static Logger LOGGER = LoggerFactory.getLogger(CUserViewService.class);

	public static CDetailSection createBasicView(final CProject project) {
		try {
			final Class<?> clazz = CUser.class;
			CDetailSection scr = createBaseScreenEntity(project, clazz);
			// create screen lines
			scr.addScreenLine(CDetailLinesService.createSection(CUserViewService.BASE_PANEL_NAME));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastname"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "login"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "password"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "phone"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "email"));
			/******************/
			scr.addScreenLine(CDetailLinesService.createSection("System Access"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "roles"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "userRole"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "enabled"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "company"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "userType"));
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			LOGGER.error("Error creating basic user view: {}", e.getMessage(), e);
			e.printStackTrace();
			return null;
		}
	}
}
