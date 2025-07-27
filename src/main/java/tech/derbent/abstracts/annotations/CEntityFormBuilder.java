package tech.derbent.abstracts.annotations;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.abstracts.domains.CEntityDB;

/**
 * CEntityFormBuilder - Utility class for building forms from entity classes using
 * MetaData annotations. Supports automatic form generation for various field types
 * including text, boolean, enum, numeric, and entity references.
 * <p>
 * <strong>Enhanced ComboBox Data Provider Support:</strong> This form builder now
 * supports annotation-based data provider configuration, making it more generic and
 * maintainable. ComboBox fields can specify their data providers using MetaData
 * annotations in three ways:
 * </p>
 * <ol>
 * <li><strong>Bean Name:</strong>
 * {@code @MetaData(dataProviderBean = "activityTypeService")}</li>
 * <li><strong>Bean Class:</strong>
 * {@code @MetaData(dataProviderClass = CActivityTypeService.class)}</li>
 * <li><strong>Automatic:</strong> Automatically resolves service by entity type naming
 * convention</li>
 * </ol>
 * <p>
 * <strong>Backward Compatibility:</strong> The traditional ComboBoxDataProvider approach
 * is still supported and takes precedence when provided. This allows existing code to
 * continue working while new code can benefit from the annotation-based approach.
 * </p>
 * <p>
 * <strong>Usage Examples:</strong>
 * </p>
 *
 * <pre>
 *
 * {
 * 	&#64;code
 * 	// New annotation-based approach - no data provider needed in view code
 * 	public class CActivity extends CEntityOfProject {
 *
 * 		&#64;MetaData (
 * 			displayName = "Activity Type", dataProviderBean = "activityTypeService"
 * 		)
 * 		private CActivityType activityType;
 *
 * 		@MetaData (
 * 			displayName = "Assigned User", dataProviderClass = CUserService.class,
 * 			dataProviderMethod = "findAllActive"
 * 		)
 * 		private CUser assignedUser;
 * 	}
 * 	// In view - much simpler now
 * 	Div form = CEntityFormBuilder.buildForm(CActivity.class, binder); // No data
 * 																		// provider
 * 																		// needed!
 * 	// Legacy approach still works
 * 	ComboBoxDataProvider provider = new ComboBoxDataProvider() {
 *
 * 		public List getItems(Class entityType) {
 * 			return customLogic(entityType);
 * 		}
 * 	};
 * 	Div form = CEntityFormBuilder.buildForm(CActivity.class, binder, provider);
 * }
 * </pre>
 *
 * Layer: Utility (MVC)
 * @author Derbent Framework
 * @since 1.0
 * @see tech.derbent.abstracts.annotations.MetaData
 * @see tech.derbent.abstracts.annotations.CDataProviderResolver
 */
@org.springframework.stereotype.Component
public final class CEntityFormBuilder implements ApplicationContextAware {

	/**
	 * Interface for providing data to ComboBox components. Implementations should provide
	 * lists of entities for specific entity types.
	 * <p>
	 * <strong>Note:</strong> This interface is maintained for backward compatibility. New
	 * implementations should use the annotation-based approach with MetaData annotations.
	 * </p>
	 * @see MetaData#dataProviderBean()
	 * @see MetaData#dataProviderClass()
	 * @see CDataProviderResolver
	 */
	public interface ComboBoxDataProvider {

		/**
		 * Gets items for a specific entity type.
		 * @param entityType the class type of the entity
		 * @return list of entities to populate the ComboBox
		 */
		<T extends CEntityDB> List<T> getItems(Class<T> entityType);
	}

	private static final Logger LOGGER =
		LoggerFactory.getLogger(CEntityFormBuilder.class);

	protected static final String LabelMinWidth_210PX = "210px";

	/**
	 * Cached instance of the data provider resolver for performance.
	 */
	private static CDataProviderResolver dataProviderResolver;

	public static <EntityClass> Div buildForm(final Class<?> entityClass,
		final BeanValidationBinder<EntityClass> binder) {
		return buildForm(entityClass, binder, null, null);
	}

