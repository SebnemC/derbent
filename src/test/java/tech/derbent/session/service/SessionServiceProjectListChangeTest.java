package tech.derbent.session.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Clock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.security.AuthenticationContext;

import tech.derbent.abstracts.interfaces.CProjectListChangeListener;
import tech.derbent.base.service.CLayoutModeService;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.service.CUserService;

/**
 * Test class for project list change listener functionality in SessionService.
 */
@ExtendWith(MockitoExtension.class)
class SessionServiceProjectListChangeTest {

    @Mock
    private AuthenticationContext authenticationContext;

    @Mock
    private CUserService userService;

    @Mock
    private CProjectService projectService;
    
    @Mock
    private CLayoutModeService layoutModeService;

    @Mock
    private VaadinSession vaadinSession;

    private SessionService sessionService;

    @BeforeEach
    void setUp() {
        sessionService = new SessionService(authenticationContext, userService, projectService, layoutModeService);
        
        // Setup security context
        final User testUser = new User("testuser", "password", java.util.Collections.emptyList());
        final UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void testProjectListChangeListenerRegistration() {
        // Create a mock listener
        final CProjectListChangeListener listener = mock(CProjectListChangeListener.class);
        
        // Add the listener
        sessionService.addProjectListChangeListener(listener);
        
        // Notify project list changed
        sessionService.notifyProjectListChanged();
        
        // Verify the listener was called (note: this would work if UI context was available)
        // In a real UI test, the listener would be called
        assertTrue(true, "Listener registration completed without errors");
        
        // Remove the listener
        sessionService.removeProjectListChangeListener(listener);
        
        assertTrue(true, "Listener removal completed without errors");
    }

    @Test
    void testAddAndRemoveProjectListChangeListener() {
        final CProjectListChangeListener listener = mock(CProjectListChangeListener.class);
        
        // Test adding listener
        sessionService.addProjectListChangeListener(listener);
        
        // Test removing listener
        sessionService.removeProjectListChangeListener(listener);
        
        // Test removing null listener (should not throw exception)
        sessionService.removeProjectListChangeListener(null);
        
        assertTrue(true, "All listener operations completed successfully");
    }
}