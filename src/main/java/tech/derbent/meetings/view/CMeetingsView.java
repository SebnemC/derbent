package tech.derbent.meetings.view;

import java.util.HashSet;
import java.util.Set;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.service.CMeetingService;
import tech.derbent.meetings.service.CMeetingTypeService;
import tech.derbent.session.service.SessionService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserService;

@Route("meetings/:meeting_id?/:action?(edit)")
@PageTitle("Meeting Master Detail")
@Menu(order = 3, icon = "vaadin:group", title = "Settings.Meetings")
@PermitAll // When security is enabled, allow all authenticated users
public class CMeetingsView extends CProjectAwareMDPage<CMeeting> {

    private static final long serialVersionUID = 1L;
    private final String ENTITY_ID_FIELD = "meeting_id";
    private final String ENTITY_ROUTE_TEMPLATE_EDIT = "meetings/%s/edit";
    private final CMeetingTypeService meetingTypeService;
    private final CUserService userService;
    
    private MultiSelectComboBox<CUser> participantsField;

    public CMeetingsView(final CMeetingService entityService, final SessionService sessionService, 
                        final CMeetingTypeService meetingTypeService, final CUserService userService) {
        super(CMeeting.class, entityService, sessionService);
        addClassNames("meetings-view");
        this.meetingTypeService = meetingTypeService;
        this.userService = userService;
        // createDetailsLayout();
    }

    @Override
    protected void createDetailsLayout() {
        LOGGER.info("Creating details layout for CMeetingsView with custom participants handling");
        final Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");
        
        // Build the standard form (excluding participants field which is hidden)
        final Div formDiv = CEntityFormBuilder.buildForm(CMeeting.class, getBinder());
        
        // Create and add participants multi-select field manually
        createParticipantsField();
        formDiv.add(participantsField);
        
        editorLayoutDiv.add(formDiv);
        getBaseDetailsLayout().add(editorLayoutDiv);
    }
    
    private void createParticipantsField() {
        LOGGER.debug("Creating participants multi-select field");
        participantsField = new MultiSelectComboBox<>("Participants");
        participantsField.setHelperText("Select users participating in the meeting");
        participantsField.setWidthFull();
        
        // Load users from userService
        try {
            final var users = userService.list(org.springframework.data.domain.Pageable.unpaged());
            participantsField.setItems(users);
            participantsField.setItemLabelGenerator(user -> user.getName() != null ? user.getName() : "User #" + user.getId());
            LOGGER.debug("Loaded {} users for participants selection", users.size());
        } catch (final Exception e) {
            LOGGER.error("Error loading users for participants field: {}", e.getMessage(), e);
            participantsField.setItems();
        }
        
        // Manual binding for participants field with proper type handling
        getBinder().forField(participantsField)
            .withConverter(
                (Set<CUser> selectedUsers) -> selectedUsers != null ? new HashSet<>(selectedUsers) : new HashSet<CUser>(),
                (Set<CUser> participantsSet) -> participantsSet != null ? participantsSet : Set.<CUser>of()
            )
            .bind(CMeeting::getParticipants, CMeeting::setParticipants);
    }

    @Override
    protected void createGridForEntity() {
        LOGGER.info("Creating grid for meetings");
        
        grid.addColumn(meeting -> meeting.getName())
            .setAutoWidth(true)
            .setHeader("Name")
            .setSortable(true);
            
        grid.addColumn(meeting -> meeting.getMeetingType() != null ? meeting.getMeetingType().getName() : "")
            .setAutoWidth(true)
            .setHeader("Type")
            .setSortable(true);
            
        grid.addColumn(meeting -> meeting.getDescription())
            .setAutoWidth(true)
            .setHeader("Description")
            .setSortable(true);
            
        grid.addColumn(meeting -> meeting.getMeetingDate())
            .setAutoWidth(true)
            .setHeader("Start Time")
            .setSortable(true);
            
        grid.addColumn(meeting -> meeting.getEndDate())
            .setAutoWidth(true)
            .setHeader("End Time")
            .setSortable(true);
            
        grid.addColumn(meeting -> meeting.getParticipants().size())
            .setAutoWidth(true)
            .setHeader("Participants")
            .setSortable(true);
    }

    @Override
    protected CMeeting createNewEntityInstance() {
        return new CMeeting();
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
    protected java.util.List<CMeeting> getProjectFilteredData(final tech.derbent.projects.domain.CProject project, final org.springframework.data.domain.Pageable pageable) {
        return ((CMeetingService) entityService).listByProject(project, pageable).getContent();
    }

    @Override
    protected void initPage() {
        // Initialize page components if needed
    }

    @Override
    protected CMeeting newEntity() {
        return super.newEntity(); // Uses the project-aware implementation from parent
    }

    @Override
    protected void setProjectForEntity(final CMeeting entity, final tech.derbent.projects.domain.CProject project) {
        entity.setProject(project);
    }

    @Override
    protected void setupToolbar() {
        // TODO: Implement toolbar setup if needed
    }
}