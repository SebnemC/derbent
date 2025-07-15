package tech.derbent.base.ui.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import jakarta.annotation.security.PermitAll;

/**
 * This view shows up when a user navigates to the root ('/') of the
 * application. this is an empty page!!!!!!!!!!!!!!!!!!!!!!!!!
 */
@Route
@PermitAll // When security is enabled, allow all authenticated users
public final class MainView extends Main {
	// TODO Replace with your own main view.

	private static final long serialVersionUID = 1L;

	/**
	 * Navigates to the main view.
	 */
	public static void showMainView() {
		UI.getCurrent().navigate(MainView.class);
	}

	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

	// now it is a simple view with a message
	MainView() {
		LOGGER.info("Creating MainView");
		addClassName(LumoUtility.Padding.MEDIUM);
		// dont create the main toolbar. it is not needed here add(new
		// ViewToolbar("Main"));
		add(new Div("Hi! Please select a view from the menu on the left."));
	}
}
