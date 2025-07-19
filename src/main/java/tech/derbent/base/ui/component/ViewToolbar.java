package tech.derbent.base.ui.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.Flex;
import com.vaadin.flow.theme.lumo.LumoUtility.FlexDirection;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.FontWeight;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.JustifyContent;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;

import tech.derbent.base.service.SessionService;
import tech.derbent.projects.domain.CProject;

/* ViewToolbar.java
 *
 * This class defines a toolbar for views in the application, providing a
 * consistent header with a title and optional action components.
 *
 * It extends Composite to allow for easy composition of the toolbar's content.
 */
public final class ViewToolbar extends Composite<Header> {

	private static final long serialVersionUID = 1L;

	/*
	 * just used to create a group of components with nice styling. Not related to a
	 * toolbar
	 */
	public static Component group(final Component... components) {
		final var group = new Div(components);
		group.addClassNames(Display.FLEX, FlexDirection.COLUMN, AlignItems.STRETCH, Gap.SMALL, FlexDirection.Breakpoint.Medium.ROW, AlignItems.Breakpoint.Medium.CENTER);
		return group;
	}

	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	private final H1 title;
	private final SessionService sessionService;
	private MultiSelectComboBox<CProject> projectSelector;

	/**
	 * Constructs a ViewToolbar with a title and optional components.
	 * @param viewTitle      The title of the view to be displayed in the toolbar.
	 * @param sessionService The session service for managing current user and projects.
	 * @param components     Optional components to be added to the toolbar.
	 */
	public ViewToolbar(final String viewTitle, final SessionService sessionService, final Component... components) {
		this.sessionService = sessionService;
		LOGGER.debug("Creating ViewToolbar for {}", viewTitle);
		addClassNames(Display.FLEX, FlexDirection.COLUMN, JustifyContent.BETWEEN, AlignItems.STRETCH, Gap.MEDIUM, FlexDirection.Breakpoint.Medium.ROW, AlignItems.Breakpoint.Medium.CENTER);
		// this is a button that toggles the drawer in the app layout
		final var drawerToggle = new DrawerToggle();
		drawerToggle.addClassNames(Margin.NONE);
		title = new H1(viewTitle);
		title.addClassNames(FontSize.XLARGE, Margin.NONE, FontWeight.LIGHT);
		// put them together
		final var toggleAndTitle = new Div(drawerToggle, title);
		toggleAndTitle.addClassNames(Display.FLEX, AlignItems.CENTER);
		// add them to the content of the header
		getContent().add(toggleAndTitle);
		// add more if passed as a parameter
		if (components.length > 0) {
			// If there are additional components, add them to the toolbar
			final var actions = new Div(components);
			actions.addClassNames(Display.FLEX, FlexDirection.COLUMN, JustifyContent.BETWEEN, Flex.GROW, Gap.SMALL, FlexDirection.Breakpoint.Medium.ROW);
			getContent().add(actions);
		}
		
		// Create project selector
		createProjectSelector();
	}

	public void setPageTitle(final String title) {
		this.title.setText(title);
	}
	
	/**
	 * Creates and configures the project selector component.
	 */
	private void createProjectSelector() {
		try {
			projectSelector = new MultiSelectComboBox<>("Select Projects");
			projectSelector.setItems(sessionService.getAccessibleProjects());
			projectSelector.setItemLabelGenerator(CProject::getName);
			projectSelector.setPlaceholder("Choose projects...");
			
			// Set current selection from session
			final var currentSelection = sessionService.getSelectedProjectIds();
			final var accessibleProjects = sessionService.getAccessibleProjects();
			final var selectedProjects = accessibleProjects.stream()
				.filter(project -> currentSelection.contains(project.getId()))
				.toList();
			projectSelector.setValue(selectedProjects.stream().collect(java.util.stream.Collectors.toSet()));
			
			// Add value change listener
			projectSelector.addValueChangeListener(event -> {
				final var selectedProjectIds = event.getValue().stream()
					.map(CProject::getId)
					.collect(java.util.stream.Collectors.toSet());
				sessionService.setSelectedProjectIds(selectedProjectIds);
				LOGGER.info("Project selection changed to: {}", selectedProjectIds);
			});
			
			getContent().add(projectSelector);
			LOGGER.debug("Project selector created with {} accessible projects", sessionService.getAccessibleProjects().size());
		} catch (final Exception e) {
			LOGGER.warn("Failed to create project selector: {}", e.getMessage());
			// Fallback to showing a message
			final var fallbackMessage = new Div("Projects will be available after login");
			getContent().add(fallbackMessage);
		}
	}
}
