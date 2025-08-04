package tech.derbent.base.ui.view;

import java.io.ByteArrayInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.FontWeight;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.IconSize;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import com.vaadin.flow.theme.lumo.LumoUtility.TextColor;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.utils.CAuxillaries;
import tech.derbent.base.ui.component.CHierarchicalSideMenu;
import tech.derbent.base.ui.component.CViewToolbar;
import tech.derbent.base.ui.dialogs.CWarningDialog;
import tech.derbent.session.service.CSessionService;
import tech.derbent.session.service.LayoutService;
import com.vaadin.flow.server.StreamResource;
import tech.derbent.base.utils.CImageUtils;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.view.CUserProfileDialog;

/**
 * The main layout is a top-level placeholder for other views. It provides a side navigation menu and a user menu.
 */
// vaadin applayout is used to create a layout with a side navigation menu it consists of
// a header, a side navigation, and a user menu the side navigation is dynamically
// populated with menu entries from `MenuConfiguration`. Each entry is represented as a
// `SideNavItem` with With Flow, the root layout can be defined using the @Layout
// annotation, which tells the router to render all routes or views inside of it. use
// these functions to add content to 3 sections:addToNavBar addToDrawer addToHeader added
// afterNavigationObserver to the layout to handle navigation events
@Layout
@PermitAll // When security is enabled, allow all authenticated users
public final class MainLayout extends AppLayout implements AfterNavigationObserver {

    private static final long serialVersionUID = 1L;

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private final User currentUser;

    private final AuthenticationContext authenticationContext;

    private final CSessionService sessionService;

    private final LayoutService layoutService;

    private final PasswordEncoder passwordEncoder;

    private final CUserService userService;

    private CViewToolbar mainToolbar;

    MainLayout(final AuthenticationContext authenticationContext, final CSessionService sessionService,
            final LayoutService layoutService, final PasswordEncoder passwordEncoder, final CUserService userService) {
        this.authenticationContext = authenticationContext;
        this.sessionService = sessionService;
        this.layoutService = layoutService;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.currentUser = authenticationContext.getAuthenticatedUser(User.class).orElse(null);
        setPrimarySection(Section.DRAWER);
        // this is the main layout, so we add the side navigation menu and the user menu
        // to the drawer and the toolbar to the navbar
        addToNavbar(true, createNavBar()); // Add the toggle button to the navbar
        addToDrawer(createHeader());
        // ok, lets put it in a scroller, so it can scroll if it is too long????
        addToDrawer(new Scroller(createSlidingHeader()));
        // why this is in a scroller? Add the side navigation menu to the drawer, wrapped
        // in a Scroller for better scrolling behavior addToDrawer(new
        // Scroller(createSideNav()));
        addToDrawer(createUserMenu()); // Add the user menu to the navbar
    }

    @Override
    public void afterNavigation(final AfterNavigationEvent event) {
        LOGGER.debug("After navigation in MainLayout");
        // Update the view title in the toolbar after navigation
        final String pageTitle = MenuConfiguration.getPageHeader(getContent()).orElse("Main Layout");
        mainToolbar.setPageTitle(pageTitle); // Set the page title in the toolbar
        // addToNavbar(true, new CViewToolbar(pageTitle)); // Add the toolbar with the
        // page title
        /*
         * Component content = getContent(); if (content instanceof HasDynamicTitle) { String title = ((HasDynamicTitle)
         * content).getPageTitle(); viewTitle.setText(title); } else { viewTitle.setText(""); }
         */
    }

    @SuppressWarnings("unused")
    private Div createAppMarker() {
        final var slidingHeader = new Div();
        slidingHeader.addClassNames(Display.FLEX, AlignItems.CENTER, Margin.Horizontal.MEDIUM, Gap.SMALL);
        slidingHeader.getStyle().set("flex-wrap", "nowrap"); // Ensure single line
        // Original header content (logo and app name) - version removed
        final var appLogo = VaadinIcon.CUBES.create();
        appLogo.addClassNames(TextColor.PRIMARY, IconSize.LARGE);
        slidingHeader.add(appLogo);
        final var appName = new Span("Derbent");
        appName.addClassNames(FontWeight.SEMIBOLD, FontSize.LARGE);
        appName.getStyle().set("white-space", "nowrap"); // Prevent text wrapping
        slidingHeader.add(appName);
        return slidingHeader;
    }

    private Div createHeader() {
        // TODO Replace with real application logo and name
        final var appLogo = VaadinIcon.HOME.create();
        appLogo.addClassNames(TextColor.PRIMARY, IconSize.LARGE);
        final var appName = new Span("Derbent");
        appName.addClassNames(FontWeight.SEMIBOLD, FontSize.LARGE);
        final var header = new Div(appLogo, appName);
        header.addClassNames(Display.FLEX, Padding.MEDIUM, Gap.MEDIUM, AlignItems.CENTER);
        // Make the header clickable to navigate to dashboard
        header.getStyle().set("cursor", "pointer");
        header.addClickListener(event -> {
            LOGGER.debug("Header clicked - navigating to dashboard");
            com.vaadin.flow.component.UI.getCurrent().navigate("dashboard");
        });
        // Add hover effects
        header.getElement().addEventListener("mouseenter",
                e -> header.getStyle().set("background-color", "var(--lumo-contrast-5pct)"));
        header.getElement().addEventListener("mouseleave", e -> header.getStyle().remove("background-color"));
        return header;
    }

