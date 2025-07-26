package tech.derbent.activities.view;

import java.util.List;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.SessionService;

@Route("activities/:activity_id?/:action?(edit)")
@PageTitle("Activity Master Detail")
@Menu(order = 0, icon = "vaadin:calendar-clock", title = "Project.Activities")
@PermitAll // When security is enabled, allow all authenticated users
public class CActivitiesView extends CProjectAwareMDPage<CActivity> {

	private static final long serialVersionUID = 1L;
	private final String ENTITY_ID_FIELD = "activity_id";
	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "activities/%s/edit";
	private CPanelActivityDescription descriptionPanel;

	public CActivitiesView(final CActivityService entityService,
		final SessionService sessionService) {
		super(CActivity.class, entityService, sessionService);
		LOGGER.info(
			"Initializing CActivitiesView with entityService");
		addClassNames("activities-view");
	}

	@Override
	protected void createDetailsLayout() {
		LOGGER.info(
			"Creating details layout for CActivitiesView using CPanelActivityDescription");
		createEntityDetails();
		// Note: Buttons are now automatically added to the details tab by the parent
		// class
	}

	/**
	 * Creates the entity details section using CPanelActivityDescription. Follows the
	 * same pattern as CUsersView for consistency.
	 */
	protected void createEntityDetails() {
		LOGGER.info("Creating entity details for CActivitiesView");
		// Create description panel for activity details
		descriptionPanel = new CPanelActivityDescription(getCurrentEntity(), getBinder(),
			(CActivityService) entityService);
		getBaseDetailsLayout().add(descriptionPanel);
	}

	@Override
	protected void createGridForEntity() {
		// property name must match the field name in CProject
		grid.addColumn("name").setAutoWidth(true).setHeader("Activity Name");
		
		// Add status column to display activity status
		grid.addColumn(activity -> {
			CActivity activityEntity = (CActivity) activity;
			return activityEntity.getActivityStatus() != null ? 
				activityEntity.getActivityStatus().getName() : "";
		}).setAutoWidth(true).setHeader("Status");
		
		// Note: Grid selection listener is handled by the base class CAbstractMDPage
		// Removing duplicate listener that was causing conflicts with New/Delete buttons
	}

	// private final BeanValidationBinder<CProject> binder; private final CProjectService
	// userService; private final Grid<CProject> grid;// = new Grid<>(CProject.class,
	// false);
	@Override
	protected CActivity createNewEntityInstance() {
		return new CActivity();
	}

	@Override
	protected String getEntityRouteIdField() { // TODO Auto-generated method stub
		return ENTITY_ID_FIELD;
	}

	@Override
	protected String getEntityRouteTemplateEdit() { // TODO Auto-generated method stub
		return ENTITY_ROUTE_TEMPLATE_EDIT;
	}

	@Override
	protected List<CActivity> getProjectFilteredData(final CProject project,
		final org.springframework.data.domain.Pageable pageable) {
		return ((CActivityService) entityService).listByProject(project, pageable)
			.getContent();
	}

	@Override
	protected void initPage() {
		// Initialize the page components and layout This method can be overridden to set
		// up the view's components
	}

	@Override
	protected CActivity newEntity() {
		return super.newEntity(); // Uses the project-aware implementation from parent
	}

	@Override
	protected void populateForm(final CActivity value) {
		super.populateForm(value);
		LOGGER.info("Populating form with activity data: {}",
			value != null ? value.getName() : "null");
		// Update the description panel when an activity is selected
		if (descriptionPanel != null) {
			descriptionPanel.populateForm(value);
		}
	}

	@Override
	protected void setProjectForEntity(final CActivity entity, final CProject project) {
		entity.setProject(project);
	}

	@Override
	protected void setupToolbar() {
		// TODO Auto-generated method stub
	}
}