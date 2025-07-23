package tech.derbent.activities.view;

import java.util.List;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.activities.service.CActivityTypeService;
import tech.derbent.layout.service.LayoutStateService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.SessionService;

@Route("activities/:activity_id?/:action?(edit)")
@PageTitle("Activity Master Detail")
@Menu(order = 0, icon = "vaadin:calendar-clock", title = "Settings.Activities")
@PermitAll // When security is enabled, allow all authenticated users
public class CActivitiesView extends CProjectAwareMDPage<CActivity> {

	private static final long serialVersionUID = 1L;
	private final String ENTITY_ID_FIELD = "activity_id";
	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "activities/%s/edit";
	private final CActivityTypeService activityTypeService;

	public CActivitiesView(final CActivityService entityService, final SessionService sessionService, 
			final CActivityTypeService activityTypeService, final LayoutStateService layoutStateService) {
		super(CActivity.class, entityService, sessionService, layoutStateService);
		addClassNames("activities-view");
		this.activityTypeService = activityTypeService;
		// createDetailsLayout();
	}

	@Override
	protected void createDetailsLayout() {
		LOGGER.info("Creating details layout for CActivitiesView using annotation-based data providers");
		final Div editorLayoutDiv = new Div();
		editorLayoutDiv.setClassName("editor-layout");
		// NEW APPROACH: No data provider needed! The @MetaData annotation on
		// CActivity.activityType specifies dataProviderBean = "CActivityTypeService"
		// This makes the code much simpler and more maintainable
		editorLayoutDiv.add(CEntityFormBuilder.buildForm(CActivity.class, getBinder()));
		// LEGACY APPROACH (still supported for backward compatibility): Create data
		// provider for ComboBoxes - this is the old complex way final
		// CEntityFormBuilder.ComboBoxDataProvider dataProvider = new
		// CEntityFormBuilder.ComboBoxDataProvider() { @Override
		// @SuppressWarnings("unchecked") public <T extends CEntityDB> java.util.List<T>
		// getItems(final Class<T> entityType) { if (entityType == CActivityType.class)
		// { return (java.util.List<T>)
		// activityTypeService.list(org.springframework.data.domain.Pageable.unpaged());
		// } // With multiple ComboBoxes, this becomes complex and hard to maintain //
		// What if we add more ComboBox fields? More if-else blocks needed! return
		// java.util.Collections.emptyList(); } };
		// editorLayoutDiv.add(CEntityFormBuilder.buildForm(CActivity.class,
		// getBinder(), dataProvider));
		// Note: Buttons are now automatically added to the details tab by the parent
		// class
		getBaseDetailsLayout().add(editorLayoutDiv);
	}

	@Override
	protected void createGridForEntity() {
		// property name must match the field name in CProject
		grid.addColumn("name").setAutoWidth(true);
		// when a row is selected or deselected, populate form
		grid.asSingleSelect().addValueChangeListener(event -> {
			if (event.getValue() != null) {
				UI.getCurrent().navigate(String.format(ENTITY_ROUTE_TEMPLATE_EDIT, event.getValue().getId()));
			}
			else {
				clearForm();
				UI.getCurrent().navigate(CActivitiesView.class);
			}
		});
	}

	// private final BeanValidationBinder<CProject> binder; private final
	// CProjectService userService; private final Grid<CProject> grid;// = new
	// Grid<>(CProject.class, false);
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
	protected List<CActivity> getProjectFilteredData(final CProject project, final org.springframework.data.domain.Pageable pageable) {
		return ((CActivityService) entityService).listByProject(project, pageable).getContent();
	}

	@Override
	protected void initPage() {
		// Initialize the page components and layout This method can be overridden to
		// set up the view's components
	}

	@Override
	protected CActivity newEntity() {
		return super.newEntity(); // Uses the project-aware implementation from parent
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