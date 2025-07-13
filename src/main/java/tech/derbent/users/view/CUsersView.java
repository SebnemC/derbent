package tech.derbent.users.view;

import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.views.CAbstractMDPage;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserService;

@Route("users/:user_id?/:action?(edit)")
@PageTitle("User Master Detail")
@Menu(order = 0, icon = "vaadin:clipboard-check", title = "Settings.Users")
@PermitAll // When security is enabled, allow all authenticated users
public class CUsersView extends CAbstractMDPage<CUser> {

	private static final long serialVersionUID = 1L;
	private final String ENTITY_ID_FIELD = "user_id";
	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "users/%s/edit";
	private TextField name;
	private Button cancel;
	private Button save;
	// private final BeanValidationBinder<CUser> binder; private final CUserService
	// userService; private final Grid<CUser> grid;// = new Grid<>(CUser.class,
	// false);

	public CUsersView(final CUserService entityService) {
		super(CUser.class, entityService);
		addClassNames("users-view");
		// Configure Form Bind fields. This is where you'd define e.g. validation rules
		binder.bindInstanceFields(this);
	}

	private void createButtonLayout(final Div editorLayoutDiv) {
		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setClassName("button-layout");
		cancel = new Button("Cancel");
		save = new Button("Save");
		cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		cancel.addClickListener(e -> {
			clearForm();
			refreshGrid();
		});
		save.addClickListener(e -> {
			try {
				if (currentEntity == null) {
					currentEntity = new CUser();
				}
				binder.writeBean(currentEntity);
				entityService.save(currentEntity);
				clearForm();
				refreshGrid();
				Notification.show("Data updated");
				UI.getCurrent().navigate(CUsersView.class);
			} catch (final ObjectOptimisticLockingFailureException exception) {
				final Notification n = Notification.show("Error updating the data. Somebody else has updated the record while you were making changes.");
				n.setPosition(Position.MIDDLE);
				n.addThemeVariants(NotificationVariant.LUMO_ERROR);
			} catch (final ValidationException validationException) {
				Notification.show("Failed to update the data. Check again that all values are valid");
			}
		});
		buttonLayout.add(save, cancel);
		editorLayoutDiv.add(buttonLayout);
	}

	@Override
	protected void createDetailsLayout(final SplitLayout splitLayout) {
		final Div editorLayoutDiv = new Div();
		editorLayoutDiv.setClassName("editor-layout");
		final Div editorDiv = new Div();
		editorDiv.setClassName("editor");
		editorLayoutDiv.add(editorDiv);
		final FormLayout formLayout = new FormLayout();
		name = new TextField("First Name");
		// formLayout.add(firstName, lastName, email, phone, dateOfBirth, occupation,
		// role, important);
		formLayout.add(name);
		editorDiv.add(formLayout);
		createButtonLayout(editorLayoutDiv);
		splitLayout.addToSecondary(editorLayoutDiv);
	}

	@Override
	protected void createGridForEntity() {
		// property name must match the field name in CUser
		grid.addColumn("name").setAutoWidth(true);
		// when a row is selected or deselected, populate form
		grid.asSingleSelect().addValueChangeListener(event -> {
			if (event.getValue() != null) {
				UI.getCurrent().navigate(String.format(ENTITY_ROUTE_TEMPLATE_EDIT, event.getValue().getId()));
			}
			else {
				clearForm();
				UI.getCurrent().navigate(CUsersView.class);
			}
		});
	}

	@Override
	protected String getEntityRouteIdField() { // TODO Auto-generated method stub
		return ENTITY_ID_FIELD;
	}

	@Override
	protected String getEntityRouteTemplateEdit() { // TODO Auto-generated method stub
		return ENTITY_ROUTE_TEMPLATE_EDIT;
	}

	@Override
	protected void initPage() {
		// Initialize the page components and layout This method can be overridden to
		// set up the view's components
	}

	@Override
	protected void setupContent() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void setupToolbar() {
		// TODO Auto-generated method stub
	}
}
