package tech.derbent.abstracts.views;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;

import tech.derbent.base.enums.CLayoutMode;

/**
 * CLayoutToggleButton - Button component for toggling between layout modes.
 * Layer: View (MVC)
 * 
 * Extends the base CButton functionality to provide layout mode toggling
 * with appropriate icons and tooltips for horizontal and vertical views.
 */
public class CLayoutToggleButton extends Button {
    
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(CLayoutToggleButton.class);
    
    private CLayoutMode currentMode;
    
    /**
     * Creates a layout toggle button with the specified initial mode.
     * 
     * @param initialMode The initial layout mode to display
     * @param clickListener The click event listener for handling toggle actions
     */
    public CLayoutToggleButton(final CLayoutMode initialMode, 
                              final ComponentEventListener<ClickEvent<Button>> clickListener) {
        super();
        LOGGER.debug("Creating CLayoutToggleButton with initial mode: {}", initialMode);
        
        this.currentMode = initialMode;
        
        // Set button styling
        addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        
        // Update icon and tooltip based on current mode
        updateButtonAppearance();
        
        // Add click listener
        if (clickListener != null) {
            addClickListener(clickListener);
        }
    }
    
    /**
     * Updates the layout mode and refreshes the button appearance.
     * 
     * @param newMode The new layout mode to set
     */
    public void setLayoutMode(final CLayoutMode newMode) {
        if (newMode != null && newMode != this.currentMode) {
            LOGGER.debug("Updating layout toggle button mode from {} to {}", this.currentMode, newMode);
            this.currentMode = newMode;
            updateButtonAppearance();
        }
    }
    
    /**
     * Gets the current layout mode.
     * 
     * @return The current layout mode
     */
    public CLayoutMode getCurrentMode() {
        return currentMode;
    }
    
    /**
     * Updates the button's icon and tooltip based on the current mode.
     * Shows the icon for the current mode with tooltip indicating what clicking will do.
     */
    private void updateButtonAppearance() {
        if (currentMode == null) {
            return;
        }
        
        // Set icon based on current mode
        final Icon icon = new Icon(currentMode.getIconName());
        setIcon(icon);
        
        // Set tooltip to indicate what clicking will do (switch to the other mode)
        final CLayoutMode toggleToMode = currentMode.toggle();
        final String tooltipText = "Switch to " + toggleToMode.getDisplayName();
        setTooltipText(tooltipText);
        
        // Set aria-label for accessibility
        getElement().setAttribute("aria-label", tooltipText);
        
        LOGGER.debug("Updated button appearance for mode: {} with tooltip: {}", currentMode, tooltipText);
    }
}