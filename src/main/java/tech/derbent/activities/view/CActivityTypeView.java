package tech.derbent.activities.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.abstracts.domains.CInterfaceIconSet;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.activities.domain.CActivityType;
import tech.derbent.activities.service.CActivityTypeService;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.session.service.CSessionService;

/** CActivityTypeView - View for managing activity types. Layer: View (MVC) Provides CRUD operations for activity types using the abstract
 * master-detail pattern with project awareness. */
@Route ("cactivitytypeview/:activity_type_id?/:action?(edit)")
@PageTitle ("Activity Types")
@Menu (order = 10.4, icon = "class:tech.derbent.activities.view.CActivityTypeView", title = "Types.Activity Types")
@PermitAll
public class CActivityTypeView extends CProjectAwareMDPage<CActivityType> implements CInterfaceIconSet {
	private static final long serialVersionUID = 1L;

	public static String getIconColorCode() {
		return CActivityType.getIconColorCode(); // Use the static method from
													// CActivityType
	}

	public static String getIconFilename() { return CActivityType.getIconFilename(); }

	private final String ENTITY_ID_FIELD = "activity_type_id";
	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "cactivitytypeview/%s/edit";

	public CActivityTypeView(final CActivityTypeService entityService, final CSessionService sessionService, final CScreenService screenService) {
		super(CActivityType.class, entityService, sessionService, screenService);
		addClassNames("activity-types-view");
	}

	@Override
	protected void createGridForEntity() {
		grid.addIdColumn(CEntityDB::getId, "#", ENTITY_ID_FIELD);
		grid.addColumnEntityNamed(CEntityOfProject::getProject, "Project");
		grid.addShortTextColumn(CEntityNamed::getName, "Name", "name");
		grid.addColumn(CEntityNamed::getDescriptionShort, "Description");
		grid.addStatusColumn(type -> type, "Type", "type");
		grid.addShortTextColumn(CActivityType::getName, "Name", "name");
		grid.addShortTextColumn(CActivityType::getColor, "Color", "color");
		grid.addBooleanColumn(CActivityType::isActive, "Active", "Active", "Inactive");
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected String getEntityRouteTemplateEdit() { return ENTITY_ROUTE_TEMPLATE_EDIT; }

	@Override
	protected void setupToolbar() {
		// TODO: Implement toolbar setup if needed
	}
}
