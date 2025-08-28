package tech.derbent.abstracts.views;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import jakarta.annotation.PostConstruct;
import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.components.CSearchToolbar;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.interfaces.CLayoutChangeListener;
import tech.derbent.abstracts.interfaces.CSearchable;
import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.abstracts.utils.PageableUtils;
import tech.derbent.abstracts.views.components.CButton;
import tech.derbent.abstracts.views.components.CFlexLayout;
import tech.derbent.abstracts.views.components.CVerticalLayout;
import tech.derbent.abstracts.views.grids.CGrid;
import tech.derbent.abstracts.views.grids.CMasterViewSectionGrid;
import tech.derbent.base.ui.dialogs.CConfirmationDialog;
import tech.derbent.base.ui.dialogs.CWarningDialog;
import tech.derbent.session.service.CSessionService;
import tech.derbent.session.service.LayoutService;

public abstract class CAbstractEntityDBPage<EntityClass extends CEntityDB<EntityClass>> extends CAbstractPage implements CLayoutChangeListener {
	private static final long serialVersionUID = 1L;
	protected final Class<EntityClass> entityClass;
	private final CEnhancedBinder<EntityClass> binder;
	// Search functionality
	protected CSearchToolbar searchToolbar;
	protected String currentSearchText = "";
	protected CMasterViewSectionGrid<EntityClass> masterViewSection;
	// divide screen into two parts
	protected SplitLayout splitLayout = new SplitLayout();
	private final CFlexLayout baseDetailsLayout;
	// private final VerticalLayout baseDetailsLayout = new VerticalLayout();
	private final Div detailsTabLayout = new Div();
	private EntityClass currentEntity;
	protected final CAbstractService<EntityClass> entityService;
	protected LayoutService layoutService; // Optional injection
	ArrayList<CAccordionDBEntity<EntityClass>> AccordionList = new ArrayList<CAccordionDBEntity<EntityClass>>(); // List of accordions
	{}
	protected CSessionService sessionService;

	protected CAbstractEntityDBPage(final Class<EntityClass> entityClass, final CAbstractService<EntityClass> entityService,
			final CSessionService sessionService) {
		super();
		this.entityClass = entityClass;
		this.entityService = entityService;
		this.sessionService = sessionService;
		this.baseDetailsLayout = CFlexLayout.forEntityPage();
		// dont setid here, as it may not be initialized yet
		binder = new CEnhancedBinder<>(entityClass);
		// layout for the secondary part of the split layout create the tab layout for the
		// details view top
		detailsTabLayout.setClassName("details-tab-layout");
		// now the content are!!!
		final Scroller scroller = new Scroller();
		// FLEX LAYOUT///////////////////
		scroller.setContent(baseDetailsLayout);
		scroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
		final CVerticalLayout detailsBase = new CVerticalLayout(false, false, false);
		detailsBase.add(detailsTabLayout, scroller);
		initSplitLayout(detailsBase);
		// Initial layout setup - will be updated when layout service is available
		updateLayoutOrientation();
	}

	// for details view
	protected void addAccordionPanel(final CAccordionDBEntity<EntityClass> accordion) {
		AccordionList.add(accordion);
		getBaseDetailsLayout().add(accordion);
	}

	// this method is called before the page is entered
	@Override
	public void beforeEnter(final BeforeEnterEvent event) {
		LOGGER.debug("beforeEnter called for {}", getClass().getSimpleName());
		if (masterViewSection == null) {
			LOGGER.warn("Grid is null, cannot populate form");
			return;
		}
		Optional<EntityClass> lastEntity = null;
		final Optional<Long> entityID = event.getRouteParameters().get(getEntityRouteIdField()).map(Long::parseLong);
		if (entityID.isPresent()) {
			lastEntity = entityService.getById(entityID.get());
			if (lastEntity.isEmpty()) {
				LOGGER.warn("Entity with ID {} not found in database", entityID.get());
			}
		} else {
			lastEntity = entityService.getById(sessionService.getActiveId(entityClass.getSimpleName()));
		}
		// populateForm(null);
		masterViewSection.selectLastOrFirst(lastEntity.orElse(null));
	}

