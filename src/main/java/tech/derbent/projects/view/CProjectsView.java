package tech.derbent.projects.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.views.CAbstractMDPage;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;

/**
 * CProjectsView - View for managing projects. Layer: View (MVC) Provides CRUD operations for projects using the
 * abstract master-detail pattern with multiple layout options.
 */
@Route("projects/:project_id?/:action?(edit)")
@PageTitle("Project Master Detail")
@Menu(order = 0, icon = "vaadin:briefcase", title = "Settings.Projects")
@PermitAll // When security is enabled, allow all authenticated users
public class CProjectsView extends CAbstractMDPage<CProject> {

    private static final long serialVersionUID = 1L;
    private final String ENTITY_ID_FIELD = "project_id";
    private final String ENTITY_ROUTE_TEMPLATE_EDIT = "projects/%s/edit";
    
    // Layout modes enum
    public enum LayoutMode {
        ENHANCED_CARDS("Enhanced Cards", "layout-enhanced-cards"),
        KANBAN_BOARD("Kanban Board", "layout-kanban-board"),
        CARD_GRID("Card Grid", "layout-card-grid"),
        COMPACT_SIDEBAR("Compact Sidebar", "layout-compact-sidebar"),
        DASHBOARD_WIDGETS("Dashboard Widgets", "layout-dashboard-widgets"),
        TIMELINE_VIEW("Timeline View", "layout-timeline-view");
        
        private final String displayName;
        private final String cssClass;
        
        LayoutMode(String displayName, String cssClass) {
            this.displayName = displayName;
            this.cssClass = cssClass;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getCssClass() {
            return cssClass;
        }
    }
    
    private LayoutMode currentLayoutMode = LayoutMode.ENHANCED_CARDS;
    private Select<LayoutMode> layoutSelector;

    public CProjectsView(final CProjectService entityService) {
        super(CProject.class, entityService);
        addClassNames("projects-view");
        // Apply default layout mode CSS class
        addClassName(currentLayoutMode.getCssClass());
        setupLayoutSelector();
        
        // Add layout selector to the view header
        final HorizontalLayout layoutSelectorWrapper = new HorizontalLayout();
        layoutSelectorWrapper.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        layoutSelectorWrapper.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        layoutSelectorWrapper.setWidthFull();
        layoutSelectorWrapper.setPadding(true);
        layoutSelectorWrapper.add(new Span("Layout Style:"), layoutSelector);
        
        // Add to the beginning of the view
        getElement().insertChild(0, layoutSelectorWrapper.getElement());
        
        LOGGER.info("CProjectsView initialized successfully with multiple layout options");
    }

    @Override
    protected void createDetailsLayout() {
        LOGGER.info("Creating details layout for mode: " + (currentLayoutMode != null ? currentLayoutMode.getDisplayName() : "Default"));
        
        // Clear previous layout
        getBaseDetailsLayout().removeAll();
        
        // Default to enhanced cards if currentLayoutMode is null
        LayoutMode layoutToUse = currentLayoutMode != null ? currentLayoutMode : LayoutMode.ENHANCED_CARDS;
        
        switch (layoutToUse) {
            case ENHANCED_CARDS:
                createEnhancedCardsLayout();
                break;
            case KANBAN_BOARD:
                createKanbanBoardLayout();
                break;
            case CARD_GRID:
                createCardGridLayout();
                break;
            case COMPACT_SIDEBAR:
                createCompactSidebarLayout();
                break;
            case DASHBOARD_WIDGETS:
                createDashboardWidgetsLayout();
                break;
            case TIMELINE_VIEW:
                createTimelineViewLayout();
                break;
            default:
                createEnhancedCardsLayout();
        }
    }

    @Override
    protected Div createDetailsTabLeftContent() {
        // Create enhanced tab content with icon
        final Div detailsTabLabel = new Div();
        detailsTabLabel.setClassName("details-tab-label");
        
        final Icon projectIcon = new Icon(VaadinIcon.BRIEFCASE);
        final Span labelText = new Span("Project Information");
        
        detailsTabLabel.add(projectIcon, labelText);
        return detailsTabLabel;
    }

