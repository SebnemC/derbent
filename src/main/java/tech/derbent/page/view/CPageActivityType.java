package tech.derbent.page.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.components.CCrudToolbar;
import tech.derbent.activities.domain.CActivityType;
import tech.derbent.activities.service.CActivityTypeService;
import tech.derbent.activities.view.CActivityTypeView;
import tech.derbent.orders.domain.COrder;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.session.service.CSessionService;

@Route ("cpageactivitytype")
@PageTitle ("Activity Type Management")
@Menu (order = 1.2, icon = "class:tech.derbent.page.view.CPageEntityView", title = "Settings.Activity Type Management")
@PermitAll // When security is enabled, allow all authenticated users
public class CPageActivityType extends CPageGenericEntity<CActivityType> {

	private static final long serialVersionUID = 1L;

	public static String getStaticEntityColorCode() { return getStaticIconColorCode(); }

	public static String getStaticIconColorCode() {
		return "#007bff"; // Blue color for activity type entities
	}

	public static String getStaticIconFilename() { return CActivityType.getIconFilename(); }

	private final CActivityTypeService activityTypeService;

	public CPageActivityType(final CSessionService sessionService, final CGridEntityService gridEntityService,
			final CDetailSectionService screenService, final CActivityTypeService activityTypeService) {
		super(sessionService, screenService, gridEntityService, activityTypeService, CActivityType.class, CActivityTypeView.VIEW_NAME);
		this.activityTypeService = activityTypeService;
	}

	/** Configures the dependency checker for activity types to prevent deletion when activities are using them */
	@Override
	@SuppressWarnings ("unchecked")
	protected void configureCrudToolbar(CCrudToolbar<?> toolbar) {
		super.configureCrudToolbar(toolbar);
		// Add dependency checker for activity types with activities
		CCrudToolbar<CActivityType> typedToolbar = (CCrudToolbar<CActivityType>) toolbar;
		typedToolbar.setDependencyChecker(activityType -> {
			try {
				long activityCount = activityTypeService.countActivitiesUsingType(activityType);
				if (activityCount > 0) {
					return "Cannot delete this activity type because it is used by " + activityCount
							+ " activity(ies). Please change the activity type for those activities first.";
				}
				return null; // No dependencies, deletion allowed
			} catch (Exception e) {
				return "Error checking for dependent data. Please try again.";
			}
		});
	}

	@Override
	protected CActivityType createNewEntityInstance() {
		CActivityType newActivityType = new CActivityType();
		// Set project if available
		sessionService.getActiveProject().ifPresent(newActivityType::setProject);
		return newActivityType;
	}
}
