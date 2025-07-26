package tech.derbent.risks.view;

import java.util.List;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.projects.domain.CProject;
import tech.derbent.risks.domain.CRisk;
import tech.derbent.risks.service.CRiskService;
import tech.derbent.session.service.SessionService;

/**
 * CRiskView - View for managing project risks. Layer: View (MVC) Provides CRUD operations
 * for risks using the project-aware master-detail pattern.
 */
@PageTitle("Project Risks")
@Route("risks/:risk_id?/:action?(edit)")
@Menu(order = 2, icon = "vaadin:warning", title = "Project.Risks")
@PermitAll
public class CRiskView extends CProjectAwareMDPage<CRisk> {

	private static final long serialVersionUID = 1L;
	private static final String ENTITY_ID_FIELD = "risk_id";
	private static final String ENTITY_ROUTE_TEMPLATE_EDIT = "risks/%s/edit";

	public CRiskView(final CRiskService entityService,
		final SessionService sessionService) {
		super(CRisk.class, entityService, sessionService);
		addClassNames("risk-view");
		// createDetailsLayout();
		LOGGER.info("CRiskView initialized successfully");
	}

	@Override
	protected void createDetailsLayout() {
		LOGGER.info("Creating details layout for CRiskView");
		final Div detailsLayout = CEntityFormBuilder.buildForm(CRisk.class, getBinder());
		// Note: Buttons are now automatically added to the details tab by the parent
		// class
		getBaseDetailsLayout().add(detailsLayout);
	}

	@Override
	protected void createGridForEntity() {
		LOGGER.info("Creating grid for risks");
		grid.addColumn("name").setHeader("Name").setAutoWidth(true).setSortable(true);
		grid.addColumn(risk -> risk.getRiskSeverity().name()).setHeader("Severity")
			.setAutoWidth(true).setSortable(true);
		// Note: Grid selection listener is handled by the base class CAbstractMDPage
		// Removing duplicate listener that was causing conflicts with New/Delete buttons
	}

	@Override
	protected CRisk createNewEntityInstance() {
		return new CRisk();
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected String getEntityRouteTemplateEdit() { return ENTITY_ROUTE_TEMPLATE_EDIT; }

	@Override
	protected List<CRisk> getProjectFilteredData(final CProject project,
		final org.springframework.data.domain.Pageable pageable) {
		return ((CRiskService) entityService).listByProject(project, pageable)
			.getContent();
	}

	@Override
	protected void initPage() {
		// Initialize page components if needed
	}

	@Override
	protected CRisk newEntity() {
		return super.newEntity(); // Uses the project-aware implementation from parent
	}

	@Override
	protected void setProjectForEntity(final CRisk entity, final CProject project) {
		entity.setProject(project);
	}

	@Override
	protected void setupToolbar() {
		// TODO: Implement toolbar setup if needed
	}
}