    @Override
    protected void createGridForEntity() {
        LOGGER.info("Creating grid for layout mode: " + (currentLayoutMode != null ? currentLayoutMode.getDisplayName() : "Default"));
        
        // Apply layout-specific CSS class (handle null case)
        removeClassNames("layout-enhanced-cards", "layout-kanban-board", "layout-card-grid", 
                         "layout-compact-sidebar", "layout-dashboard-widgets", "layout-timeline-view");
        if (currentLayoutMode != null) {
            addClassName(currentLayoutMode.getCssClass());
        }
        
        // Default to enhanced cards if currentLayoutMode is null
        LayoutMode layoutToUse = currentLayoutMode != null ? currentLayoutMode : LayoutMode.ENHANCED_CARDS;
        
        switch (layoutToUse) {
            case KANBAN_BOARD:
                createKanbanGrid();
                break;
            case CARD_GRID:
                createCardGridView();
                break;
            case COMPACT_SIDEBAR:
                createCompactSidebarGrid();
                break;
            case DASHBOARD_WIDGETS:
                createDashboardGrid();
                break;
            case TIMELINE_VIEW:
                createTimelineGrid();
                break;
            default:
                createEnhancedGrid();
        }
    }

    @Override
    protected String getEntityRouteIdField() {
        return ENTITY_ID_FIELD;
    }

    @Override
    protected String getEntityRouteTemplateEdit() {
        return ENTITY_ROUTE_TEMPLATE_EDIT;
    }

    @Override
    protected void initPage() {
        // Initialize the page components and layout This method can be overridden to
        // set up the view's components
    }

    @Override
    protected CProject newEntity() {
        return new CProject();
    }

    @Override
    protected void setupToolbar() {
        // Add layout selector to toolbar
        if (layoutSelector != null) {
            final HorizontalLayout toolbarLayout = new HorizontalLayout();
            toolbarLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
            toolbarLayout.add(new Span("Layout:"), layoutSelector);
            
            // If there's an existing toolbar, add to it, otherwise create new one
            // This depends on the parent class implementation
            LOGGER.info("Layout selector added to toolbar");
        }
    }
    
    /**
     * Setup layout selector dropdown
     */
    private void setupLayoutSelector() {
        layoutSelector = new Select<>();
        layoutSelector.setItems(LayoutMode.values());
        layoutSelector.setValue(currentLayoutMode);
        layoutSelector.setItemLabelGenerator(LayoutMode::getDisplayName);
        layoutSelector.addValueChangeListener(event -> {
            if (event.getValue() != null && event.getValue() != currentLayoutMode) {
                currentLayoutMode = event.getValue();
                refreshLayout();
            }
        });
        layoutSelector.setWidth("200px");
    }
    
    /**
     * Refresh the entire layout when switching modes
     */
    private void refreshLayout() {
        LOGGER.info("Switching to layout mode: " + currentLayoutMode.getDisplayName());
        createGridForEntity();
        createDetailsLayout();
        refreshGrid();
    }
    
    /**
     * Layout Option 1: Enhanced Cards (Current Implementation)
     */
    private void createEnhancedCardsLayout() {
        // Create main form wrapper with card styling
        final VerticalLayout formWrapper = new VerticalLayout();
        formWrapper.setClassName("details-form-card");
        formWrapper.setSpacing(true);
        formWrapper.setPadding(false);
        formWrapper.setSizeFull();
        
        // Create form header with icon
        final H3 formHeader = new H3();
        formHeader.add(new Icon(VaadinIcon.FOLDER), new Span("Project Details"));
        formWrapper.add(formHeader);
        
        // Build the form using the existing form builder
        final Div formContent = CEntityFormBuilder.buildForm(CProject.class, getBinder());
        formWrapper.add(formContent);
        
        // Add additional project information section
        final Div projectInfoCard = createProjectInfoCard();
        formWrapper.add(projectInfoCard);
        
        getBaseDetailsLayout().add(formWrapper);
    }
    
    /**
     * Layout Option 2: Kanban Board Style
     */
    private void createKanbanBoardLayout() {
        final VerticalLayout kanbanLayout = new VerticalLayout();
        kanbanLayout.setClassName("kanban-layout");
        kanbanLayout.setSizeFull();
        
        // Kanban header
        final H3 kanbanHeader = new H3();
        kanbanHeader.add(new Icon(VaadinIcon.DASHBOARD), new Span("Project Kanban Board"));
        kanbanLayout.add(kanbanHeader);
        
        // Kanban columns
        final HorizontalLayout columnsLayout = new HorizontalLayout();
        columnsLayout.setClassName("kanban-columns");
        columnsLayout.setSizeFull();
        
        // Active Projects Column
        final VerticalLayout activeColumn = createKanbanColumn("Active Projects", VaadinIcon.CHECK_CIRCLE, "kanban-active");
        final VerticalLayout planningColumn = createKanbanColumn("In Planning", VaadinIcon.CLOCK, "kanban-planning");
        final VerticalLayout completedColumn = createKanbanColumn("Completed", VaadinIcon.CHECK_SQUARE, "kanban-completed");
        
        columnsLayout.add(activeColumn, planningColumn, completedColumn);
        kanbanLayout.add(columnsLayout);
        
        // Add form at bottom
        final Div formContent = CEntityFormBuilder.buildForm(CProject.class, getBinder());
        kanbanLayout.add(formContent);
        
        getBaseDetailsLayout().add(kanbanLayout);
    }
    