	public static <EntityClass> Div buildForm(final Class<?> entityClass,
		final BeanValidationBinder<EntityClass> binder,
		final ComboBoxDataProvider dataProvider, final List<String> entityFields) {

		// Enhanced null pointer checking with detailed logging
		if (entityClass == null) {
			LOGGER.error("Entity class parameter is null - cannot build form");
			throw new IllegalArgumentException("Entity class cannot be null");
		}

		if (binder == null) {
			LOGGER.error(
				"Binder parameter is null for entity class: {} - cannot build form",
				entityClass.getSimpleName());
			throw new IllegalArgumentException("Binder cannot be null");
		}
		final Div panel = new Div();
		panel.setClassName("editor-layout");
		final FormLayout formLayout = new FormLayout();
		// Collect all fields from the class hierarchy with enhanced logging
		final List<Field> allFields = new ArrayList<>();
		getListOfAllFields(entityClass, allFields);
		// LOGGER.debug("Total fields collected from hierarchy: {}", allFields.size());
		// Filter and sort fields with enhanced null checking and logging
		final List<Field> sortedFields = getSortedFilteredFieldsList(allFields);
		LOGGER.info("Processing {} visible fields for form generation",
			sortedFields.size());
		// Create components with enhanced error handling and logging
		int processedComponents = 0;

		for (final Field field : sortedFields) {
			// skip if entityFields is not null and does not contain the field name or
			// entityFields is empty
			boolean skip = false;

			if (entityFields == null) {
				// skip = false; //already false by default
			}

			if ((entityFields != null) && !entityFields.contains(field.getName())) {
				skip = true;
			}

			if (skip) {
				continue;
			}
			processedComponents = processMetaForField(binder, dataProvider, formLayout,
				processedComponents, field);
		}
		LOGGER.info(
			"Form generation completed. Successfully processed {} out of {} components",
			processedComponents, sortedFields.size());
		panel.add(formLayout);
		return panel;
	}

	public static <EntityClass> Div buildForm(final Class<?> entityClass,
		final BeanValidationBinder<EntityClass> binder, final List<String> entityFields) {
		return buildForm(entityClass, binder, null, entityFields);
	}

	public static <EntityClass> Div buildFormAll(final Class<?> entityClass,
		final BeanValidationBinder<EntityClass> binder,
		final ComboBoxDataProvider dataProvider) {
		return buildForm(entityClass, binder, dataProvider, null);
	}

	private static Checkbox createCheckbox(final Field field, final MetaData meta,
		final BeanValidationBinder<?> binder) {

		if ((field == null) || (meta == null) || (binder == null)) {
			LOGGER.error(
				"Null parameters in createCheckbox - field: {}, meta: {}, binder: {}",
				field != null ? field.getName() : "null",
				meta != null ? "present" : "null", binder != null ? "present" : "null");
			return null;
		}
		final Checkbox checkbox = new Checkbox();

		// Safe null checking and parsing for default value
		if ((meta.defaultValue() != null) && !meta.defaultValue().trim().isEmpty()) {

			try {
				checkbox.setValue(Boolean.parseBoolean(meta.defaultValue()));
				// LOGGER.debug("Set default value for checkbox '{}': {}",
				// field.getName(), meta.defaultValue());
			} catch (final Exception e) {
				LOGGER.warn("Invalid boolean default value '{}' for field '{}': {}",
					meta.defaultValue(), field.getName(), e.getMessage());
			}
		}

		try {
			binder.bind(checkbox, field.getName());
		} catch (final Exception e) {
			LOGGER.error("Failed to bind checkbox for field '{}': {}", field.getName(),
				e.getMessage());
			return null;
		}
		return checkbox;
	}

