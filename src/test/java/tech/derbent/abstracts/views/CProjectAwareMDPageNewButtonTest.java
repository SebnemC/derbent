package tech.derbent.abstracts.views;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.vaadin.flow.component.html.Div;

import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.SessionService;

/**
 * Test class to verify the "New" button behavior in CProjectAwareMDPage
 * Tests the project-aware functionality for Activities and similar entities
 */
public class CProjectAwareMDPageNewButtonTest {

    @Mock
    private CActivityService activityService;

    @Mock
    private SessionService sessionService;

    @Mock
    private Clock clock;

    private TestActivityMDPage testPage;
    private CProject testProject;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Create test project
        testProject = new CProject();
        testProject.setName("Test Project");
        
        // Setup mock service behavior
        when(activityService.list(any(Pageable.class))).thenReturn(new ArrayList<>());
        when(activityService.count()).thenReturn(0);
        when(sessionService.getActiveProject()).thenReturn(Optional.of(testProject));
        
        testPage = new TestActivityMDPage(activityService, sessionService);
    }

    @Test
    public void testNewButtonCreatesActivityWithProject() {
        // Given: page starts with no current entity and an active project
        testPage.setCurrentEntity(null);
        
        // When: New button is clicked
        CButton newButton = testPage.createNewButton("New");
        newButton.click();
        
        // Then: a new activity should be created with the active project set
        assertNotNull(testPage.getCurrentEntity(), "Current entity should not be null after clicking New button");
        assertNull(testPage.getCurrentEntity().getId(), "New entity should not have an ID yet");
        assertEquals(testProject, testPage.getCurrentEntity().getProject(), "New activity should have the active project set");
    }

    @Test
    public void testNewEntityCreationWithProject() {
        // When: newEntity() is called
        CActivity newActivity = testPage.newEntity();
        
        // Then: a new activity with project should be created
        assertNotNull(newActivity, "newEntity() should return a non-null instance");
        assertNull(newActivity.getId(), "New entity should not have an ID");
        assertEquals(testProject, newActivity.getProject(), "New activity should have the active project set");
    }

    @Test
    public void testNewEntityCreationWithoutActiveProject() {
        // Given: no active project
        when(sessionService.getActiveProject()).thenReturn(Optional.empty());
        
        // When: newEntity() is called
        CActivity newActivity = testPage.newEntity();
        
        // Then: a new activity should be created but without project
        assertNotNull(newActivity, "newEntity() should return a non-null instance");
        assertNull(newActivity.getId(), "New entity should not have an ID");
        assertNull(newActivity.getProject(), "New activity should not have a project when none is active");
    }

    /**
     * Test implementation of CProjectAwareMDPage for Activities
     */
    private static class TestActivityMDPage extends CProjectAwareMDPage<CActivity> {
        
        public TestActivityMDPage(CAbstractService<CActivity> entityService, SessionService sessionService) {
            super(CActivity.class, entityService, sessionService);
        }

        @Override
        protected void createDetailsLayout() {
            // Simple test implementation
            getBaseDetailsLayout().add(new Div("Test Activity Details"));
        }

        @Override
        protected CActivity createNewEntityInstance() {
            return new CActivity();
        }

        @Override
        protected void createGridForEntity() {
            // Simple test implementation
            grid.addColumn(CActivity::getName).setHeader("Activity Name");
        }

        @Override
        protected String getEntityRouteIdField() {
            return "activity_id";
        }

        @Override
        protected String getEntityRouteTemplateEdit() {
            return "activities/%s/edit";
        }

        @Override
        protected List<CActivity> getProjectFilteredData(CProject project, Pageable pageable) {
            // Mock implementation - return empty list for testing
            return Collections.emptyList();
        }

        @Override
        protected void initPage() {
            // Test implementation
        }

        @Override
        protected void setProjectForEntity(CActivity entity, CProject project) {
            entity.setProject(project);
        }

        @Override
        protected void setupToolbar() {
            // Test implementation
        }
    }
}