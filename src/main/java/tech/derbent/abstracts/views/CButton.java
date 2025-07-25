package tech.derbent.abstracts.views;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;

/**
 * CButton - Abstract base class for all buttons in the application. Layer: View (MVC) Provides common initialization
 * patterns, logging, and standardized button configurations. Extends Vaadin Button with application-specific
 * enhancements.
 */
public class CButton extends Button {

    private static final long serialVersionUID = 1L;
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    /**
     * Default constructor for CButton.
     */
    public CButton() {
        super();
        initializeButton();
    }

    /**
     * Constructor with text.
     * 
     * @param text
     *            the button text
     */
    public CButton(final String text) {
        super(text);
        LOGGER.debug("Creating CButton with text: {}", text);
        initializeButton();
    }

    /**
     * Constructor with text and click listener.
     * 
     * @param text
     *            the button text
     * @param clickListener
     *            the click event listener
     */
    public CButton(final String text, final ComponentEventListener<ClickEvent<Button>> clickListener) {
        super(text, clickListener);
        LOGGER.debug("Creating CButton with text: {} and click listener", text);
        initializeButton();
    }

    /**
     * Constructor with icon.
     * 
     * @param icon
     *            the button icon
     */
    public CButton(final Icon icon) {
        super(icon);
        LOGGER.debug("Creating CButton with icon");
        initializeButton();
    }

    /**
     * Constructor with icon and click listener.
     * 
     * @param icon
     *            the button icon
     * @param clickListener
     *            the click event listener
     */
    public CButton(final Icon icon, final ComponentEventListener<ClickEvent<Button>> clickListener) {
        super(icon, clickListener);
        LOGGER.debug("Creating CButton with icon and click listener");
        initializeButton();
    }

    /**
     * Constructor with text and icon.
     * 
     * @param text
     *            the button text
     * @param icon
     *            the button icon
     */
    public CButton(final String text, final Icon icon) {
        super(text, icon);
        LOGGER.debug("Creating CButton with text: {} and icon", text);
        initializeButton();
    }

    /**
     * Constructor with text, icon and click listener.
     * 
     * @param text
     *            the button text
     * @param icon
     *            the button icon
     * @param clickListener
     *            the click event listener
     */
    public CButton(final String text, final Icon icon, final ComponentEventListener<ClickEvent<Button>> clickListener) {
        super(text, icon, clickListener);
        LOGGER.debug("Creating CButton with text: {}, icon and click listener", text);
        initializeButton();
    }

    /**
     * Creates a primary button with the specified text.
     * 
     * @param text
     *            the button text
     * @return a new CButton with primary styling
     */
    public static CButton createPrimary(final String text) {
        final CButton button = new CButton(text);
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return button;
    }

    /**
     * Creates a primary button with text and click listener.
     * 
     * @param text
     *            the button text
     * @param clickListener
     *            the click event listener
     * @return a new CButton with primary styling
     */
    public static CButton createPrimary(final String text,
            final ComponentEventListener<ClickEvent<Button>> clickListener) {
        final CButton button = new CButton(text, clickListener);
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return button;
    }

    /**
     * Creates a tertiary button with the specified text.
     * 
     * @param text
     *            the button text
     * @return a new CButton with tertiary styling
     */
    public static CButton createTertiary(final String text) {
        final CButton button = new CButton(text);
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        return button;
    }

    /**
     * Creates a tertiary button with text and click listener.
     * 
     * @param text
     *            the button text
     * @param clickListener
     *            the click event listener
     * @return a new CButton with tertiary styling
     */
    public static CButton createTertiary(final String text,
            final ComponentEventListener<ClickEvent<Button>> clickListener) {
        final CButton button = new CButton(text, clickListener);
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        return button;
    }

    /**
     * Creates a tertiary button with text and icon.
     * 
     * @param text
     *            the button text
     * @param icon
     *            the button icon
     * @return a new CButton with tertiary styling and icon
     */
    public static CButton createTertiary(final String text, final Icon icon) {
        final CButton button = new CButton(text, icon);
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        return button;
    }

    /**
     * Creates a tertiary button with text, icon and click listener.
     * 
     * @param text
     *            the button text
     * @param icon
     *            the button icon
     * @param clickListener
     *            the click event listener
     * @return a new CButton with tertiary styling and icon
     */
    public static CButton createTertiary(final String text, final Icon icon,
            final ComponentEventListener<ClickEvent<Button>> clickListener) {
        final CButton button = new CButton(text, icon, clickListener);
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        return button;
    }

    /**
     * Creates an error button with the specified text.
     * 
     * @param text
     *            the button text
     * @return a new CButton with error styling
     */
    public static CButton createError(final String text) {
        final CButton button = new CButton(text);
        button.addThemeVariants(ButtonVariant.LUMO_ERROR);
        return button;
    }

    /**
     * Creates an error button with text and click listener.
     * 
     * @param text
     *            the button text
     * @param clickListener
     *            the click event listener
     * @return a new CButton with error styling
     */
    public static CButton createError(final String text,
            final ComponentEventListener<ClickEvent<Button>> clickListener) {
        final CButton button = new CButton(text, clickListener);
        button.addThemeVariants(ButtonVariant.LUMO_ERROR);
        return button;
    }

    /**
     * Creates a primary button with text and icon.
     * 
     * @param text
     *            the button text
     * @param icon
     *            the button icon
     * @return a new CButton with primary styling and icon
     */
    public static CButton createPrimary(final String text, final Icon icon) {
        final CButton button = new CButton(text, icon);
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return button;
    }

    /**
     * Creates a primary button with text, icon and click listener.
     * 
     * @param text
     *            the button text
     * @param icon
     *            the button icon
     * @param clickListener
     *            the click event listener
     * @return a new CButton with primary styling and icon
     */
    public static CButton createPrimary(final String text, final Icon icon,
            final ComponentEventListener<ClickEvent<Button>> clickListener) {
        final CButton button = new CButton(text, icon, clickListener);
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return button;
    }

    /**
     * Creates an error button with text and icon.
     * 
     * @param text
     *            the button text
     * @param icon
     *            the button icon
     * @return a new CButton with error styling and icon
     */
    public static CButton createError(final String text, final Icon icon) {
        final CButton button = new CButton(text, icon);
        button.addThemeVariants(ButtonVariant.LUMO_ERROR);
        return button;
    }

    /**
     * Creates an error button with text, icon and click listener.
     * 
     * @param text
     *            the button text
     * @param icon
     *            the button icon
     * @param clickListener
     *            the click event listener
     * @return a new CButton with error styling and icon
     */
    public static CButton createError(final String text, final Icon icon,
            final ComponentEventListener<ClickEvent<Button>> clickListener) {
        final CButton button = new CButton(text, icon, clickListener);
        button.addThemeVariants(ButtonVariant.LUMO_ERROR);
        return button;
    }

    /**
     * Common initialization for all CButton instances. This method can be overridden by subclasses to provide
     * additional initialization.
     */
    protected void initializeButton() {
        // Common initialization logic for all buttons
        // This can be extended in the future for application-wide button behaviors
    }
}