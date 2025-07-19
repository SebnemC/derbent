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
 */
public abstract class CProjectAwareMDPage<EntityClass extends CEntityDB> extends CAbstractMDPage<EntityClass> {

	private static final long serialVersionUID = 1L;
	protected final SessionService sessionService;
	private Long lastProjectChangeTime = 0L;

	protected CProjectAwareMDPage(final Class<EntityClass> entityClass, final CAbstractService<EntityClass> entityService, final SessionService sessionService) {
		super(entityClass, entityService);
		this.sessionService = sessionService;
		// Now that sessionService is set, we can populate the grid
		refreshProjectAwareGrid();
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		// Check for project changes when view is attached
		checkForProjectChanges();
	}

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
	 */
	protected void refreshProjectAwareGrid() {
		if (sessionService == null || grid == null) {
			// Not fully initialized yet
			return;
		}
		
		final Optional<CProject> activeProject = sessionService.getActiveProject();
		if (activeProject.isPresent()) {
			grid.setItems(query -> getProjectFilteredData(activeProject.get(), VaadinSpringDataHelpers.toSpringPageRequest(query)).stream());
		} else {
			// If no active project, show empty grid
			grid.setItems(Collections.emptyList());
		}
	}

	@Override
	protected void refreshGrid() {
		grid.select(null);
		refreshProjectAwareGrid();
	}

	@Override
	protected EntityClass newEntity() {
		final EntityClass entity = createNewEntityInstance();
		// Set the active project if available
		sessionService.getActiveProject().ifPresent(project -> setProjectForEntity(entity, project));
		return entity;
	}

	/**
	 * Checks for project changes and refreshes if needed.
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
	 */
	protected abstract java.util.List<EntityClass> getProjectFilteredData(CProject project, org.springframework.data.domain.Pageable pageable);

	/**
	 * Creates a new instance of the entity.
	 */
	protected abstract EntityClass createNewEntityInstance();

	/**
	 * Sets the project for the entity.
	 */
	protected abstract void setProjectForEntity(EntityClass entity, CProject project);
}