    /**
     * Layout Option 3: Card Grid Layout
     */
    private void createCardGridLayout() {
        final VerticalLayout gridLayout = new VerticalLayout();
        gridLayout.setClassName("card-grid-layout");
        gridLayout.setSizeFull();
        
        // Grid header with statistics
        final HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setClassName("card-grid-header");
        headerLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        
        final H3 gridHeader = new H3();
        gridHeader.add(new Icon(VaadinIcon.GRID), new Span("Project Gallery"));
        
        final HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.add(
            createStatCard("Total Projects", "12", VaadinIcon.BRIEFCASE),
            createStatCard("Active", "8", VaadinIcon.CHECK_CIRCLE),
            createStatCard("Completed", "4", VaadinIcon.CHECK_SQUARE)
        );
        
        headerLayout.add(gridHeader, statsLayout);
        gridLayout.add(headerLayout);
        
        // Project cards grid (will be populated via custom grid)
        final Div cardsContainer = new Div();
        cardsContainer.setClassName("project-cards-container");
        gridLayout.add(cardsContainer);
        
        // Add form at bottom
        final Div formContent = CEntityFormBuilder.buildForm(CProject.class, getBinder());
        gridLayout.add(formContent);
        
        getBaseDetailsLayout().add(gridLayout);
    }
    
    /**
     * Layout Option 4: Compact Sidebar Navigation
     */
    private void createCompactSidebarLayout() {
        final HorizontalLayout compactLayout = new HorizontalLayout();
        compactLayout.setClassName("compact-sidebar-layout");
        compactLayout.setSizeFull();
        
        // Compact project list sidebar
        final VerticalLayout sidebar = new VerticalLayout();
        sidebar.setClassName("compact-sidebar");
        sidebar.setWidth("300px");
        
        final H4 sidebarHeader = new H4("Projects");
        sidebarHeader.add(new Icon(VaadinIcon.LIST));
        sidebar.add(sidebarHeader);
        
        // Main content area
        final VerticalLayout mainContent = new VerticalLayout();
        mainContent.setClassName("compact-main-content");
        mainContent.setSizeFull();
        
        // Content header
        final H2 contentHeader = new H2();
        contentHeader.add(new Icon(VaadinIcon.FILE_TEXT), new Span("Project Details"));
        mainContent.add(contentHeader);
        
        // Add form
        final Div formContent = CEntityFormBuilder.buildForm(CProject.class, getBinder());
        mainContent.add(formContent);
        
        compactLayout.add(sidebar, mainContent);
        getBaseDetailsLayout().add(compactLayout);
    }
    
    /**
     * Layout Option 5: Dashboard with Widgets
     */
    private void createDashboardWidgetsLayout() {
        final VerticalLayout dashboardLayout = new VerticalLayout();
        dashboardLayout.setClassName("dashboard-widgets-layout");
        dashboardLayout.setSizeFull();
        
        // Dashboard header
        final H3 dashboardHeader = new H3();
        dashboardHeader.add(new Icon(VaadinIcon.DASHBOARD), new Span("Project Dashboard"));
        dashboardLayout.add(dashboardHeader);
        
        // Widgets row
        final HorizontalLayout widgetsRow = new HorizontalLayout();
        widgetsRow.setClassName("dashboard-widgets");
        widgetsRow.setSizeFull();
        
        // Create dashboard widgets
        widgetsRow.add(
            createDashboardWidget("Active Projects", "8", VaadinIcon.PLAY, "widget-active"),
            createDashboardWidget("Total Tasks", "156", VaadinIcon.TASKS, "widget-tasks"),
            createDashboardWidget("Completion Rate", "85%", VaadinIcon.CHART, "widget-completion"),
            createDashboardWidget("Team Members", "12", VaadinIcon.USERS, "widget-team")
        );
        
        dashboardLayout.add(widgetsRow);
        
        // Charts and graphs section
        final HorizontalLayout chartsSection = new HorizontalLayout();
        chartsSection.setClassName("dashboard-charts");
        chartsSection.setSizeFull();
        
        final Div chartPlaceholder = new Div();
        chartPlaceholder.setClassName("chart-placeholder");
        chartPlaceholder.add(new Icon(VaadinIcon.CHART_LINE), new Span("Project Progress Chart"));
        chartsSection.add(chartPlaceholder);
        
        dashboardLayout.add(chartsSection);
        
        // Add form
        final Div formContent = CEntityFormBuilder.buildForm(CProject.class, getBinder());
        dashboardLayout.add(formContent);
        
        getBaseDetailsLayout().add(dashboardLayout);
    }
    
