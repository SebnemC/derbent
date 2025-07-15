package tech.derbent.taskmanagement.service;

import java.time.Clock;
import java.time.LocalDate;

import org.jspecify.annotations.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.taskmanagement.domain.Task;
import tech.derbent.taskmanagement.domain.TaskRepository;

@Service // TaskService.java
@PreAuthorize("isAuthenticated()") // Ensures that only authenticated users can access the methods of this service
public class TaskService extends CAbstractService<Task> {

	TaskService(final TaskRepository repository, final Clock clock) {
		super(repository, clock);
	}

	@Transactional
	public void createTask(final String description, @Nullable final LocalDate dueDate) {
		if ("fail".equals(description)) {
			throw new RuntimeException("This is for testing the error handler");
		}
		final var task = new Task();
		task.setDescription(description);
		task.setCreationDate(clock.instant());
		task.setDueDate(dueDate);
		repository.saveAndFlush(task);
	}

	@Transactional
	public void createViewClassDebuggingTask() {
		createTask("view class debugging", null);
	}
}
