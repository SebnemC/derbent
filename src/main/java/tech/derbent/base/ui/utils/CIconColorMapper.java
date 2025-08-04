package tech.derbent.base.ui.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * CIconColorMapper - Utility class for mapping icon names to colors
 * Provides consistent color theming across the application for different functional areas.
 */
public final class CIconColorMapper {

    private static final Map<String, String> ICON_COLOR_MAP = new HashMap<>();
    
    static {
        // Project Management - Blue tones
        ICON_COLOR_MAP.put("vaadin:calendar-clock", "#007bff");    // Activities - blue
        ICON_COLOR_MAP.put("vaadin:tasks", "#007bff");             // Tasks/Activities - blue
        ICON_COLOR_MAP.put("vaadin:dashboard", "#0056b3");         // Kanban - darker blue
        ICON_COLOR_MAP.put("vaadin:grid-big", "#004085");          // Generic Kanban - darkest blue
        ICON_COLOR_MAP.put("vaadin:briefcase", "#fd7e14");         // Projects - orange
        
        // Meetings - Green tones
        ICON_COLOR_MAP.put("vaadin:group", "#28a745");             // Meetings - green
        ICON_COLOR_MAP.put("vaadin:calendar", "#20c997");          // Meeting Kanban - teal
        
        // Decisions & Legal - Purple tones
        ICON_COLOR_MAP.put("vaadin:gavel", "#6f42c1");             // Decisions - purple
        
        // Risks & Warnings - Red/Orange tones
        ICON_COLOR_MAP.put("vaadin:warning", "#dc3545");           // Risks - red
        ICON_COLOR_MAP.put("vaadin:exclamation-circle", "#fd7e14"); // Comment priorities - orange
        
        // Orders & Commerce - Indigo
        ICON_COLOR_MAP.put("vaadin:cart", "#6610f2");              // Orders - indigo
        
        // Users & People - Purple variants
        ICON_COLOR_MAP.put("vaadin:users", "#6f42c1");             // Users - purple
        ICON_COLOR_MAP.put("vaadin:user", "#6f42c1");              // Single user - purple
        
        // Settings & Configuration - Gray tones
        ICON_COLOR_MAP.put("vaadin:cogs", "#6c757d");              // Settings - gray
        ICON_COLOR_MAP.put("vaadin:tools", "#495057");             // System settings - darker gray
        ICON_COLOR_MAP.put("vaadin:building", "#17a2b8");          // Companies - cyan
        
        // Status & Types - Consistent colors by type
        ICON_COLOR_MAP.put("vaadin:flag", "#ffc107");              // Statuses - yellow/amber
        ICON_COLOR_MAP.put("vaadin:tags", "#17a2b8");              // Types - cyan
        
        // Home & Navigation - Primary colors
        ICON_COLOR_MAP.put("vaadin:home", "#007bff");              // Home - primary blue
        ICON_COLOR_MAP.put("vaadin:clipboard-check", "#28a745");   // Check items - green
        
        // Fallback for unknown icons
        ICON_COLOR_MAP.put("default", "#6c757d");                  // Default gray
    }
    
    /**
     * Gets the color for a given icon name.
     * @param iconName The Vaadin icon name (e.g., "vaadin:home")
     * @return The hex color code for the icon
     */
    public static String getIconColor(final String iconName) {
        if (iconName == null || iconName.trim().isEmpty()) {
            return ICON_COLOR_MAP.get("default");
        }
        
        return ICON_COLOR_MAP.getOrDefault(iconName.trim(), ICON_COLOR_MAP.get("default"));
    }
    
    /**
     * Gets a hover color (lighter version) for a given icon name.
     * @param iconName The Vaadin icon name
     * @return The hex color code for the hover state
     */
    public static String getIconHoverColor(final String iconName) {
        final String baseColor = getIconColor(iconName);
        // Add 33% transparency to create hover effect
        return baseColor + "aa";
    }
    
    /**
     * Checks if an icon should be colorful (non-default color).
     * @param iconName The Vaadin icon name
     * @return true if the icon should have a custom color, false for default
     */
    public static boolean isColorfulIcon(final String iconName) {
        if (iconName == null || iconName.trim().isEmpty()) {
            return false;
        }
        
        final String color = ICON_COLOR_MAP.get(iconName.trim());
        return color != null && !color.equals(ICON_COLOR_MAP.get("default"));
    }
    
    // Private constructor to prevent instantiation
    private CIconColorMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}