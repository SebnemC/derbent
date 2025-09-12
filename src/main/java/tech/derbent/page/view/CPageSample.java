package tech.derbent.page.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.components.CCrudToolbar;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.activities.view.CActivitiesView;
import tech.derbent.comments.service.CCommentService;
import tech.derbent.orders.domain.COrder;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.session.service.CSessionService;

@Route ("cpagesample")
@PageTitle ("Sample Page")
@Menu (order = 1.1, icon = "class:tech.derbent.page.view.CPageEntityView", title = "Settings.Sample Page")
@PermitAll // When security is enabled, allow all authenticated users
public class CPageSample extends CPageGenericEntity<CActivity> {

	private static final long serialVersionUID = 1L;

	public static String getStaticEntityColorCode() { return getStaticIconColorCode(); }

	public static String getStaticIconColorCode() {
		return "#102bff"; // Blue color for activity entities
	}

	public static String getStaticIconFilename() { return COrder.getIconFilename(); }

	private final CCommentService commentService;

	public CPageSample(final CSessionService sessionService, final CGridEntityService gridEntityService, final CDetailSectionService screenService,
			final CActivityService activityService, final CCommentService commentService) {
		super(sessionService, screenService, gridEntityService, activityService, CActivity.class, CActivitiesView.VIEW_NAME);
		this.commentService = commentService;
	}

	/** Configures the dependency checker for activities to prevent deletion when comments exist */
	@Override
	@SuppressWarnings ("unchecked")
	protected void configureCrudToolbar(CCrudToolbar<?> toolbar) {
		super.configureCrudToolbar(toolbar);
		// Add dependency checker for activities with comments
		CCrudToolbar<CActivity> typedToolbar = (CCrudToolbar<CActivity>) toolbar;
		typedToolbar.setDependencyChecker(activity -> {
			try {
				long commentCount = commentService.countByActivity(activity);
				if (commentCount > 0) {
					return "Cannot delete this activity because it has " + commentCount + " comment(s). Please remove the comments first.";
				}
				return null; // No dependencies, deletion allowed
			} catch (Exception e) {
				return "Error checking for dependent data. Please try again.";
			}
		});
	}

	@Override
	protected CActivity createNewEntityInstance() {
		CActivity newActivity = new CActivity();
		// Set project if available
		sessionService.getActiveProject().ifPresent(newActivity::setProject);
		return newActivity;
	}
}
