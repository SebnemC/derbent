package tech.derbent.abstracts.views;

import java.util.Collections;
import java.util.Optional;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;

import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.interfaces.CProjectChangeListener;
import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.layout.service.LayoutStateService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.SessionService;

/**
 * Abstract project-aware MD page that filters entities by the currently active project. Implements
 * CProjectChangeListener to receive immediate notifications when the active project changes.
 */
public abstract class CProjectAwareMDPage<EntityClass extends CEntityDB> extends CAbstractMDPage<EntityClass>
        implements CProjectChangeListener {

    private static final long serialVersionUID = 1L;
    protected final SessionService sessionService;

    protected CProjectAwareMDPage(final Class<EntityClass> entityClass,
            final CAbstractService<EntityClass> entityService, final SessionService sessionService) {
        this(entityClass, entityService, sessionService, null);
    }

    protected CProjectAwareMDPage(final Class<EntityClass> entityClass,
            final CAbstractService<EntityClass> entityService, final SessionService sessionService,
            final LayoutStateService layoutStateService) {
        super(entityClass, entityService, layoutStateService);
        this.sessionService = sessionService;
        // Now that sessionService is set, we can populate the grid
        refreshProjectAwareGrid();
    }

    @Override
    protected void createGridLayout() {
        LOGGER.debug("Creating grid layout for project-aware MD page");
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
     * Creates a new instance of the entity.
     */
    protected abstract EntityClass createNewEntityInstance();

    /**
     * Gets filtered data for the current project.
     */
    protected abstract java.util.List<EntityClass> getProjectFilteredData(CProject project,
            org.springframework.data.domain.Pageable pageable);

    @Override
    protected EntityClass newEntity() {
        LOGGER.debug("Creating new entity instance for project-aware MD page");
        final EntityClass entity = createNewEntityInstance();
        // Set the active project if available
        sessionService.getActiveProject().ifPresent(project -> setProjectForEntity(entity, project));
        return entity;
    }

    @Override
    protected void onAttach(final AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // Register this component to receive project change notifications
        sessionService.addProjectChangeListener(this);
        LOGGER.debug("Registered project change listener for: {}", getClass().getSimpleName());
    }

    /**
     * Called when the component is detached from the UI. Unregisters the project change listener to prevent memory
     * leaks.
     */
    @Override
    protected void onDetach(final DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        // Unregister this component to prevent memory leaks
        sessionService.removeProjectChangeListener(this);
        LOGGER.debug("Unregistered project change listener for: {}", getClass().getSimpleName());
    }

    /**
     * Implementation of CProjectChangeListener interface. Called when the active project changes via the
     * SessionService.
     * 
     * @param newProject
     *            The newly selected project
     */
    @Override
    public void onProjectChanged(final CProject newProject) {
        LOGGER.debug("Project change notification received: {}", newProject != null ? newProject.getName() : "null");
        refreshProjectAwareGrid();
    }

    @Override
    protected void refreshGrid() {
        grid.select(null);
        refreshProjectAwareGrid();
    }

    /**
     * Refreshes the grid with project-aware data.
     */
    protected void refreshProjectAwareGrid() {
        LOGGER.debug("Refreshing project-aware grid");
        if ((sessionService == null) || (grid == null)) {
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
     * Sets the project for the entity.
     */
    protected abstract void setProjectForEntity(EntityClass entity, CProject project);
}