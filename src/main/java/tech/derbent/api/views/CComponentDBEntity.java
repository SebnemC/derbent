package tech.derbent.api.views;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.annotations.CFormBuilder;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.components.CVerticalLayout;

public abstract class CComponentDBEntity<EntityClass extends CEntityDB<EntityClass>> extends CVerticalLayout implements IContentOwner {

	private static final long serialVersionUID = 1L;
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	protected final Class<EntityClass> entityClass;
	private final CEnhancedBinder<EntityClass> binder;
	protected CAbstractService<EntityClass> entityService;
	private List<String> EntityFields = null;
	private boolean isPanelInitialized = false;
	protected IContentOwner parentContent;

	public CComponentDBEntity(final String title, IContentOwner parentContent, final CEnhancedBinder<EntityClass> beanValidationBinder,
			final Class<EntityClass> entityClass, final CAbstractService<EntityClass> entityService) {
		super(false, true, false); // no padding, with spacing, no margin
		Check.notNull(entityClass, "Entity class cannot be null");
		Check.notNull(beanValidationBinder, "Binder cannot be null");
		this.entityClass = entityClass;
		this.binder = beanValidationBinder;
		this.entityService = entityService;
		this.parentContent = parentContent;
		addClassName("c-component-db-entity");
		setWidthFull();
	}

	public void clearForm() {
		try {
			Check.notNull(binder, "Binder cannot be null when clearing form");
			binder.readBean(null);
		} catch (Exception e) {
			LOGGER.error("Failed to clear form: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to clear form", e);
		}
	}

	// Override if you need to customize the panel content creation
	protected void createPanelContent() throws Exception {
		try {
			add(CFormBuilder.buildForm(entityClass, getBinder(), getEntityFields()));
		} catch (Exception e) {
			LOGGER.error("Failed to create panel content for entity {}: {}", entityClass.getSimpleName(), e.getMessage(), e);
			throw new RuntimeException("Failed to create panel content", e);
		}
	}

	public CEnhancedBinder<EntityClass> getBinder() { return binder; }

	@Override
	public EntityClass getCurrentEntity() { return parentContent != null ? (EntityClass) parentContent.getCurrentEntity() : null; }

	public Object getLocalContextValue(final String contextName) {
		return null;
	}

	public List<String> getEntityFields() {
		if (EntityFields == null) {
			updatePanelEntityFields();
		}
		return EntityFields;
	}

	protected String getHelpText() { return "Configure settings for this entity."; }

	protected void setEntityFields(final List<String> fields) {
		Check.notNull(fields, "Entity fields list cannot be null");
		EntityFields = fields;
	}

	protected abstract void updatePanelEntityFields();

	public void initPanel() throws Exception {
		if (!isPanelInitialized) {
			try {
				LOGGER.debug("Initializing component panel for entity class: {}", entityClass.getSimpleName());
				createPanelContent();
				isPanelInitialized = true;
			} catch (Exception e) {
				LOGGER.error("Failed to initialize panel for entity {}: {}", entityClass.getSimpleName(), e.getMessage(), e);
				throw new RuntimeException("Failed to initialize panel", e);
			}
		}
	}

	public boolean isPanelVisible() { return true; }

	public void openPanel() {
		setVisible(true);
	}

	public void closePanel() {
		setVisible(false);
	}

	protected void onPanelOpened() {
		LOGGER.debug("Component panel opened for entity class: {}", entityClass.getSimpleName());
	}

	protected String getPanelTitle() { return entityClass.getSimpleName() + " Settings"; }

	protected void saveFormData() throws Exception {
		try {
			EntityClass entity = getCurrentEntity();
			Check.notNull(entity, "Current entity cannot be null when saving form data");
			Check.notNull(binder, "Binder cannot be null when saving form data");
			binder.writeBean(entity);
			entityService.save(entity);
			LOGGER.debug("Form data saved for entity: {}", entity);
		} catch (Exception e) {
			LOGGER.error("Failed to save form data for entity {}: {}", entityClass.getSimpleName(), e.getMessage(), e);
			throw new RuntimeException("Failed to save form data", e);
		}
	}

	public boolean validateForm() {
		try {
			return binder != null && binder.validate().isOk();
		} catch (Exception e) {
			LOGGER.error("Failed to validate form for entity {}: {}", entityClass.getSimpleName(), e.getMessage(), e);
			return false;
		}
	}
}
