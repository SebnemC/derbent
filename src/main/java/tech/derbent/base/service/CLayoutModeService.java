package tech.derbent.base.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;

import tech.derbent.abstracts.interfaces.CLayoutModeChangeListener;
import tech.derbent.base.enums.CLayoutMode;

/**
 * CLayoutModeService - Service to manage layout mode state across the application.
 * Layer: Service (MVC)
 * 
 * Manages the current layout mode (HORIZONTAL or VERTICAL) for the user session
 * and notifies registered listeners when the mode changes.
 */
@Service
public class CLayoutModeService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CLayoutModeService.class);
    private static final String LAYOUT_MODE_KEY = "layoutMode";
    
    // Thread-safe set to store layout mode change listeners
    private final Set<CLayoutModeChangeListener> layoutModeChangeListeners = ConcurrentHashMap.newKeySet();
    
    /**
     * Registers a component to receive notifications when the layout mode changes.
     * Components should call this method when they are attached to the UI.
     * 
     * @param listener The component that wants to be notified of layout mode changes
     */
    public void addLayoutModeChangeListener(final CLayoutModeChangeListener listener) {
        if (listener != null) {
            layoutModeChangeListeners.add(listener);
            LOGGER.debug("Layout mode change listener registered: {}", listener.getClass().getSimpleName());
        }
    }
    
    /**
     * Unregisters a component from receiving layout mode change notifications.
     * Components should call this method when they are detached from the UI.
     * 
     * @param listener The component to unregister
     */
    public void removeLayoutModeChangeListener(final CLayoutModeChangeListener listener) {
        if (listener != null) {
            layoutModeChangeListeners.remove(listener);
            LOGGER.debug("Layout mode change listener unregistered: {}", listener.getClass().getSimpleName());
        }
    }
    
    /**
     * Gets the current layout mode from the session.
     * Returns VERTICAL as default if no mode is set.
     * 
     * @return The current layout mode
     */
    public CLayoutMode getCurrentLayoutMode() {
        final VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            return CLayoutMode.VERTICAL; // Default mode
        }
        
        final CLayoutMode currentMode = (CLayoutMode) session.getAttribute(LAYOUT_MODE_KEY);
        return currentMode != null ? currentMode : CLayoutMode.VERTICAL;
    }
    
    /**
     * Sets the layout mode and notifies all registered listeners.
     * 
     * @param mode The new layout mode to set
     */
    public void setLayoutMode(final CLayoutMode mode) {
        LOGGER.info("Setting layout mode to: {}", mode);
        
        final VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute(LAYOUT_MODE_KEY, mode);
        }
        
        // Notify all registered listeners
        notifyLayoutModeChangeListeners(mode);
    }
    
    /**
     * Toggles between HORIZONTAL and VERTICAL layout modes.
     */
    public void toggleLayoutMode() {
        final CLayoutMode currentMode = getCurrentLayoutMode();
        final CLayoutMode newMode = currentMode.toggle();
        setLayoutMode(newMode);
    }
    
    /**
     * Notifies all registered layout mode change listeners about a mode change.
     * This method safely handles UI access for components that may be in different UIs.
     * 
     * @param newMode The new layout mode
     */
    private void notifyLayoutModeChangeListeners(final CLayoutMode newMode) {
        LOGGER.debug("Notifying {} layout mode change listeners of mode change to {}", 
                    layoutModeChangeListeners.size(), newMode);
        
        final UI ui = UI.getCurrent();
        if (ui != null) {
            ui.access(() -> {
                layoutModeChangeListeners.forEach(listener -> {
                    try {
                        listener.onLayoutModeChanged(newMode);
                        LOGGER.debug("Notified layout mode listener: {}", listener.getClass().getSimpleName());
                    } catch (final Exception e) {
                        LOGGER.error("Error notifying layout mode change listener: {}", 
                                   listener.getClass().getSimpleName(), e);
                    }
                });
            });
        }
    }
    
    /**
     * Clears layout mode data from session.
     * Called when session is cleared or user logs out.
     */
    public void clearLayoutMode() {
        final VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute(LAYOUT_MODE_KEY, null);
            LOGGER.info("Layout mode data cleared from session");
        }
        
        // Clear all listeners when session is cleared
        layoutModeChangeListeners.clear();
        LOGGER.debug("Layout mode change listeners cleared");
    }
}