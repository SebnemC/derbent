package tech.derbent.taskmanagement.ui.view;

import java.time.Clock;

import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.views.CAbstractMDPage;
import tech.derbent.taskmanagement.domain.Task;
import tech.derbent.taskmanagement.service.TaskService;

@Route("tasks/:task_id?/:action?(edit)")
@PageTitle("Task Management")
@Menu(order = 1, icon = "vaadin:edit", title = "Tasks.		Task Details")
@PermitAll
public class TaskDetailView extends CAbstractMDPage<Task> {

	private static final long serialVersionUID = 1L;
	private final String ENTITY_ID_FIELD = "task_id";
	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "tasks/%s/edit";
	private TextField description;
	private DatePicker dueDate;
	private TextField creationDate;
	private Button cancel;
	private Button save;
	private Button delete;
	private final Clock clock;

	public TaskDetailView(final TaskService taskService, final Clock clock) {
		super(Task.class, taskService);
		this.clock = clock;
		addClassNames("task-detail-view");
		binder.bindInstanceFields(this);
	}

	private void createButtonLayout(final Div editorLayoutDiv) {
		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setClassName("button-layout");
		cancel = new Button("Cancel");
		save = new Button("Save");
		delete = new Button("Delete");
		delete.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		
		cancel.addClickListener(e -> {
			clearForm();
			refreshGrid();
		});
		
		delete.addClickListener(e -> {
			if (currentEntity != null) {
				entityService.delete(currentEntity);
				refreshGrid();
				clearForm();
				Notification.show("Task deleted", 3000, Notification.Position.BOTTOM_END)
					.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
			}
		});
		
		save.addClickListener(e -> {
			try {
				if (currentEntity == null) {
					currentEntity = new Task();
					currentEntity.setCreationDate(clock.instant());
				}
				binder.writeBean(currentEntity);
				entityService.save(currentEntity);
				clearForm();
				refreshGrid();
				Notification.show("Task saved", 3000, Notification.Position.BOTTOM_END)
					.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				UI.getCurrent().navigate(TaskDetailView.class);
			} catch (final ObjectOptimisticLockingFailureException exception) {
				final Notification n = Notification.show("Error updating the data. Somebody else has updated the record while you were making changes.");
				n.setPosition(Position.MIDDLE);
				n.addThemeVariants(NotificationVariant.LUMO_ERROR);
			} catch (final ValidationException validationException) {
				Notification.show("Failed to update the data. Check again that all values are valid");
			}
		});
		
		buttonLayout.add(save, cancel, delete);
		editorLayoutDiv.add(buttonLayout);
	}

	@Override
	protected void createDetailsLayout(final SplitLayout splitLayout) {
		final Div editorLayoutDiv = new Div();
		editorLayoutDiv.setClassName("editor-layout");
		final Div editorDiv = new Div();
		editorDiv.setClassName("editor");
		editorLayoutDiv.add(editorDiv);
		
		final FormLayout formLayout = new FormLayout();
		description = new TextField("Description");
		description.setMaxLength(Task.DESCRIPTION_MAX_LENGTH);
		description.setRequiredIndicatorVisible(true);
		
		dueDate = new DatePicker("Due Date");
		
		creationDate = new TextField("Creation Date");
		creationDate.setReadOnly(true);
		
		// Custom binding for creation date to display formatted date
		binder.forField(creationDate)
			.bind(
				task -> task.getCreationDate() != null ? task.getCreationDate().toString() : "",
				(task, value) -> { /* read-only field, no setter needed */ }
			);
		
		formLayout.add(description, dueDate, creationDate);
		editorDiv.add(formLayout);
		createButtonLayout(editorLayoutDiv);
		splitLayout.addToSecondary(editorLayoutDiv);
	}

	@Override
	protected void createGridForEntity() {
		grid.addColumn(Task::getDescription).setHeader("Description").setAutoWidth(true);
		grid.addColumn(task -> task.getDueDate() != null ? task.getDueDate().toString() : "No due date")
			.setHeader("Due Date").setAutoWidth(true);
		grid.addColumn(task -> task.getCreationDate().toString()).setHeader("Creation Date").setAutoWidth(true);
		
		grid.asSingleSelect().addValueChangeListener(event -> {
			if (event.getValue() != null) {
				UI.getCurrent().navigate(String.format(ENTITY_ROUTE_TEMPLATE_EDIT, event.getValue().getId()));
			} else {
				clearForm();
				UI.getCurrent().navigate(TaskDetailView.class);
			}
		});
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
		// Initialize the page components and layout
	}

	@Override
	protected void setupContent() {
		// Setup content if needed
	}

	@Override
	protected void setupToolbar() {
		// Setup toolbar if needed
	}
}