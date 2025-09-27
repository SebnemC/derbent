package tech.derbent.api.interfaces;

import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.domains.CEntityDB;

/** Interface for components that support data binding and initialization. This interface provides common patterns for components that need to: -
 * Initialize form fields and data - Bind to external binders - Populate data from entities - Extract data for validation and saving
 * @param <EntityClass> the entity type this component works with */
public interface IBindableComponent<EntityClass extends CEntityDB<EntityClass>> {

	/** Initializes the component with default configuration. This method should set up UI components, layouts, and default values.
	 * @throws Exception if initialization fails */
	void initializeComponent() throws Exception;
	/** Binds this component to an external binder for form integration.
	 * @param externalBinder the binder to use for data binding
	 * @throws Exception if binding setup fails */
	void bindToExternalBinder(CEnhancedBinder<EntityClass> externalBinder) throws Exception;
	/** Populates the component with data from an entity.
	 * @param entity the entity to populate from, can be null for new entities
	 * @throws Exception if data population fails */
	void populateData(EntityClass entity) throws Exception;
	/** Extracts the current data from the component. This method should validate the form and return the current entity state.
	 * @return the current entity with form data, or null if validation fails
	 * @throws Exception if data extraction or validation fails */
	EntityClass getCurrentData() throws Exception;
	/** Validates the current form data.
	 * @return true if validation passes, false otherwise */
	boolean validateData();
	/** Clears all form fields and resets the component to initial state. */
	void clearData();
	/** Gets the internal binder used by this component.
	 * @return the component's binder, may be null if not initialized */
	CEnhancedBinder<EntityClass> getBinder();
}
