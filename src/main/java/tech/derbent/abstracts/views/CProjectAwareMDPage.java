package tech.derbent.abstracts.views;

import java.util.List;

import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.SessionService;

/**
 * Abstract project-aware MD page that filters entities by the currently active
 * project. The base class CAbstractMDPage now handles all the project change
 * listener functionality, so this class just provides convenience methods
 * for project-aware implementations.
 */
public abstract class CProjectAwareMDPage<EntityClass extends CEntityDB> extends CAbstractMDPage<EntityClass> {

	private static final long serialVersionUID = 1L;

	protected CProjectAwareMDPage(final Class<EntityClass> entityClass, final CAbstractService<EntityClass> entityService, final SessionService sessionService) {
		// Call the project-aware constructor of the base class
		super(entityClass, entityService, sessionService);
	}

	/**
	 * Abstract method that subclasses must implement to create new entity instances.
	 * This is called by the base class when creating new entities.
	 */
	protected abstract EntityClass createNewEntityInstance();

	/**
	 * Abstract method that subclasses must implement to provide filtered data.
	 * This is called by the base class when refreshing the grid.
	 */
	protected abstract List<EntityClass> getProjectFilteredData(CProject project, org.springframework.data.domain.Pageable pageable);

	/**
	 * Abstract method that subclasses must implement to set the project for entities.
	 * This is called by the base class when creating new entities.
	 */
	protected abstract void setProjectForEntity(EntityClass entity, CProject project);

	/**
	 * Implementation of the newEntity method required by the base class.
	 * Delegates to the abstract createNewEntityInstance method.
	 */
	@Override
	protected final EntityClass newEntity() {
		return createNewEntityInstance();
	}
}