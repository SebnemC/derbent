package tech.derbent.base.ui.dialogs;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;

import tech.derbent.abstracts.views.CButton;
import tech.derbent.abstracts.views.CDialog;

public abstract class CBaseInfoDialog extends CDialog {

	private static final long serialVersionUID = 1L;
	private final String message;
	private final String title;
	private final Icon icon;

	/**
	 * @param title   The dialog title
	 * @param message The message to display to the user
	 */
	public CBaseInfoDialog(final String title, final String message, final Icon icon) {
		super();
		LOGGER.debug("CBaseInfoDialog constructor called for {}", getClass().getSimpleName());
		if (icon == null) {
			throw new IllegalArgumentException("Icon cannot be null");
		}
		this.title = title;
		this.message = message;
		this.icon = icon;
		icon.setColor("var(--lumo-warning-color)");
		setupDialog();// call setupDialog() to initialize the dialog
	}

	@Override
	protected Icon getFormIcon() { return icon; }

	@Override
	protected String getFormTitle() { return title; }

	@Override
	public String getHeaderTitle() { return title; }

	/**
	 * Sets up the OK button.
	 */
	@Override
	protected void setupButtons() {
		final CButton okButton = CButton.createPrimary("OK", e -> close());
		okButton.setAutofocus(true);
		buttonLayout.add(okButton);
	}

	/**
	 * Sets up the dialog content with icon and message.
	 */
	@Override
	protected void setupContent() {
		// Header with icon and title (title already added by CDialog) Message content
		final Div messageDiv = new Div();
		messageDiv.setText(message);
		messageDiv.getStyle().set("text-align", "center");
		messageDiv.getStyle().set("margin", "16px 0");
		mainLayout.add(messageDiv);
	}
}