package tech.derbent.abstracts.views;

import java.util.Optional;

import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;

import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.interfaces.CProjectChangeListener;
import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.SessionService;
import tech.derbent.users.view.CUsersView;

public abstract class CAbstractMDPage<EntityClass extends CEntityDB> extends CAbstractPage implements CProjectChangeListener {

	private static final long serialVersionUID = 1L;
	protected final Class<EntityClass> entityClass;
	protected Grid<EntityClass> grid;// = new Grid<>(CProject.class, false);
	private final BeanValidationBinder<EntityClass> binder;
	protected SplitLayout splitLayout = new SplitLayout();
	VerticalLayout detailsLayout = new VerticalLayout();
	protected EntityClass currentEntity;
	protected final CAbstractService<EntityClass> entityService;
	protected final SessionService sessionService; // Optional - can be null for non-project-aware pages
	private boolean isProjectAware = false;

	// Constructor for non-project-aware pages (backward compatibility)
	protected CAbstractMDPage(final Class<EntityClass> entityClass, final CAbstractService<EntityClass> entityService) {
		this(entityClass, entityService, null);
	}

	// Constructor for project-aware pages
	protected CAbstractMDPage(final Class<EntityClass> entityClass, final CAbstractService<EntityClass> entityService, final SessionService sessionService) {
		super();
		this.entityClass = entityClass;
		this.entityService = entityService;
		this.sessionService = sessionService;
		this.isProjectAware = (sessionService != null);
		binder = new BeanValidationBinder<>(entityClass);
		addClassNames("md-page");
		setSizeFull();
		// create a split layout for the main content, vertical split
		splitLayout.setSizeFull();
		splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
		// Create UI
		createGridLayout();
		splitLayout.addToSecondary(detailsLayout);
		detailsLayout.add(createDetailsLayout());
		createGridForEntity();
		// binder = new BeanValidationBinder<>(entityClass
		add(splitLayout);
		
		// Initialize project-aware grid AFTER all UI components are created
		// This is safe because grid is now initialized and sessionService is available
		if (isProjectAware) {
			initializeProjectAwareData();
		}
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

	protected void createButtonLayout(final Div layout) {
		LOGGER.info("Creating button layout for CUsersView");
		// Create a horizontal layout for buttons
		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setClassName("button-layout");
		buttonLayout.add(createSaveButton("Save"), createCancelButton("Cancel"), createDeleteButton("Delete"));
		layout.add(buttonLayout);
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

	protected abstract Component createDetailsLayout();

	protected abstract void createGridForEntity();

	protected void createGridLayout() {
		grid = new Grid<>(entityClass, false);
		grid.getColumns().forEach(grid::removeColumn);
		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
		
		// Set initial data provider - will be overridden by project-aware pages
		if (!isProjectAware) {
			grid.setItems(query -> entityService.list(VaadinSpringDataHelpers.toSpringPageRequest(query)).stream());
		}
		
		grid.addColumn(entity -> entity.getId().toString()).setHeader("ID").setKey("id");
		// Add selection listener to the grid
		grid.asSingleSelect().addValueChangeListener(event -> {
			populateForm(event.getValue());
		});
		final Div wrapper = new Div();
		wrapper.setClassName("grid-wrapper");
		wrapper.add(grid);
		splitLayout.addToPrimary(wrapper);
	}

	protected Button createSaveButton(final String buttonText) {
		LOGGER.info("Creating save button for CUsersView");
		final Button save = new Button(buttonText);
		save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		save.addClickListener(e -> {
			try {
				if (currentEntity == null) {
					currentEntity = createEntityWithProject();
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
	 * Creates a new entity instance with project set if this is a project-aware page.
	 * This method is called internally and delegates to the abstract newEntity() method.
	 */
	protected final EntityClass createEntityWithProject() {
		final EntityClass entity = newEntity();
		// Set the active project if this is a project-aware page
		if (isProjectAware && sessionService != null) {
			sessionService.getActiveProject().ifPresent(project -> setProjectForEntity(entity, project));
		}
		return entity;
	}

	protected abstract EntityClass newEntity();

	protected void populateForm(final EntityClass value) {
		currentEntity = value;
		binder.readBean(currentEntity);
	}

	protected void refreshGrid() {
		grid.select(null);
		if (isProjectAware) {
			refreshProjectAwareGrid();
		} else {
			grid.getDataProvider().refreshAll();
		}
	}

	/**
	 * Override point for project-aware pages to handle attach events
	 */
	@Override
	protected void onAttach(final AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		if (isProjectAware && sessionService != null) {
			// Register this component to receive project change notifications
			sessionService.addProjectChangeListener(this);
			LOGGER.debug("Registered project change listener for: {}", getClass().getSimpleName());
		}
	}

	/**
	 * Override point for project-aware pages to handle detach events
	 */
	@Override
	protected void onDetach(final DetachEvent detachEvent) {
		super.onDetach(detachEvent);
		if (isProjectAware && sessionService != null) {
			// Unregister this component to prevent memory leaks
			sessionService.removeProjectChangeListener(this);
			LOGGER.debug("Unregistered project change listener for: {}", getClass().getSimpleName());
		}
	}

	/**
	 * Default implementation of CProjectChangeListener interface.
	 * Project-aware subclasses can override this method if needed.
	 * 
	 * @param newProject The newly selected project
	 */
	@Override
	public void onProjectChanged(final CProject newProject) {
		if (isProjectAware) {
			LOGGER.debug("Project change notification received: {}", newProject != null ? newProject.getName() : "null");
			refreshProjectAwareGrid();
		}
	}

	/**
	 * Initializes project-aware data after all UI components are created.
	 * This is called from the constructor after UI initialization is complete.
	 */
	protected void initializeProjectAwareData() {
		if (isProjectAware && sessionService != null && grid != null) {
			refreshProjectAwareGrid();
		}
	}

	/**
	 * Refreshes the grid with project-aware data.
	 * Only called for project-aware pages.
	 */
	protected void refreshProjectAwareGrid() {
		if (!isProjectAware || sessionService == null || grid == null) {
			return;
		}
		
		LOGGER.debug("Refreshing project-aware grid");
		final Optional<CProject> activeProject = sessionService.getActiveProject();
		if (activeProject.isPresent()) {
			grid.setItems(query -> getProjectFilteredData(activeProject.get(), VaadinSpringDataHelpers.toSpringPageRequest(query)).stream());
		} else {
			// If no active project, show empty grid
			grid.setItems(java.util.Collections.emptyList());
		}
	}

	/**
	 * Override point for project-aware pages to provide filtered data.
	 * Default implementation returns all data (non-project-aware behavior).
	 * 
	 * @param project The current active project
	 * @param pageable Pagination information
	 * @return List of entities filtered by project
	 */
	protected java.util.List<EntityClass> getProjectFilteredData(final CProject project, final org.springframework.data.domain.Pageable pageable) {
		// Default implementation for non-project-aware pages
		return entityService.list(pageable);
	}

	/**
	 * Override point for project-aware pages to create new entity instances.
	 * Default implementation creates a new entity using newEntity().
	 */
	protected EntityClass createNewEntityInstance() {
		return newEntity();
	}

	/**
	 * Override point for project-aware pages to set the project for entities.
	 * Default implementation does nothing (for non-project-aware entities).
	 * 
	 * @param entity The entity to set the project for
	 * @param project The project to set
	 */
	protected void setProjectForEntity(final EntityClass entity, final CProject project) {
		// Default implementation does nothing
	}

	/**
	 * Sets up the toolbar for the page.
	 */
	@Override
	protected abstract void setupToolbar();
}
