package tech.derbent.abstracts.views;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasOrderedComponents;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;

import jakarta.annotation.PostConstruct;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.interfaces.CLayoutChangeListener;
import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.base.ui.dialogs.CConfirmationDialog;
import tech.derbent.base.ui.dialogs.CWarningDialog;
import tech.derbent.session.service.LayoutService;

public abstract class CAbstractMDPage<EntityClass extends CEntityDB> extends CAbstractPage
	implements CLayoutChangeListener {

	private static final long serialVersionUID = 1L;

	protected final Class<EntityClass> entityClass;

	protected CGrid<EntityClass> grid;// = new CGrid<>(EntityClass.class, false);

	private final BeanValidationBinder<EntityClass> binder;

	protected SplitLayout splitLayout = new SplitLayout();

	// private final FlexLayout baseDetailsLayout = new FlexLayout();
	private final VerticalLayout baseDetailsLayout = new VerticalLayout();

	private final Div detailsTabLayout = new Div();

	private EntityClass currentEntity;

	protected final CAbstractService<EntityClass> entityService;

	protected LayoutService layoutService; // Optional injection

	ArrayList<CAccordionDescription<EntityClass>> AccordionList =
		new ArrayList<CAccordionDescription<EntityClass>>(); // List of accordions
	{}

	protected CAbstractMDPage(final Class<EntityClass> entityClass,
		final CAbstractService<EntityClass> entityService) {
		super();
		this.entityClass = entityClass;
		this.entityService = entityService;
		binder = new BeanValidationBinder<>(entityClass);
		addClassNames("md-page");
		setSizeFull();
		createGridLayout();
		// layout for the secondary part of the split layout
		final VerticalLayout detailsBase = new VerticalLayout();
		// create the tab layout for the details view top
		detailsTabLayout.setClassName("details-tab-layout");
		detailsBase.add(detailsTabLayout);
		// now the content are!!!
		final Scroller scroller = new Scroller();
		detailsBase.add(scroller);
		initBaseDetailsLayout();
		scroller.setContent(baseDetailsLayout);
		scroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
		// baseDetailsLayout.add(baseDescriptionAccordion);
		createGridForEntity();
		// binder = new BeanValidationBinder<>(entityClass
		initSplitLayout(detailsBase);
		// Initial layout setup - will be updated when layout service is available
		updateLayoutOrientation();
	}

	// for details view
	protected void addAccordionPanel(final CAccordionDescription<EntityClass> accordion) {
		AccordionList.add(accordion);
		getBaseDetailsLayout().add(accordion);
	}

	// this method is called before the page is entered
	@Override
	public void beforeEnter(final BeforeEnterEvent event) {
		LOGGER.debug("beforeEnter called for {}", getClass().getSimpleName());
		final Optional<Long> entityID =
			event.getRouteParameters().get(getEntityRouteIdField()).map(Long::parseLong);

		if (entityID.isPresent()) {
			final Optional<EntityClass> samplePersonFromBackend =
				entityService.get(entityID.get());

			if (samplePersonFromBackend.isPresent()) {
				final Optional<EntityClass> entity = entityService.get(entityID.get());
				populateForm(entity.get());

				if (grid != null) {
					grid.select(entity.get()); // Ensure grid selection matches the form
				}
				setLastSelectedEntityId(entity.get().getId());
			}
			else {
				Notification.show(
					String.format("The requested samplePerson was not found, ID = %s",
						entityID.get()),
					3000, Notification.Position.BOTTOM_START);
				// when a row is selected but the data is no longer available, refresh
				// grid
				refreshGrid();
			}
		}
		else if (getLastSelectedEntityId() != -1) {
			// If no specific entity ID in URL, try to select the last selected entity
			LOGGER.debug(
				"No entity ID in URL, trying to select last selected entity ID: {}",
				getLastSelectedEntityId());
			final Optional<EntityClass> lastEntity =
				entityService.get(getLastSelectedEntityId());

			if (lastEntity.isPresent()) {
				populateForm(lastEntity.get());

				if (grid != null) {
					grid.select(lastEntity.get()); // Ensure grid selection matches the
													// form
				}
			}
		}
		else {
			// No specific entity ID in URL (list mode), auto-select first item if
			// available
			LOGGER.debug("No entity ID in URL for {}, attempting auto-selection",
				getClass().getSimpleName());
			selectFirstItemIfAvailable();
		}
	}

	protected void clearForm() {
		populateForm(null);
	}

	/**
	 * Creates a button layout for additional buttons if needed by subclasses. Note:
	 * Default save/delete/cancel buttons are now in the details tab. This method can be
	 * used for additional custom buttons in the main content area.
	 * @param layout The layout to add buttons to
	 */
	protected void createButtonLayout(final Div layout) {
		LOGGER.debug(
			"createButtonLayout called - default save/delete/cancel buttons are now in details tab");
		// Default implementation does nothing - buttons are in the tab Subclasses can
		// override this for additional custom buttons in the main content area
	}

	protected CButton createCancelButton(final String buttonText) {
		final CButton cancel = CButton.createTertiary(buttonText, e -> {
			clearForm();
			refreshGrid();
		});
		return cancel;
	}

	protected CButton createDeleteButton(final String buttonText) {
		LOGGER.info("Creating delete button for CUsersView");
		final CButton delete = CButton.createTertiary(buttonText);
		delete.addClickListener(e -> {

			if (currentEntity == null) {
				new CWarningDialog("Please select an item to delete.").open();
				return;
			}
			// Show confirmation dialog for delete operation
			final String confirmMessage = String.format(
				"Are you sure you want to delete this %s? This action cannot be undone.",
				entityClass.getSimpleName().replace("C", "").toLowerCase());
			new CConfirmationDialog(confirmMessage, () -> {
				entityService.delete(currentEntity);
				clearForm();
				refreshGrid();
			}).open();
		});
		return delete;
	}

	@PostConstruct
	protected abstract void createDetailsLayout();

	/**
	 * Creates the button layout for the details tab. Contains new, save, cancel, and
	 * delete buttons with consistent styling.
	 * @return HorizontalLayout with action buttons
	 */
	protected HorizontalLayout createDetailsTabButtonLayout() {
		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setClassName("details-tab-button-layout");
		buttonLayout.setSpacing(true);
		buttonLayout.add(createNewButton("New"), createSaveButton("Save"),
			createCancelButton("Cancel"), createDeleteButton("Delete"));
		return buttonLayout;
	}

	@PostConstruct
	protected void createDetailsTabLayout() {
		// Create the default details view tab with buttons
		createDetailsViewTab();
	}

	/**
	 * Creates the left content of the details tab. Subclasses can override this to
	 * provide custom tab content.
	 * @return Component for the left side of the tab
	 */
	protected Div createDetailsTabLeftContent() {
		final Div detailsTabLabel = new Div();
		detailsTabLabel.setText("Details");
		detailsTabLabel.setClassName("details-tab-label");
		return detailsTabLabel;
	}

	/**
	 * Creates the default details view tab content with save/delete/cancel buttons.
	 * Subclasses can override this method to customize the tab content while maintaining
	 * consistent button placement and styling.
	 */
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

	protected abstract void createGridForEntity();

	protected void createGridLayout() {
		LOGGER.info("Creating grid layout for {}", getClass().getSimpleName());
		grid = new CGrid<>(entityClass, false);
		grid.getColumns().forEach(grid::removeColumn);
		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
		// Use a custom data provider that properly handles pagination and sorting
		grid.setItems(query -> {
			LOGGER.debug("Grid query - offset: {}, limit: {}, sortOrders: {}",
				query.getOffset(), query.getLimit(), query.getSortOrders());
			final org.springframework.data.domain.Pageable pageable =
				VaadinSpringDataHelpers.toSpringPageRequest(query);
			LOGGER.debug("Spring Pageable - pageNumber: {}, pageSize: {}, sort: {}",
				pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
			final java.util.List<EntityClass> result = entityService.list(pageable);
			LOGGER.debug("Data provider returned {} items", result.size());
			return result.stream();
		});
		grid.addColumn(entity -> entity.getId().toString()).setHeader("ID").setKey("id");
		// Add selection listener to the grid
		grid.asSingleSelect().addValueChangeListener(event -> {
			populateForm(event.getValue());

			if (event.getValue() != null) {
				setLastSelectedEntityId(event.getValue().getId());
			}
		});
		final Div wrapper = new Div();
		wrapper.setClassName("grid-wrapper");
		wrapper.add(grid);
		splitLayout.addToPrimary(wrapper);
		// Auto-select first item if available after grid is set up
		// selectFirstItemIfAvailable();
	}

	protected CButton createNewButton(final String buttonText) {
		LOGGER.info("Creating new button for {}", getClass().getSimpleName());
		final CButton newButton = CButton.createTertiary(buttonText, e -> {
			LOGGER.debug("New button clicked, creating new entity");
			// Clear current selection and create new entity
			grid.deselectAll();
			setCurrentEntity(null);
			clearForm();
			// Navigate to the base view URL to indicate "new" mode
			UI.getCurrent().navigate(getClass());
		});
		return newButton;
	}

	protected CButton createSaveButton(final String buttonText) {
		LOGGER.info("Creating save button for CUsersView");
		final CButton save = CButton.createPrimary(buttonText, e -> {

			try {

				if (currentEntity == null) {
					currentEntity = newEntity();
				}
				getBinder().writeBean(currentEntity);
				entityService.save(currentEntity);
				clearForm();
				refreshGrid();
				Notification.show("Data updated");
				// Navigate back to the current view (list mode)
				UI.getCurrent().navigate(getClass());
			} catch (final ObjectOptimisticLockingFailureException exception) {
				final Notification n = Notification.show(
					"Error updating the data. Somebody else has updated the record while you were making changes.");
				n.setPosition(Position.MIDDLE);
				n.addThemeVariants(NotificationVariant.LUMO_ERROR);
			} catch (final ValidationException validationException) {
				new CWarningDialog(
					"Failed to save the data. Please check that all required fields are filled and values are valid.")
					.open();
			} catch (final Exception exception) {
				LOGGER.error("Unexpected error during save operation", exception);
				new CWarningDialog(
					"An unexpected error occurred while saving. Please try again.")
					.open();
			}
		});
		return save;
	}

	public HasOrderedComponents getBaseDetailsLayout() { return baseDetailsLayout; }

	public BeanValidationBinder<EntityClass> getBinder() { return binder; }

	public EntityClass getCurrentEntity() { return currentEntity; }

	public Div getDetailsTabLayout() { return detailsTabLayout; }

	protected abstract String getEntityRouteIdField();
	protected abstract String getEntityRouteTemplateEdit();

	// if not present returns -1
	public Long getLastSelectedEntityId() {
		final String LAST_SELECTED_ID_KEY =
			"lastSelectedEntityId_" + entityClass.getSimpleName();

		if (VaadinSession.getCurrent() == null) {
			return -1L; // Return -1 if session is not available
		}

		if (VaadinSession.getCurrent().getAttribute(LAST_SELECTED_ID_KEY) == null) {
			return -1L; // Return -1 if attribute is not set
		}
		// Cast to Long since we expect the ID to be a Long
		final Long lastId =
			(Long) VaadinSession.getCurrent().getAttribute(LAST_SELECTED_ID_KEY);
		return lastId;
	}

	private void initBaseDetailsLayout() {
		baseDetailsLayout.setClassName("base-details-layout");
		baseDetailsLayout.setSizeFull();
		baseDetailsLayout.setAlignItems(FlexLayout.Alignment.STRETCH);
		/* FOR FLEX LAYOUT */
		// baseDetailsLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		// baseDetailsLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
		baseDetailsLayout.setJustifyContentMode(FlexLayout.JustifyContentMode.START);
	}

	private void initSplitLayout(final VerticalLayout detailsBase) {
		splitLayout.setSizeFull();
		splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
		splitLayout.addToSecondary(detailsBase);
		add(splitLayout);
	}

	protected abstract EntityClass newEntity();

	// this method is called when the page is attached to the UI
	@Override
	protected void onAttach(final AttachEvent attachEvent) {
		LOGGER.debug("onAttach called for {}", getClass().getSimpleName());
		super.onAttach(attachEvent);

		// Register for layout change notifications if service is available
		if (layoutService != null) {
			layoutService.addLayoutChangeListener(this);
			// Update layout based on current mode
			updateLayoutOrientation();
		}
		// this is called when the page is attached to the UI if you dont call it, the
		// first item will not be selected
		selectFirstItemIfAvailable();
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
		LOGGER.debug("Layout mode changed to: {} for {}", newMode,
			getClass().getSimpleName());
		updateLayoutOrientation();
	}

	protected void populateAccordionPanels(final EntityClass entity) {
		// This method can be overridden by subclasses to populate accordion panels
		AccordionList.forEach(accordion -> {
			accordion.populateForm(entity);
		});
	}

	protected  void populateForm(final EntityClass value) {
		currentEntity = value;
		binder.readBean(currentEntity);
		populateAccordionPanels(value);
	}

	protected void refreshGrid() {
		LOGGER.info("Refreshing grid for {}", getClass().getSimpleName());
		grid.select(null);
		grid.getDataProvider().refreshAll();
	}

	/**
	 * Automatically selects the first item in the grid if available. This ensures that
	 * details panels are populated when there is at least one item. Following the
	 * requirement to always show details when data is available. Only applies when no
	 * specific entity is already selected (i.e., in list mode).
	 */
	protected void selectFirstItemIfAvailable() {

		if ((grid == null) || (currentEntity != null)) {
			return;
		}

		try {
			// Use UI.access to ensure this runs in the correct UI thread
			getUI().ifPresent(ui -> ui.access(() -> {

				try {
					// Try to get the first item using a more direct approach Get the
					// first page of results from the entity service
					final var firstPageResults = entityService
						.list(org.springframework.data.domain.PageRequest.of(0, 1));

					if ((firstPageResults != null) && !firstPageResults.isEmpty()) {
						grid.select(firstPageResults.get(0));
					}
				} catch (final Exception e) {
					LOGGER.error("Error querying first item for {}: {}",
						getClass().getSimpleName(), e.getMessage(), e);
				}
			}));
		} catch (final Exception e) {
			LOGGER.error("Error selecting first item for {}: {}",
				getClass().getSimpleName(), e.getMessage(), e);
		}
	}

	public void setCurrentEntity(final EntityClass currentEntity) {
		this.currentEntity = currentEntity;
	}

	public void setLastSelectedEntityId(final Long lastSelectedEntityId) {

		// this method conflicts with the currentEntity setter
		if (lastSelectedEntityId == null) {
			return;
		}
		final String LAST_SELECTED_ID_KEY =
			"lastSelectedEntityId_" + entityClass.getSimpleName();
		VaadinSession.getCurrent().setAttribute(LAST_SELECTED_ID_KEY,
			lastSelectedEntityId);
	}

	/**
	 * Sets the layout service. This is typically called via dependency injection or
	 * manually after construction.
	 */
	public void setLayoutService(final LayoutService layoutService) {
		this.layoutService = layoutService;
		// Update layout based on current mode
		updateLayoutOrientation();
	}

	/**
	 * Sets up the toolbar for the page.
	 */
	@Override
	protected abstract void setupToolbar();

	/**
	 * Updates the split layout orientation based on the current layout mode.
	 */
	private void updateLayoutOrientation() {

		if ((layoutService != null) && (splitLayout != null)) {
			final LayoutService.LayoutMode currentMode =
				layoutService.getCurrentLayoutMode();
			LOGGER.debug("Updating layout orientation to: {} for {}", currentMode,
				getClass().getSimpleName());

			if (currentMode == LayoutService.LayoutMode.HORIZONTAL) {
				splitLayout.setOrientation(SplitLayout.Orientation.HORIZONTAL);
				// For horizontal layout, give more space to the grid (left side)
				splitLayout.setSplitterPosition(50.0); // 50% for grid, 50% for details
			}
			else {
				splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
				// For vertical layout, give more space to the grid (top)
				splitLayout.setSplitterPosition(30.0); // 30% for grid, 70% for details
			}
			// Force UI refresh to apply changes immediately
			getUI().ifPresent(ui -> ui.access(() -> {
				splitLayout.getElement().callJsFunction("$server.requestUpdate");
			}));
			LOGGER.debug("Updated split layout orientation to: {} with position: {}",
				splitLayout.getOrientation(), splitLayout.getSplitterPosition());
		}
		else {
			// Default fallback when no layout service is available
			splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
			splitLayout.setSplitterPosition(30.0);
			LOGGER.debug("Applied default vertical layout (no layout service available)");
		}
	}
}
