package tech.derbent.abstracts.views;

import java.util.Collections;
import java.util.Optional;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;

import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.SessionService;

/**
 * Abstract project-aware MD page that filters entities by the currently active project.
 * Layer: View (MVC)
 * 
 * This abstract base class provides common functionality for views that need to filter
 * data based on the currently selected project. It handles:
 * - Project-aware data filtering
 * - Automatic grid refresh when project changes
 * - Session management integration
 * - Lazy loading prevention through proper service calls
 */
public abstract class CProjectAwareMDPage<EntityClass extends CEntityDB> extends CAbstractMDPage<EntityClass> {

	private static final long serialVersionUID = 1L;
	protected final SessionService sessionService;
	private Long lastProjectChangeTime = 0L;

	/**
	 * Constructor for project-aware MD page.
	 * @param entityClass The class of the entity being managed
	 * @param entityService The service for entity operations
	 * @param sessionService The session service for project management
	 */
	protected CProjectAwareMDPage(final Class<EntityClass> entityClass, 
	                            final CAbstractService<EntityClass> entityService, 
	                            final SessionService sessionService) {
		super(entityClass, entityService);
		this.sessionService = sessionService;
		// Now that sessionService is set, we can populate the grid
		refreshProjectAwareGrid();
	}

	/**
	 * Called when the view is attached to the UI.
	 * Checks for project changes and refreshes data if needed.
	 */
	@Override
	protected void onAttach(final AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		// Check for project changes when view is attached
		checkForProjectChanges();
	}

	/**
	 * Creates the grid layout with proper configuration.
	 * Initially sets empty items to avoid lazy loading issues during initialization.
	 */
	@Override
	protected void createGridLayout() {
		grid = new com.vaadin.flow.component.grid.Grid<>(entityClass, false);
		grid.getColumns().forEach(grid::removeColumn);
		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
		
		// Initially set empty items - will be populated after view is fully initialized
		grid.setItems(Collections.emptyList());
		
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

	/**
	 * Refreshes the grid with project-aware data.
	 * Uses project-filtered service methods to avoid lazy loading issues.
	 */
	protected void refreshProjectAwareGrid() {
		if (sessionService == null || grid == null) {
			// Not fully initialized yet
			return;
		}
		
		final Optional<CProject> activeProject = sessionService.getActiveProject();
		if (activeProject.isPresent()) {
			grid.setItems(query -> getProjectFilteredData(activeProject.get(), 
				VaadinSpringDataHelpers.toSpringPageRequest(query)).stream());
		} else {
			// If no active project, show empty grid
			grid.setItems(Collections.emptyList());
		}
	}

	/**
	 * Overrides the base refresh grid method to use project-aware filtering.
	 */
	@Override
	protected void refreshGrid() {
		grid.select(null);
		refreshProjectAwareGrid();
	}

	/**
	 * Creates a new entity instance with the active project pre-set.
	 * @return New entity instance with project association
	 */
	@Override
	protected EntityClass newEntity() {
		final EntityClass entity = createNewEntityInstance();
		// Set the active project if available
		sessionService.getActiveProject().ifPresent(project -> setProjectForEntity(entity, project));
		return entity;
	}

	/**
	 * Checks for project changes and refreshes grid if needed.
	 * Uses session attributes to detect when project has been changed.
	 */
	private void checkForProjectChanges() {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session != null) {
			final Long projectChangeTime = (Long) session.getAttribute("projectChanged");
			if (projectChangeTime != null && projectChangeTime > lastProjectChangeTime) {
				lastProjectChangeTime = projectChangeTime;
				refreshProjectAwareGrid();
				LOGGER.debug("Refreshed grid due to project change");
			}
		}
	}

	/**
	 * Gets filtered data for the current project.
	 * Implementations should use @Transactional service methods with JOIN FETCH
	 * to avoid lazy loading issues.
	 * @param project The project to filter by
	 * @param pageable Pagination information
	 * @return List of entities belonging to the specified project
	 */
	protected abstract java.util.List<EntityClass> getProjectFilteredData(CProject project, 
		org.springframework.data.domain.Pageable pageable);

	/**
	 * Creates a new instance of the entity.
	 * @return New entity instance
	 */
	protected abstract EntityClass createNewEntityInstance();

	/**
	 * Sets the project for the entity.
	 * @param entity The entity to set the project for
	 * @param project The project to associate with the entity
	 */
	protected abstract void setProjectForEntity(EntityClass entity, CProject project);
}