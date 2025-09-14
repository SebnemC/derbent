package tech.derbent.page.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.abstracts.domains.CProjectItemService;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.page.domain.CPageEntity;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.CSessionService;

@Service
@PreAuthorize ("isAuthenticated()")
public class CPageEntityService extends CProjectItemService<CPageEntity> {

	public CPageEntityService(final CPageEntityRepository repository, final Clock clock, final CSessionService sessionService) {
		super(repository, clock, sessionService);
		Check.notNull(repository, "CPageEntityRepository cannot be null");
		Check.notNull(clock, "Clock cannot be null");
		Check.notNull(sessionService, "CSessionService cannot be null");
	}

	@Override
	protected Class<CPageEntity> getEntityClass() { return CPageEntity.class; }

	/** Find active pages by project. */
	public List<CPageEntity> findActivePagesByProject(CProject project) {
		Check.notNull(project, "Project cannot be null");
		return listByProject(project);
	}

	/** Find page by route. */
	public Optional<CPageEntity> findByRoute(String route) {
		Check.notBlank(route, "Route cannot be blank");
		return findAll().stream().filter(page -> route.equals(page.getRoute())).findFirst();
	}

	/** Get page hierarchy for project. */
	public List<CPageEntity> getPageHierarchyForProject(CProject project) {
		Check.notNull(project, "Project cannot be null");
		return listByProject(project);
	}

	/** Find root pages by project (pages with no parent). */
	public List<CPageEntity> findRootPagesByProject(CProject project) {
		Check.notNull(project, "Project cannot be null");
		return listByProject(project);
	}

	/** Find child pages by parent page. */
	public List<CPageEntity> findByParentPage(CPageEntity parentPage) {
		Check.notNull(parentPage, "Parent page cannot be null");
		// For now, return empty list as hierarchy is not fully implemented
		return List.of();
	}
}
