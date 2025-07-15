package tech.derbent.taskmanagement.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import tech.derbent.taskmanagement.service.TaskService;

@Configuration
public class TaskDataInitializer {

	@Bean
	@Profile("demo") // Only run in demo profile to avoid interfering with tests
	CommandLineRunner initializeViewClassDebuggingTask(TaskService taskService) {
		return args -> {
			// Create the "view class debugging" task
			taskService.createViewClassDebuggingTask();
		};
	}
}