    /**
     * Layout Option 6: Timeline/Activity View
     */
    private void createTimelineViewLayout() {
        final VerticalLayout timelineLayout = new VerticalLayout();
        timelineLayout.setClassName("timeline-layout");
        timelineLayout.setSizeFull();
        
        // Timeline header
        final H3 timelineHeader = new H3();
        timelineHeader.add(new Icon(VaadinIcon.CLOCK), new Span("Project Timeline"));
        timelineLayout.add(timelineHeader);
        
        // Timeline container
        final VerticalLayout timelineContainer = new VerticalLayout();
        timelineContainer.setClassName("timeline-container");
        
        // Add timeline items
        timelineContainer.add(
            createTimelineItem("Project Created", "Derbent Project was created", "2025-01-01", VaadinIcon.PLUS),
            createTimelineItem("Status Update", "Project marked as active", "2025-01-15", VaadinIcon.CHECK),
            createTimelineItem("Team Assignment", "Team members assigned", "2025-01-20", VaadinIcon.USERS),
            createTimelineItem("Milestone Reached", "First milestone completed", "2025-02-01", VaadinIcon.FLAG)
        );
        
        timelineLayout.add(timelineContainer);
        
        // Add form
        final Div formContent = CEntityFormBuilder.buildForm(CProject.class, getBinder());
        timelineLayout.add(formContent);
        
        getBaseDetailsLayout().add(timelineLayout);
    }
    
