package tech.derbent.abstracts.views;

import java.util.Optional;

import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;

import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.users.view.CUsersView;

public abstract class CAbstractMDPage<EntityClass extends CEntityDB> extends CAbstractPage {

	private static final long serialVersionUID = 1L;
	private final Class<EntityClass> entityClass;
	protected Grid<EntityClass> grid;// = new Grid<>(CProject.class, false);
	private final BeanValidationBinder<EntityClass> binder;
	protected SplitLayout splitLayout = new SplitLayout();
	protected EntityClass currentEntity;
	protected final CAbstractService<EntityClass> entityService;

	protected CAbstractMDPage(final Class<EntityClass> entityClass, final CAbstractService<EntityClass> entityService) {
		super();
		this.entityClass = entityClass;
		this.entityService = entityService;
		binder = new BeanValidationBinder<>(entityClass);
		addClassNames("md-page");
		setSizeFull();
		// create a split layout for the main content, vertical split
		splitLayout.setSizeFull();
		splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
		// Create UI
		createGridLayout(splitLayout);
		createDetailsLayout(splitLayout);
		createGridForEntity();
		// binder = new BeanValidationBinder<>(entityClass
		setupContent();
		add(splitLayout);
	}

	@Override
	public void beforeEnter(final BeforeEnterEvent event) {
		final Optional<Long> entityID = event.getRouteParameters().get(getEntityRouteIdField()).map(Long::parseLong);
		if (entityID.isPresent()) {
			final Optional<EntityClass> samplePersonFromBackend = entityService.get(entityID.get());
			if (samplePersonFromBackend.isPresent()) {
				populateForm(samplePersonFromBackend.get());
			}
			else {
				Notification.show(String.format("The requested samplePerson was not found, ID = %s", entityID.get()), 3000, Notification.Position.BOTTOM_START);
				// when a row is selected but the data is no longer available, refresh grid
				refreshGrid();
				// event.forwardTo(CProjectsView.class);
			}
		}
	}

	protected void clearForm() {
		populateForm(null);
	}

	protected Button createCancelButton(final String buttonText) {
		final Button cancel = new Button(buttonText);
		cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		cancel.addClickListener(e -> {
			clearForm();
			refreshGrid();
		});
		return cancel;
	}

	protected Button createDeleteButton(final String buttonText) {
		LOGGER.info("Creating delete button for CUsersView");
		final Button delete = new Button(buttonText);
		delete.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		delete.addClickListener(e -> {
			if (currentEntity != null) {
				entityService.delete(currentEntity);
				clearForm();
				refreshGrid();
				// .EntityClass.UI.getCurrent().navigate(CUsersView.class);
			}
			else {
				Notification.show("No entity selected for deletion", 3000, Position.BOTTOM_START).addThemeVariants(NotificationVariant.LUMO_ERROR);
			}
		});
		return delete;
	}

	protected abstract void createDetailsLayout(final SplitLayout splitLayout);

	protected abstract void createGridForEntity();

	protected void createGridLayout(final SplitLayout splitLayout) {
		grid = new Grid<>(entityClass, false);
		grid.getColumns().forEach(grid::removeColumn);
		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
		grid.setItems(query -> entityService.list(VaadinSpringDataHelpers.toSpringPageRequest(query)).stream());
		grid.addColumn(entity -> entity.getId().toString()).setHeader("ID").setKey("id");
		final Div wrapper = new Div();
		wrapper.setClassName("grid-wrapper");
		splitLayout.addToPrimary(wrapper);
		wrapper.add(grid);
	}

	protected Button createSaveButton(final String buttonText) {
		LOGGER.info("Creating save button for CUsersView");
		final Button save = new Button(buttonText);
		save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		save.addClickListener(e -> {
			try {
				if (currentEntity == null) {
					currentEntity = newEntity();
				}
				getBinder().writeBean(currentEntity);
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
		return save;
	}

	public BeanValidationBinder<EntityClass> getBinder() { return binder; }

	protected abstract String getEntityRouteIdField();

	protected abstract String getEntityRouteTemplateEdit();

	/**
	 * Initializes the page with necessary components and layout.
	 */
	@Override
	protected abstract void initPage();

	protected abstract EntityClass newEntity();

	protected void populateForm(final EntityClass value) {
		currentEntity = value;
		binder.readBean(currentEntity);
	}

	protected void refreshGrid() {
		grid.select(null);
		grid.getDataProvider().refreshAll();
	}

	/**
	 * Sets up the main content area of the page.
	 */
	protected abstract void setupContent();

	/**
	 * Sets up the toolbar for the page.
	 */
	@Override
	protected abstract void setupToolbar();
}
