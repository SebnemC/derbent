package tech.derbent.abstracts.views;

import java.util.List;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.annotations.CEntityFormBuilder.ComboBoxDataProvider;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.services.CAbstractService;

public abstract class CAccordionDBEntity<EntityClass extends CEntityDB>
	extends CAccordion {

	private static final long serialVersionUID = 1L;

	protected final Class<EntityClass> entityClass;

	private final BeanValidationBinder<EntityClass> binder;

	private EntityClass currentEntity;

	protected CAbstractService<EntityClass> entityService;

	private List<String> entityFields;

	private final ComboBoxDataProvider detailsDataProvider;

	/**
	 * Constructor for CAccordionDescription with custom title.
	 * @param title                custom title for the accordion panel
	 * @param currentEntity        current entity instance
	 * @param beanValidationBinder validation binder
	 * @param entityClass          entity class type
	 * @param entityService        service for the entity
	 */
	public CAccordionDBEntity(final String title, final EntityClass currentEntity,
		final BeanValidationBinder<EntityClass> beanValidationBinder,
		final Class<EntityClass> entityClass,
		final CAbstractService<EntityClass> entityService) {
		super(title);
		this.entityClass = entityClass;
		this.binder = beanValidationBinder;
		this.entityService = entityService;
		this.currentEntity = currentEntity;
		this.detailsDataProvider = createComboBoxDataProvider();
	}

	public void clearForm() {
		binder.readBean(null);
	}

	protected ComboBoxDataProvider createComboBoxDataProvider() {
		return null;
	}

	// Override if you need to customize the panel content creation
	protected void createPanelContent() {
		updatePanelEntityFields(); // Set the entity fields first
		getBaseLayout().add(
			CEntityFormBuilder.buildForm(entityClass, getBinder(), getEntityFields()));
	}

	public BeanValidationBinder<EntityClass> getBinder() { return binder; }

	public EntityClass getCurrentEntity() { return currentEntity; }

	public ComboBoxDataProvider getDetailsDataProvider() { return detailsDataProvider; }

	public List<String> getEntityFields() { return entityFields; }

	public void populateForm(final EntityClass entity) {
		LOGGER.debug("Populating form with entity: {}", entity);
		currentEntity = entity;

		if (entity == null) {
			clearForm();
		}
		else {
			// LOGGER.debug("Populating form with entity: {}", entity); Populate the form
			// fields with the entity data
			binder.readBean(entity);
		}
	}

	// used if there is a specific save logic for the entity
	public void saveEventHandler() {}

	protected void setEntityFields(final List<String> fields) { entityFields = fields; }

	protected void updatePanelEntityFields() {
		// TODO Auto-generated method stub
	}
}
