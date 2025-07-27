package tech.derbent.risks.view;

import java.util.List;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.views.CAccordionDescription;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.projects.domain.CProject;
import tech.derbent.risks.domain.CRisk;
import tech.derbent.risks.service.CRiskService;
import tech.derbent.session.service.SessionService;

/**
 * CRiskView - View for managing project risks. Layer: View (MVC) Provides CRUD operations
 * for risks using the project-aware master-detail pattern.
 */
@PageTitle ("Project Risks")
@Route ("risks/:risk_id?/:action?(edit)")
@Menu (order = 1.3, icon = "vaadin:warning", title = "Project.Risks")
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
	}

	@Override
	protected void createDetailsLayout() {
		final CAccordionDescription<CRisk> panel;
		panel = new CPanelRiskBasicInfo(getCurrentEntity(), getBinder(),
			(CRiskService) entityService);
		addAccordionPanel(panel);
	}

	@Override
	protected void createGridForEntity() {
		grid.addShortTextColumn(CRisk::getName, "Name", "name");
		grid.addShortTextColumn(risk -> {
			return risk.getRiskSeverity().name();
		}, "Severity", null);
		grid.addColumn(item -> {
			final String desc = item.getDescription();

			if (desc == null) {
				return "Not set";
			}
			return desc.length() > 50 ? desc.substring(0, 50) + "..." : desc;
		}, "Description", null);
		/***/
		grid.asSingleSelect().addValueChangeListener(event -> {

			if (event.getValue() != null) {
				UI.getCurrent().navigate(
					String.format(ENTITY_ROUTE_TEMPLATE_EDIT, event.getValue().getId()));
			}
			else {
				clearForm();
				UI.getCurrent().navigate("risks");
			}
		});
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