    /**
     * Create enhanced grid (default layout)
     */
    private void createEnhancedGrid() {
        // Configure grid columns with better styling
        grid.addComponentColumn(this::createProjectRowComponent)
            .setAutoWidth(true)
            .setHeader("Projects")
            .setSortable(false);
            
        // Add row styling
        grid.setClassNameGenerator(project -> "project-row");
        
        // Enhanced selection listener
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(ENTITY_ROUTE_TEMPLATE_EDIT, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(CProjectsView.class);
            }
        });
    }
    
    /**
     * Create kanban-style grid
     */
    private void createKanbanGrid() {
        grid.addComponentColumn(this::createKanbanProjectCard)
            .setAutoWidth(true)
            .setHeader("Project Cards")
            .setSortable(false);
            
        grid.setClassNameGenerator(project -> "kanban-project-row");
        setupGridSelectionListener();
    }
    
    /**
     * Create card grid view
     */
    private void createCardGridView() {
        grid.addComponentColumn(this::createLargeProjectCard)
            .setAutoWidth(true)
            .setHeader("Project Gallery")
            .setSortable(false);
            
        grid.setClassNameGenerator(project -> "card-grid-project-row");
        setupGridSelectionListener();
    }
    
    /**
     * Create compact sidebar grid
     */
    private void createCompactSidebarGrid() {
        grid.addComponentColumn(this::createCompactProjectItem)
            .setAutoWidth(true)
            .setHeader("Projects")
            .setSortable(false);
            
        grid.setClassNameGenerator(project -> "compact-project-row");
        setupGridSelectionListener();
    }
    
    /**
     * Create dashboard grid
     */
    private void createDashboardGrid() {
        grid.addComponentColumn(this::createDashboardProjectWidget)
            .setAutoWidth(true)
            .setHeader("Project Widgets")
            .setSortable(false);
            
        grid.setClassNameGenerator(project -> "dashboard-project-row");
        setupGridSelectionListener();
    }
    
    /**
     * Create timeline grid
     */
    private void createTimelineGrid() {
        grid.addComponentColumn(this::createTimelineProjectItem)
            .setAutoWidth(true)
            .setHeader("Project Timeline")
            .setSortable(false);
            
        grid.setClassNameGenerator(project -> "timeline-project-row");
        setupGridSelectionListener();
    }
    
    /**
     * Common grid selection listener setup
     */
    private void setupGridSelectionListener() {
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(ENTITY_ROUTE_TEMPLATE_EDIT, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(CProjectsView.class);
            }
        });
    }
    /**
     * Creates an enhanced project row component with icon and metadata
     * @param project The project to display
     * @return HorizontalLayout containing the project information
     */
    private HorizontalLayout createProjectRowComponent(final CProject project) {
        final HorizontalLayout card = new HorizontalLayout();
        card.setClassName("project-info-card");
        card.setDefaultVerticalComponentAlignment(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
        card.setSizeFull();
        
        // Project icon
        final Div projectIcon = new Div();
        projectIcon.setClassName("project-icon");
        projectIcon.setText(project.getName() != null && !project.getName().isEmpty() 
            ? project.getName().substring(0, 1).toUpperCase() 
            : "P");
        
        // Project details
        final VerticalLayout projectDetails = new VerticalLayout();
        projectDetails.setClassName("project-details");
        projectDetails.setSpacing(false);
        projectDetails.setPadding(false);
        
        final Span projectName = new Span(project.getName() != null ? project.getName() : "Unnamed Project");
        projectName.setClassName("project-name");
        
        final Span projectMeta = new Span("ID: " + project.getId());
        projectMeta.setClassName("project-meta");
        
        projectDetails.add(projectName, projectMeta);
        
        card.add(projectIcon, projectDetails);
        return card;
    }
    
    /**
     * Create kanban project card
     */
    private Div createKanbanProjectCard(final CProject project) {
        final Div card = new Div();
        card.setClassName("kanban-project-card");
        
        final Div header = new Div();
        header.setClassName("kanban-card-header");
        header.add(new Span(project.getName() != null ? project.getName() : "Unnamed Project"));
        
        final Div content = new Div();
        content.setClassName("kanban-card-content");
        content.add(new Span("ID: " + project.getId()));
        
        final Div footer = new Div();
        footer.setClassName("kanban-card-footer");
        footer.add(new Span("Active"));
        
        card.add(header, content, footer);
        return card;
    }
    
    /**
     * Create large project card for grid layout
     */
    private Div createLargeProjectCard(final CProject project) {
        final Div card = new Div();
        card.setClassName("large-project-card");
        
        final Div cardHeader = new Div();
        cardHeader.setClassName("large-card-header");
        
        final Icon icon = new Icon(VaadinIcon.BRIEFCASE);
        final H4 title = new H4(project.getName() != null ? project.getName() : "Unnamed Project");
        cardHeader.add(icon, title);
        
        final Div cardContent = new Div();
        cardContent.setClassName("large-card-content");
        cardContent.add(
            new Span("Project ID: " + project.getId()),
            new Span("Status: Active"),
            new Span("Created: " + java.time.LocalDate.now().toString())
        );
        
        final Div cardActions = new Div();
        cardActions.setClassName("large-card-actions");
        final Button viewButton = new Button("View", new Icon(VaadinIcon.EYE));
        viewButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        cardActions.add(viewButton);
        
        card.add(cardHeader, cardContent, cardActions);
        return card;
    }
    
    /**
     * Create compact project item for sidebar
     */
    private HorizontalLayout createCompactProjectItem(final CProject project) {
        final HorizontalLayout item = new HorizontalLayout();
        item.setClassName("compact-project-item");
        item.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        
        final Div statusDot = new Div();
        statusDot.setClassName("status-dot status-active");
        
        final VerticalLayout details = new VerticalLayout();
        details.setSpacing(false);
        details.setPadding(false);
        
        final Span name = new Span(project.getName() != null ? project.getName() : "Unnamed Project");
        name.setClassName("compact-project-name");
        
        final Span id = new Span("ID: " + project.getId());
        id.setClassName("compact-project-id");
        
        details.add(name, id);
        item.add(statusDot, details);
        
        return item;
    }
    
    /**
     * Create dashboard project widget
     */
    private Div createDashboardProjectWidget(final CProject project) {
        final Div widget = new Div();
        widget.setClassName("dashboard-project-widget");
        
        final HorizontalLayout header = new HorizontalLayout();
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.add(
            new Icon(VaadinIcon.BRIEFCASE),
            new H4(project.getName() != null ? project.getName() : "Unnamed Project")
        );
        
        final Div metrics = new Div();
        metrics.setClassName("widget-metrics");
        metrics.add(
            createMetric("Progress", "75%"),
            createMetric("Tasks", "12/16"),
            createMetric("Team", "4 members")
        );
        
        widget.add(header, metrics);
        return widget;
    }
    
    /**
     * Create timeline project item
     */
    private HorizontalLayout createTimelineProjectItem(final CProject project) {
        final HorizontalLayout item = new HorizontalLayout();
        item.setClassName("timeline-project-item");
        item.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        
        final Div timelineDot = new Div();
        timelineDot.setClassName("timeline-dot");
        
        final VerticalLayout content = new VerticalLayout();
        content.setSpacing(false);
        content.setPadding(false);
        
        final H4 projectName = new H4(project.getName() != null ? project.getName() : "Unnamed Project");
        final Span timestamp = new Span("Created: " + java.time.LocalDate.now().toString());
        timestamp.setClassName("timeline-timestamp");
        
        content.add(projectName, timestamp);
        item.add(timelineDot, content);
        
        return item;
    }
    
    /**
     * Helper method to create kanban column
     */
    private VerticalLayout createKanbanColumn(String title, VaadinIcon icon, String className) {
        final VerticalLayout column = new VerticalLayout();
        column.setClassName("kanban-column " + className);
        column.setWidth("33%");
        
        final HorizontalLayout header = new HorizontalLayout();
        header.setClassName("kanban-column-header");
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.add(new Icon(icon), new H4(title));
        
        column.add(header);
        return column;
    }
    
    /**
     * Helper method to create stat card
     */
    private Div createStatCard(String title, String value, VaadinIcon icon) {
        final Div card = new Div();
        card.setClassName("stat-card");
        
        final Icon cardIcon = new Icon(icon);
        final H4 cardValue = new H4(value);
        final Span cardTitle = new Span(title);
        
        card.add(cardIcon, cardValue, cardTitle);
        return card;
    }
    
    /**
     * Helper method to create dashboard widget
     */
    private Div createDashboardWidget(String title, String value, VaadinIcon icon, String className) {
        final Div widget = new Div();
        widget.setClassName("dashboard-widget " + className);
        
        final Icon widgetIcon = new Icon(icon);
        final H3 widgetValue = new H3(value);
        final Span widgetTitle = new Span(title);
        
        widget.add(widgetIcon, widgetValue, widgetTitle);
        return widget;
    }
    
    /**
     * Helper method to create timeline item
     */
    private HorizontalLayout createTimelineItem(String title, String description, String date, VaadinIcon icon) {
        final HorizontalLayout item = new HorizontalLayout();
        item.setClassName("timeline-item");
        item.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.START);
        
        final Div timelineIcon = new Div();
        timelineIcon.setClassName("timeline-icon");
        timelineIcon.add(new Icon(icon));
        
        final VerticalLayout content = new VerticalLayout();
        content.setSpacing(false);
        content.setPadding(false);
        
        final H4 itemTitle = new H4(title);
        final Span itemDescription = new Span(description);
        final Span itemDate = new Span(date);
        itemDate.setClassName("timeline-date");
        
        content.add(itemTitle, itemDescription, itemDate);
        item.add(timelineIcon, content);
        
        return item;
    }
    
    /**
     * Helper method to create metric
     */
    private Div createMetric(String label, String value) {
        final Div metric = new Div();
        metric.setClassName("metric");
        
        final Span metricLabel = new Span(label);
        metricLabel.setClassName("metric-label");
        final Span metricValue = new Span(value);
        metricValue.setClassName("metric-value");
        
        metric.add(metricLabel, metricValue);
        return metric;
    }
    
    /**
     * Creates additional project information card
     * @return Div containing project statistics and info
     */
    private Div createProjectInfoCard() {
        final Div infoCard = new Div();
        infoCard.setClassName("details-form-card");
        
        final H3 infoHeader = new H3();
        infoHeader.add(new Icon(VaadinIcon.INFO_CIRCLE), new Span("Project Statistics"));
        
        final Div statsContent = new Div();
        final Span statusIndicator = new Span("Active");
        statusIndicator.setClassName("status-indicator status-active");
        
        final VerticalLayout stats = new VerticalLayout();
        stats.setSpacing(false);
        stats.setPadding(false);
        stats.add(
            new Span("Status: "),
            statusIndicator,
            new Span("Created: " + java.time.LocalDate.now().toString()),
            new Span("Last modified: " + java.time.LocalDate.now().toString())
        );
        
        statsContent.add(stats);
        infoCard.add(infoHeader, statsContent);
        
        return infoCard;
    }
}