    private Div createNavBar() {
        final Div navBar = new Div();
        // dont add any other compoents to the navbar, just the toolbar otherwise call it
        // with ,xyz,xyz etc..
        mainToolbar = new CViewToolbar("Main Layout", sessionService, layoutService, authenticationContext);
        navBar.add(mainToolbar);
        return navBar;
    }

    /**
     * Creates the side navigation menu. The navigation menu is dynamically populated with menu entries from
     * `MenuConfiguration`. Each entry is represented as a `SideNavItem` with optional icons.
     * 
     * @return A `SideNav` component containing the navigation items.
     */
    @SuppressWarnings("unused")
    private SideNav createSideNav() {
        final var nav = new SideNav(); // Create the side navigation
        nav.addClassNames(Margin.Horizontal.MEDIUM); // Style the navigation
        MenuConfiguration.getMenuEntries().forEach(entry -> createSideNavItem(nav, entry)); // Add menu entries
        return nav;
    }

    /**
     * Creates a side navigation item for a given menu entry. Each menu entry is represented as a `SideNavItem` with
     * optional icons.
     * 
     * @param menuEntry
     *            The menu entry to create a navigation item for.
     * @return A `SideNavItem` representing the menu entry.
     */
    private void createSideNavItem(final SideNav nav, final MenuEntry menuEntry) {

        if (menuEntry == null) {
            return; // Return null if the menu entry is null
        }
        // read the menu entry properties
        String title = menuEntry.title();
        final String path = menuEntry.path();
        final String icon = menuEntry.icon();
        SideNavItem navItem = null; // Initialize the SideNavItem
        // if title contains a dot, it is a sub-menu entry

        if (title.contains(".")) {
            final var parts = title.split("\\.");
            title = parts[parts.length - 1]; // Use the last part as the title
            final String parent_title = parts[0]; // Use the first part as the parent
                                                  // title
                                                  // find the parent menu entry
            SideNavItem parentItem = nav.getItems().stream().filter(item -> item.getLabel().equals(parent_title))
                    .findFirst().orElse(null);

            if (parentItem == null) {
                parentItem = new SideNavItem(parent_title);
                parentItem.setPrefixComponent(new Icon(icon)); // Set the icon for the
                                                               // parent item
                nav.addItem(parentItem); // Add the parent item to the navigation
            }
            // Create a sub-menu item under the parent entry
            navItem = new SideNavItem(title, path, new Icon(icon));
            parentItem.addItem(navItem);
        } else {
            navItem = new SideNavItem(title, path, new Icon(icon));
            // Create a top-level menu item
            nav.addItem(navItem); // Create item with
        }
        CAuxillaries.generateId(navItem); // Generate an ID for the item
    }

    private Div createSlidingHeader() {
        // Add hierarchical side menu below the header content
        final var hierarchicalMenu = new CHierarchicalSideMenu();
        hierarchicalMenu.addClassNames(Margin.Top.MEDIUM);
        // Create container for the complete sliding header with menu
        final var completeHeader = new Div();
        /// final var slidingHeader = createAppMarker(); dont add header: slidingHeader
        completeHeader.add(hierarchicalMenu);
        LOGGER.info("Sliding header with hierarchical menu created successfully");
        return completeHeader;
    }

    private Component createUserMenu() {
        LOGGER.debug("Creating user menu for user: {}", currentUser != null ? currentUser.getUsername() : "null");

        final var user = currentUser;
        final var avatar = new Avatar();
        avatar.addThemeVariants(AvatarVariant.LUMO_SMALL); // Changed from XSMALL to SMALL for better visibility
        avatar.addClassNames(Margin.Right.SMALL);
        avatar.setColorIndex(5);

        // Set user name for avatar
        if (user != null) {
            avatar.setName(user.getUsername());
            avatar.setAbbreviation(
                    user.getUsername().length() > 0 ? user.getUsername().substring(0, 1).toUpperCase() : "U");
        }

        // Try to get current user's profile picture
        try {
            final var currentUserOptional = sessionService.getActiveUser();
            if (currentUserOptional.isPresent()) {
                final CUser currentCUser = currentUserOptional.get();
                setAvatarImage(avatar, currentCUser);
            } else {
                LOGGER.debug("No active user found, using default avatar");
            }
        } catch (final Exception e) {
            LOGGER.warn("Error loading user profile picture, using default: {}", e.getMessage());
        }

        final var userMenu = new MenuBar();
        userMenu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        userMenu.addClassNames(Margin.MEDIUM);
        final var userMenuItem = userMenu.addItem(avatar);
        userMenuItem.add(user.getUsername());
        userMenuItem.getSubMenu().addItem("Edit Profile", event -> openUserProfileDialog());
        // TODO Add additional items to the user menu if needed
        userMenuItem.getSubMenu().addItem("Logout", event -> authenticationContext.logout());
        return userMenu;
    }