	@SuppressWarnings ("unchecked")
	private static <T extends CEntityDB> ComboBox<T> createComboBox(final Field field,
		final MetaData meta, final BeanValidationBinder<?> binder,
		final ComboBoxDataProvider dataProvider) {

		// Enhanced null pointer checking with detailed logging
		if (field == null) {
			LOGGER.error("Field parameter is null in createComboBox");
			return null;
		}

		if (meta == null) {
			LOGGER.error("MetaData parameter is null for field: {}", field.getName());
			return null;
		}

		if (binder == null) {
			LOGGER.error("Binder parameter is null for field: {}", field.getName());
			return null;
		}
		final Class<T> fieldType = (Class<T>) field.getType();
		final ComboBox<T> comboBox = new ComboBox<>();
		// Enhanced item label generator with null safety and proper display formatting
		// Fix for combobox display issue: use getName() for CEntityNamed entities instead of toString()
		comboBox.setItemLabelGenerator(item -> {

			if (item == null) {
				return "N/A";
			}

			try {
				// Check if the item is a CEntityNamed subclass (like CUser, CProject, etc.)
				// and has a getName() method - use it for better display instead of toString()
				if (item instanceof tech.derbent.abstracts.domains.CEntityNamed) {
					final tech.derbent.abstracts.domains.CEntityNamed namedEntity = 
						(tech.derbent.abstracts.domains.CEntityNamed) item;
					final String name = namedEntity.getName();
					return (name != null && !name.trim().isEmpty()) ? name : 
						"Unnamed " + item.getClass().getSimpleName() + " #" + namedEntity.getId();
				}
				
				// Check if any entity has a getName() method using reflection (e.g., CCompany)
				// This handles entities that have names but don't extend CEntityNamed
				try {
					final java.lang.reflect.Method getNameMethod = item.getClass().getMethod("getName");
					if (getNameMethod.getReturnType() == String.class) {
						final String name = (String) getNameMethod.invoke(item);
						if (name != null && !name.trim().isEmpty()) {
							return name;
						}
						// If getName() returns null/empty, try to get ID for fallback
						try {
							final java.lang.reflect.Method getIdMethod = item.getClass().getMethod("getId");
							final Object id = getIdMethod.invoke(item);
							return "Unnamed " + item.getClass().getSimpleName() + " #" + id;
						} catch (final Exception idException) {
							return "Unnamed " + item.getClass().getSimpleName();
						}
					}
				} catch (final java.lang.NoSuchMethodException e) {
					// No getName() method found, continue to toString()
				} catch (final Exception e) {
					LOGGER.debug("Error calling getName() method on {}: {}", 
						item.getClass().getSimpleName(), e.getMessage());
				}
				
				// For entities without getName() method, fall back to toString()
				return item.toString();
			} catch (final Exception e) {
				LOGGER.warn("Error generating label for ComboBox item of type {}: {}",
					item.getClass().getSimpleName(), e.getMessage());
				return "Error: " + item.getClass().getSimpleName();
			}
		});
		// Data provider resolution with priority order: 1. Legacy ComboBoxDataProvider
		// (if provided) - for backward compatibility 2. Annotation-based resolution using
		// CDataProviderResolver 3. Empty list as fallback
		List<T> items = null;

		// Priority 1: Use legacy data provider if provided
		if (dataProvider != null) {

			try {
				items = dataProvider.getItems(fieldType);

				if (items != null) {}
				else {
					LOGGER.warn(
						"Legacy data provider returned null for field '{}' of type '{}'",
						field.getName(), fieldType.getSimpleName());
				}
			} catch (final Exception e) {
				LOGGER.error(
					"Error using legacy data provider for field '{}' of type '{}': {}",
					field.getName(), fieldType.getSimpleName(), e.getMessage(), e);
			}
		}

		// Priority 2: Use annotation-based resolution if legacy provider didn't work
		if ((items == null) && (dataProviderResolver != null)) {

			try {
				items = dataProviderResolver.resolveData(fieldType, meta);

				if (items != null) {}
				else {
					LOGGER.warn(
						"Annotation-based resolver returned null for field '{}' of type '{}'",
						field.getName(), fieldType.getSimpleName());
				}
			} catch (final Exception e) {
				LOGGER.error(
					"Error using annotation-based data resolver for field '{}' of type '{}': {}",
					field.getName(), fieldType.getSimpleName(), e.getMessage(), e);
			}
		}

		// Priority 3: Fallback to empty list
		if (items == null) {
			LOGGER.warn(
				"No data provider could supply items for field '{}' of type '{}' - using empty list",
				field.getName(), fieldType.getSimpleName());
			items = List.of();
		}

		// Set items on ComboBox with validation
		try {
			comboBox.setItems(items);
		} catch (final Exception e) {
			LOGGER.error("Error setting items on ComboBox for field '{}': {}",
				field.getName(), e.getMessage(), e);
			comboBox.setItems(List.of()); // Fallback to empty list
		}
		// Enhanced converter with better error handling for lazy loading and proxy
		// objects
		binder.forField(comboBox).withConverter(
			// Convert from ComboBox value to entity (forward conversion)
			comboBoxValue -> {

				if (comboBoxValue == null) {
					return null;
				}
				return comboBoxValue;
			},
			// Convert from entity to ComboBox value (reverse conversion) - handles lazy
			// loading
			entityValue -> {

				if (entityValue == null) {
					return null;
				}

				try {
					// Get the entity ID for comparison - handles proxy objects safely
					final Long entityId = entityValue.getId();

					if (entityId == null) {
						LOGGER.warn(
							"Entity has null ID for field '{}' - cannot match with ComboBox items",
							field.getName());
						return null;
					}
					// Find matching item in ComboBox by comparing IDs
					final List<T> allItems = comboBox.getDataProvider()
						.fetch(new com.vaadin.flow.data.provider.Query<>())
						.collect(java.util.stream.Collectors.toList());
					final T matchingItem = allItems.stream().filter(item -> {

						if (item == null) {
							return false;
						}
						final Long itemId = item.getId();
						return (itemId != null) && itemId.equals(entityId);
					}).findFirst().orElse(null);

					if (matchingItem != null) {}
					else {
						LOGGER.warn(
							"No matching ComboBox item found for entity ID {} in field '{}'",
							entityId, field.getName());
					}
					return matchingItem;
				} catch (final Exception e) {
					LOGGER.error(
						"Error converting entity to ComboBox value for field '{}': {}",
						field.getName(), e.getMessage(), e);
					return null;
				}
			}).bind(field.getName());
		return comboBox;
	}

