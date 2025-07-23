package tech.derbent.abstracts.interfaces;

import tech.derbent.base.enums.CLayoutMode;

/**
 * CLayoutModeChangeListener - Interface for components that need to be notified
 * when the layout mode changes.
 * Layer: Interface (MVC)
 * 
 * Components implementing this interface will receive notifications when
 * the user switches between horizontal and vertical layout modes.
 */
public interface CLayoutModeChangeListener {
    
    /**
     * Called when the layout mode changes.
     * Implementers should update their layout accordingly.
     * 
     * @param newMode The new layout mode (HORIZONTAL or VERTICAL)
     */
    void onLayoutModeChanged(CLayoutMode newMode);
}