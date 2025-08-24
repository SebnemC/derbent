package tech.derbent.users.view;

import java.lang.reflect.InvocationTargetException;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.abstracts.domains.CInterfaceIconSet;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.session.service.CSessionService;
import tech.derbent.users.domain.CUserType;
import tech.derbent.users.service.CUserTypeService;

/** CUserTypeView - View for managing user types. Layer: View (MVC) Provides CRUD operations for user types using the abstract master-detail pattern
 * with project awareness. */
@Route ("cusertypeview/:user_type_id?/:action?(edit)")
@PageTitle ("User Types")
@Menu (order = 10.3, icon = "class:tech.derbent.users.view.CUserTypeView", title = "Settings.User Types")
@PermitAll
public class CUserTypeView extends CProjectAwareMDPage<CUserType> implements CInterfaceIconSet {
	private static final long serialVersionUID = 1L;

	public static String getIconColorCode() {
		return CUserType.getIconColorCode(); // Use the static method from CUserType
	}

	public static String getIconFilename() { return CUserType.getIconFilename(); }

	private final String ENTITY_ID_FIELD = "user_type_id";
	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "cusertypeview/%s/edit";

	/** Constructor for CUserTypeView.
	 * @param entityService  the service for user type operations
	 * @param sessionService */
	public CUserTypeView(final CUserTypeService entityService, final CSessionService sessionService, final CScreenService screenService) {
		super(CUserType.class, entityService, sessionService, screenService);
		addClassNames("user-types-view");
	}

	@Override
	protected void createDetailsLayout() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
		final Div detailsLayout = new Div();
		detailsLayout.setClassName("editor-layout");
		detailsLayout.add(CEntityFormBuilder.buildForm(CUserType.class, getBinder()));
		getBaseDetailsLayout().add(detailsLayout);
	}

	@Override
	protected void createGridForEntity() {
		grid.addIdColumn(CEntityDB::getId, "#", ENTITY_ID_FIELD);
		grid.addColumnEntityNamed(CEntityOfProject::getProject, "Project");
		grid.addShortTextColumn(CEntityNamed::getName, "Name", "name");
		grid.addColumn(CEntityNamed::getDescriptionShort, "Description");
		grid.addDateTimeColumn(CEntityNamed::getCreatedDate, "Created", null);
		// Add profile picture column first
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected String getEntityRouteTemplateEdit() { return ENTITY_ROUTE_TEMPLATE_EDIT; }

	@Override
	public void setProjectForEntity(final CUserType entity, final CProject project) {
		assert entity != null : "Entity must not be null";
		assert project != null : "Project must not be null";
		entity.setProject(project);
	}

	@Override
	protected void setupToolbar() {
		// Toolbar setup is handled by the parent class
	}
}
