package tech.derbent.projects.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.views.CAbstractMDPage;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;

/**
 * CProjectsView - View for managing projects. Layer: View (MVC) Provides CRUD operations for projects using the
 * abstract master-detail pattern.
 */
@Route("projects/:project_id?/:action?(edit)")
@PageTitle("Project Master Detail")
@Menu(order = 0, icon = "vaadin:briefcase", title = "Settings.Projects")
@PermitAll // When security is enabled, allow all authenticated users
public class CProjectsView extends CAbstractMDPage<CProject> {

    private static final long serialVersionUID = 1L;
    private final String ENTITY_ID_FIELD = "project_id";
    private final String ENTITY_ROUTE_TEMPLATE_EDIT = "projects/%s/edit";

    public CProjectsView(final CProjectService entityService) {
        super(CProject.class, entityService);
        addClassNames("projects-view");
        // createDetailsLayout();
        LOGGER.info("CProjectsView initialized successfully");
    }

    @Override
    protected void createDetailsLayout() {
        LOGGER.info("Creating details layout for CProjectsView");
        final Div detailsLayout = CEntityFormBuilder.buildForm(CProject.class, getBinder());
        // Note: Buttons are now automatically added to the details tab by the parent class
        getBaseDetailsLayout().add(detailsLayout);
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
        grid.addColumn("name").setAutoWidth(true).setHeader("Name").setSortable(true);
        // Note: Grid selection listener is handled by the base class CAbstractMDPage
        // Removing duplicate listener that was causing conflicts with New/Delete buttons
    }

    @Override
    protected String getEntityRouteIdField() {
        return ENTITY_ID_FIELD;
    }

    @Override
    protected String getEntityRouteTemplateEdit() {
        return ENTITY_ROUTE_TEMPLATE_EDIT;
    }

    @Override
    protected void initPage() {
        // Initialize the page components and layout This method can be overridden to
        // set up the view's components
    }

    @Override
    protected CProject newEntity() {
        return new CProject();
    }

    @Override
    protected void setupToolbar() {
        // TODO: Implement toolbar setup if needed
    }
}
