package tech.derbent.risks.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.views.CAbstractMDPage;
import tech.derbent.base.ui.view.MainLayout;
import tech.derbent.risks.domain.CRisk;
import tech.derbent.risks.domain.ERiskSeverity;
import tech.derbent.risks.service.CRiskService;

@PageTitle("Project Risks")
@Route(value = "risks", layout = MainLayout.class)
@Menu(order = 2, icon = "vaadin:clipboard-check", title = "Settings.Risks")
@PermitAll
public class CRiskView extends CAbstractMDPage<CRisk> {

    private static final long serialVersionUID = 1L;
    private static final String ENTITY_ID_FIELD = "risk_id";
    private static final String ENTITY_ROUTE_TEMPLATE_EDIT = "risks/%s/edit";

    private TextField nameField;
    private ComboBox<ERiskSeverity> severityBox;
    private Button saveButton;
    private Button deleteButton;

    public CRiskView(final CRiskService entityService) {
        super(CRisk.class, entityService);
        addClassNames("risk-view");
        System.out.println("binder initialized? " + (this.binder != null));
        // Initialize the binder for the CRisk entity
        //binder = new BeanValidationBinder<>(CRisk.class);
        // Configure Form Bind fields. This is where you'd define e.g. validation rules
        binder.bindInstanceFields(this);
    }

    @Override
    protected void createDetailsLayout(SplitLayout splitLayout) {
        FormLayout formLayout = new FormLayout();

        nameField = new TextField("Risk Name");
        severityBox = new ComboBox<>("Severity", ERiskSeverity.values());

        severityBox.setItems(ERiskSeverity.values());
        severityBox.setItemLabelGenerator(ERiskSeverity::name);

        formLayout.add(nameField, severityBox);

        saveButton = new Button("Save", e -> saveRisk());
        deleteButton = new Button("Delete", e -> deleteRisk());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, deleteButton);
        formLayout.add(buttonLayout);

        splitLayout.addToSecondary(formLayout);
    }

    @Override
    protected void createGridForEntity() {
        grid.addColumn("name").setHeader("Name").setAutoWidth(true);
        grid.addColumn(risk -> risk.getRiskSeverity().name()).setHeader("Severity").setAutoWidth(true);

        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                binder.setBean(event.getValue());
                currentEntity = event.getValue();
                // Optionally navigate to the edit view for the selected risk
                //UI.getCurrent().navigate(String.format(ENTITY_ROUTE_TEMPLATE_EDIT, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate("risks");
            }
        });
    }

    private void saveRisk() {
        try {
            CRisk risk = currentEntity; // currentEntity is set by the grid selection	
            if (risk == null) {
                risk = new CRisk();
            }
            binder.writeBean(risk);
            entityService.save(risk);
            clearForm();
            grid.getDataProvider().refreshAll();
            Notification.show("Risk saved successfully", 3000, Notification.Position.MIDDLE);

        } catch (ValidationException e) {
            Notification.show("Validation failed: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        } catch (Exception e) {
            Notification.show("An unexpected error occurred: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
            e.printStackTrace();
        }
    }

    private void deleteRisk() {
        CRisk selected = grid.asSingleSelect().getValue();
        if (selected != null) {
            entityService.delete(selected);
            grid.getDataProvider().refreshAll();
            clearForm();
            Notification.show("Risk deleted", 3000, Notification.Position.MIDDLE);
        } else {
            Notification.show("No risk selected", 3000, Notification.Position.MIDDLE);
        }
    }

    protected void clearForm() {
        binder.readBean(new CRisk());
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

    }

    @Override
    protected void setupContent() {
        // Initialize the binder for the CRisk entity
        binder.bind(nameField, CRisk::getName, CRisk::setName);
        binder.bind(severityBox, CRisk::getRiskSeverity, CRisk::setRiskSeverity);
    }

    @Override
    protected void setupToolbar() {

    }
}
