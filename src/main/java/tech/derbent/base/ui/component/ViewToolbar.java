package tech.derbent.base.ui.component;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
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

import tech.derbent.abstracts.interfaces.CProjectListChangeListener;
import tech.derbent.abstracts.views.CLayoutToggleButton;
import tech.derbent.base.enums.CLayoutMode;
import tech.derbent.base.service.CLayoutModeService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.SessionService;

/* ViewToolbar.java
 *
 * This class defines a toolbar for views in the application, providing a
 * consistent header with a title and optional action components.
 *
 * It extends Composite to allow for easy composition of the toolbar's content.
 */
public final class ViewToolbar extends Composite<Header> implements CProjectListChangeListener {

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
    private final CLayoutModeService layoutModeService;
    private ComboBox<CProject> projectComboBox;
    private CLayoutToggleButton layoutToggleButton;

    /**
     * Constructs a ViewToolbar with a title and optional components.
     * 
     * @param viewTitle
     *            The title of the view to be displayed in the toolbar.
     * @param sessionService
     *            The session service for managing project selection.
     * @param layoutModeService
     *            The layout mode service for managing display mode.
     * @param components
     *            Optional components to be added to the toolbar.
     */
    public ViewToolbar(final String viewTitle, final SessionService sessionService, 
                      final CLayoutModeService layoutModeService, final Component... components) {
        LOGGER.debug("Creating ViewToolbar for {}", viewTitle);
        this.sessionService = sessionService;
        this.layoutModeService = layoutModeService;
        // Set the header to use flex with space-between to properly separate left and right content
        getContent().addClassNames(Display.FLEX, JustifyContent.BETWEEN, AlignItems.CENTER);
        
        // Add separation line below the toolbar
        getContent().getStyle().set("border-bottom", "1px solid var(--lumo-contrast-20pct)");

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
        
        // Create layout toggle button
        createLayoutToggleButton();

        // Create project selector with spacing after the title
        final var projectSelector = new Div(new Span("Active Project:"), projectComboBox);
        projectSelector.addClassNames(Display.FLEX, AlignItems.CENTER, Gap.SMALL, Margin.Left.LARGE);
        
        // Create the left side container with title and project selector
        final var leftContainer = new Div(toggleAndTitle, projectSelector);
        leftContainer.addClassNames(Display.FLEX, AlignItems.CENTER, Gap.LARGE);
        
        // add them to the content of the header
        getContent().add(leftContainer);

        // add more if passed as a parameter
        if (components.length > 0) {
            // If there are additional components, add them to the toolbar
            final var actions = new Div(components);
            actions.addClassNames(Display.FLEX, FlexDirection.COLUMN, JustifyContent.BETWEEN, Flex.GROW, Gap.SMALL,
                    FlexDirection.Breakpoint.Medium.ROW);
            getContent().add(actions);
        }

        // Create right side container with user info and layout toggle (right-aligned)
        final var rightContainer = new Div();
        rightContainer.addClassNames(Display.FLEX, AlignItems.CENTER, Gap.MEDIUM);
        
        // Add user display
        final var userDisplay = createUserDisplay();
        if (userDisplay != null) {
            rightContainer.add(userDisplay);
        }
        
        // Add layout toggle button
        rightContainer.add(layoutToggleButton);
        
        getContent().add(rightContainer);

        // Register for project list change notifications
        sessionService.addProjectListChangeListener(this);
    }
    
    /**
     * Backward compatible constructor for existing code.
     * Uses a null layout mode service which will disable the layout toggle feature.
     * 
     * @param viewTitle
     *            The title of the view to be displayed in the toolbar.
     * @param sessionService
     *            The session service for managing project selection.
     * @param components
     *            Optional components to be added to the toolbar.
     */
    public ViewToolbar(final String viewTitle, final SessionService sessionService, final Component... components) {
        this(viewTitle, sessionService, null, components);
    }

    /**
     * Override onAttach to ensure listener registration on component attach.
     */
    @Override
    protected void onAttach(final AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // Re-register in case it was missed during construction
        sessionService.addProjectListChangeListener(this);
    }

    /**
     * Override onDetach to clean up listener registration when component is detached.
     */
    @Override
    protected void onDetach(final DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        // Unregister to prevent memory leaks
        sessionService.removeProjectListChangeListener(this);
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
     * Called when the project list changes. Refreshes the ComboBox items.
     */
    @Override
    public void onProjectListChanged() {
        LOGGER.debug("Project list changed, refreshing ComboBox");
        refreshProjectList();
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
    
    /**
     * Creates and configures the layout toggle button.
     */
    private void createLayoutToggleButton() {
        if (layoutModeService == null) {
            LOGGER.warn("Layout mode service is null, creating invisible layout toggle button");
            // Layout mode service not available, create a placeholder button that's not visible
            layoutToggleButton = new CLayoutToggleButton(CLayoutMode.VERTICAL, null);
            layoutToggleButton.setVisible(false);
            return;
        }
        
        LOGGER.info("Creating layout toggle button with service available");
        final CLayoutMode currentMode = layoutModeService.getCurrentLayoutMode();
        layoutToggleButton = new CLayoutToggleButton(currentMode, event -> {
            LOGGER.info("Layout toggle button clicked, switching layout mode");
            layoutModeService.toggleLayoutMode();
            
            // Update button to reflect new mode
            final CLayoutMode newMode = layoutModeService.getCurrentLayoutMode();
            layoutToggleButton.setLayoutMode(newMode);
        });
        
        // Ensure button is visible
        layoutToggleButton.setVisible(true);
        
        LOGGER.info("Layout toggle button created with mode: {} and visible: {}", currentMode, layoutToggleButton.isVisible());
    }
    
    /**
     * Creates a user display component showing the logged-in user's name.
     * 
     * @return A Div containing the user information, or null if no user is available
     */
    private Div createUserDisplay() {
        return sessionService.getActiveUserDisplayName()
            .map(userName -> {
                final Div userDisplay = new Div();
                userDisplay.addClassNames(Display.FLEX, AlignItems.CENTER, Gap.SMALL);
                
                // Add user icon
                final var userIcon = new com.vaadin.flow.component.icon.Icon(
                    com.vaadin.flow.component.icon.VaadinIcon.USER);
                userIcon.addClassNames(com.vaadin.flow.theme.lumo.LumoUtility.IconSize.SMALL);
                
                // Add user name
                final Span userNameSpan = new Span(userName);
                userNameSpan.addClassNames(FontWeight.NORMAL);
                
                userDisplay.add(userIcon, userNameSpan);
                
                LOGGER.debug("Created user display for user: {}", userName);
                return userDisplay;
            })
            .orElse(null);
    }
}
