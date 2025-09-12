package tech.derbent.page.view;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import tech.derbent.abstracts.components.CCrudToolbar;
import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.interfaces.CProjectChangeListener;
import tech.derbent.abstracts.services.CDetailsBuilder;
import tech.derbent.abstracts.views.components.CDiv;
import tech.derbent.abstracts.views.components.CFlexLayout;
import tech.derbent.abstracts.views.components.CVerticalLayout;
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

	/** Hook method for subclasses to configure the CRUD toolbar with specific behavior like dependency checking */
	protected void configureCrudToolbar(CCrudToolbar<?> toolbar) {
		// Default implementation does nothing - subclasses can override to add specific configuration
	}

	/** Abstract method to create a new entity instance with project set */
	protected abstract <T extends CEntityDB<T>> T createNewEntity();

	protected void buildScreen(final String baseViewName) {
		buildScreen(baseViewName, CEntityDB.class, null, getBaseDetailsLayout());
	}

	protected <T extends CEntityDB<?>> void buildScreen(final String baseViewName, final Class<T> entityClass) {
		buildScreen(baseViewName, entityClass, null, getBaseDetailsLayout());
	}

	/** Build screen with optional toolbar integration. Creates its own binder to use for both form and toolbar components.
	 * @param baseViewName the view name to build
	 * @param entityClass  the entity class type
	 * @param toolbar      optional toolbar that will use the same binder (can be null) */
	protected <T extends CEntityDB<?>> void buildScreen(final String baseViewName, final Class<T> entityClass, final CCrudToolbar<?> toolbar) {
		buildScreen(baseViewName, entityClass, toolbar, getBaseDetailsLayout());
	}

	/** Build screen with optional toolbar integration and parameterized details layout.
	 * @param baseViewName  the view name to build
	 * @param entityClass   the entity class type
	 * @param toolbar       optional toolbar that will use the same binder (can be null)
	 * @param detailsLayout the layout to build the screen into */
	protected <T extends CEntityDB<?>> void buildScreen(final String baseViewName, final Class<T> entityClass, final CCrudToolbar<?> toolbar,
			final CFlexLayout detailsLayout) {
		try {
			detailsLayout.removeAll();
			final CDetailSection screen = screenService.findByNameAndProject(sessionService.getActiveProject().orElse(null), baseViewName);
			if (screen == null) {
				final String errorMsg = "Screen not found: " + baseViewName + " for project: "
						+ sessionService.getActiveProject().map(CProject::getName).orElse("No Project");
				detailsLayout.add(new CDiv(errorMsg));
				currentBinder = null;
				return;
			}
			// Only create binder if not already set for this entity type or if no current binder exists
			if (currentBinder == null || !currentBinder.getBeanType().equals(entityClass)) {
				@SuppressWarnings ("unchecked")
				final CEnhancedBinder<CEntityDB<?>> localBinder = new CEnhancedBinder<>((Class<CEntityDB<?>>) (Class<?>) entityClass);
				currentBinder = localBinder;
			}
			// Add toolbar and scrollable content directly to base layout (similar to CAbstractEntityDBPage pattern)
			if (toolbar != null) {
				// Add toolbar first (stays at top, not scrollable)
				toolbar.addClassName("crud-toolbar");
				detailsLayout.add(toolbar);
				// Create scrollable content area
				CFlexLayout scrollableContent = CFlexLayout.forEntityPage();
				final Scroller contentScroller = new Scroller();
				contentScroller.setContent(scrollableContent);
				contentScroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
				// Add scrollable content below toolbar
				detailsLayout.add(contentScroller);
				detailsLayout.setFlexGrow(1, contentScroller);
				// Build details in the scrollable content area
				detailsBuilder.buildDetails(screen, currentBinder, scrollableContent);
			} else {
				// No toolbar - build details directly
				detailsBuilder.buildDetails(screen, currentBinder, detailsLayout);
			}
		} catch (final Exception e) {
			final String errorMsg = "Error building details layout for screen: " + baseViewName;
			e.printStackTrace();
			detailsLayout.add(new CDiv(errorMsg));
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
