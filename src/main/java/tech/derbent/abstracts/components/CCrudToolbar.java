package tech.derbent.abstracts.components;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.ValidationException;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.interfaces.CEntityUpdateListener;
import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.abstracts.views.components.CButton;
import tech.derbent.base.ui.dialogs.CConfirmationDialog;
import tech.derbent.base.ui.dialogs.CWarningDialog;

/** Generic CRUD toolbar component that provides comprehensive Create, Read, Update, Delete, and Refresh functionality for any entity type. Includes
 * proper binding integration, validation, error handling, and update notifications.
 * @param <EntityClass> the entity type this toolbar operates on */
public class CCrudToolbar<EntityClass extends CEntityDB<EntityClass>> extends HorizontalLayout {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CCrudToolbar.class);
	private final CEnhancedBinder<EntityClass> binder;
	private final CAbstractService<EntityClass> entityService;
	private final Class<EntityClass> entityClass;
	private final List<CEntityUpdateListener> updateListeners = new ArrayList<>();
	private EntityClass currentEntity;
	private Supplier<EntityClass> newEntitySupplier;
	private Consumer<EntityClass> refreshCallback;
	private CButton createButton;
	private CButton saveButton;
	private CButton deleteButton;
	private CButton refreshButton;

	/** Creates a comprehensive CRUD toolbar.
	 * @param binder        the binder for form validation and data binding
	 * @param entityService the service for CRUD operations
	 * @param entityClass   the entity class type */
	public CCrudToolbar(final CEnhancedBinder<EntityClass> binder, final CAbstractService<EntityClass> entityService,
			final Class<EntityClass> entityClass) {
		this.binder = binder;
		this.entityService = entityService;
		this.entityClass = entityClass;
		setSpacing(true);
		setPadding(true);
		addClassName("crud-toolbar");
		createToolbarButtons();
		LOGGER.debug("Created CCrudToolbar for entity type: {}", entityClass.getSimpleName());
	}

	/** Adds an update listener to be notified of CRUD operations.
	 * @param listener the listener to add */
	public void addUpdateListener(final CEntityUpdateListener listener) {
		if (listener != null && !updateListeners.contains(listener)) {
			updateListeners.add(listener);
		}
	}

	/** Sets the supplier for creating new entity instances.
	 * @param newEntitySupplier supplier that creates new entity instances */
	public void setNewEntitySupplier(final Supplier<EntityClass> newEntitySupplier) {
		this.newEntitySupplier = newEntitySupplier;
		updateButtonStates();
	}

	/** Sets the callback for refresh operations.
	 * @param refreshCallback callback to execute when refresh is triggered */
	public void setRefreshCallback(final Consumer<EntityClass> refreshCallback) {
		this.refreshCallback = refreshCallback;
		updateButtonStates();
	}

	/** Updates the current entity and refreshes button states.
	 * @param entity the current entity */
	public void setCurrentEntity(final EntityClass entity) {
		this.currentEntity = entity;
		updateButtonStates();
		// Bind the entity to the form if available
		if (entity != null && binder != null) {
			try {
				binder.setBean(entity);
				LOGGER.debug("Entity bound to form: {} ID: {}", entityClass.getSimpleName(), entity.getId());
			} catch (Exception e) {
				LOGGER.warn("Error binding entity to form: {}", e.getMessage());
			}
		}
	}

	/** Gets the current entity.
	 * @return the current entity */
	public EntityClass getCurrentEntity() { return currentEntity; }

	/** Removes an update listener.
	 * @param listener the listener to remove */
	public void removeUpdateListener(final CEntityUpdateListener listener) {
		updateListeners.remove(listener);
	}

	/** Creates all the CRUD toolbar buttons. */
	private void createToolbarButtons() {
		// Create (New) Button
		createButton = CButton.createPrimary("New", VaadinIcon.PLUS.create(), e -> handleCreate());
		createButton.getElement().setAttribute("title", "Create new " + entityClass.getSimpleName());
		// Save (Update) Button
		saveButton = CButton.createPrimary("Save", VaadinIcon.CHECK.create(), e -> handleSave());
		saveButton.getElement().setAttribute("title", "Save current " + entityClass.getSimpleName());
		// Delete Button
		deleteButton = CButton.createError("Delete", VaadinIcon.TRASH.create(), e -> handleDelete());
		deleteButton.getElement().setAttribute("title", "Delete current " + entityClass.getSimpleName());
		// Refresh Button
		refreshButton = CButton.createTertiary("Refresh", VaadinIcon.REFRESH.create(), e -> handleRefresh());
		refreshButton.getElement().setAttribute("title", "Refresh data");
		add(createButton, saveButton, deleteButton, refreshButton);
		updateButtonStates();
	}

	/** Updates button enabled/disabled states based on current context. */
	private void updateButtonStates() {
		boolean hasEntity = (currentEntity != null);
		boolean hasEntityId = hasEntity && (currentEntity.getId() != null);
		boolean canCreate = (newEntitySupplier != null);
		boolean canRefresh = (refreshCallback != null);
		if (createButton != null) {
			createButton.setEnabled(canCreate);
		}
		if (saveButton != null) {
			saveButton.setEnabled(hasEntity);
		}
		if (deleteButton != null) {
			deleteButton.setEnabled(hasEntityId);
		}
		if (refreshButton != null) {
			refreshButton.setEnabled(canRefresh);
		}
	}

	/** Handles the create (new entity) operation. */
	private void handleCreate() {
		if (newEntitySupplier == null) {
			showErrorNotification("Cannot create new entity: No entity supplier configured.");
			return;
		}
		try {
			LOGGER.debug("Create button clicked for entity type: {}", entityClass.getSimpleName());
			// Create new entity
			EntityClass newEntity = newEntitySupplier.get();
			if (newEntity == null) {
				showErrorNotification("Failed to create new entity instance.");
				return;
			}
			// Set as current entity and bind to form
			setCurrentEntity(newEntity);
			showSuccessNotification("New " + entityClass.getSimpleName() + " created. Fill in the details and click Save.");
			LOGGER.info("New entity created successfully: {}", entityClass.getSimpleName());
		} catch (Exception exception) {
			LOGGER.error("Error during create operation for entity: {}", entityClass.getSimpleName(), exception);
			showErrorNotification("An error occurred while creating new entity. Please try again.");
		}
	}

	/** Handles the save (update) operation with proper validation, error handling, and notifications. */
	private void handleSave() {
		if (currentEntity == null) {
			showErrorNotification("Cannot save: No entity selected.");
			return;
		}
		try {
			LOGGER.debug("Save button clicked for entity: {} ID: {}", entityClass.getSimpleName(), currentEntity.getId());
			// Write form data to entity (this will validate)
			binder.writeBean(currentEntity);
			// Save entity
			final EntityClass savedEntity = entityService.save(currentEntity);
			LOGGER.info("Entity saved successfully: {} with ID: {}", entityClass.getSimpleName(), savedEntity.getId());
			// Update current entity reference
			currentEntity = savedEntity;
			updateButtonStates();
			showSuccessNotification("Data saved successfully");
			// Notify listeners
			notifyListenersSaved(savedEntity);
		} catch (final ObjectOptimisticLockingFailureException exception) {
			LOGGER.error("Optimistic locking failure during save", exception);
			showErrorNotification("Error updating the data. Somebody else has updated the record while you were making changes.");
		} catch (final ValidationException validationException) {
			LOGGER.error("Validation error during save", validationException);
			handleValidationError(validationException);
		} catch (final Exception exception) {
			LOGGER.error("Unexpected error during save operation for entity: {}", entityClass.getSimpleName(), exception);
			showErrorNotification("An unexpected error occurred while saving. Please try again.");
		}
	}

	/** Handles the delete operation with confirmation dialog and proper error handling. */
	private void handleDelete() {
		if (currentEntity == null || currentEntity.getId() == null) {
			showErrorNotification("Cannot delete: No entity selected or entity not saved yet.");
			return;
		}
		// Show confirmation dialog
		CConfirmationDialog confirmDialog =
				new CConfirmationDialog("Are you sure you want to delete this " + entityClass.getSimpleName() + "?", this::performDelete);
		confirmDialog.open();
	}

	/** Performs the actual delete operation after confirmation. */
	private void performDelete() {
		try {
			LOGGER.debug("Performing delete for entity: {} with ID: {}", entityClass.getSimpleName(), currentEntity.getId());
			EntityClass entityToDelete = currentEntity;
			entityService.delete(currentEntity);
			LOGGER.info("Entity deleted successfully: {} with ID: {}", entityClass.getSimpleName(), entityToDelete.getId());
			// Clear current entity
			currentEntity = null;
			if (binder != null) {
				binder.setBean(null);
			}
			updateButtonStates();
			showSuccessNotification("Entity deleted successfully");
			// Notify listeners
			notifyListenersDeleted(entityToDelete);
		} catch (final Exception exception) {
			LOGGER.error("Error during delete operation for entity: {}", entityClass.getSimpleName(), exception);
			showErrorNotification("An error occurred while deleting. Please try again.");
		}
	}

	/** Handles the refresh operation. */
	private void handleRefresh() {
		if (refreshCallback == null) {
			showErrorNotification("Cannot refresh: No refresh callback configured.");
			return;
		}
		try {
			LOGGER.debug("Refresh button clicked for entity type: {}", entityClass.getSimpleName());
			refreshCallback.accept(currentEntity);
			showSuccessNotification("Data refreshed successfully");
			LOGGER.info("Refresh operation completed for entity type: {}", entityClass.getSimpleName());
		} catch (Exception exception) {
			LOGGER.error("Error during refresh operation for entity: {}", entityClass.getSimpleName(), exception);
			showErrorNotification("An error occurred while refreshing. Please try again.");
		}
	}

	/** Handles validation errors with enhanced error reporting. */
	private void handleValidationError(final ValidationException validationException) {
		if (binder.hasValidationErrors()) {
			LOGGER.error("Detailed validation errors:");
			LOGGER.error(binder.getFormattedErrorSummary());
			// Show detailed error information
			final StringBuilder errorMessage = new StringBuilder("Failed to save the data. Please check:\n");
			binder.getFieldsWithErrors()
					.forEach(field -> errorMessage.append("• ").append(field).append(": ").append(binder.getFieldError(field)).append("\n"));
			new CWarningDialog(errorMessage.toString()).open();
		} else {
			new CWarningDialog("Failed to save the data. Please check that all required fields are filled and values are valid.").open();
		}
	}

	/** Notifies all listeners that an entity was saved. */
	private void notifyListenersSaved(final EntityClass entity) {
		updateListeners.forEach(listener -> {
			try {
				listener.onEntitySaved(entity);
			} catch (final Exception e) {
				LOGGER.warn("Error notifying listener of entity save", e);
			}
		});
	}

	/** Notifies all listeners that an entity was deleted. */
	private void notifyListenersDeleted(final EntityClass entity) {
		updateListeners.forEach(listener -> {
			try {
				listener.onEntityDeleted(entity);
			} catch (final Exception e) {
				LOGGER.warn("Error notifying listener of entity deletion", e);
			}
		});
	}

	/** Shows an error notification. */
	private void showErrorNotification(final String message) {
		try {
			final Notification notification = Notification.show(message);
			notification.setPosition(Notification.Position.MIDDLE);
			notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
		} catch (final Exception e) {
			LOGGER.warn("Error showing error notification '{}': {}", message, e.getMessage());
		}
	}

	/** Shows a success notification. */
	private void showSuccessNotification(final String message) {
		try {
			final Notification notification = Notification.show(message);
			notification.setPosition(Notification.Position.BOTTOM_START);
		} catch (final Exception e) {
			LOGGER.warn("Error showing notification '{}': {}", message, e.getMessage());
		}
	}
}
