package tech.derbent.layout.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.vaadin.flow.server.VaadinSession;

/**
 * Service to manage layout state (horizontal vs vertical) per user session.
 * Uses Vaadin session to persist the layout preference.
 */
@Service
public class LayoutStateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LayoutStateService.class);
    private static final String LAYOUT_MODE_KEY = "layoutMode";
    
    public enum LayoutMode {
        HORIZONTAL, VERTICAL
    }

    /**
     * Gets the current layout mode for the session.
     * Defaults to VERTICAL if not set.
     * 
     * @return the current layout mode
     */
    public LayoutMode getLayoutMode() {
        final VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            final LayoutMode mode = (LayoutMode) session.getAttribute(LAYOUT_MODE_KEY);
            return mode != null ? mode : LayoutMode.VERTICAL; // Default to vertical
        }
        return LayoutMode.VERTICAL;
    }

    /**
     * Sets the layout mode for the current session.
     * 
     * @param mode the layout mode to set
     */
    public void setLayoutMode(final LayoutMode mode) {
        final VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute(LAYOUT_MODE_KEY, mode);
            LOGGER.debug("Layout mode set to: {}", mode);
        }
    }

    /**
     * Toggles between horizontal and vertical layout modes.
     * 
     * @return the new layout mode after toggling
     */
    public LayoutMode toggleLayoutMode() {
        final LayoutMode currentMode = getLayoutMode();
        final LayoutMode newMode = currentMode == LayoutMode.HORIZONTAL ? LayoutMode.VERTICAL : LayoutMode.HORIZONTAL;
        setLayoutMode(newMode);
        LOGGER.info("Layout mode toggled from {} to {}", currentMode, newMode);
        return newMode;
    }

    /**
     * Checks if the current layout mode is horizontal.
     * 
     * @return true if horizontal, false if vertical
     */
    public boolean isHorizontalMode() {
        return getLayoutMode() == LayoutMode.HORIZONTAL;
    }

    /**
     * Checks if the current layout mode is vertical.
     * 
     * @return true if vertical, false if horizontal
     */
    public boolean isVerticalMode() {
        return getLayoutMode() == LayoutMode.VERTICAL;
    }
}