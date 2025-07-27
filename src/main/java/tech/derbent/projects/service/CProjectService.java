package tech.derbent.projects.service;

import java.time.Clock;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CAbstractNamedEntityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.events.ProjectListChangeEvent;

@Service
@PreAuthorize("isAuthenticated()")
public class CProjectService extends CAbstractNamedEntityService<CProject> {

    private final ApplicationEventPublisher eventPublisher;

    CProjectService(final CProjectRepository repository, final Clock clock, final ApplicationEventPublisher eventPublisher) {
        super(repository, clock);
        this.eventPublisher = eventPublisher;
        LOGGER.info("CProjectService constructor called");
    }

    @Override
    @Transactional
    public void createEntity(final String name) {
        LOGGER.info("Creating project with name: {}", name);
        
        // Use parent validation and creation logic
        super.createEntity(name);
        
        // Find the created entity to publish event
        final var entity = findByName(name).orElseThrow(() -> 
            new RuntimeException("Created project not found: " + name));
            
        // Publish project list change event
        eventPublisher.publishEvent(new ProjectListChangeEvent(this, entity, ProjectListChangeEvent.ChangeType.CREATED));
    }

    @Override
    protected CProject createNewEntityInstance() {
        return new CProject();
    }

    @Override
    @Transactional
    public void delete(final CProject entity) {
        super.delete(entity);
        // Publish project list change event after deletion
        eventPublisher.publishEvent(new ProjectListChangeEvent(this, entity, ProjectListChangeEvent.ChangeType.DELETED));
    }

    @PreAuthorize("permitAll()")
    public List<CProject> findAll() {
        LOGGER.info("Fetching all projects");
        return repository.findAll();
    }

    @PreAuthorize("permitAll()")
    public long getTotalProjectCount() {
        LOGGER.info("Counting total number of projects");
        return repository.count();
    }

    @Override
    @Transactional
    public CProject save(final CProject entity) {
        final boolean isNew = entity.getId() == null;
        final CProject savedEntity = super.save(entity);
        // Publish project list change event after saving
        final ProjectListChangeEvent.ChangeType changeType = isNew ? 
            ProjectListChangeEvent.ChangeType.CREATED : 
            ProjectListChangeEvent.ChangeType.UPDATED;
        eventPublisher.publishEvent(new ProjectListChangeEvent(this, savedEntity, changeType));
        return savedEntity;
    }
}