	protected void createButtonLayout(final Div layout) {
		LOGGER.debug("createButtonLayout called - default save/delete/cancel buttons are now in details tab");
		// Default implementation does nothing - buttons are in the tab Subclasses can
		// override this for additional custom buttons in the main content area
	}

	protected CButton createCancelButton(final String buttonText) {
		final CButton cancel = CButton.createTertiary(buttonText, null, e -> {
			final Long lastSelectedId = sessionService.getActiveId(getClass().getSimpleName());
			masterViewSection.selectLastOrFirst(entityService.getById(lastSelectedId).orElse(null));
		});
		return cancel;
	}

	protected CButton createDeleteButton(final String buttonText) {
		final CButton delete = CButton.createTertiary(buttonText, null, null);
		delete.addClickListener(e -> {
			if (currentEntity == null) {
				new CWarningDialog("Please select an item to delete.").open();
				return;
			}
			// Show confirmation dialog for delete operation
			final String confirmMessage = String.format("Are you sure you want to delete this %s? This action cannot be undone.",
					entityClass.getSimpleName().replace("C", "").toLowerCase());
			new CConfirmationDialog(confirmMessage, () -> {
				try {
					LOGGER.info("Deleting entity: {} with ID: {}", entityClass.getSimpleName(), currentEntity.getId());
					entityService.delete(currentEntity);
					masterViewSection.selectLastOrFirst(null);
					safeShowNotification("Item deleted successfully");
				} catch (final Exception exception) {
					LOGGER.error("Error deleting entity", exception);
					new CWarningDialog("Failed to delete the item. Please try again.").open();
				}
			}).open();
		});
		return delete;
	}

	@PostConstruct
	private final void createDetails() throws Exception {
		createDetailsViewTab();
		createDetailsComponent();
		updateDetailsComponent();
	}

	protected abstract void createDetailsComponent() throws Exception;

	/** Creates the button layout for the details tab. Contains new, save, cancel, and delete buttons with consistent styling.
	 * @return HorizontalLayout with action buttons */
	protected HorizontalLayout createDetailsTabButtonLayout() {
		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setClassName("details-tab-button-layout");
		buttonLayout.setSpacing(true);
		buttonLayout.add(createNewButton("New"), createSaveButton("Save"), createCancelButton("Cancel"), createDeleteButton("Delete"));
		return buttonLayout;
	}

	/** Creates the left content of the details tab. Subclasses can override this to provide custom tab content.
	 * @return Component for the left side of the tab */
	protected Div createDetailsTabLeftContent() {
		final Div detailsTabLabel = new Div();
		detailsTabLabel.setText("Details");
		detailsTabLabel.setClassName("details-tab-label");
		return detailsTabLabel;
	}

	/** Creates the default details view tab content with save/delete/cancel buttons. Subclasses can override this method to customize the tab content
	 * while maintaining consistent button placement and styling. */
	protected void createDetailsViewTab() {
		// Clear any existing content
		getDetailsTabLayout().removeAll();
		// Create a horizontal layout for the tab content
		final HorizontalLayout tabContent = new HorizontalLayout();
		tabContent.setWidthFull();
		tabContent.setJustifyContentMode(HorizontalLayout.JustifyContentMode.BETWEEN);
		tabContent.setPadding(true);
		tabContent.setSpacing(true);
		tabContent.setClassName("details-tab-content");
		// Left side: Tab label or custom content (can be overridden by subclasses)
		final Div leftContent = createDetailsTabLeftContent();
		// Right side: Action buttons
		final HorizontalLayout buttonLayout = createDetailsTabButtonLayout();
		tabContent.add(leftContent, buttonLayout);
		getDetailsTabLayout().add(tabContent);
	}

	public void createGridForEntity(final CGrid<EntityClass> grid) {}

