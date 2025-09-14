package tech.derbent.abstracts.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.abstracts.views.components.CButton;
import tech.derbent.screens.domain.CGridEntity;
import tech.derbent.screens.view.CComponentGridEntity;
import tech.derbent.screens.view.CFieldSelectionDialog;

public class CComponentDetailsMasterToolbar extends HorizontalLayout {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentDetailsMasterToolbar.class);
	private static final long serialVersionUID = 1L;
	private CButton btnEditGrid;
	private TextField searchField;
	private CComponentGridEntity grid;

	public CComponentDetailsMasterToolbar(CComponentGridEntity grid) {
		this.grid = grid;
		setSpacing(true);
		setPadding(true);
		addClassName("crud-toolbar");
		setWidthFull(); // Make toolbar take full width
		createToolbarButtons();
	}

	/** Creates all the CRUD toolbar buttons. */
	private void createToolbarButtons() {
		// Search field for grid filtering
		searchField = new TextField();
		searchField.setPlaceholder("Search...");
		searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
		searchField.setClearButtonVisible(true);
		searchField.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.LAZY);
		searchField.addValueChangeListener(e -> handleSearch(e.getValue()));
		// Edit Grid Columns Button
		btnEditGrid = CButton.createPrimary("Edit Columns", VaadinIcon.GRID_V.create(), e -> handleEditGridEntity());
		btnEditGrid.getElement().setAttribute("title", "Edit Grid Columns");
		add(searchField, btnEditGrid);
		updateButtonStates();
	}

	private void updateButtonStates() {
		// Enable edit button only if grid is available
		btnEditGrid.setEnabled(grid != null);
	}

	/** Handles search field value changes */
	private void handleSearch(String searchValue) {
		Check.notNull(searchValue, "Search value is null");
		Check.notNull(grid, "Grid component is not set");
		// Apply search filter to grid
		grid.setSearchFilter(searchValue);
	}

	private void handleEditGridEntity() {
		try {
			Check.notNull(grid, "Grid component is not set");
			// Get current grid entity information
			CGridEntity gridEntity = getCurrentGridEntity();
			Check.notNull(gridEntity, "No grid entity found for the current grid");
			String entityType = extractEntityTypeFromService(gridEntity.getDataServiceBeanName());
			String currentSelections = gridEntity.getSelectedFields();
			// Open field selection dialog
			CFieldSelectionDialog dialog = new CFieldSelectionDialog(entityType, currentSelections, selectedFields -> {
				// Update grid entity with new field selection
				String newSelectionString = selectedFields.stream().map(fs -> fs.getFieldInfo().getFieldName() + ":" + fs.getOrder())
						.reduce((a, b) -> a + "," + b).orElse("");
				gridEntity.setSelectedFields(newSelectionString);
				// Refresh grid
				grid.refreshGrid();
				Notification.show("Grid columns updated successfully");
			});
			dialog.open();
		} catch (Exception e) {
			LOGGER.error("Error opening grid editor", e);
			Notification.show("Error opening grid editor: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
		}
	}

	/** Gets the current grid entity being displayed */
	private CGridEntity getCurrentGridEntity() {
		return grid != null ? grid.getGridEntity() : null;
	}

	/** Extracts entity type from service bean name */
	private String extractEntityTypeFromService(String serviceBeanName) {
		Check.notNull(serviceBeanName, "Service bean name is null");
		Check.isTrue(serviceBeanName.endsWith("Service"), "Service bean name does not end with 'Service'");
		// Convert activityService -> CActivity or CActivityService -> CActivity
		String baseName = serviceBeanName.substring(0, serviceBeanName.length() - "Service".length());
		// If it's already in the format "CActivity", return as is
		if (baseName.startsWith("C") && baseName.length() > 1 && Character.isUpperCase(baseName.charAt(1))) {
			return baseName;
		}
		// Convert from camelCase bean name to proper class name
		// activityService -> CActivity, meetingService -> CMeeting, etc.
		if (baseName.length() > 0) {
			return "C" + Character.toUpperCase(baseName.charAt(0)) + baseName.substring(1);
		}
		return null;
	}
}
