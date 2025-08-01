package tech.derbent.activities.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;

import tech.derbent.activities.domain.CActivity;

/**
 * CActivityCard - UI component representing a single activity card in the Kanban board.
 * Layer: View (MVC) Displays activity information including name, description/summary,
 * and status. Used within CActivityKanbanColumn to represent individual activities.
 */
public class CActivityCard extends Div {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(CActivityCard.class);

	private final CActivity activity;

	private H4 titleElement;

	private Paragraph descriptionElement;

	private Span statusElement;

	/**
	 * Constructor for CActivityCard.
	 * @param activity the activity to display in the card
	 */
	public CActivityCard(final CActivity activity) {
		LOGGER.debug("Creating CActivityCard for activity: {}",
			activity != null ? activity.getName() : "null");

		if (activity == null) {
			throw new IllegalArgumentException("Activity cannot be null");
		}
		this.activity = activity;
		initializeCard();
	}

	/**
	 * Gets the associated activity.
	 * @return the activity represented by this card
	 */
	public CActivity getActivity() { return activity; }

	/**
	 * Gets activity description or summary for display.
	 * @return description text for the activity
	 */
	private String getActivityDescription() {

		// For now, we'll use a placeholder or derive from activity name This can be
		// enhanced when activity entity has description field
		if ((activity.getName() != null) && (activity.getName().length() > 20)) {
			return activity.getName().substring(0, 17) + "...";
		}
		return activity.getName() != null ? activity.getName() : "No description";
	}

	/**
	 * Initializes the card components and layout.
	 */
	private void initializeCard() {
		LOGGER.debug("Initializing card layout for activity: {}", activity.getName());
		// Set CSS class for styling
		addClassName("activity-card");
		// Create title element
		titleElement =
			new H4(activity.getName() != null ? activity.getName() : "Unnamed Activity");
		titleElement.addClassName("activity-card-title");
		// Create description element
		final String description = getActivityDescription();
		descriptionElement = new Paragraph(description);
		descriptionElement.addClassName("activity-card-description");
		// Create status element if available
		statusElement = new Span();
		statusElement.addClassName("activity-card-status");
		updateStatusElement();
		// Add components to card
		add(titleElement, descriptionElement, statusElement);
		// Add click listener for potential navigation
		addClickListener(event -> {
			LOGGER.debug("Activity card clicked for: {}", activity.getName());
			// Can be extended to navigate to activity details
		});
	}

	/**
	 * Refreshes the card display with updated activity data. Useful for real-time
	 * updates.
	 */
	public void refresh() {
		LOGGER.debug("Refreshing activity card for: {}", activity.getName());

		if (titleElement != null) {
			titleElement.setText(
				activity.getName() != null ? activity.getName() : "Unnamed Activity");
		}

		if (descriptionElement != null) {
			descriptionElement.setText(getActivityDescription());
		}

		if (statusElement != null) {
			updateStatusElement();
		}
	}

	/**
	 * Updates the status element with current activity status.
	 */
	private void updateStatusElement() {

		if ((activity.getStatus() != null) && (activity.getStatus().getName() != null)) {
			statusElement.setText(activity.getStatus().getName());
			statusElement.setVisible(true);
		}
		else {
			statusElement.setText("No Status");
			statusElement.setVisible(true);
		}
	}
}