	protected void createGridLayout() {
		masterViewSection = new CMasterViewSectionGrid<EntityClass>(entityClass, this);
		masterViewSection.addSelectionChangeListener(this::onSelectionChanged);
		// Create search toolbar if entity supports searching
		if (CSearchable.class.isAssignableFrom(entityClass)) {
			searchToolbar = new CSearchToolbar("Search " + entityClass.getSimpleName().replace("C", "").toLowerCase() + "...");
			searchToolbar.addSearchListener(event -> {
				currentSearchText = event.getSearchText();
				refreshGrid();
			});
		}
		// Use a custom data provider that properly handles pagination, sorting and
		// searching
		masterViewSection.getGrid().setItems(query -> {
			LOGGER.debug("Grid query - offset: {}, limit: {}, sortOrders: {}, searchText: '{}'", query.getOffset(), query.getLimit(),
					query.getSortOrders(), currentSearchText);
			try {
				final Pageable originalPageable = VaadinSpringDataHelpers.toSpringPageRequest(query);
				final Pageable safePageable = PageableUtils.validateAndFix(originalPageable);
				LOGGER.debug("Safe Pageable - pageNumber: {}, pageSize: {}, sort: {}", safePageable.getPageNumber(), safePageable.getPageSize(),
						safePageable.getSort());
				final List<EntityClass> result;
				if ((currentSearchText != null) && !currentSearchText.trim().isEmpty() && CSearchable.class.isAssignableFrom(entityClass)) {
					result = entityService.list(safePageable, currentSearchText);
				} else {
					result = entityService.list(safePageable).getContent();
				}
				LOGGER.debug("Data provider returned {} items", result.size());
				return result.stream();
			} catch (final Exception e) {
				LOGGER.error("Error in grid data provider for {}: {}", entityClass.getSimpleName(), e.getMessage());
				// Check if this is a lazy loading exception
				if (e.getCause() instanceof org.hibernate.LazyInitializationException) {
					LOGGER.error("LazyInitializationException detected - check repository fetch joins for {}", entityClass.getSimpleName());
				}
				// Return empty stream on error to prevent UI crashes
				return Stream.empty();
			}
		});
		// grid.addIdColumn(entity -> entity.getId().toString(), "ID", "id");
		// Create the grid container with search toolbar
		final VerticalLayout gridContainer = new VerticalLayout();
		gridContainer.setClassName("grid-container");
		gridContainer.setPadding(false);
		gridContainer.setSpacing(false);
		// Add search toolbar if available
		if (searchToolbar != null) {
			gridContainer.add(searchToolbar);
		}
		gridContainer.add(masterViewSection);
		gridContainer.setFlexGrow(1, masterViewSection);
		splitLayout.addToPrimary(gridContainer);
	}

	@PostConstruct
	protected final void createMaster() throws Exception {
		createMasterComponent();
		updateMasterComponent();
	}

	protected abstract void createMasterComponent();

	protected CButton createNewButton(final String buttonText) {
		final CButton newButton = CButton.createTertiary(buttonText, null, e -> {
			LOGGER.debug("New button clicked, emptying bound fields");
			try {
				// Empty the bound fields by creating new entity and binding it
				final EntityClass newEntityInstance = createNewEntity();
				setCurrentEntity(newEntityInstance);
				populateForm(newEntityInstance);
				LOGGER.debug("Bound fields emptied for new entity: {}", newEntityInstance.getClass().getSimpleName());
			} catch (final Exception exception) {
				LOGGER.error("Error emptying bound fields", exception);
				new CWarningDialog("Failed to empty fields. Please try again.").open();
			}
		});
		return newButton;
	}

	protected EntityClass createNewEntity() {
		return entityService.newEntity();
	}

