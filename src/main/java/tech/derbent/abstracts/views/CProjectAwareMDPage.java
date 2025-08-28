package tech.derbent.abstracts.views;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.abstracts.interfaces.CProjectChangeListener;
import tech.derbent.abstracts.services.CAbstractNamedEntityService;
import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.abstracts.utils.PageableUtils;
import tech.derbent.abstracts.views.components.CVerticalLayout;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.session.service.CSessionService;

/** Abstract project-aware MD page that filters entities by the currently active project. Implements CProjectChangeListener to receive immediate
 * notifications when the active project changes. */
public abstract class CProjectAwareMDPage<EntityClass extends CEntityOfProject<EntityClass>> extends CAbstractNamedEntityPage<EntityClass>
		implements CProjectChangeListener {
	private static final long serialVersionUID = 1L;
	protected final CSessionService sessionService;

	protected CProjectAwareMDPage(final Class<EntityClass> entityClass, final CAbstractNamedEntityService<EntityClass> entityService,
			final CSessionService sessionService, final CScreenService screenService) {
		super(entityClass, entityService, sessionService, screenService);
		this.sessionService = sessionService;
		// Now that sessionService is set, we can populate the grid
		refreshProjectAwareGrid();
	}

	@Override
	protected EntityClass createNewEntity() {
		final String name = "New Item";
		final CProject project = sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No current project set in session"));
		return ((CEntityOfProjectService<EntityClass>) entityService).newEntity(name, project);
	}

	@Override
	protected void onAttach(final AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		// Register this component to receive project change notifications
		sessionService.addProjectChangeListener(this);
	}

	/** Called when the component is detached from the UI. Unregisters the project change listener to prevent memory leaks. */
	@Override
	protected void onDetach(final DetachEvent detachEvent) {
		super.onDetach(detachEvent);
		// Unregister this component to prevent memory leaks
		sessionService.removeProjectChangeListener(this);
	}

	/** Implementation of CProjectChangeListener interface. Called when the active project changes via the SessionService.
	 * @param newProject The newly selected project */
	@Override
	public void onProjectChanged(final CProject newProject) {
		LOGGER.debug("Project change notification received: {}", newProject != null ? newProject.getName() : "null");
		refreshProjectAwareGrid();
	}

	/** Refreshes the grid with project-aware data. */
	protected void refreshProjectAwareGrid() {
		LOGGER.debug("Refreshing project-aware grid");
		if ((sessionService == null) || (masterViewSection == null)) {
			// Not fully initialized yet
			return;
		}
		final Optional<CProject> activeProject = sessionService.getActiveProject();
		if (activeProject.isPresent()) {
			LOGGER.debug("Loading entities for active project: {}", activeProject.get().getName());
			List<EntityClass> entities;
			// Check if the entity service is for CEntityOfProject entities
			if (entityService instanceof CEntityOfProjectService) {
				final CEntityOfProjectService<EntityClass> projectService = (CEntityOfProjectService<EntityClass>) entityService;
				entities = projectService.findEntriesByProject(activeProject.get(), PageableUtils.createSafe(0, 10)).getContent();
			} else {
				// For non-project entities, show all entities (they don't have project
				// filtering)
				LOGGER.debug("Entity service is not project-aware, showing all entities");
				entities = entityService.list(PageableUtils.createSafe(0, 10)).getContent();
			}
			masterViewSection.getGrid().setItems(entities);
		} else {
			// If no active project, show empty grid
			LOGGER.debug("No active project found, clearing grid items");
			masterViewSection.getGrid().setItems(Collections.emptyList());
		}
	}

	/** Sets the project for the entity. */
	public void setProjectForEntity(final EntityClass entity, final CProject project) {
		assert entity != null : "Entity must not be null";
		assert project != null : "Project must not be null";
		if (entity instanceof CEntityOfProject) {
			entity.setProject(project);
		} else {
			throw new IllegalArgumentException("Entity must implement CEntityOfProject interface");
		}
	}

	@Override
	protected void updateDetailsComponent()
			throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException, Exception {
		final CVerticalLayout formLayout = CEntityFormBuilder.buildForm(entityClass, getBinder());
		getBaseDetailsLayout().add(formLayout);
	}
}
