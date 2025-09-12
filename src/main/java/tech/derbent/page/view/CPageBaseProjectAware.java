package tech.derbent.page.view;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.interfaces.CProjectChangeListener;
import tech.derbent.abstracts.services.CDetailsBuilder;
import tech.derbent.abstracts.views.components.CDiv;
import tech.derbent.abstracts.views.components.CFlexLayout;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.session.service.CLayoutService;
import tech.derbent.session.service.CSessionService;

public abstract class CPageBaseProjectAware extends CPageBase implements CProjectChangeListener {

	private static final long serialVersionUID = 1L;
	protected CFlexLayout baseDetailsLayout;
	protected CEnhancedBinder<CEntityDB<?>> currentBinder; // Store current binder for data binding
	protected final CDetailsBuilder detailsBuilder = new CDetailsBuilder();
	protected CLayoutService layoutService;
	private CDetailSectionService screenService;
	protected final CSessionService sessionService;
	protected SplitLayout splitLayout = new SplitLayout();

	protected CPageBaseProjectAware(final CSessionService sessionService, CDetailSectionService screenService) {
		super();
		this.screenService = screenService;
		this.sessionService = sessionService;
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		LOGGER.debug("Entering Sample Page");
	}

	protected void buildScreen(final String baseViewName) {
		buildScreen(baseViewName, CEntityDB.class, null);
	}

	protected <T extends CEntityDB<?>> void buildScreen(final String baseViewName, final Class<T> entityClass) {
		buildScreen(baseViewName, entityClass, null);
	}

	protected <T extends CEntityDB<?>> void buildScreen(final String baseViewName, final Class<T> entityClass, final Component toolbar) {
		try {
			// Clear previous content from details layout to avoid accumulation
			getBaseDetailsLayout().removeAll();
			final CDetailSection screen = screenService.findByNameAndProject(sessionService.getActiveProject().orElse(null), baseViewName);
			if (screen == null) {
				final String errorMsg = "Screen not found: " + baseViewName + " for project: "
						+ sessionService.getActiveProject().map(CProject::getName).orElse("No Project");
				getBaseDetailsLayout().add(new CDiv(errorMsg));
				currentBinder = null; // Clear binder if screen not found
				return;
			}
			// Create a local binder for this specific screen using the actual entity class
			@SuppressWarnings ("unchecked")
			final CEnhancedBinder<CEntityDB<?>> localBinder = new CEnhancedBinder<>((Class<CEntityDB<?>>) (Class<?>) entityClass);
			currentBinder = localBinder; // Store the binder for data binding
			// If toolbar is provided, add it to the details container (at top level, above the scroller)
			if (toolbar != null && this instanceof CPageGenericEntity) {
				CPageGenericEntity<?> pageEntity = (CPageGenericEntity<?>) this;
				// Clear any existing toolbar from details container first
				pageEntity.baseDetailsLayout.getChildren()
						.filter(child -> child == toolbar || child.getClass().getSimpleName().contains("CrudToolbar"))
						.forEach(pageEntity.baseDetailsLayout::remove);
				// Add toolbar at the top of details container (before the scroller)
				pageEntity.baseDetailsLayout.addComponentAsFirst(toolbar);
				pageEntity.baseDetailsLayout.setFlexGrow(0, toolbar); // Toolbar has fixed size
			}
			detailsBuilder.buildDetails(screen, localBinder, getBaseDetailsLayout());
		} catch (final Exception e) {
			final String errorMsg = "Error building details layout for screen: " + baseViewName;
			e.printStackTrace();
			getBaseDetailsLayout().add(new CDiv(errorMsg));
			currentBinder = null; // Clear binder on error
		}
	}

	public CFlexLayout getBaseDetailsLayout() { return baseDetailsLayout; }

	/** Get the current binder for data binding operations */
	protected CEnhancedBinder<CEntityDB<?>> getCurrentBinder() { return currentBinder; }

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
	}

	@Override
	protected void setupToolbar() {
		LOGGER.debug("Setting up toolbar in Sample Page");
	}
}
