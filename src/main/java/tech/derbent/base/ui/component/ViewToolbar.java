package tech.derbent.base.ui.component;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.spring.security.AuthenticationContext;
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
import tech.derbent.layout.service.LayoutStateService;
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
    private final LayoutStateService layoutStateService;
    private final AuthenticationContext authenticationContext;
    private ComboBox<CProject> projectComboBox;
    private Button layoutToggleButton;

    /**
     * Constructs a ViewToolbar with a title and optional components.
     * 
     * @param viewTitle
     *            The title of the view to be displayed in the toolbar.
     * @param sessionService
     *            The session service for managing project selection.
     * @param layoutStateService
     *            The layout state service for managing layout mode.
     * @param authenticationContext
     *            The authentication context for getting user information.
     * @param components
     *            Optional components to be added to the toolbar.
     */
    public ViewToolbar(final String viewTitle, final SessionService sessionService, 
                      final LayoutStateService layoutStateService, final AuthenticationContext authenticationContext,
                      final Component... components) {
        LOGGER.debug("Creating ViewToolbar for {}", viewTitle);
        this.sessionService = sessionService;
        this.layoutStateService = layoutStateService;
        this.authenticationContext = authenticationContext;
        
        addClassNames(Display.FLEX, FlexDirection.COLUMN, JustifyContent.BETWEEN, AlignItems.STRETCH, Gap.MEDIUM,
                FlexDirection.Breakpoint.Medium.ROW, AlignItems.Breakpoint.Medium.CENTER);
        
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

        // Create the right side with project selector, user info, and layout toggle
        createRightSideLayout();

        // Register for project list change notifications
        sessionService.addProjectListChangeListener(this);
    }

    /**
     * Legacy constructor for backwards compatibility.
     * This constructor assumes no layout state service and authentication context are available.
     */
    public ViewToolbar(final String viewTitle, final SessionService sessionService, final Component... components) {
        this(viewTitle, sessionService, null, null, components);
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

    /**
     * Creates the right side layout with project selector, user info, and layout toggle.
     */
    private void createRightSideLayout() {
        final var rightSideLayout = new Div();
        rightSideLayout.addClassNames(Display.FLEX, AlignItems.CENTER, Gap.MEDIUM);

        // Add project selector
        final var projectSelector = new Div(new Span("Active Project:"), projectComboBox);
        projectSelector.addClassNames(Display.FLEX, AlignItems.CENTER, Gap.SMALL);
        rightSideLayout.add(projectSelector);

        // Add user info if authentication context is available
        if (authenticationContext != null) {
            final var userInfo = createUserInfo();
            if (userInfo != null) {
                rightSideLayout.add(userInfo);
            }
        }

        // Add layout toggle button if layout state service is available
        if (layoutStateService != null) {
            createLayoutToggleButton();
            rightSideLayout.add(layoutToggleButton);
        }

        getContent().add(rightSideLayout);
    }

    /**
     * Creates the user info display showing the logged-in user's name.
     */
    private Component createUserInfo() {
        try {
            final var user = authenticationContext.getAuthenticatedUser(User.class).orElse(null);
            if (user != null) {
                final var userSpan = new Span(user.getUsername());
                userSpan.addClassNames(FontWeight.MEDIUM);
                userSpan.getStyle().set("color", "var(--lumo-secondary-text-color)");
                
                final var userContainer = new Div(new Icon(VaadinIcon.USER), userSpan);
                userContainer.addClassNames(Display.FLEX, AlignItems.CENTER, Gap.SMALL);
                
                return userContainer;
            }
        } catch (final Exception e) {
            LOGGER.debug("Could not get authenticated user: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Creates the layout toggle button for switching between horizontal and vertical layouts.
     */
    private void createLayoutToggleButton() {
        layoutToggleButton = new Button();
        layoutToggleButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        
        // Set initial icon based on current layout mode
        updateLayoutToggleButton();
        
        // Handle button click to toggle layout
        layoutToggleButton.addClickListener(event -> {
            final var newMode = layoutStateService.toggleLayoutMode();
            updateLayoutToggleButton();
            
            // Trigger a page refresh to apply the new layout
            UI.getCurrent().getPage().reload();
        });
        
        layoutToggleButton.setTooltipText("Toggle between Horizontal and Vertical layouts");
    }

    /**
     * Updates the layout toggle button icon and tooltip based on current layout mode.
     */
    private void updateLayoutToggleButton() {
        if (layoutToggleButton != null && layoutStateService != null) {
            final boolean isHorizontal = layoutStateService.isHorizontalMode();
            
            if (isHorizontal) {
                layoutToggleButton.setIcon(new Icon(VaadinIcon.SPLIT));
                layoutToggleButton.setTooltipText("Switch to Vertical Layout");
            } else {
                layoutToggleButton.setIcon(new Icon(VaadinIcon.MENU));
                layoutToggleButton.setTooltipText("Switch to Horizontal Layout");
            }
        }
    }

    public void setPageTitle(final String title) {
        this.title.setText(title);
    }
}
