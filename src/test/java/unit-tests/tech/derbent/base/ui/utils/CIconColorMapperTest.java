package tech.derbent.base.ui.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for CIconColorMapper utility.
 */
class CIconColorMapperTest {

    @Test
    void testGetIconColor_ValidIcon() {
        // Test specific icon mappings
        assertEquals("#007bff", CIconColorMapper.getIconColor("vaadin:calendar-clock"));
        assertEquals("#28a745", CIconColorMapper.getIconColor("vaadin:group"));
        assertEquals("#fd7e14", CIconColorMapper.getIconColor("vaadin:briefcase"));
        assertEquals("#6f42c1", CIconColorMapper.getIconColor("vaadin:users"));
    }

    @Test
    void testGetIconColor_NullIcon() {
        // Test null icon returns default color
        assertEquals("#6c757d", CIconColorMapper.getIconColor(null));
    }

    @Test
    void testGetIconColor_EmptyIcon() {
        // Test empty icon returns default color
        assertEquals("#6c757d", CIconColorMapper.getIconColor(""));
        assertEquals("#6c757d", CIconColorMapper.getIconColor("   "));
    }

    @Test
    void testGetIconColor_UnknownIcon() {
        // Test unknown icon returns default color
        assertEquals("#6c757d", CIconColorMapper.getIconColor("vaadin:unknown-icon"));
    }

    @Test
    void testGetIconHoverColor() {
        // Test hover color includes transparency
        String baseColor = CIconColorMapper.getIconColor("vaadin:calendar-clock");
        String hoverColor = CIconColorMapper.getIconHoverColor("vaadin:calendar-clock");
        assertTrue(hoverColor.startsWith(baseColor));
        assertTrue(hoverColor.endsWith("aa"));
    }

    @Test
    void testIsColorfulIcon() {
        // Test colorful icons
        assertTrue(CIconColorMapper.isColorfulIcon("vaadin:calendar-clock"));
        assertTrue(CIconColorMapper.isColorfulIcon("vaadin:group"));
        assertTrue(CIconColorMapper.isColorfulIcon("vaadin:briefcase"));
        
        // Test non-colorful inputs
        assertFalse(CIconColorMapper.isColorfulIcon(null));
        assertFalse(CIconColorMapper.isColorfulIcon(""));
        assertFalse(CIconColorMapper.isColorfulIcon("vaadin:unknown-icon"));
    }
}