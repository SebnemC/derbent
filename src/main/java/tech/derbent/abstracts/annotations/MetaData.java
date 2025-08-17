package tech.derbent.abstracts.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * MetaData annotation for field metadata and form generation. Used by CEntityFormBuilder to automatically generate UI
 * components.
 * <p>
 * This annotation provides comprehensive metadata for entity fields to control how they are displayed and validated in
 * automatically generated forms.
 * </p>
 * <p>
 * <strong>Usage Example:</strong>
 * </p>
 *
 * <pre>{@code
 *
 * @MetaData(displayName = "User Name", required = true, description = "The full name of the user", order = 1, maxLength = 255)
 * private String name;
 * }</pre>
 * <p>
 * <strong>Parameter Validation:</strong>
 * </p>
 * <ul>
 * <li>All String parameters are null-safe (empty string used as default)</li>
 * <li>Numeric parameters have sensible defaults and ranges</li>
 * <li>Boolean parameters default to false for safety</li>
 * </ul>
 * 
 * @author Derbent Framework
 * @since 1.0
 * @see tech.derbent.abstracts.annotations.CEntityFormBuilder
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MetaData {

    /**
     * Specifies the Spring bean name to use as data provider for ComboBox fields.
     * <p>
     * This attribute allows you to specify which Spring service bean should provide data for ComboBox components. The
     * bean must implement a method that returns a List of items for the field's entity type.
     * </p>
     * <p>
     * <strong>Usage Examples:</strong>
     * </p>
     * <ul>
     * <li>{@code dataProviderBean = "activityTypeService"} - uses the activityTypeService Spring bean</li>
     * <li>{@code dataProviderBean = "projectService"} - uses the projectService Spring bean</li>
     * </ul>
     * <p>
     * The specified bean should have a method named "list" or "findAll" that returns a List&lt;EntityType&gt;. If the
     * method name is different, use {@link #dataProviderMethod()} to specify it.
     * </p>
     * 
     * @return the Spring bean name to use as data provider, empty string by default
     * @see #dataProviderMethod()
     * @see #dataProviderClass()
     */
    String dataProviderBean() default "";

    /**
     * Specifies the class type of the data provider service.
     * <p>
     * Alternative to {@link #dataProviderBean()}, this allows specifying the service class type instead of bean name.
     * The Spring context will be searched for a bean of this type.
     * </p>
     * <p>
     * <strong>Usage Example:</strong>
     * </p>
     * 
     * <pre>{@code
     *
     * @MetaData(displayName = "Activity Type", dataProviderClass = CActivityTypeService.class)
     * private CActivityType activityType;
     * }</pre>
     * 
     * @return the class type of the data provider service, Object.class by default (no provider)
     * @see #dataProviderBean()
     * @see #dataProviderMethod()
     */
    Class<?> dataProviderClass() default Object.class;

    /**
     * Specifies the method name to call on the data provider bean.
     * <p>
     * Use this when the data provider bean method is not named "list" or "findAll". The method should return a List of
     * entities compatible with the field type.
     * </p>
     * <p>
     * <strong>Usage Examples:</strong>
     * </p>
     * <ul>
     * <li>{@code dataProviderMethod = "findAllActive"} - calls findAllActive() method</li>
     * <li>{@code dataProviderMethod = "listByStatus"} - calls listByStatus() method</li>
     * </ul>
     * 
     * @return the method name to call, defaults to "list"
     * @see #dataProviderBean()
     */
    String dataProviderMethod() default "list";

    /**
     * Default value for the field when creating new instances.
     * <p>
     * <strong>Note:</strong> This value is converted to the appropriate type. For boolean fields, use "true" or
     * "false". For numeric fields, use string representation of the number.
     * </p>
     * 
     * @return the default value as string, never null
     */
    String defaultValue() default "";

    /**
     * Descriptive help text that appears below the field.
     * <p>
     * This text should provide additional context or validation rules to help users understand what to enter in the
     * field.
     * </p>
     * 
     * @return the description text, never null
     */
    String description() default "";

    /**
     * Display label for the field in forms and grids.
     * <p>
     * This is the human-readable name that will be shown to users. It should be descriptive and follow proper
     * capitalization.
     * </p>
     * 
     * @return the display name, never null, defaults to "Field" if not specified
     */
    String displayName() default "Field";

    /**
     * Whether the field should be hidden from forms and grids.
     * <p>
     * Hidden fields are not displayed but their values are still preserved. Useful for technical fields like IDs or
     * internal timestamps.
     * </p>
     * 
     * @return true if field should be hidden, false by default
     */
    boolean hidden() default false;

    /**
     * Maximum value for numeric fields.
     * <p>
     * Only applies to numeric field types (int, long, double, float). Ignored for other field types. Use
     * {@code Double.MAX_VALUE} to indicate no maximum.
     * </p>
     * 
     * @return the maximum allowed value, defaults to Double.MAX_VALUE (no limit)
     */
    double max() default Double.MAX_VALUE;

    /**
     * Maximum character length for text fields.
     * <p>
     * Only applies to String fields. Use -1 to indicate no length limit. This should match any JPA
     * {@code @Column(length)} annotations.
     * </p>
     * 
     * @return the maximum length, must be > 0 or -1 for no limit, defaults to -1
     */
    int maxLength() default -1;

    /**
     * Minimum value for numeric fields.
     * <p>
     * Only applies to numeric field types (int, long, double, float). Ignored for other field types. Use
     * {@code Double.MIN_VALUE} to indicate no minimum.
     * </p>
     * 
     * @return the minimum allowed value, defaults to Double.MIN_VALUE (no limit)
     */
    double min() default Double.MIN_VALUE;

    /**
     * Order of the field in forms and grids.
     * <p>
     * Fields are sorted by this value in ascending order. Lower values appear first. Use gaps (10, 20, 30) to allow
     * easy insertion of fields later.
     * </p>
     * 
     * @return the sort order, must be >= 0, defaults to 100
     */
    int order() default 100;

    /**
     * Whether the field should be read-only in forms.
     * <p>
     * Read-only fields are displayed but cannot be edited by users. Useful for computed values or system-maintained
     * fields.
     * </p>
     * 
     * @return true if field should be read-only, false by default
     */
    boolean readOnly() default false;

    /**
     * Whether the field is required and must have a value.
     * <p>
     * Required fields will show a visual indicator and prevent form submission if left empty. This works in conjunction
     * with bean validation.
     * </p>
     * 
     * @return true if field is required, false by default
     */
    boolean required() default false;

    boolean setBackgroundFromColor() default false;

    boolean useIcon() default false;

    /**
     * Whether enum fields should use radio buttons instead of dropdown.
     * <p>
     * Only applies to enum field types. Radio buttons are better for small sets of options (typically 2-5 items), while
     * dropdowns are better for larger sets.
     * </p>
     * 
     * @return true to use radio buttons, false to use dropdown, defaults to false
     */
    boolean useRadioButtons() default false;

    /**
     * Width hint for the UI component.
     * <p>
     * Accepts CSS width values like "200px", "50%", "15em". If empty, the component will use full available width.
     * </p>
     * <p>
     * <strong>Examples:</strong>
     * </p>
     * <ul>
     * <li>"200px" - fixed pixel width</li>
     * <li>"50%" - percentage of container</li>
     * <li>"15em" - em-based width</li>
     * </ul>
     * 
     * @return the width specification, never null
     */
    String width() default "";

    /**
     * Whether the combobox should allow users to enter custom values not in the list.
     * <p>
     * By default, comboboxes are selection-only to maintain data integrity. Set this to true
     * to allow users to type custom values that aren't in the provided data list.
     * </p>
     * 
     * @return true to allow custom values, false by default (selection only)
     */
    boolean allowCustomValue() default false;

    /**
     * Placeholder text to show in the combobox when no value is selected.
     * <p>
     * This provides a hint to users about what to select or enter in the field.
     * </p>
     * 
     * @return the placeholder text, empty string by default
     */
    String placeholder() default "";

    /**
     * Whether to auto-select the first item in the combobox when no default value is specified.
     * <p>
     * When true, the first item from the data provider will be automatically selected.
     * This is useful for required fields to ensure a value is always present.
     * </p>
     * 
     * @return true to auto-select first item, false by default
     */
    boolean autoSelectFirst() default false;

    /**
     * Whether to clear the combobox selection when the data provider returns empty results.
     * <p>
     * When true, the combobox value will be cleared if the data provider returns no items.
     * This is useful for dependent comboboxes where the selection should be cleared when
     * the parent selection changes and no valid options remain.
     * </p>
     * 
     * @return true to clear on empty data, false by default
     */
    boolean clearOnEmptyData() default false;

    /**
     * Custom filter method name for the combobox data provider.
     * <p>
     * When specified, this method will be called on the data provider bean with a filter string
     * parameter to provide filtered results as the user types. The method should accept a String
     * parameter and return a filtered List of entities.
     * </p>
     * 
     * @return the filter method name, empty string for no custom filtering
     * @see #dataProviderBean()
     * @see #dataProviderClass()
     */
    String filterMethod() default "";

    /**
     * Whether the combobox should be read-only but still show its value.
     * <p>
     * This is different from the general readOnly attribute as it specifically controls
     * combobox behavior. A read-only combobox displays the selected value but cannot be changed.
     * </p>
     * 
     * @return true for read-only combobox, false by default
     */
    boolean comboboxReadOnly() default false;
}