package tech.derbent.gannt.view;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.views.CAbstractEntityDBPage;
import tech.derbent.abstracts.views.grids.CMasterViewSectionBase;
import tech.derbent.gannt.domain.CGanttData;
import tech.derbent.gannt.service.CGanttDataService;
import tech.derbent.gannt.view.components.CGanttChart;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.CSessionService;

public class CMasterViewSectionGannt<EntityClass extends CEntityDB<EntityClass>> extends CMasterViewSectionBase<EntityClass> {

	// --- Custom Event Definition ---
	public static class SelectionChangeEvent<T extends CEntityDB<T>> extends ComponentEvent<CMasterViewSectionGannt<T>> {

		private static final long serialVersionUID = 1L;
		private final T selectedItem;

		public SelectionChangeEvent(final CMasterViewSectionGannt<T> source, final T selectedItem) {
			super(source, false);
			this.selectedItem = selectedItem;
		}

		public T getSelectedItem() { return selectedItem; }
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(CMasterViewSectionGannt.class);
	private static final long serialVersionUID = 1L;
	protected CGanttChart ganttChart;
	private CGanttDataService ganttDataService;
	private CSessionService sessionService;

	public CMasterViewSectionGannt(final Class<EntityClass> entityClass, final CAbstractEntityDBPage<EntityClass> page,
			CGanttDataService ganttDataService, CSessionService sessionService) {
		super(entityClass, page);
		this.ganttDataService = ganttDataService;
		this.sessionService = sessionService;
		LOGGER.debug("Initializing CMasterViewSectionGannt for entity: {}", entityClass.getSimpleName());
		createMasterView();
	}

	@Override
	public void createMasterView() {
		LOGGER.debug("Creating Gantt chart master view");
		ganttChart = new CGanttChart();
		// Load project data for Gantt chart
		refreshMasterView();
		add(ganttChart);
	}

	@Override
	public EntityClass getSelectedItem() {
		LOGGER.debug("Getting selected item from Gantt chart");
		return null; // Gantt chart doesn't have traditional selection
	}

	@SuppressWarnings ("unchecked")
	protected void onSelectionChange(final ValueChangeEvent<?> event) {
		LOGGER.debug("Gantt chart selection changed: {}", event.getValue() != null ? event.getValue().toString() : "null");
		final EntityClass value = (EntityClass) event.getValue();
		fireEvent(new SelectionChangeEvent<>(this, value));
	}

	@Override
	public void refreshMasterView() {
		LOGGER.debug("Refreshing Gantt chart master view");
		if (ganttChart == null) {
			return;
		}
		// Get current project from session (with null safety)
		CProject currentProject = null;
		try {
			if (sessionService != null) {
				currentProject = sessionService.getActiveProject().orElse(null);
			}
		} catch (final Exception e) {
			LOGGER.warn("Error getting active project from session", e);
		}
		if (currentProject == null) {
			LOGGER.debug("No active project, showing empty Gantt chart");
			ganttChart.setGanttData(new CGanttData(null));
			return;
		}
		// Load Gantt data for the current project
		try {
			final CGanttData ganttData;
			if (ganttDataService != null) {
				ganttData = ganttDataService.loadGanttDataForProject(currentProject);
			} else {
				ganttData = new CGanttData(currentProject);
			}
			ganttChart.setGanttData(ganttData);
			LOGGER.debug("Loaded Gantt data for project: {} with {} items", currentProject.getName(), ganttData.size());
		} catch (final Exception e) {
			LOGGER.error("Error loading Gantt data for project: {}", currentProject.getName(), e);
			ganttChart.setGanttData(new CGanttData(currentProject));
		}
	}

	@Override
	public void select(final EntityClass object) {
		LOGGER.debug("Selecting object in Gantt chart: {}", object != null ? object.toString() : "null");
		// Gantt chart doesn't support traditional selection, but we could implement
		// highlighting or scrolling to a specific item in the future
	}

	@Override
	public void selectLastOrFirst(final EntityClass orElse) {
		LOGGER.debug("Selecting last or first in Gantt chart, default: {}", orElse != null ? orElse.toString() : "null");
		// Gantt chart doesn't support traditional selection
	}

	@Override
	public void setDataProvider(final CallbackDataProvider<EntityClass, Void> masterQuery) {
		LOGGER.debug("Setting data provider for Gantt chart");
		// Gantt chart loads its own data through the service, so this is not used
		refreshMasterView();
	}

	@Override
	public void setItems(final List<EntityClass> filteredMeetings) {
		LOGGER.debug("Setting items with {} items for Gantt chart", filteredMeetings != null ? filteredMeetings.size() : 0);
		// Gantt chart loads its own data through the service, so this is not used
		refreshMasterView();
	}
}
