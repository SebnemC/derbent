package tech.derbent.page.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.components.CCrudToolbar;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.view.CUsersView;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.session.service.CSessionService;

@Route ("team-manager")
@PageTitle ("Team Manager")
@Menu (order = 3.1, icon = "vaadin:users", title = "Administration.Team Manager")
@PermitAll // When security is enabled, allow all authenticated users
public class CPageTeamManager extends CPageGenericEntity<CUser> {

	private static final long serialVersionUID = 1L;

	public static String getStaticEntityColorCode() { return getStaticIconColorCode(); }

	public static String getStaticIconColorCode() {
		return "#fd7e14"; // Orange color for team management
	}

	public static String getStaticIconFilename() { return "vaadin:users"; }

	public CPageTeamManager(final CSessionService sessionService, final CGridEntityService gridEntityService,
			final CDetailSectionService screenService, final CUserService userService) {
		super(sessionService, screenService, gridEntityService, userService, CUser.class, CUsersView.VIEW_NAME);
	}

	/** Configures the dependency checker for users */
	@Override
	@SuppressWarnings ("unchecked")
	protected void configureCrudToolbar(CCrudToolbar<?> toolbar) {
		super.configureCrudToolbar(toolbar);
		CCrudToolbar<CUser> typedToolbar = (CCrudToolbar<CUser>) toolbar;
		typedToolbar.setDependencyChecker(user -> {
			try {
				// Check if user has assigned tasks, created items, etc.
				// This is a simplified example - real implementation would check dependencies
				if ("admin".equals(user.getUsername())) {
					return "Cannot delete admin user - it's required for system operation.";
				}
				return null; // No dependencies, deletion allowed
			} catch (Exception e) {
				return "Error checking for dependent data. Please try again.";
			}
		});
	}

	@Override
	protected CUser createNewEntityInstance() {
		return new CUser();
	}
}