	private static Component createComponentForField(final Field field,
		final MetaData meta, final BeanValidationBinder<?> binder,
		final ComboBoxDataProvider dataProvider) {
		Component component = null;

		// Enhanced null pointer checking
		if (field == null) {
			LOGGER.error("Field parameter is null");
			return null;
		}

		if (meta == null) {
			LOGGER.error("MetaData parameter is null for field: {}", field.getName());
			return null;
		}

		if (binder == null) {
			LOGGER.error("Binder parameter is null for field: {}", field.getName());
			return null;
		}
		final Class<?> fieldType = field.getType();

		if (fieldType == null) {
			LOGGER.error("Field type is null for field: {}", field.getName());
			return null;
		}
		LOGGER.debug(
			"Creating component for field '{}' of type '{}' with displayName '{}'",
			field.getName(), fieldType.getSimpleName(), meta.displayName());

		// Handle different field types with detailed logging
		if ((fieldType == Boolean.class) || (fieldType == boolean.class)) {
			component = createCheckbox(field, meta, binder);
		}
		else if (fieldType == String.class) {
			component = createTextField(field, meta, binder);
		}
		else if ((fieldType == Integer.class) || (fieldType == int.class)
			|| (fieldType == Long.class) || (fieldType == long.class)) {
			// Integer types
			component = createIntegerField(field, meta, binder);
		}
		else if ((fieldType == Double.class) || (fieldType == double.class)
			|| (fieldType == Float.class) || (fieldType == float.class)
			|| (fieldType == BigDecimal.class)) {
			// Floating-point types
			component = createFloatingPointField(field, meta, binder);
		}
		else if (fieldType == LocalDate.class) {
			component = createDatePicker(field, meta, binder);
		}
		else if ((fieldType == LocalDateTime.class) || (fieldType == Instant.class)) {
			component = createDateTimePicker(field, meta, binder);
		}
		else if (fieldType.isEnum()) {
			component = createEnumComponent(field, meta, binder);
		}
		else if (CEntityDB.class.isAssignableFrom(fieldType)) {
			component = createComboBox(field, meta, binder, dataProvider);
		}
		else {
			LOGGER.warn(
				"Unsupported field type '{}' for field '{}' - no component created",
				fieldType.getSimpleName(), field.getName());
			return null;
		}
		setRequiredIndicatorVisible(meta, component);
		setHelperText(meta, component);
		setComponentWidth(component, meta);
		// setclass name for styling in format of form-field{ComponentType}
		component.setClassName("form-field-" + component.getClass().getSimpleName());
		// Create field
		return component;
	}

