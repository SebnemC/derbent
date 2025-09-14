package tech.derbent.meetings.service;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.abstracts.interfaces.CKanbanService;
import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.domain.CMeetingStatus;
import tech.derbent.session.service.CSessionService;

@Service
@PreAuthorize ("isAuthenticated()")
public class CMeetingService extends CEntityOfProjectService<CMeeting> implements CKanbanService<CMeeting, CMeetingStatus> {

	CMeetingService(final CMeetingRepository repository, final Clock clock, final CSessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public List<CMeetingStatus> getAllStatuses() { return tech.derbent.abstracts.utils.CKanbanUtils.getEmptyStatusList(this.getClass()); }

	@Override
	public Map<CMeetingStatus, List<CMeeting>> getEntitiesGroupedByStatus(final Long projectId) {
		return tech.derbent.abstracts.utils.CKanbanUtils.getEmptyGroupedStatus(this.getClass());
	}

	@Override
	protected Class<CMeeting> getEntityClass() { return CMeeting.class; }

	@Override
	public CMeeting updateEntityStatus(final CMeeting entity, final CMeetingStatus newStatus) {
		tech.derbent.abstracts.utils.CKanbanUtils.updateEntityStatusSimple(entity, newStatus, CMeeting::setStatus);
		return save(entity);
	}
}
