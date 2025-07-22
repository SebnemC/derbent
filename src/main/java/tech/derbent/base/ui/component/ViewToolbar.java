package tech.derbent.base.ui.component;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.Flex;
import com.vaadin.flow.theme.lumo.LumoUtility.FlexDirection;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.FontWeight;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.JustifyContent;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;

import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.SessionService;

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
     * just used to create a group of components with nice styling. Not related to a toolbar
     */
    public static Component group(final Component... components) {
        final var group = new Div(components);
        group.addClassNames(Display.FLEX, FlexDirection.COLUMN, AlignItems.STRETCH, Gap.SMALL,
                FlexDirection.Breakpoint.Medium.ROW, AlignItems.Breakpoint.Medium.CENTER);
        return group;
    }

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private final H1 title;
    private final SessionService sessionService;
    private ComboBox<CProject> projectComboBox;

    /**
     * Constructs a ViewToolbar with a title and optional components.
     * 
     * @param viewTitle
     *            The title of the view to be displayed in the toolbar.
     * @param sessionService
     *            The session service for managing project selection.
     * @param components
     *            Optional components to be added to the toolbar.
     */
    public ViewToolbar(final String viewTitle, final SessionService sessionService, final Component... components) {
        LOGGER.debug("Creating ViewToolbar for {}", viewTitle);
        this.sessionService = sessionService;
        addClassNames(Display.FLEX, FlexDirection.COLUMN, JustifyContent.BETWEEN, AlignItems.STRETCH, Gap.MEDIUM,
                FlexDirection.Breakpoint.Medium.ROW, AlignItems.Breakpoint.Medium.CENTER);

        // this is a button that toggles the drawer in the app layout
        final var drawerToggle = new DrawerToggle();
        drawerToggle.addClassNames(Margin.NONE);
        title = new H1(viewTitle);
        title.addClassNames(FontSize.XLARGE, Margin.NONE, FontWeight.LIGHT);

        // put them together
        final var toggleAndTitle = new Div(drawerToggle, title);
        toggleAndTitle.addClassNames(Display.FLEX, AlignItems.CENTER);

        // Create project selection combobox
        createProjectComboBox();

        // add them to the content of the header
        getContent().add(toggleAndTitle);

        // add more if passed as a parameter
        if (components.length > 0) {
            // If there are additional components, add them to the toolbar
            final var actions = new Div(components);
            actions.addClassNames(Display.FLEX, FlexDirection.COLUMN, JustifyContent.BETWEEN, Flex.GROW, Gap.SMALL,
                    FlexDirection.Breakpoint.Medium.ROW);
            getContent().add(actions);
        }

        // Add project selector to the right side
        final var projectSelector = new Div(new Span("Active Project:"), projectComboBox);
        projectSelector.addClassNames(Display.FLEX, AlignItems.CENTER, Gap.SMALL);
        getContent().add(projectSelector);
    }

    /**
     * Creates and configures the project selection ComboBox.
     */
    private void createProjectComboBox() {
        projectComboBox = new ComboBox<>();
        projectComboBox.setItemLabelGenerator(CProject::getName);
        projectComboBox.setPlaceholder("Select Project");
        projectComboBox.setWidth("200px");

        // Load available projects
        refreshProjectList();

        // Set current active project
        sessionService.getActiveProject().ifPresent(projectComboBox::setValue);

        // Handle project selection change
        projectComboBox.addValueChangeListener(event -> {
            final CProject selectedProject = event.getValue();
            if (selectedProject != null) {
                LOGGER.info("Project changed to: {}", selectedProject.getName());
                sessionService.setActiveProject(selectedProject);
            }
        });
    }

    /**
     * Refreshes the project list in the ComboBox.
     */
    public void refreshProjectList() {
        if (sessionService != null && projectComboBox != null) {
            final List<CProject> projects = sessionService.getAvailableProjects();
            projectComboBox.setItems(projects);

            // If no project is selected but projects are available, select the first one
            if (projectComboBox.getValue() == null && !projects.isEmpty()) {
                projectComboBox.setValue(projects.get(0));
            }
        }
    }

    public void setPageTitle(final String title) {
        this.title.setText(title);
    }
}
