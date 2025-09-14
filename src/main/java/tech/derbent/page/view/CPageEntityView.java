package tech.derbent.page.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.abstracts.views.grids.CGrid;
import tech.derbent.abstracts.views.grids.CGridViewBaseProject;
import tech.derbent.page.domain.CPageEntity;
import tech.derbent.page.service.CPageEntityService;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.session.service.CSessionService;

@Route ("cpageentityview")
@PageTitle ("Page Master Detail")
@Menu (order = 1.1, icon = "class:tech.derbent.page.view.CPageEntityView", title = "Settings.Pages")
@PermitAll // When security is enabled, allow all authenticated users
public final class CPageEntityView extends CGridViewBaseProject<CPageEntity> {

	private static final long serialVersionUID = 1L;
	public static final String VIEW_NAME = "Pages View";

	public static String getStaticEntityColorCode() { return getStaticIconColorCode(); }

	public static String getStaticIconColorCode() {
		return CPageEntity.getStaticIconColorCode(); // Use the static method from CActivity
	}

	public CPageEntityView(final CPageEntityService entityService, final CSessionService sessionService, final CDetailSectionService screenService) {
		super(CPageEntity.class, entityService, sessionService, screenService);
	}

	public static String getStaticIconFilename() { return CPageEntity.getStaticIconFilename(); }

	private final String ENTITY_ID_FIELD = "pageentity_id";

	@Override
	public void createGridForEntity(final CGrid<CPageEntity> grid) {
		grid.addIdColumn(CEntityDB::getId, "#", ENTITY_ID_FIELD);
		grid.addColumnEntityNamed(CEntityOfProject::getProject, "Project");
		grid.addShortTextColumn(CEntityNamed::getName, "Name", "name");
		grid.addColumn(CEntityNamed::getDescriptionShort, "Description");
		grid.addColumn(CPageEntity::getRoute, "Route");
		grid.addColumn(CPageEntity::getTitle, "Menu Title");
		grid.addColumn(CPageEntity::getMenuOrder, "Order");
		grid.addColumn(CPageEntity::getParentMenu, "Parent Menu");
		grid.addColumn(CPageEntity::getSecurityPermissions, "Security");
		grid.addColumn(CPageEntity::getEnabled, "Enabled");
	}

	@Override
	protected String getEntityRouteIdField() { // TODO Auto-generated method stub
		return null;
	}
}
