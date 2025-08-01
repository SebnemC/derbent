package tech.derbent.meetings.view;

import java.util.List;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.service.CMeetingService;

/**
 * CPanelMeetingMinutes - Panel for grouping minutes and linked elements fields of CMeeting entity.
 * Layer: View (MVC) Groups fields: minutes, linkedElement
 */
public class CPanelMeetingMinutes extends CPanelMeetingBase {

	private static final long serialVersionUID = 1L;

	public CPanelMeetingMinutes(final CMeeting currentEntity,
		final BeanValidationBinder<CMeeting> beanValidationBinder,
		final CMeetingService entityService) {
		super("Minutes & References", currentEntity, beanValidationBinder, entityService, null);
	}

	@Override
	protected void updatePanelEntityFields() {
		// Minutes and references fields - post-meeting documentation
		setEntityFields(List.of("minutes", "linkedElement"));
	}
}