package tech.derbent.base.ui.component;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.FlexDirection;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.FontWeight;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.JustifyContent;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;

import tech.derbent.abstracts.interfaces.CProjectListChangeListener;
import tech.derbent.abstracts.views.CButton;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.LayoutService;
import tech.derbent.session.service.SessionService;
import tech.derbent.users.domain.CUser;

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
    private final LayoutService layoutService;
    private final AuthenticationContext authenticationContext;
    private ComboBox<CProject> projectComboBox;
    private CButton layoutToggleButton;
    private Avatar userAvatar;
    private Span usernameSpan;

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
        this(viewTitle, sessionService, null, null, components);
    }

    /**
     * Constructs a ViewToolbar with a title, services, and optional components.
     * 
     * @param viewTitle
     *            The title of the view to be displayed in the toolbar.
     * @param sessionService
     *            The session service for managing project selection.
     * @param layoutService
     *            The layout service for managing layout mode (optional).
     * @param authenticationContext
     *            The authentication context for user information (optional).
     * @param components
     *            Optional components to be added to the toolbar.
     */
    public ViewToolbar(final String viewTitle, final SessionService sessionService, 
                      final LayoutService layoutService, final AuthenticationContext authenticationContext,
                      final Component... components) {
        LOGGER.debug("Creating ViewToolbar for {}", viewTitle);
        this.sessionService = sessionService;
        this.layoutService = layoutService;
        this.authenticationContext = authenticationContext;
        
        addClassNames(Display.FLEX, FlexDirection.ROW, JustifyContent.BETWEEN, AlignItems.CENTER, Gap.MEDIUM);
        
        // Add separation line below the toolbar
        getContent().getStyle().set("border-bottom", "1px solid var(--lumo-contrast-20pct)");
        getContent().getStyle().set("padding", "var(--lumo-space-s) var(--lumo-space-m)");

        // this is a button that toggles the drawer in the app layout
        final var drawerToggle = new DrawerToggle();
        drawerToggle.addClassNames(Margin.NONE);
        
        title = new H1(viewTitle);
        title.addClassNames(FontSize.XLARGE, Margin.NONE, FontWeight.LIGHT);

        // Create project selection combobox
        createProjectComboBox();
        
        // Left side: toggle, title, and project selector
        final var projectSelector = new Div(new Span("Active Project:"), projectComboBox);
        projectSelector.addClassNames(Display.FLEX, AlignItems.CENTER, Gap.SMALL);
        
        final var leftSide = new Div(drawerToggle, title, projectSelector);
        leftSide.addClassNames(Display.FLEX, AlignItems.CENTER, Gap.MEDIUM);

        // Spacer to push user info and layout toggle to the right
        final var spacer = new Div();
        spacer.getStyle().set("flex-grow", "1");

        // Right side: user info and layout toggle
        final var rightSide = createRightSideComponents();

        // Add all components to the toolbar
        getContent().add(leftSide, spacer, rightSide);

        // add additional components if passed as a parameter
        if (components.length > 0) {
            // If there are additional components, add them before the spacer
            final var actions = new Div(components);
            actions.addClassNames(Display.FLEX, AlignItems.CENTER, Gap.SMALL);
            // Re-add components in the correct order
            getContent().removeAll();
            getContent().add(leftSide, actions, spacer, rightSide);
        }

        // Register for project list change notifications
        sessionService.addProjectListChangeListener(this);
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
     * Creates the project selection ComboBox.
     */
    private void createProjectComboBox() {
        LOGGER.debug("Creating project ComboBox with sessionService: {}", 
                    sessionService != null ? sessionService.getClass().getSimpleName() : "null");
        
        if (sessionService == null) {
            LOGGER.error("SessionService is null, cannot create project ComboBox");
            return;
        }
        
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
            } else {
                LOGGER.debug("Project selection cleared");
            }
        });
    }

    /**
     * Creates the right side components containing user info and layout toggle.
     */
    private HorizontalLayout createRightSideComponents() {
        final var rightSide = new HorizontalLayout();
        rightSide.setSpacing(true);
        rightSide.setAlignItems(HorizontalLayout.Alignment.CENTER);

        // Create user info components
        createUserInfoComponents();
        
        // Create layout toggle button
        createLayoutToggleButton();
        
        // Add components to right side if they are available
        if (userAvatar != null && usernameSpan != null) {
            rightSide.add(userAvatar, usernameSpan);
        }
        
        if (layoutToggleButton != null) {
            rightSide.add(layoutToggleButton);
        }

        return rightSide;
    }

    /**
     * Creates user info components (avatar and username).
     */
    private void createUserInfoComponents() {
        LOGGER.debug("Creating user info components with authenticationContext: {}", 
                    authenticationContext != null ? "available" : "null");
        
        if (authenticationContext != null) {
            // Try to get user from session service first
            if (sessionService != null) {
                final var activeUser = sessionService.getActiveUser();
                if (activeUser.isPresent()) {
                    final CUser user = activeUser.get();
                    final String username = user.getName() != null && !user.getName().trim().isEmpty() 
                        ? user.getName() 
                        : user.getLogin();
                    if (username != null && !username.trim().isEmpty()) {
                        createUserComponents(username);
                        return;
                    }
                }
            }
            
            // Fallback to authentication context
            final var authenticatedUser = authenticationContext.getAuthenticatedUser(User.class);
            if (authenticatedUser.isPresent()) {
                final String username = authenticatedUser.get().getUsername();
                if (username != null && !username.trim().isEmpty()) {
                    createUserComponents(username);
                } else {
                    LOGGER.warn("Authenticated user has null or empty username");
                }
            } else {
                LOGGER.debug("No authenticated user found in authentication context");
            }
        } else {
            LOGGER.debug("AuthenticationContext is null, user info components will not be created");
        }
    }

    /**
     * Creates user avatar and username components.
     */
    private void createUserComponents(final String username) {
        LOGGER.debug("Creating user components for username: {}", username);
        
        if (username == null || username.trim().isEmpty()) {
            LOGGER.warn("Cannot create user components - username is null or empty");
            return;
        }
        
        // Create simple avatar without icon
        userAvatar = new Avatar();
        userAvatar.setName(username);
        userAvatar.setAbbreviation(username.length() > 0 ? username.substring(0, 1).toUpperCase() : "U");
        userAvatar.addThemeVariants(AvatarVariant.LUMO_XSMALL);
        userAvatar.setColorIndex(3);
        userAvatar.getElement().setAttribute("title", "User: " + username);

        // Create username span without icon
        usernameSpan = new Span(username);
        usernameSpan.addClassNames(FontWeight.MEDIUM);
        usernameSpan.getStyle().set("color", "var(--lumo-secondary-text-color)");
        
        LOGGER.debug("User components created successfully for: {}", username);
    }

    /**
     * Creates the layout toggle button.
     */
    private void createLayoutToggleButton() {
        LOGGER.debug("Creating layout toggle button with layoutService: {}", 
                    layoutService != null ? layoutService.getClass().getSimpleName() : "null");
        
        if (layoutService != null) {
            layoutToggleButton = new CButton();
            layoutToggleButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
            layoutToggleButton.getElement().setAttribute("title", "Toggle Layout Mode");
            
            // Set initial icon based on current layout mode
            updateLayoutToggleIcon();
            
            // Handle layout toggle
            layoutToggleButton.addClickListener(event -> {
                LOGGER.info("Layout toggle button clicked");
                if (layoutService != null) {
                    final LayoutService.LayoutMode oldMode = layoutService.getCurrentLayoutMode();
                    layoutService.toggleLayoutMode();
                    final LayoutService.LayoutMode newMode = layoutService.getCurrentLayoutMode();
                    
                    LOGGER.info("Layout toggled from {} to {}", oldMode, newMode);
                    updateLayoutToggleIcon();
                    
                    // Force UI update
                    final var ui = getUI();
                    if (ui.isPresent()) {
                        ui.get().access(() -> {
                            LOGGER.debug("Forcing UI update after layout toggle");
                            ui.get().push();
                        });
                    }
                } else {
                    LOGGER.warn("LayoutService is null, cannot toggle layout mode");
                }
            });
        } else {
            LOGGER.debug("LayoutService is null, layout toggle button will not be created");
        }
    }

    /**
     * Updates the layout toggle button icon based on current layout mode.
     */
    private void updateLayoutToggleIcon() {
        LOGGER.debug("Updating layout toggle icon with layoutToggleButton: {}, layoutService: {}", 
                    layoutToggleButton != null ? "available" : "null",
                    layoutService != null ? "available" : "null");
        
        if (layoutToggleButton != null && layoutService != null) {
            final LayoutService.LayoutMode currentMode = layoutService.getCurrentLayoutMode();
            if (currentMode == null) {
                LOGGER.warn("Current layout mode is null, using default VERTICAL");
                return;
            }
            
            final Icon icon = currentMode == LayoutService.LayoutMode.HORIZONTAL 
                ? VaadinIcon.GRID_H.create() 
                : VaadinIcon.GRID_V.create();
            
            layoutToggleButton.setIcon(icon);
            layoutToggleButton.getElement().setAttribute("title", 
                "Current: " + currentMode + " - Click to toggle");
            
            LOGGER.debug("Layout toggle icon updated to: {}", currentMode);
        } else {
            LOGGER.debug("Cannot update layout toggle icon - button or service is null");
        }
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
        LOGGER.debug("Refreshing project list with sessionService: {}, projectComboBox: {}", 
                    sessionService != null ? "available" : "null",
                    projectComboBox != null ? "available" : "null");
        
        if (sessionService != null && projectComboBox != null) {
            try {
                final List<CProject> projects = sessionService.getAvailableProjects();
                if (projects == null) {
                    LOGGER.warn("SessionService returned null project list");
                    return;
                }
                
                projectComboBox.setItems(projects);
                LOGGER.debug("Project list refreshed with {} projects", projects.size());

                // If no project is selected but projects are available, select the first one
                if (projectComboBox.getValue() == null && !projects.isEmpty()) {
                    projectComboBox.setValue(projects.get(0));
                    LOGGER.debug("Auto-selected first project: {}", projects.get(0).getName());
                }
            } catch (final Exception e) {
                LOGGER.error("Error refreshing project list", e);
            }
        } else {
            LOGGER.debug("Cannot refresh project list - sessionService or projectComboBox is null");
        }
    }

    public void setPageTitle(final String title) {
        LOGGER.debug("Setting page title to: {}", title);
        if (title == null || title.trim().isEmpty()) {
            LOGGER.warn("Cannot set page title - title is null or empty");
            return;
        }
        this.title.setText(title);
    }
}
