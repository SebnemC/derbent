# Derbent CSS Design System Approach

## Overview

This document outlines the comprehensive CSS design system implemented for the Derbent project, featuring a Java/Sun OS inspired pastel color palette with a complete variable-based architecture. The system is designed to be maintainable, scalable, and consistent across all UI components.

## Design Philosophy

### Java/Sun OS Color Inspiration

The color palette draws inspiration from the classic Java and Sun Microsystems branding, translated into modern pastel tones:

- **Java Blue Heritage**: The primary color family uses soft, approachable blues reminiscent of Java's brand identity
- **Sun Orange Warmth**: Secondary colors feature warm orange and peach tones inspired by Sun Microsystems
- **Professional Pastels**: All colors are softened to pastel variants for modern, comfortable user experiences

### Variable-First Architecture

Every design token is implemented as a CSS custom property (variable), ensuring:
- **Consistency**: All components use the same color values
- **Maintainability**: Colors can be updated globally by changing variables
- **Theming**: Easy implementation of dark mode or alternative themes
- **Developer Experience**: Semantic naming makes code self-documenting

## Color System Structure

### Primary Color Families

Each color family includes 10 shades (50-900) following modern design system conventions:

#### Primary Colors (Java Blue Family)
```css
--derbent-primary-50: #f0f7ff;   /* Lightest - backgrounds */
--derbent-primary-500: #1e7fff;  /* Main primary - buttons, links */
--derbent-primary-900: #003399;  /* Darkest - high contrast text */
```

#### Secondary Colors (Sun Orange Family)
```css
--derbent-secondary-50: #fff7ed;  /* Lightest - warm backgrounds */
--derbent-secondary-500: #f97316; /* Main secondary - accent elements */
--derbent-secondary-900: #7c2d12; /* Darkest - strong emphasis */
```

#### Semantic Colors
- **Success**: Soft green family for positive actions and feedback
- **Warning**: Soft yellow family for caution and alerts
- **Error**: Soft red/pink family for errors and destructive actions
- **Neutral**: Professional gray family for text and borders

### Semantic Color Assignments

Colors are assigned semantic meanings for consistent usage:

```css
/* Text Colors */
--derbent-text-primary: var(--derbent-neutral-900);    /* Main body text */
--derbent-text-secondary: var(--derbent-neutral-600);  /* Supporting text */
--derbent-text-muted: var(--derbent-neutral-500);      /* Subtle text */

/* Background Colors */
--derbent-bg-primary: #ffffff;                         /* Main backgrounds */
--derbent-bg-accent: var(--derbent-primary-50);        /* Highlighted areas */
--derbent-bg-warm: var(--derbent-secondary-50);        /* Warm sections */
```

## Typography System

### Font Stack
- **Primary**: Inter font family with system fallbacks
- **Monospace**: Fira Code for code display
- **Display**: Inter for headings and emphasis

### Size Scale
Based on a modular scale providing consistent typography hierarchy:
```css
--derbent-text-xs: 0.75rem;    /* 12px - Small labels */
--derbent-text-base: 1rem;     /* 16px - Body text */
--derbent-text-xl: 1.25rem;    /* 20px - Large text */
--derbent-text-3xl: 1.875rem;  /* 30px - Section headings */
```

### Weight and Leading
Complete font weight scale (100-900) and line height options for optimal readability.

## Border Radius System

Consistent border radius values for different component types:

```css
--derbent-radius-sm: 4px;       /* Small elements */
--derbent-radius-md: 6px;       /* Standard buttons, inputs */
--derbent-radius-xl: 12px;      /* Cards, large containers */
--derbent-radius-full: 9999px;  /* Pills, avatars */
```

### Component-Specific Radius
```css
--derbent-radius-button: var(--derbent-radius-md);
--derbent-radius-card: var(--derbent-radius-xl);
--derbent-radius-input: var(--derbent-radius-md);
```

## Button System

Comprehensive button styling with hover and active states:

### Primary Buttons
```css
--derbent-btn-primary-bg: var(--derbent-primary-500);
--derbent-btn-primary-hover: var(--derbent-primary-600);
--derbent-btn-primary-active: var(--derbent-primary-700);
```

### Button Variants
- **Primary**: Main call-to-action buttons
- **Secondary**: Alternative actions
- **Outline**: Subtle emphasis buttons
- **Ghost**: Minimal visual weight buttons

## Spacing and Layout

Consistent spacing scale based on 4px increments:
```css
--derbent-space-1: 0.25rem;  /* 4px */
--derbent-space-4: 1rem;     /* 16px - standard spacing */
--derbent-space-8: 2rem;     /* 32px - section spacing */
```

## Shadow System

Layered shadow system for depth and hierarchy:
```css
--derbent-shadow-sm: 0 1px 3px 0 rgba(0, 0, 0, 0.1);  /* Subtle elevation */
--derbent-shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1); /* Card elevation */
```

## Implementation Guidelines

### Variable Usage
Always use semantic variables instead of specific color values:

✅ **Correct**:
```css
.my-component {
    background-color: var(--derbent-bg-accent);
    color: var(--derbent-text-primary);
    border-radius: var(--derbent-radius-card);
}
```

❌ **Incorrect**:
```css
.my-component {
    background-color: #f0f7ff;
    color: #171717;
    border-radius: 12px;
}
```

### Component Development
1. **Use semantic variables**: Choose variables that describe the purpose, not the appearance
2. **Maintain hierarchy**: Respect the color and spacing scales
3. **Consider states**: Always define hover, focus, and active states
4. **Test accessibility**: Ensure adequate contrast ratios

### Vaadin Integration
The system integrates seamlessly with Vaadin's Lumo theme:
- Variables can override Lumo defaults
- Maintains compatibility with Vaadin components
- Extends Lumo's capabilities with additional design tokens

## Component Examples

### Enhanced Details Tab
```css
.details-tab-layout {
    background-color: var(--derbent-bg-warm);
    border-radius: var(--derbent-radius-card);
    border: 1px solid var(--derbent-border-light);
    box-shadow: var(--derbent-shadow-sm);
    padding: var(--derbent-space-4);
}
```

### Accordion Styling
```css
vaadin-accordion-heading {
    font-size: var(--derbent-text-lg);
    font-weight: var(--derbent-font-semibold);
    color: var(--derbent-text-secondary);
    background-color: var(--derbent-bg-secondary);
    border-radius: var(--derbent-radius-md);
}
```

## Utility Classes

Pre-built utility classes for common styling needs:

```css
.derbent-text-primary { color: var(--derbent-text-primary); }
.derbent-bg-accent { background-color: var(--derbent-bg-accent); }
.derbent-rounded-lg { border-radius: var(--derbent-radius-lg); }
```

## Future Enhancements

### Planned Features
1. **Dark Mode Support**: Duplicate variable set for dark theme
2. **Animation System**: Consistent timing and easing variables
3. **Responsive Breakpoints**: Viewport-based variable adjustments
4. **Component Variants**: Extended button and form styling options

### Maintenance Strategy
1. **Regular Reviews**: Quarterly assessment of color usage and consistency
2. **Documentation Updates**: Keep this document current with any system changes
3. **Component Audits**: Ensure all components follow the design system
4. **Performance Monitoring**: Track CSS bundle size and optimization opportunities

## Conclusion

This design system provides a solid foundation for consistent, maintainable UI development in the Derbent project. By following the variable-first approach and semantic naming conventions, developers can create cohesive user interfaces that reflect the Java/Sun OS inspired aesthetic while maintaining modern usability standards.

The system is designed to grow with the project while maintaining backward compatibility and consistent visual identity across all components and features.