package tech.derbent.activities.view;

import java.util.List;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.session.service.SessionService;

/**
 * CPanelActivityBudgetManagement - Panel for grouping budget management related fields
 * of CActivity entity.
 * Layer: View (MVC)
 * Groups fields: estimatedCost, actualCost, hourlyRate
 */
public class CPanelActivityBudgetManagement extends CPanelActivityBase {

	private static final long serialVersionUID = 1L;

	public CPanelActivityBudgetManagement(final CActivity currentEntity,
		final BeanValidationBinder<CActivity> beanValidationBinder,
		final CActivityService entityService, final SessionService sessionService) {
		super("Budget Management", currentEntity, beanValidationBinder, entityService, sessionService);
	}

	@Override
	protected void updatePanelEntityFields() {
		// Budget Management fields - cost estimation and tracking
		setEntityFields(List.of("estimatedCost", "actualCost", "hourlyRate"));
	}
}