package tech.derbent.decisions.view;

import java.util.List;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.decisions.domain.CDecision;
import tech.derbent.decisions.service.CDecisionService;

/**
 * CPanelDecisionDescription - Panel for basic decision information. Layer: View (MVC) Displays and allows editing of
 * fundamental decision fields including name, description, decision type, and project information. Following the
 * established pattern from CPanelActivityDescription.
 */
public class CPanelDecisionDescription extends CPanelDecisionBase {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor for decision description panel.
     * 
     * @param currentEntity
     *            the current decision entity
     * @param beanValidationBinder
     *            validation binder for the decision
     * @param entityService
     *            decision service for data operations
     */
    public CPanelDecisionDescription(final CDecision currentEntity,
            final CEnhancedBinder<CDecision> beanValidationBinder, final CDecisionService entityService) {
        super("Basic Information", currentEntity, beanValidationBinder, entityService);
        // Only open this panel by default
        initPanel();
    }

    @Override
    protected void updatePanelEntityFields() {
        // Basic Information panel - only fundamental fields that have @MetaData
        // annotations
        setEntityFields(List.of("name", "description", "decisionType"));
    }
}