    /**
     * Sets the avatar image based on the user's profile picture data. This method properly creates a StreamResource for
     * the Avatar component.
     * 
     * @param avatar
     *            The avatar component to update
     * @param user
     *            The user whose profile picture should be displayed
     */
    private void setAvatarImage(final Avatar avatar, final CUser user) {
        LOGGER.debug("Setting avatar image for user: {}", user != null ? user.getLogin() : "null");

        if (user == null) {
            return; // Avatar will use default behavior
        }

        final byte[] profilePictureData = user.getProfilePictureData();

        if (profilePictureData != null && profilePictureData.length > 0) {
            try {
                // Create a StreamResource from the profile picture data
                final StreamResource imageResource = new StreamResource("profile-" + user.getId() + ".jpg",
                        () -> new ByteArrayInputStream(profilePictureData));
                imageResource.setContentType("image/jpeg");

                // Set the image resource to the avatar
                avatar.setImageResource(imageResource);
                LOGGER.debug("Set avatar image from user profile picture data for user: {}", user.getLogin());
                return;
            } catch (final Exception e) {
                LOGGER.warn("Error creating StreamResource from profile picture: {}", e.getMessage());
            }
        }

        // Fall back to user initials if no profile picture is available
        setupAvatarInitials(avatar, user);
        LOGGER.debug("Using initials avatar for user: {}", user.getLogin());
    }

    /**
     * Sets up avatar with user initials when no profile picture is available.
     * 
     * @param avatar
     *            The avatar component to configure
     * @param user
     *            The user whose initials to display
     */
    private void setupAvatarInitials(final Avatar avatar, final CUser user) {
        if (user == null) {
            return;
        }

        String initials = "";

        // Get initials from first name
        if (user.getName() != null && !user.getName().trim().isEmpty()) {
            String[] nameParts = user.getName().trim().split("\\s+");
            for (String part : nameParts) {
                if (!part.isEmpty()) {
                    initials += part.substring(0, 1).toUpperCase();
                    if (initials.length() >= 2)
                        break; // Limit to 2 initials
                }
            }
        }

        // Add last name initial if we have less than 2 initials
        if (user.getLastname() != null && !user.getLastname().trim().isEmpty() && initials.length() < 2) {
            initials += user.getLastname().substring(0, 1).toUpperCase();
        }

        // Fall back to username if no name is available
        if (initials.isEmpty() && user.getLogin() != null && !user.getLogin().trim().isEmpty()) {
            initials = user.getLogin().substring(0, 1).toUpperCase();
        }

        // Final fallback
        if (initials.isEmpty()) {
            initials = "U";
        }

        avatar.setAbbreviation(initials);

        // Set tooltip with full name
        String displayName = "";
        if (user.getName() != null && !user.getName().trim().isEmpty()) {
            displayName = user.getName();
            if (user.getLastname() != null && !user.getLastname().trim().isEmpty()) {
                displayName += " " + user.getLastname();
            }
        } else {
            displayName = user.getLogin();
        }
        avatar.getElement().setAttribute("title", displayName);
    }

    /**
     * Opens the user profile dialog for the current user.
     */
    private void openUserProfileDialog() {
        LOGGER.info("Opening user profile dialog for user: {}",
                currentUser != null ? currentUser.getUsername() : "null");

        try {
            // Get current user from session service
            final var currentUserOptional = sessionService.getActiveUser();

            if (currentUserOptional.isEmpty()) {
                LOGGER.warn("No active user found in session");
                new CWarningDialog("Unable to load user profile. Please try logging in again.").open();
                return;
            }
            final CUser currentCUser = currentUserOptional.get();
            // Create and open profile dialog
            final CUserProfileDialog profileDialog = new CUserProfileDialog(currentCUser, this::saveUserProfile,
                    passwordEncoder);
            profileDialog.open();
            LOGGER.debug("User profile dialog opened successfully");
        } catch (final Exception e) {
            LOGGER.error("Error opening user profile dialog", e);
            new CWarningDialog("Failed to open profile dialog: " + e.getMessage()).open();
        }
    }

    /**
     * Saves the user profile after editing.
     * 
     * @param user
     *            The updated user object
     */
    private void saveUserProfile(final CUser user) {
        LOGGER.info("Saving user profile for user: {}", user != null ? user.getLogin() : "null");

        try {

            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }
            // Save user using user service
            final CUser savedUser = userService.save(user);
            // Update session with saved user
            sessionService.setActiveUser(savedUser);
            LOGGER.info("User profile saved successfully for user: {}", savedUser.getLogin());
        } catch (final Exception e) {
            LOGGER.error("Error saving user profile", e);
            throw new RuntimeException("Failed to save user profile: " + e.getMessage(), e);
        }
    }
}
