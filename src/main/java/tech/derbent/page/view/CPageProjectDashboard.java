package tech.derbent.page.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.components.CCrudToolbar;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.projects.view.CProjectsView;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.session.service.CSessionService;

@Route ("project-dashboard")
@PageTitle ("Project Dashboard")
@Menu (order = 1.2, icon = "vaadin:dashboard", title = "Dashboard.Projects")
@PermitAll // When security is enabled, allow all authenticated users
public class CPageProjectDashboard extends CPageGenericEntity<CProject> {

	private static final long serialVersionUID = 1L;

	public static String getStaticEntityColorCode() { return getStaticIconColorCode(); }

	public static String getStaticIconColorCode() {
		return "#28a745"; // Green color for projects
	}

	public static String getStaticIconFilename() { return "vaadin:dashboard"; }

	public CPageProjectDashboard(final CSessionService sessionService, final CGridEntityService gridEntityService,
			final CDetailSectionService screenService, final CProjectService projectService) {
		super(sessionService, screenService, gridEntityService, projectService, CProject.class, CProjectsView.VIEW_NAME);
	}

	/** Configures the dependency checker for projects */
	@Override
	@SuppressWarnings ("unchecked")
	protected void configureCrudToolbar(CCrudToolbar<?> toolbar) {
		super.configureCrudToolbar(toolbar);
		CCrudToolbar<CProject> typedToolbar = (CCrudToolbar<CProject>) toolbar;
		typedToolbar.setDependencyChecker(project -> {
			try {
				// You could add project dependency checks here
				return null; // No dependencies, deletion allowed
			} catch (Exception e) {
				return "Error checking for dependent data. Please try again.";
			}
		});
	}

	@Override
	protected CProject createNewEntityInstance() {
		return new CProject();
	}
}
