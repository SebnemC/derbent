package tech.derbent.session.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;

import tech.derbent.abstracts.interfaces.CLayoutChangeListener;

/**
 * Service to manage layout state (horizontal vs vertical) for views.
 * Uses Vaadin session to store layout preference.
 */
@Service
public class LayoutService {

    public enum LayoutMode {
        HORIZONTAL,
        VERTICAL
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(LayoutService.class);
    private static final String LAYOUT_MODE_KEY = "layoutMode";
    
    // Thread-safe set to store layout change listeners
    private final Set<CLayoutChangeListener> layoutChangeListeners = ConcurrentHashMap.newKeySet();

    /**
     * Gets the current layout mode from the session. Defaults to VERTICAL if not set.
     */
    public LayoutMode getCurrentLayoutMode() {
        LOGGER.debug("Getting current layout mode");
        
        final VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            LOGGER.debug("VaadinSession is null, returning default VERTICAL mode");
            return LayoutMode.VERTICAL; // Default to vertical
        }
        
        final LayoutMode mode = (LayoutMode) session.getAttribute(LAYOUT_MODE_KEY);
        final LayoutMode result = mode != null ? mode : LayoutMode.VERTICAL;
        LOGGER.debug("Current layout mode: {}", result);
        return result;
    }

    /**
     * Sets the layout mode and notifies all registered listeners.
     */
    public void setLayoutMode(final LayoutMode layoutMode) {
        LOGGER.info("Setting layout mode to: {}", layoutMode);
        
        if (layoutMode == null) {
            LOGGER.warn("Cannot set layout mode - layoutMode is null");
            return;
        }
        
        final VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute(LAYOUT_MODE_KEY, layoutMode);
            notifyLayoutChangeListeners(layoutMode);
            LOGGER.debug("Layout mode set successfully to: {}", layoutMode);
        } else {
            LOGGER.warn("VaadinSession is null, cannot set layout mode");
        }
    }

    /**
     * Toggles between horizontal and vertical layout modes.
     */
    public void toggleLayoutMode() {
        LOGGER.debug("Toggling layout mode");
        
        final LayoutMode currentMode = getCurrentLayoutMode();
        final LayoutMode newMode = currentMode == LayoutMode.HORIZONTAL 
            ? LayoutMode.VERTICAL 
            : LayoutMode.HORIZONTAL;
        
        LOGGER.info("Toggling from {} to {}", currentMode, newMode);
        setLayoutMode(newMode);
    }

    /**
     * Registers a component to receive notifications when the layout mode changes.
     */
    public void addLayoutChangeListener(final CLayoutChangeListener listener) {
        LOGGER.debug("Adding layout change listener: {}", 
                    listener != null ? listener.getClass().getSimpleName() : "null");
        
        if (listener != null) {
            layoutChangeListeners.add(listener);
            LOGGER.debug("Layout change listener registered: {}", listener.getClass().getSimpleName());
        } else {
            LOGGER.warn("Cannot add layout change listener - listener is null");
        }
    }

    /**
     * Unregisters a component from receiving layout change notifications.
     */
    public void removeLayoutChangeListener(final CLayoutChangeListener listener) {
        LOGGER.debug("Removing layout change listener: {}", 
                    listener != null ? listener.getClass().getSimpleName() : "null");
        
        if (listener != null) {
            final boolean removed = layoutChangeListeners.remove(listener);
            if (removed) {
                LOGGER.debug("Layout change listener unregistered: {}", listener.getClass().getSimpleName());
            } else {
                LOGGER.debug("Layout change listener was not found in the list: {}", listener.getClass().getSimpleName());
            }
        } else {
            LOGGER.warn("Cannot remove layout change listener - listener is null");
        }
    }

    /**
     * Clears all layout change listeners (typically called on session clear).
     */
    public void clearLayoutChangeListeners() {
        LOGGER.debug("Clearing all layout change listeners - current count: {}", layoutChangeListeners.size());
        layoutChangeListeners.clear();
        LOGGER.debug("Layout change listeners cleared");
    }

    /**
     * Notifies all registered layout change listeners about a layout mode change.
     */
    private void notifyLayoutChangeListeners(final LayoutMode newMode) {
        LOGGER.debug("Notifying {} layout change listeners of layout change to {}", 
                    layoutChangeListeners.size(), newMode);
        
        if (newMode == null) {
            LOGGER.warn("Cannot notify listeners - newMode is null");
            return;
        }
        
        final UI ui = UI.getCurrent();
        if (ui != null) {
            ui.access(() -> {
                layoutChangeListeners.forEach(listener -> {
                    if (listener != null) {
                        try {
                            listener.onLayoutModeChanged(newMode);
                            LOGGER.debug("Notified layout listener: {}", listener.getClass().getSimpleName());
                        } catch (final Exception e) {
                            LOGGER.error("Error notifying layout change listener: {}", 
                                       listener.getClass().getSimpleName(), e);
                        }
                    } else {
                        LOGGER.warn("Encountered null listener in the list");
                    }
                });
                // Force push to update the UI immediately
                ui.push();
            });
        } else {
            // If no UI context, try direct notification
            LOGGER.warn("UI.getCurrent() is null, attempting direct notification");
            layoutChangeListeners.forEach(listener -> {
                if (listener != null) {
                    try {
                        listener.onLayoutModeChanged(newMode);
                        LOGGER.debug("Directly notified layout listener: {}", listener.getClass().getSimpleName());
                    } catch (final Exception e) {
                        LOGGER.error("Error directly notifying layout change listener: {}", 
                                   listener.getClass().getSimpleName(), e);
                    }
                } else {
                    LOGGER.warn("Encountered null listener in the list");
                }
            });
        }
    }
}