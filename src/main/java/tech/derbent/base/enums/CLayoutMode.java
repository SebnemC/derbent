package tech.derbent.base.enums;

/**
 * CLayoutMode - Enum representing different layout display modes for views.
 * Layer: Domain (MVC)
 * 
 * Defines the available layout orientations for master-detail views.
 * VERTICAL mode stacks components vertically (traditional).
 * HORIZONTAL mode splits components side by side.
 */
public enum CLayoutMode {
    /**
     * Vertical layout mode - components are stacked vertically (default)
     */
    VERTICAL("Vertical View", "vaadin:split"),
    
    /**
     * Horizontal layout mode - components are split side by side
     */
    HORIZONTAL("Horizontal View", "vaadin:split-h");
    
    private final String displayName;
    private final String iconName;
    
    CLayoutMode(final String displayName, final String iconName) {
        this.displayName = displayName;
        this.iconName = iconName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getIconName() {
        return iconName;
    }
    
    /**
     * Returns the opposite layout mode for toggling functionality.
     */
    public CLayoutMode toggle() {
        return this == VERTICAL ? HORIZONTAL : VERTICAL;
    }
}