	private static DatePicker createDatePicker(final Field field, final MetaData meta,
		final BeanValidationBinder<?> binder) {
		final DatePicker datePicker = new DatePicker();
		binder.bind(datePicker, field.getName());
		return datePicker;
	}

	private static DateTimePicker createDateTimePicker(final Field field,
		final MetaData meta, final BeanValidationBinder<?> binder) {
		final DateTimePicker dateTimePicker = new DateTimePicker();

		// For Instant fields, we need a custom converter
		if (field.getType() == Instant.class) {
			binder.forField(dateTimePicker)
				.withConverter(localDateTime -> localDateTime != null
					? localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()
					: null,
					instant -> instant != null ? instant
						.atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
						: null)
				.bind(field.getName());
		}
		else {
			binder.bind(dateTimePicker, field.getName());
		}
		return dateTimePicker;
	}

	@SuppressWarnings ({
		"unchecked", "rawtypes" }
	)
	private static Component createEnumComponent(final Field field, final MetaData meta,
		final BeanValidationBinder<?> binder) {
		final Class<? extends Enum> enumType = (Class<? extends Enum>) field.getType();
		final Enum[] enumConstants = enumType.getEnumConstants();

		if (meta.useRadioButtons()) {
			final RadioButtonGroup<Enum> radioGroup = new RadioButtonGroup<>();
			radioGroup.setItems(enumConstants);
			radioGroup.setItemLabelGenerator(Enum::name);
			binder.bind(radioGroup, field.getName());
			return radioGroup;
		}
		else {
			final ComboBox<Enum> comboBox = new ComboBox<>();
			comboBox.setItems(enumConstants);
			comboBox.setItemLabelGenerator(Enum::name);
			binder.bind(comboBox, field.getName());
			return comboBox;
		}
	}

	private static HorizontalLayout createFieldLayout(final MetaData meta,
		final Component component) {

		if (meta == null) {
			LOGGER.error("MetaData is null in createFieldLayout");
			return null;
		}

		if (component == null) {
			LOGGER.error("Component is null in createFieldLayout for displayName: {}",
				meta.displayName() != null ? meta.displayName() : "unknown");
			return null;
		}
		final HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setPadding(false);
		horizontalLayout.setSpacing(false);
		horizontalLayout.setMargin(false);
		horizontalLayout.setJustifyContentMode(JustifyContentMode.START);
		horizontalLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
		// Safe handling of display name
		final String displayName =
			((meta.displayName() != null) && !meta.displayName().trim().isEmpty())
				? meta.displayName() : "Field";
		final Div labelDiv = new Div(displayName);
		labelDiv.setMinWidth(LabelMinWidth_210PX);

		if (meta.required()) {
			labelDiv.getStyle().set("font-weight", "bold");
			// LOGGER.debug("Applied bold styling for required field: {}", displayName);
		}
		horizontalLayout.add(labelDiv);
		horizontalLayout.add(component);
		// LOGGER.debug("Successfully created field layout for: {}", displayName);
		return horizontalLayout;
	}

	private static NumberField createFloatingPointField(final Field field,
		final MetaData meta, final BeanValidationBinder<?> binder) {
		final NumberField numberField = new NumberField();

		try {

			if ((field.getType() == Double.class) || (field.getType() == double.class)) {
				binder.forField(numberField)
					.withConverter(value -> value == null ? null : value.doubleValue(),
						value -> value == null ? null : value, "Invalid double format")
					.bind(field.getName());
			}
			else if ((field.getType() == Float.class)
				|| (field.getType() == float.class)) {
				binder.forField(numberField)
					.withConverter(value -> value == null ? null : value.floatValue(),
						value -> value == null ? null : value.doubleValue(),
						"Invalid float format")
					.bind(field.getName());
			}
			else if (field.getType() == BigDecimal.class) {
				binder.forField(numberField)
					.withConverter(
						value -> value == null ? null : BigDecimal.valueOf(value),
						value -> value == null ? null : value.doubleValue(),
						"Invalid number format")
					.bind(field.getName());
			}
		} catch (final Exception e) {
			LOGGER.error("Failed to bind floating-point number field for field '{}': {}",
				field.getName(), e.getMessage());
			return null;
		}
		return numberField;
	}

