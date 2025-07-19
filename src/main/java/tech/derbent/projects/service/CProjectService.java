package tech.derbent.projects.service;

import java.time.Clock;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.projects.domain.CProject;

@Service
@PreAuthorize("isAuthenticated()")
public class CProjectService extends CAbstractService<CProject> {

	CProjectService(final CProjectRepository repository, final Clock clock) {
		super(repository, clock);
		LOGGER.info("CProjectService constructor called");
	}

	@Transactional
	public void createEntity(final String name) {
		LOGGER.info("Creating project with name: {}", name);
		if ("fail".equals(name)) {
			throw new RuntimeException("This is for testing the error handler");
		}
		final var entity = new CProject();
		entity.setName(name);
		repository.saveAndFlush(entity);
	}

	@Transactional
	public List<CProject> findAll() {
		LOGGER.info("Fetching all projects");
		return repository.findAll();
	}
}
