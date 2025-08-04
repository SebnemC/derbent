package unit_tests.tech.derbent.activities.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.kanban.view.CActivityKanbanColumn;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/**
 * Unit tests for CActivityKanbanColumn component.
 */
class CActivityKanbanColumnTest extends CTestBase {

	@Override
	protected void setupForTest() {
		// TODO Auto-generated method stub
	}

	@Test
	void testKanbanColumnCreation() {
		// Given
		final CActivityStatus status = new CActivityStatus("TODO", project);
		project.setName("Test Project");
		final CActivity activity1 = new CActivity("Activity 1", project);
		final CActivity activity2 = new CActivity("Activity 2", project);
		final List<CActivity> activities = Arrays.asList(activity1, activity2);
		// When
		final CActivityKanbanColumn column =
			new CActivityKanbanColumn(status, activities);
		// Then
		assertNotNull(column);
		assertEquals(status, column.getActivityStatus());
		assertEquals(2, column.getActivities().size());
	}

	@Test
	void testKanbanColumnWithEmptyActivities() {
		// Given
		final CActivityStatus status = new CActivityStatus("IN_PROGRESS", project);
		final List<CActivity> activities = List.of();
		// When
		final CActivityKanbanColumn column =
			new CActivityKanbanColumn(status, activities);
		// Then
		assertNotNull(column);
		assertEquals(status, column.getActivityStatus());
		assertEquals(0, column.getActivities().size());
	}

	@Test
	void testKanbanColumnWithNullStatusThrowsException() {
		// Given - null status
		final List<CActivity> activities = List.of();
		// When & Then
		assertThrows(IllegalArgumentException.class, () -> {
			new CActivityKanbanColumn(null, activities);
		});
	}

	@Test
	void testRefresh() {
		// Given
		final CActivityStatus status = new CActivityStatus("DONE", project);
		final List<CActivity> activities = List.of();
		final CActivityKanbanColumn column =
			new CActivityKanbanColumn(status, activities);
		// When
		column.refresh();
		// Then - should not throw exception
		assertNotNull(column);
	}

	@Test
	void testUpdateActivities() {
		// Given
		final CActivityStatus status = new CActivityStatus("REVIEW", project);
		project.setName("Test Project");
		final CActivity activity1 = new CActivity("Activity 1", project);
		final List<CActivity> initialActivities = Arrays.asList(activity1);
		final CActivityKanbanColumn column =
			new CActivityKanbanColumn(status, initialActivities);
		// When
		final CActivity activity2 = new CActivity("Activity 2", project);
		final CActivity activity3 = new CActivity("Activity 3", project);
		final List<CActivity> newActivities =
			Arrays.asList(activity1, activity2, activity3);
		column.updateActivities(newActivities);
		// Then
		assertEquals(3, column.getActivities().size());
	}
}