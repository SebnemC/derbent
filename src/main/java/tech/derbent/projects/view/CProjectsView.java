package tech.derbent.projects.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.views.CAbstractNamedEntityPage;
import tech.derbent.abstracts.views.CAccordionDBEntity;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.session.service.CSessionService;

/**
 * CProjectsView - View for managing projects. Layer: View (MVC) Provides CRUD operations
 * for projects using the abstract master-detail pattern.
 */
@Route ("projects/:project_id?/:action?(edit)")
@PageTitle ("Project Master Detail")
@Menu (order = 1.1, icon = "vaadin:briefcase", title = "Settings.Projects")
@PermitAll // When security is enabled, allow all authenticated users
public class CProjectsView extends CAbstractNamedEntityPage<CProject> {

	private static final long serialVersionUID = 1L;

	private final String ENTITY_ID_FIELD = "project_id";

	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "projects/%s/edit";

	public CProjectsView(final CProjectService entityService,
		final CSessionService sessionService) {
		super(CProject.class, entityService, sessionService);
		addClassNames("projects-view");
		// createDetailsLayout();
		LOGGER.info("CProjectsView initialized successfully");
	}

	@Override
	protected void createDetailsLayout() {
		CAccordionDBEntity<CProject> panel;
		panel = new CPanelProjectBasicInfo(getCurrentEntity(), getBinder(),
			(CProjectService) entityService);
		addAccordionPanel(panel);
	}

	@Override
	protected Div createDetailsTabLeftContent() {
		// Create custom tab content for projects view
		final Div detailsTabLabel = new Div();
		detailsTabLabel.setText("Project Information");
		detailsTabLabel.setClassName("details-tab-label");
		return detailsTabLabel;
	}

	@Override
	protected void createGridForEntity() {
		LOGGER.info("Creating grid for projects");
		// property name must match the field name in CProject
		grid.addShortTextColumn(CProject::getName, "Name", "name");
		grid.addColumn(item -> {
			final String desc = item.getDescription();

			if (desc == null) {
				return "Not set";
			}
			return desc.length() > 50 ? desc.substring(0, 50) + "..." : desc;
		}, "Description", null);
		// when a row is selected or deselected, populate form
		grid.asSingleSelect().addValueChangeListener(event -> {

			if (event.getValue() != null) {
				UI.getCurrent().navigate(
					String.format(ENTITY_ROUTE_TEMPLATE_EDIT, event.getValue().getId()));
			}
			else {
				clearForm();
				UI.getCurrent().navigate(CProjectsView.class);
			}
		});
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected String getEntityRouteTemplateEdit() { return ENTITY_ROUTE_TEMPLATE_EDIT; }

	@Override
	protected void initPage() {
		// Initialize the page components and layout This method can be overridden to set
		// up the view's components
	}

	@Override
	protected void setupToolbar() {
		// TODO: Implement toolbar setup if needed
	}
}