	protected CButton createSaveButton(final String buttonText) {
		final CButton save = CButton.createPrimary(buttonText, null, e -> {
			try {
				// Ensure we have an entity to save
				LOGGER.debug("Save button clicked, current entity: {}", currentEntity != null ? currentEntity.getId() : "null");
				if (currentEntity == null) {
					LOGGER.warn("No current entity for save operation, creating new entity");
					currentEntity = createNewEntity();
					populateForm(currentEntity);
				}
				if (!onBeforeSaveEvent()) {
					return;
				}
				// Write form data to entity
				getBinder().writeBean(currentEntity);
				// Validate entity before saving
				validateEntityForSave(currentEntity);
				// Save entity
				final EntityClass savedEntity = entityService.save(currentEntity);
				LOGGER.info("Entity saved successfully with ID: {}", savedEntity.getId());
				// Update current entity with saved version (includes generated ID)
				setCurrentEntity(savedEntity);
				// Clear form and refresh grid
				refreshGrid();
				// Show success notification
				safeShowNotification("Data saved successfully");
				// Navigate back to the current view (list mode) safely
				safeNavigateToClass();
			} catch (final ObjectOptimisticLockingFailureException exception) {
				LOGGER.error("Optimistic locking failure during save", exception);
				safeShowErrorNotification("Error updating the data. Somebody else has updated the record while you were making changes.");
			} catch (final ValidationException validationException) {
				LOGGER.error("Validation error during save", validationException);
				new CWarningDialog("Failed to save the data. Please check that all required fields are filled and values are valid.").open();
			} catch (final Exception exception) {
				LOGGER.error("Unexpected error during save operation", exception);
				new CWarningDialog("An unexpected error occurred while saving. Please try again.").open();
			}
		});
		return save;
	}

	public HasComponents getBaseDetailsLayout() { return baseDetailsLayout; }

	public CEnhancedBinder<EntityClass> getBinder() { return binder; }

	public EntityClass getCurrentEntity() { return currentEntity; }

	public Div getDetailsTabLayout() { return detailsTabLayout; }

	protected abstract String getEntityRouteIdField();

	/** Gets the search toolbar component, if available.
	 * @return the search toolbar component, or null if entity doesn't support searching */
	public CSearchToolbar getSearchToolbar() {
		return searchToolbar;
	}

	@PostConstruct
	protected void initPageId() {
		// set page ID in this syntax to check with playwright tests
		final String pageid = this.getClass().getSimpleName().toLowerCase();
		super.setId("pageid-" + pageid);
		super.addClassNames("class-" + pageid);
	}

	private void initSplitLayout(final VerticalLayout detailsBase) {
		splitLayout.setSizeFull();
		splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
		splitLayout.addToSecondary(detailsBase);
		add(splitLayout);
	}

	// this method is called when the page is attached to the UI
	@Override
	protected void onAttach(final AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		// Register for layout change notifications if service is available
		if (layoutService != null) {
			layoutService.addLayoutChangeListener(this);
			// Update layout based on current mode
			updateLayoutOrientation();
		}
	}

	protected boolean onBeforeSaveEvent() {
		if (entityService.onBeforeSaveEvent(currentEntity)) {
			LOGGER.info("onBeforeSaveEvent passed for entity: {} in {}", getCurrentEntity(), this.getClass().getSimpleName());
		} else {
			LOGGER.warn("onBeforeSaveEvent failed for entity: {} in {}", getCurrentEntity(), this.getClass().getSimpleName());
			return false;
		}
		return true;
	}

	@Override
	protected void onDetach(final DetachEvent detachEvent) {
		super.onDetach(detachEvent);
		// Unregister from layout change notifications
		if (layoutService != null) {
			layoutService.removeLayoutChangeListener(this);
		}
	}

	@Override
	public void onLayoutModeChanged(final LayoutService.LayoutMode newMode) {
		LOGGER.debug("Layout mode changed to: {} for {}", newMode, getClass().getSimpleName());
		updateLayoutOrientation();
	}

	protected void onSelectionChanged(final CMasterViewSectionGrid.SelectionChangeEvent<EntityClass> event) {
		final EntityClass value = (event.getSelectedItem());
		LOGGER.debug("Grid selection changed: {}", Optional.ofNullable(value).map(Object::toString).orElse("NULL"));
		populateForm(value);
		if (value == null) {
			sessionService.setActiveId(entityClass.getClass().getSimpleName(), null);
		} else {
			sessionService.setActiveId(entityClass.getClass().getSimpleName(), value.getId());
		}
	}

