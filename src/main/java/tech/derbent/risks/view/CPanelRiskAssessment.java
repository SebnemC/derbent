package tech.derbent.risks.view;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.risks.domain.CRisk;
import tech.derbent.risks.service.CRiskService;

/**
 * CPanelRiskAssessment - Panel for grouping risk assessment fields of CRisk entity.
 * Layer: View (MVC) Groups fields: riskSeverity
 */
public class CPanelRiskAssessment extends CPanelRiskBase {

	private static final long serialVersionUID = 1L;

	public CPanelRiskAssessment(final CRisk currentEntity,
		final CEnhancedBinder<CRisk> beanValidationBinder,
		final CRiskService entityService) throws NoSuchMethodException, SecurityException,
		IllegalAccessException, InvocationTargetException {
		super("Risk Assessment", currentEntity, beanValidationBinder, entityService);
		initPanel();
	}

	@Override
	protected void updatePanelEntityFields() {
		// Risk Assessment fields - severity and evaluation
		setEntityFields(List.of("riskSeverity"));
	}
}