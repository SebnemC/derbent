package tech.derbent.users.view;

import java.util.List;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserService;

/**
 * CPanelUserCompanyAssociation - Panel for grouping company association fields
 * of CUser entity.
 * Layer: View (MVC)
 * Groups fields: company, userType
 */
public class CPanelUserCompanyAssociation extends CPanelUserBase {

	private static final long serialVersionUID = 1L;

	public CPanelUserCompanyAssociation(final CUser currentEntity,
		final BeanValidationBinder<CUser> beanValidationBinder,
		final CUserService entityService) {
		super("Company Association", currentEntity, beanValidationBinder, entityService);
	}

	@Override
	protected void updatePanelEntityFields() {
		// Company Association fields - organizational relationships
		setEntityFields(List.of("company", "userType"));
	}
}