	protected void populateAccordionPanels(final EntityClass entity) {
		LOGGER.debug("Populating accordion panels for entity: {}", entity != null ? entity.getId() : "null");
		// This method can be overridden by subclasses to populate accordion panels
		AccordionList.forEach(accordion -> {
			accordion.populateForm(entity);
		});
	}

	protected void populateForm(final EntityClass value) {
		LOGGER.debug("Populating form for entity: {}", value != null ? value.getId() : "null");
		currentEntity = value;
		sessionService.setActiveId(entityClass.getSimpleName(), value == null ? null : value.getId());
		populateAccordionPanels(value);
		getBinder().readBean(value);
		if ((value == null) && (masterViewSection != null)) {
			masterViewSection.select(null);
		}
	}

	protected void refreshGrid() {
		LOGGER.info("Refreshing grid for {}", getClass().getSimpleName());
		// Store the currently selected entity ID to preserve selection after refresh
		final EntityClass selectedEntity = masterViewSection.getSelectedItem();
		final Long selectedEntityId = selectedEntity != null ? selectedEntity.getId() : null;
		// Clear selection and refresh data
		masterViewSection.select(null);
		masterViewSection.refresh();
		// Restore selection if there was a previously selected entity
		if (selectedEntityId != null) {
			masterViewSection.selectLastOrFirst(entityService.getById(selectedEntityId).orElse(null));
		}
	}

	/** Safely navigates to the view class without throwing exceptions if UI is not available. This is important for testing and edge cases where UI
	 * might not be properly initialized. */
	protected void safeNavigateToClass() {
		try {
			final UI currentUI = UI.getCurrent();
			if (currentUI != null) {
				currentUI.navigate(getClass());
			} else {
				LOGGER.warn("UI not available for navigation to {}", getClass().getSimpleName());
			}
		} catch (final Exception e) {
			LOGGER.warn("Error during navigation to {}: {}", getClass().getSimpleName(), e.getMessage());
		}
	}

	/** Safely shows an error notification without throwing exceptions if UI is not available. */
	protected void safeShowErrorNotification(final String message) {
		try {
			final Notification notification = Notification.show(message);
			notification.setPosition(Position.MIDDLE);
			notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
			LOGGER.debug("Shown error notification: {}", message);
		} catch (final Exception e) {
			LOGGER.warn("Error showing error notification '{}': {}", message, e.getMessage());
		}
	}

	/** Safely shows a success notification without throwing exceptions if UI is not available. */
	protected void safeShowNotification(final String message) {
		try {
			final Notification notification = Notification.show(message);
			notification.setPosition(Position.BOTTOM_START);
			LOGGER.debug("Shown notification: {}", message);
		} catch (final Exception e) {
			LOGGER.warn("Error showing notification '{}': {}", message, e.getMessage());
		}
	}

	public void setCurrentEntity(final EntityClass currentEntity) {
		LOGGER.debug("Setting current entity: {}", currentEntity);
		this.currentEntity = currentEntity;
	}

	@Override
	public void setId(final String id) {
		throw new UnsupportedOperationException("Use initPageId instead to set page ID for testing purposes");
	}

	/** Sets the layout service. This is typically called via dependency injection or manually after construction. */
	public void setLayoutService(final LayoutService layoutService) {
		this.layoutService = layoutService;
		// Update layout based on current mode
		updateLayoutOrientation();
	}

	@Override
	protected void setupToolbar() {}

	/** Populates the form with entity data - public wrapper for testing.
	 * @param entity the entity to populate the form with */
	public void testPopulateForm(final EntityClass entity) {
		populateForm(entity);
	}

	protected abstract void updateDetailsComponent()
			throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException, Exception;