	private static NumberField createIntegerField(final Field field, final MetaData meta,
		final BeanValidationBinder<?> binder) {
		final NumberField numberField = new NumberField();
		// Only allow integer values
		numberField.setStep(1);

		try {

			if ((field.getType() == Integer.class) || (field.getType() == int.class)) {
				binder.forField(numberField)
					.withConverter(value -> value == null ? null : value.intValue(),
						value -> value == null ? null : value.doubleValue(),
						"Invalid integer format")
					.bind(field.getName());
			}
			else if ((field.getType() == Long.class) || (field.getType() == long.class)) {
				binder.forField(numberField)
					.withConverter(value -> value == null ? null : value.longValue(),
						value -> value == null ? null : value.doubleValue(),
						"Invalid long format")
					.bind(field.getName());
			}
		} catch (final Exception e) {
			LOGGER.error("Failed to bind integer number field for field '{}': {}",
				field.getName(), e.getMessage());
			return null;
		}
		return numberField;
	}

	private static TextField createTextField(final Field field, final MetaData meta,
		final BeanValidationBinder<?> binder) {

		if ((field == null) || (meta == null) || (binder == null)) {
			LOGGER.error(
				"Null parameters in createTextField - field: {}, meta: {}, binder: {}",
				field != null ? field.getName() : "null",
				meta != null ? "present" : "null", binder != null ? "present" : "null");
			return null;
		}
		final TextField textField = new TextField();

		// Safe null checking for default value
		if ((meta.defaultValue() != null) && !meta.defaultValue().trim().isEmpty()) {
			textField.setValue(meta.defaultValue());
		}

		// Validate and set max length
		if (meta.maxLength() > 0) {
			textField.setMaxLength(meta.maxLength());
		}
		else if (meta.maxLength() < -1) {
			LOGGER.warn("Invalid maxLength value {} for field '{}' - should be > 0 or -1",
				meta.maxLength(), field.getName());
		}

		try {
			binder.bind(textField, field.getName());
		} catch (final Exception e) {
			LOGGER.error("Failed to bind text field for field '{}': {}", field.getName(),
				e.getMessage());
			return null;
		}
		return textField;
	}

	private static void getListOfAllFields(final Class<?> entityClass,
		final List<Field> allFields) {
		Class<?> current = entityClass;

		while ((current != null) && (current != Object.class)) {
			final Field[] declaredFields = current.getDeclaredFields();

			if (declaredFields != null) {
				allFields.addAll(Arrays.asList(declaredFields));
			}
			else {
				LOGGER.warn("getDeclaredFields() returned null for class: {}",
					current.getSimpleName());
			}
			current = current.getSuperclass();
		}

		// Enhanced field debugging with null checking
		for (final Field field : allFields) {

			if (field != null) {
				final MetaData metaData = field.getAnnotation(MetaData.class);

				if (metaData != null) {
					LOGGER.debug(
						"MetaData found - DisplayName: '{}', Order: {}, Required: {}, Hidden: {}",
						metaData.displayName(), metaData.order(), metaData.required(),
						metaData.hidden());
				}
				else {
					LOGGER.debug("No MetaData annotation found for field: {}",
						field.getName());
				}
			}
			else {
				LOGGER.warn("Null field encountered in field list");
			}
		}
	}

	private static List<Field> getSortedFilteredFieldsList(final List<Field> allFields) {
		return allFields.stream().filter(field -> {

			if (field == null) {
				LOGGER.warn("Null field encountered during filtering");
				return false;
			}
			return !java.lang.reflect.Modifier.isStatic(field.getModifiers());
		}).filter(field -> {
			final MetaData metaData = field.getAnnotation(MetaData.class);

			if (metaData == null) {
				return false;
			}
			return true;
		}).filter(field -> {
			final MetaData metaData = field.getAnnotation(MetaData.class);

			if (metaData.hidden()) {
				return false;
			}
			return true;
		}).sorted(Comparator.comparingInt(field -> {
			final MetaData metaData = field.getAnnotation(MetaData.class);
			return metaData != null ? metaData.order() : Integer.MAX_VALUE;
		})).collect(Collectors.toList());
	}

