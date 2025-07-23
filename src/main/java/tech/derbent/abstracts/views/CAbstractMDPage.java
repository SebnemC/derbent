package tech.derbent.abstracts.views;

import java.util.Optional;

import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;

import jakarta.annotation.PostConstruct;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.base.ui.dialogs.CConfirmationDialog;
import tech.derbent.base.ui.dialogs.CWarningDialog;
import tech.derbent.layout.service.LayoutStateService;

public abstract class CAbstractMDPage<EntityClass extends CEntityDB> extends CAbstractPage {

	private static final long serialVersionUID = 1L;
	protected final Class<EntityClass> entityClass;
	protected Grid<EntityClass> grid;// = new Grid<>(CProject.class, false);
	private final BeanValidationBinder<EntityClass> binder;
	protected SplitLayout splitLayout = new SplitLayout();
	private final Accordion baseDescriptionAccordion = new Accordion();
	private final VerticalLayout baseDetailsLayout = new VerticalLayout();
	private final Div detailsTabLayout = new Div();
	protected EntityClass currentEntity;
	protected final CAbstractService<EntityClass> entityService;
	protected final LayoutStateService layoutStateService;

	protected CAbstractMDPage(final Class<EntityClass> entityClass, final CAbstractService<EntityClass> entityService) {
		this(entityClass, entityService, null);
	}

	protected CAbstractMDPage(final Class<EntityClass> entityClass, final CAbstractService<EntityClass> entityService, 
			final LayoutStateService layoutStateService) {
		super();
		this.entityClass = entityClass;
		this.entityService = entityService;
		this.layoutStateService = layoutStateService;
		binder = new BeanValidationBinder<>(entityClass);
		addClassNames("md-page");
		setSizeFull();
		// create a split layout for the main content with dynamic orientation
		splitLayout.setSizeFull();
		// Set orientation based on layout state service
		updateLayoutOrientation();
		// Create UI
		createGridLayout();
		detailsTabLayout.setClassName("details-tab-layout");
		detailsTabLayout.setMaxHeight("100px");
		detailsTabLayout.setMinHeight("100px");
		// put a line below
		detailsTabLayout.getStyle().set("border-bottom", "1px solid var(--lumo-contrast-20pct)");
		final VerticalLayout detailsBase = new VerticalLayout();
		splitLayout.addToSecondary(detailsBase);
		detailsBase.add(detailsTabLayout);
		final Scroller scroller = new Scroller();
		detailsBase.add(scroller);
		scroller.setContent(baseDetailsLayout);
		scroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
		// baseDetailsLayout.add(baseDescriptionAccordion);
		createGridForEntity();
		// binder = new BeanValidationBinder<>(entityClass
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

	/**
	 * Creates a button layout for additional buttons if needed by subclasses. Note:
	 * Default save/delete/cancel buttons are now in the details tab. This method
	 * can be used for additional custom buttons in the main content area.
	 * @param layout The layout to add buttons to
	 */
	protected void createButtonLayout(final Div layout) {
		LOGGER.debug("createButtonLayout called - default save/delete/cancel buttons are now in details tab");
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
			final String confirmMessage = String.format("Are you sure you want to delete this %s? This action cannot be undone.", entityClass.getSimpleName().replace("C", "").toLowerCase());
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
	 * Creates the button layout for the details tab. Contains save, cancel, and
	 * delete buttons with consistent styling.
	 * @return HorizontalLayout with action buttons
	 */
	protected HorizontalLayout createDetailsTabButtonLayout() {
		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setClassName("details-tab-button-layout");
		buttonLayout.setSpacing(true);
		buttonLayout.add(createSaveButton("Save"), createCancelButton("Cancel"), createDeleteButton("Delete"));
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
	 * Subclasses can override this method to customize the tab content while
	 * maintaining consistent button placement and styling.
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
		grid = new Grid<>(entityClass, false);
		grid.getColumns().forEach(grid::removeColumn);
		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
		grid.setItems(query -> entityService.list(VaadinSpringDataHelpers.toSpringPageRequest(query)).stream());
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
				final Notification n = Notification.show("Error updating the data. Somebody else has updated the record while you were making changes.");
				n.setPosition(Position.MIDDLE);
				n.addThemeVariants(NotificationVariant.LUMO_ERROR);
			} catch (final ValidationException validationException) {
				new CWarningDialog("Failed to save the data. Please check that all required fields are filled and values are valid.").open();
			} catch (final Exception exception) {
				LOGGER.error("Unexpected error during save operation", exception);
				new CWarningDialog("An unexpected error occurred while saving. Please try again.").open();
			}
		});
		return save;
	}

	public VerticalLayout getBaseDetailsLayout() { return baseDetailsLayout; }

	public BeanValidationBinder<EntityClass> getBinder() { return binder; }

	public EntityClass getCurrentEntity() { return currentEntity; }

	public Div getDetailsTabLayout() { return detailsTabLayout; }

	protected abstract String getEntityRouteIdField();

	protected abstract String getEntityRouteTemplateEdit();

	protected abstract EntityClass newEntity();

	protected void populateForm(final EntityClass value) {
		currentEntity = value;
		binder.readBean(currentEntity);
	}

	protected void refreshGrid() {
		grid.select(null);
		grid.getDataProvider().refreshAll();
	}

	public void setCurrentEntity(final EntityClass currentEntity) { this.currentEntity = currentEntity; }

	/**
	 * Updates the split layout orientation based on the current layout state.
	 */
	private void updateLayoutOrientation() {
		if (layoutStateService != null && layoutStateService.isHorizontalMode()) {
			splitLayout.setOrientation(SplitLayout.Orientation.HORIZONTAL);
		} else {
			splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
		}
	}

	/**
	 * Sets up the toolbar for the page.
	 */
	@Override
	protected abstract void setupToolbar();
}