	/** Updates the split layout orientation based on the current layout mode. */
	private void updateLayoutOrientation() {
		if ((layoutService != null) && (splitLayout != null)) {
			final LayoutService.LayoutMode currentMode = layoutService.getCurrentLayoutMode();
			LOGGER.debug("Updating layout orientation to: {} for {}", currentMode, getClass().getSimpleName());
			if (currentMode == LayoutService.LayoutMode.HORIZONTAL) {
				splitLayout.setOrientation(SplitLayout.Orientation.HORIZONTAL);
				// For horizontal layout, give more space to the grid (left side)
				splitLayout.setSplitterPosition(50.0); // 50% for grid, 50% for details
			} else {
				splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
				// For vertical layout, give more space to the grid (top)
				splitLayout.setSplitterPosition(30.0); // 30% for grid, 70% for details
			}
			// Force UI refresh to apply changes immediately
			getUI().ifPresent(ui -> ui.access(() -> {
				splitLayout.getElement().callJsFunction("$server.requestUpdate");
			}));
		} else {
			// Default fallback when no layout service is available
			splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
			splitLayout.setSplitterPosition(30.0);
		}
	}

	protected void updateMasterComponent() throws Exception {
		if (masterViewSection == null) {
			LOGGER.warn("Grid is null, cannot add selection listener");
			return;
		}
		masterViewSection.addSelectionChangeListener(this::onSelectionChanged);
		// Create search toolbar if entity supports searching
		if (CSearchable.class.isAssignableFrom(entityClass)) {
			searchToolbar = new CSearchToolbar("Search " + entityClass.getSimpleName().replace("C", "").toLowerCase() + "...");
			searchToolbar.addSearchListener(event -> {
				currentSearchText = event.getSearchText();
				refreshGrid();
			});
		}
		// Use a custom data provider that properly handles pagination, sorting and
		// searching
		masterViewSection.getGrid().setItems(query -> {
			LOGGER.debug("Grid query - offset: {}, limit: {}, sortOrders: {}, searchText: '{}'", query.getOffset(), query.getLimit(),
					query.getSortOrders(), currentSearchText);
			try {
				final Pageable originalPageable = VaadinSpringDataHelpers.toSpringPageRequest(query);
				final Pageable safePageable = PageableUtils.validateAndFix(originalPageable);
				LOGGER.debug("Safe Pageable - pageNumber: {}, pageSize: {}, sort: {}", safePageable.getPageNumber(), safePageable.getPageSize(),
						safePageable.getSort());
				final List<EntityClass> result;
				if ((currentSearchText != null) && !currentSearchText.trim().isEmpty() && CSearchable.class.isAssignableFrom(entityClass)) {
					result = entityService.list(safePageable, currentSearchText);
				} else {
					result = entityService.list(safePageable).getContent();
				}
				LOGGER.debug("Data provider returned {} items", result.size());
				return result.stream();
			} catch (final Exception e) {
				LOGGER.error("Error in grid data provider for {}: {}", entityClass.getSimpleName(), e.getMessage());
				// Check if this is a lazy loading exception
				if (e.getCause() instanceof org.hibernate.LazyInitializationException) {
					LOGGER.error("LazyInitializationException detected - check repository fetch joins for {}", entityClass.getSimpleName());
				}
				// Return empty stream on error to prevent UI crashes
				return Stream.empty();
			}
		});
		// grid.addIdColumn(entity -> entity.getId().toString(), "ID", "id");
		// Create the grid container with search toolbar
		final VerticalLayout gridContainer = new VerticalLayout();
		gridContainer.setClassName("grid-container");
		gridContainer.setPadding(false);
		gridContainer.setSpacing(false);
		// Add search toolbar if available
		if (searchToolbar != null) {
			gridContainer.add(searchToolbar);
		}
		gridContainer.add(masterViewSection);
		gridContainer.setFlexGrow(1, masterViewSection);
		splitLayout.addToPrimary(gridContainer);
	}

	/** Validates an entity before saving. Subclasses can override this method to add custom validation logic beyond the standard bean validation.
	 * @param entity the entity to validate
	 * @throws IllegalArgumentException if validation fails */
	protected void validateEntityForSave(final EntityClass entity) {
		if (entity == null) {
			throw new IllegalArgumentException("Entity cannot be null");
		}
	}
}
