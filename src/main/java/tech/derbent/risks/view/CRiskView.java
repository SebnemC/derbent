package tech.derbent.risks.view;

import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.projects.domain.CProject;
import tech.derbent.risks.domain.CRisk;
import tech.derbent.risks.domain.ERiskSeverity;
import tech.derbent.risks.service.CRiskService;
import tech.derbent.session.service.SessionService;

@PageTitle("Project Risks")
@Route("risks/:risk_id?/:action?(edit)")
@Menu(order = 2, icon = "vaadin:clipboard-check", title = "Settings.Risks")
@PermitAll
public class CRiskView extends CProjectAwareMDPage<CRisk> {

	private static final long serialVersionUID = 1L;
	private static final String ENTITY_ID_FIELD = "risk_id";
	private static final String ENTITY_ROUTE_TEMPLATE_EDIT = "risks/%s/edit";
	private TextField nameField;
	private ComboBox<ERiskSeverity> severityBox;
	private Button saveButton;
	private Button deleteButton;

	public CRiskView(final CRiskService entityService, final SessionService sessionService) {
		super(CRisk.class, entityService, sessionService);
		addClassNames("risk-view");
		System.out.println("binder initialized? " + (this.getBinder() != null));
		// Configure Form Bind fields. This is where you'd define e.g. validation rules
		getBinder().bindInstanceFields(this);
	}

	@Override
	protected void clearForm() {
		getBinder().readBean(new CRisk());
	}

	@Override
	protected Component createDetailsLayout() {
		final FormLayout formLayout = new FormLayout();
		nameField = new TextField("Risk Name");
		severityBox = new ComboBox<>("Severity", ERiskSeverity.values());
		severityBox.setItems(ERiskSeverity.values());
		severityBox.setItemLabelGenerator(ERiskSeverity::name);
		formLayout.add(nameField, severityBox);
		saveButton = new Button("Save", e -> saveRisk());
		deleteButton = new Button("Delete", e -> deleteRisk());
		final HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, deleteButton);
		formLayout.add(buttonLayout);
		// Initialize the binder for the CRisk entity
		getBinder().bind(nameField, CRisk::getName, CRisk::setName);
		getBinder().bind(severityBox, CRisk::getRiskSeverity, CRisk::setRiskSeverity);
		return formLayout;
	}

	@Override
	protected void createGridForEntity() {
		grid.addColumn("name").setHeader("Name").setAutoWidth(true);
		grid.addColumn(risk -> risk.getRiskSeverity().name()).setHeader("Severity").setAutoWidth(true);
		grid.asSingleSelect().addValueChangeListener(event -> {
			if (event.getValue() != null) {
				getBinder().setBean(event.getValue());
				currentEntity = event.getValue();
				// Optionally navigate to the edit view for the selected risk
				UI.getCurrent().navigate(String.format(ENTITY_ROUTE_TEMPLATE_EDIT, event.getValue().getId()));
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

	private void deleteRisk() {
		final CRisk selected = grid.asSingleSelect().getValue();
		if (selected != null) {
			entityService.delete(selected);
			refreshGrid();
			clearForm();
			Notification.show("Risk deleted", 3000, Notification.Position.MIDDLE);
		}
		else {
			Notification.show("No risk selected", 3000, Notification.Position.MIDDLE);
		}
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected String getEntityRouteTemplateEdit() { return ENTITY_ROUTE_TEMPLATE_EDIT; }

	@Override
	protected List<CRisk> getProjectFilteredData(final CProject project, final org.springframework.data.domain.Pageable pageable) {
		return ((CRiskService) entityService).listByProject(project, pageable).getContent();
	}

	@Override
	protected void initPage() {}

	private void saveRisk() {
		try {
			CRisk risk = currentEntity; // currentEntity is set by the grid selection
			if (risk == null) {
				risk = newEntity(); // Use the project-aware newEntity method
			}
			getBinder().writeBean(risk);
			entityService.save(risk);
			clearForm();
			refreshGrid();
			Notification.show("Risk saved successfully", 3000, Notification.Position.MIDDLE);
		} catch (final ValidationException e) {
			Notification.show("Validation failed: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
		} catch (final Exception e) {
			Notification.show("An unexpected error occurred: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
			e.printStackTrace();
		}
	}

	@Override
	protected void setProjectForEntity(final CRisk entity, final CProject project) {
		entity.setProject(project);
	}

	@Override
	protected void setupToolbar() {}
}