	private static <EntityClass> int processMetaForField(
		final BeanValidationBinder<EntityClass> binder,
		final ComboBoxDataProvider dataProvider, final FormLayout formLayout,
		int processedComponents, final Field field) {

		if (field == null) {
			LOGGER.warn("Null field encountered in sorted fields list");
			return processedComponents;
		}
		final MetaData meta = field.getAnnotation(MetaData.class);

		if (meta == null) {
			LOGGER.warn("Field '{}' lost MetaData annotation during processing",
				field.getName());
			return processedComponents;
		}

		try {
			// LOGGER.debug("Creating component for field '{}' with displayName
			// '{}'",field.getName(), meta.displayName());
			final Component component =
				createComponentForField(field, meta, binder, dataProvider);

			if (component != null) {
				final HorizontalLayout horizontalLayout =
					createFieldLayout(meta, component);

				if (horizontalLayout != null) {
					formLayout.add(horizontalLayout);
					processedComponents++;
				}
				else {
					LOGGER.warn("createFieldLayout returned null for field '{}'",
						field.getName());
				}
			}
			else {
				LOGGER.warn(
					"createComponentForField returned null for field '{}' of type {}",
					field.getName(), field.getType().getSimpleName());
			}
		} catch (final Exception e) {
			LOGGER.error(
				"Error creating component for field '{}' of type {} with MetaData displayName '{}': {}",
				field.getName(), field.getType().getSimpleName(), meta.displayName(),
				e.getMessage(), e);
		}
		return processedComponents;
	}

	private static void setComponentWidth(final Component component,
		final MetaData meta) {

		if (component == null) {
			LOGGER.warn("Component is null in setComponentWidth - cannot set width");
			return;
		}

		if (meta == null) {
			LOGGER.warn("MetaData is null in setComponentWidth - using default width");
			return;
		}

		if (component instanceof com.vaadin.flow.component.HasSize) {
			final com.vaadin.flow.component.HasSize hasSize =
				(com.vaadin.flow.component.HasSize) component;

			if ((meta.width() != null) && !meta.width().trim().isEmpty()) {

				try {
					hasSize.setWidth(meta.width());
				} catch (final Exception e) {
					LOGGER.warn("Failed to set component width '{}': {}", meta.width(),
						e.getMessage());
					// Fall back to full width
					hasSize.setWidthFull();
				}
			}
			else {
				hasSize.setWidthFull();
			}
		}
	}

	private static void setHelperText(final MetaData meta, final Component component) {

		if ((component == null) || (meta == null)
			|| ((component instanceof HasHelper) == false)) {
			LOGGER.warn("cannot set helper text");
			return;
		}

		if ((meta.description() != null) && !meta.description().trim().isEmpty()) {
			((HasHelper) component).setHelperText(meta.description());
		}
	}

	private static void setRequiredIndicatorVisible(final MetaData meta,
		final Component field) {

		if ((field == null) || (meta == null)
			|| ((field instanceof HasValueAndElement) == false)) {
			LOGGER.warn("cannot set helper text");
			return;
		}
		((HasValueAndElement<?, ?>) field).setReadOnly(meta.readOnly());
		((HasValueAndElement<?, ?>) field).setRequiredIndicatorVisible(meta.required());
	}

	private CEntityFormBuilder() {
		// Spring component - constructor managed by Spring
	}

	/**
	 * Sets the application context and initializes the data provider resolver. This
	 * method is called automatically by Spring.
	 * @param context the Spring application context
	 */
	@Override
	public void setApplicationContext(final ApplicationContext context) {

		try {
			CEntityFormBuilder.dataProviderResolver =
				context.getBean(CDataProviderResolver.class);
			LOGGER.info(
				"CEntityFormBuilder initialized with Spring context and data provider resolver");
		} catch (final Exception e) {
			LOGGER.warn(
				"Failed to initialize CDataProviderResolver - annotation-based providers will not work: {}",
				e.getMessage());
			CEntityFormBuilder.dataProviderResolver = null;
		}
	}
}
