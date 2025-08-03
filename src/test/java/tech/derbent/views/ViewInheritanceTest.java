package tech.derbent.views;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import tech.derbent.abstracts.views.CAbstractEntityDBPage;
import tech.derbent.abstracts.views.CAbstractNamedEntityPage;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.activities.view.CActivitiesView;
import tech.derbent.decisions.view.CDecisionsView;
import tech.derbent.orders.view.COrdersView;
import tech.derbent.projects.view.CProjectsView;
import tech.derbent.users.view.CUsersView;

/**
 * ViewInheritanceTest - Tests that all entity views properly inherit save/cancel functionality.
 * 
 * This test verifies that all main entity views extend the correct abstract classes that provide save/cancel
 * functionality without requiring Spring context.
 */
public class ViewInheritanceTest {

    /**
     * Test that all entity views inherit from abstract classes that provide save/cancel functionality.
     */
    @Test
    void testEntityViewInheritance() {
        // Test that CActivitiesView inherits save/cancel functionality
        assertTrue(CProjectAwareMDPage.class.isAssignableFrom(CActivitiesView.class),
                "CActivitiesView should extend CProjectAwareMDPage");
        assertTrue(CAbstractEntityDBPage.class.isAssignableFrom(CActivitiesView.class),
                "CActivitiesView should inherit from CAbstractEntityDBPage");

        // Test that CDecisionsView inherits save/cancel functionality
        assertTrue(CProjectAwareMDPage.class.isAssignableFrom(CDecisionsView.class),
                "CDecisionsView should extend CProjectAwareMDPage");
        assertTrue(CAbstractEntityDBPage.class.isAssignableFrom(CDecisionsView.class),
                "CDecisionsView should inherit from CAbstractEntityDBPage");

        // Test that COrdersView inherits save/cancel functionality
        assertTrue(CProjectAwareMDPage.class.isAssignableFrom(COrdersView.class),
                "COrdersView should extend CProjectAwareMDPage");
        assertTrue(CAbstractEntityDBPage.class.isAssignableFrom(COrdersView.class),
                "COrdersView should inherit from CAbstractEntityDBPage");

        // Test that CProjectsView inherits save/cancel functionality
        assertTrue(CAbstractNamedEntityPage.class.isAssignableFrom(CProjectsView.class),
                "CProjectsView should extend CAbstractNamedEntityPage");
        assertTrue(CAbstractEntityDBPage.class.isAssignableFrom(CProjectsView.class),
                "CProjectsView should inherit from CAbstractEntityDBPage");

        // Test that CUsersView inherits save/cancel functionality
        assertTrue(CAbstractNamedEntityPage.class.isAssignableFrom(CUsersView.class),
                "CUsersView should extend CAbstractNamedEntityPage");
        assertTrue(CAbstractEntityDBPage.class.isAssignableFrom(CUsersView.class),
                "CUsersView should inherit from CAbstractEntityDBPage");
    }
}