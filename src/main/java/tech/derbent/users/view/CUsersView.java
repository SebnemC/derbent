package tech.derbent.users.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.views.CAbstractMDPage;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserType;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.CUserTypeService;

@Route("users/:user_id?/:action?(edit)")
@PageTitle("User Master Detail")
@Menu(order = 0, icon = "vaadin:clipboard-check", title = "Settings.Users")
@PermitAll // When security is enabled, allow all authenticated users
public class CUsersView extends CAbstractMDPage<CUser> {

	private static final long serialVersionUID = 1L;
	private final String ENTITY_ID_FIELD = "user_id";
	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "users/%s/edit";
	private final CUserProjectSettingsGrid projectSettingsGrid;
	private final CUserTypeService userTypeService;
	// private final TextField name;

	public CUsersView(final CUserService entityService, final CProjectService projectService, final CUserTypeService userTypeService) {
		super(CUser.class, entityService);
		addClassNames("users-view");
		this.userTypeService = userTypeService;
		projectSettingsGrid = new CUserProjectSettingsGrid(projectService);
		add(projectSettingsGrid);
		// name = new TextField("Name"); getBinder().bind(name, CUser::getName,
		// CUser::setName); add(name);
	}

	@Override
	protected Component createDetailsLayout() {
		LOGGER.info("Creating details layout for CUsersView");
		final Div detailsLayout = new Div();
		detailsLayout.setClassName("editor-layout");
		
		// Create data provider for ComboBoxes
		final CEntityFormBuilder.ComboBoxDataProvider dataProvider = new CEntityFormBuilder.ComboBoxDataProvider() {
			@Override
			@SuppressWarnings("unchecked")
			public <T extends CEntityDB> java.util.List<T> getItems(final Class<T> entityType) {
				if (entityType == CUserType.class && userTypeService != null) {
					return (java.util.List<T>) userTypeService.list(org.springframework.data.domain.Pageable.unpaged());
				}
				return java.util.Collections.emptyList();
			}
		};
		
		detailsLayout.add(CEntityFormBuilder.buildForm(CUser.class, getBinder(), dataProvider));
		createButtonLayout(detailsLayout);
		return detailsLayout;
	}

	@Override
	protected void createGridForEntity() {}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected String getEntityRouteTemplateEdit() { return ENTITY_ROUTE_TEMPLATE_EDIT; }

	@Override
	protected void initPage() {
		// TODO Auto-generated method stub
	}

	@Override
	protected CUser newEntity() {
		return new CUser();
	}

	@Override
	protected void populateForm(final CUser value) {
		super.populateForm(value);
		// Update the project settings grid when a user is selected
		if (value != null) {
			// Load user with project settings to avoid lazy initialization issues
			final CUser userWithSettings = ((CUserService) entityService).getUserWithProjects(value.getId());
			projectSettingsGrid.setCurrentUser(userWithSettings);
			projectSettingsGrid.setProjectSettingsAccessors(() -> userWithSettings.getProjectSettings() != null ? userWithSettings.getProjectSettings() : java.util.Collections.emptyList(),
				(settings) -> {
					userWithSettings.setProjectSettings(settings);
					// Save the user when project settings are updated
					entityService.save(userWithSettings);
				}, () -> {
					// Refresh the current entity after save
					try {
						final CUser refreshedUser = ((CUserService) entityService).getUserWithProjects(userWithSettings.getId());
						populateForm(refreshedUser);
					} catch (final Exception e) {
						LOGGER.error("Error refreshing user after project settings update", e);
					}
				});
		}
		else {
			projectSettingsGrid.setCurrentUser(null);
			projectSettingsGrid.setProjectSettingsAccessors(() -> java.util.Collections.emptyList(), (settings) -> {}, () -> {});
		}
	}

	@Override
	protected void setupToolbar() {
		// TODO Auto-generated method stub
	}
}
