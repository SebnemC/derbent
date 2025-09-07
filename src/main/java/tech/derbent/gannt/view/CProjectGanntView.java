package tech.derbent.gannt.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.views.grids.CGrid;
import tech.derbent.abstracts.views.grids.CGridViewBaseGannt;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.gannt.domain.CGanntViewEntity;
import tech.derbent.gannt.service.CGanntViewEntityService;
import tech.derbent.gannt.service.CGanttDataService;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.session.service.CSessionService;

@Route ("cprojectganntview")
@PageTitle ("Project Gannt View")
@Menu (order = 1.1, icon = "class:tech.derbent.gannt.domain.CGanntViewEntity", title = "Project.Project Gannt Chart")
@PermitAll
public class CProjectGanntView extends CGridViewBaseGannt<CGanntViewEntity> {

	private static final long serialVersionUID = 1L;

	public static String getEntityColorCode() { return getIconColorCode(); }

	public static String getIconColorCode() {
		return CActivity.getIconColorCode(); // Use the static method from CActivity
	}

	public static String getIconFilename() { return CGanntViewEntity.getIconFilename(); }

	private final String ENTITY_ID_FIELD = "ganntview_id";

	protected CProjectGanntView(final CGanntViewEntityService entityService, final CSessionService sessionService, final CScreenService screenService,
			final CGanttDataService ganttDataService) {
		super(CGanntViewEntity.class, entityService, sessionService, screenService, ganttDataService);
		final CGanntViewEntity viewEntity =
				entityService.listByProject(sessionService.getActiveProject().orElse(null)).stream().findFirst().orElse(null);
		setCurrentEntity(viewEntity);
		// createDetailsLayout();
	}

	@Override
	public void createGridForEntity(final CGrid<CGanntViewEntity> grid) {
		LOGGER.debug("Creating grid for CGanntViewEntity");
		grid.addIdColumn(CEntityDB::getId, "#", ENTITY_ID_FIELD);
		grid.addShortTextColumn(CEntityNamed::getName, "Name", "name");
		grid.addColumn(CEntityNamed::getDescriptionShort, "Description");
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }
}
