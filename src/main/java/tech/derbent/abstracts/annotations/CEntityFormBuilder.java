package tech.derbent.abstracts.annotations;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

import tech.derbent.abstracts.components.CBinderFactory;
import tech.derbent.abstracts.components.CColorAwareComboBox;
import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.domains.CEntityConstants;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.utils.CAuxillaries;
import tech.derbent.abstracts.utils.CColorUtils;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.abstracts.views.CDiv;
import tech.derbent.abstracts.views.CHorizontalLayout;
import tech.derbent.abstracts.views.CVerticalLayout;
import tech.derbent.screens.service.CEntityFieldService;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;

@org.springframework.stereotype.Component
public final class CEntityFormBuilder<EntityClass> implements ApplicationContextAware {

	public interface ComboBoxDataProvider {

		<T extends CEntityDB<T>> List<T> getItems(Class<T> entityType);
	}

	private static ApplicationContext applicationContext;

	private static CDataProviderResolver dataProviderResolver;

	protected static final String LabelMinWidth_210PX = "210px";

	private static final Logger LOGGER =
		LoggerFactory.getLogger(CEntityFormBuilder.class);

	@SuppressWarnings ("unchecked")
	public static <EntityClass> CVerticalLayout buildEnhancedForm(
		final Class<?> entityClass) throws NoSuchMethodException, SecurityException,
		IllegalAccessException, InvocationTargetException {
		final CEnhancedBinder<EntityClass> enhancedBinder =
			CBinderFactory.createEnhancedBinder((Class<EntityClass>) entityClass);
		return buildForm(entityClass, enhancedBinder, null);
	}

	@SuppressWarnings ("unchecked")
	public static <EntityClass> CVerticalLayout
		buildEnhancedForm(final Class<?> entityClass, final List<String> entityFields)
			throws NoSuchMethodException, SecurityException, IllegalAccessException,
			InvocationTargetException {
		final CEnhancedBinder<EntityClass> enhancedBinder =
			CBinderFactory.createEnhancedBinder((Class<EntityClass>) entityClass);
		return buildForm(entityClass, enhancedBinder, entityFields);
	}

	public static <EntityClass> CVerticalLayout buildForm(final Class<?> entityClass,
		final CEnhancedBinder<EntityClass> binder) throws NoSuchMethodException,
		SecurityException, IllegalAccessException, InvocationTargetException {
		return buildForm(entityClass, binder, null, null, null,
			new CVerticalLayout(false, false, false));
	}

	public static <EntityClass> CVerticalLayout buildForm(final Class<?> entityClass,
		final CEnhancedBinder<EntityClass> binder, final List<String> entityFields)
		throws NoSuchMethodException, SecurityException, IllegalAccessException,
		InvocationTargetException {
		return buildForm(entityClass, binder, entityFields, null, null,
			new CVerticalLayout(false, false, false));
	}

	public static <EntityClass> CVerticalLayout buildForm(final Class<?> entityClass,
		final CEnhancedBinder<EntityClass> binder, List<String> entityFields,
		final Map<String, Component> mapComponents,
		final Map<String, CHorizontalLayout> mapHorizontalLayouts,
		final CVerticalLayout formLayout) throws NoSuchMethodException, SecurityException,
		IllegalAccessException, InvocationTargetException {
		Check.notNull(entityClass, "Entity class");
		Check.notNull(binder, "Binder of " + entityClass.getSimpleName());
		// final FormLayout formLayout = new FormLayout();
		final List<Field> allFields = new ArrayList<>();
		getListOfAllFields(entityClass, allFields);
		final List<Field> sortedFields = getSortedFilteredFieldsList(allFields);
		LOGGER.info("Processing {} visible fields for form generation",
			sortedFields.size());
		// Create components with enhanced error handling and logging

		if (entityFields == null) {
			entityFields =
				sortedFields.stream().map(Field::getName).collect(Collectors.toList());
		}

		for (final String fieldName : entityFields) {
			final Field field = sortedFields.stream()
				.filter(f -> f.getName().equals(fieldName)).findFirst().orElse(null);

			if (field == null) {
				LOGGER.warn("Field '{}' not found in entity class {}", fieldName,
					entityClass.getSimpleName());
			}
			Check.notNull(field, "Field '" + fieldName + "' not found in entity class "
				+ entityClass.getSimpleName());
			final Component component =
				processField(binder, formLayout, mapHorizontalLayouts, field);

			if ((component != null) && (mapComponents != null)) {
				mapComponents.put(fieldName, component);
			}
		}
		return formLayout;
	}

	private static List<String> callStringDataMethod(final Object serviceBean,
		final String methodName, final String fieldName) throws NoSuchMethodException,
		SecurityException, IllegalAccessException, InvocationTargetException {

		// Try to find and call the method
		try {
			final Method method = serviceBean.getClass().getMethod(methodName);
			Check.notNull(method, "Method '" + methodName
				+ "' on service bean for field '" + fieldName + "'");
			final Object result = method.invoke(serviceBean);
			Check.notNull(result, "Result of method '" + methodName
				+ "' on service bean for field '" + fieldName + "'");
			Check.condition(result instanceof List,
				"Method '" + methodName + "' on service bean for field '" + fieldName
					+ "' did not return a List");
			final List<?> rawList = (List<?>) result;
			// Convert to List<String> if possible
			final List<String> stringList =
				rawList.stream().filter(item -> item instanceof String)
					.map(item -> (String) item).toList();
			return stringList;
		} catch (final Exception e) {
			LOGGER.error("Failed to call method '{}' on service bean for field '{}': {}",
				methodName, fieldName, e.getMessage());
			throw e;
		}
	}

	// call stringdatamethod with one parameter
	private static List<String> callStringDataMethod(final Object serviceBean,
		final String methodName, final String fieldName, final Object parameter)
		throws NoSuchMethodException, SecurityException, IllegalAccessException,
		InvocationTargetException {

		try {
			// Try to find and call the method with one parameter
			final Method method =
				serviceBean.getClass().getMethod(methodName, Object.class);
			Check.notNull(method, "Method '" + methodName
				+ "' on service bean for field '" + fieldName + "'");
			final Object result = method.invoke(serviceBean, parameter);
			Check.notNull(result, "Result of method '" + methodName
				+ "' on service bean for field '" + fieldName + "'");
			Check.condition(result instanceof List,
				"Method '" + methodName + "' on service bean for field '" + fieldName
					+ "' did not return a List");
			final List<?> rawList = (List<?>) result;
			// Convert to List<String> if possible
			final List<String> stringList =
				rawList.stream().filter(item -> item instanceof String)
					.map(item -> (String) item).toList();
			return stringList;
		} catch (final Exception e) {
			LOGGER.error("Failed to call method '{}' on service bean for field '{}': {}",
				methodName, fieldName, e.getMessage());
			throw e;
		}
	}

	private static NumberField createBigDecimalField(final EntityFieldInfo fieldInfo,
		final CEnhancedBinder<?> binder) {
		Check.notNull(binder, "Binder for BigDecimal field creation");
		Check.notNull(fieldInfo, "FieldInfo for BigDecimal field creation");
		final NumberField numberField = new NumberField();
		CAuxillaries.setId(numberField);
		numberField.setStep(0.01); // Set decimal step for BigDecimal fields

		if ((fieldInfo.getDefaultValue() != null)
			&& !fieldInfo.getDefaultValue().trim().isEmpty()) {

			try {
				final double defaultVal = Double.parseDouble(fieldInfo.getDefaultValue());
				numberField.setValue(defaultVal);
			} catch (final NumberFormatException e) {
				LOGGER.error(
					"Failed to parse default value '{}' as number for field '{}': {}",
					fieldInfo.getDefaultValue(), fieldInfo.getDisplayName(),
					e.getMessage());
			}
		}

		try {
			// Use converter to handle BigDecimal conversion
			binder.forField(numberField)
				.withConverter(value -> value != null ? BigDecimal.valueOf(value) : null,
					value -> value != null ? value.doubleValue() : null,
					"Invalid decimal value")
				.bind(fieldInfo.getFieldName());
			LOGGER.debug(
				"Successfully bound NumberField with BigDecimal converter for field '{}'",
				fieldInfo.getFieldName());
		} catch (final Exception e) {
			LOGGER.error(
				"Failed to bind BigDecimal field for field '{}': {} - using fallback binding",
				fieldInfo.getFieldName(), e.getMessage());

			// Fallback to simple binding without converter
			try {
				safeBindComponentWithField(binder, numberField, fieldInfo.getFieldName(),
					"NumberField(BigDecimal-fallback)");
			} catch (final Exception fallbackException) {
				LOGGER.error("Fallback binding also failed for BigDecimal field '{}': {}",
					fieldInfo.getFieldName(), fallbackException.getMessage());
			}
		}
		return numberField;
	}

	private static Checkbox createCheckbox(final EntityFieldInfo fieldInfo,
		final CEnhancedBinder<?> binder) {
		Check.notNull(fieldInfo, "FieldInfo for checkbox creation");
		Check.notNull(binder, "Binder for checkbox creation");
		final Checkbox checkbox = new Checkbox();
		// Set ID for better test automation
		CAuxillaries.setId(checkbox);

		// Safe null checking and parsing for default value
		if ((fieldInfo.getDefaultValue() != null)
			&& !fieldInfo.getDefaultValue().trim().isEmpty()) {

			try {
				checkbox.setValue(Boolean.parseBoolean(fieldInfo.getDefaultValue()));
				// LOGGER.debug("Set default value for checkbox '{}': {}",
				// field.getName(), meta.defaultValue());
			} catch (final Exception e) {
				LOGGER.warn("Invalid boolean default value '{}' for field '{}': {}",
					fieldInfo.getDefaultValue(), fieldInfo.getFieldName(),
					e.getMessage());
			}
		}

		try {
			safeBindComponentWithField(binder, checkbox, fieldInfo.getFieldName(),
				"Checkbox");
		} catch (final Exception e) {
			LOGGER.error("Failed to bind checkbox for field '{}': {}",
				fieldInfo.getFieldName(), e.getMessage());
			return null;
		}
		return checkbox;
	}

	@SuppressWarnings ("unchecked")
	public static <T extends CEntityDB<T>> ComboBox<T>
		createComboBox(final EntityFieldInfo fieldInfo, final CEnhancedBinder<?> binder) {
		Check.notNull(fieldInfo, "FieldInfo for ComboBox creation");
		Check.notNull(binder, "Binder for ComboBox creation");
		LOGGER.debug("Creating CColorAwareComboBox for field: {}",
			fieldInfo.getFieldName());
		final ComboBox<T> comboBox = new CColorAwareComboBox<>(fieldInfo);
		comboBox
			.setItemLabelGenerator(item -> CColorUtils.getDisplayTextFromEntity(item));
		// Data provider resolution using CDataProviderResolver
		List<T> items = null;
		Check.notNull(dataProviderResolver,
			"DataProviderResolver for field " + fieldInfo.getFieldName());
		items = dataProviderResolver.resolveData(fieldInfo);
		Check.notNull(items, "Items for field " + fieldInfo.getFieldName() + " of type "
			+ fieldInfo.getJavaType());

		if (fieldInfo.isClearOnEmptyData() && items.isEmpty()) {
			comboBox.setValue(null);
		}
		comboBox.setItems(items);

		if (!items.isEmpty()) {

			if ((fieldInfo.getDefaultValue() != null)
				&& !fieldInfo.getDefaultValue().trim().isEmpty()) {
				// For entity types, try to find by name or toString match
				final T defaultItem = items.stream().filter(item -> {
					final String itemDisplay = CColorUtils.getDisplayTextFromEntity(item);
					return fieldInfo.getDefaultValue().equals(itemDisplay);
				}).findFirst().orElse(null);

				if (defaultItem != null) {
					comboBox.setValue(defaultItem);
				}
			}
			else if (fieldInfo.isAutoSelectFirst()) {
				comboBox.setValue(items.get(0));
			}
		}
		safeBindComponentWithField(binder, comboBox, fieldInfo.getFieldName(),
			"ComboBox");
		return comboBox;
	}

	@SuppressWarnings ("rawtypes")
	private static <T> MultiSelectComboBox<T> createComboBoxMultiSelect(
		final EntityFieldInfo fieldInfo, final CEnhancedBinder<?> binder) {
		Check.notNull(fieldInfo, "FieldInfo for ComboBox creation");
		Check.notNull(binder, "Binder for ComboBox creation");
		LOGGER.debug("Creating MultiSelectComboBox for field: {}",
			fieldInfo.getFieldName());
		final MultiSelectComboBox<T> comboBox =
			new MultiSelectComboBox<T>(fieldInfo.getDisplayName());
		comboBox.setItemLabelGenerator(item -> {

			if (item instanceof CEntityNamed<?>) {
				return ((CEntityNamed) item).getName();
			}

			if (item instanceof CEntityDB) {
				return CColorUtils.getDisplayTextFromEntity(item);
			}

			if (item instanceof String) {
				return (String) item;
			}
			return "Unknown Item: " + item.toString();
		});
		// Data provider resolution using CDataProviderResolver
		Check.notNull(dataProviderResolver,
			"DataProviderResolver for field " + fieldInfo.getFieldName());
		final Set<T> items = Set.of(dataProviderResolver.resolveData(fieldInfo).stream()
			.toArray(CEntityDB[]::new));
		Check.notNull(items, "Items for field " + fieldInfo.getFieldName() + " of type "
			+ fieldInfo.getJavaType());

		if (fieldInfo.isClearOnEmptyData() && items.isEmpty()) {
			comboBox.setValue(Set.of());
		}
		comboBox.setItems(items);
		safeBindComponentWithField(binder, comboBox, fieldInfo.getFieldName(),
			"ComboBox");
		return comboBox;
	}

	private static Component createComponentForField(final EntityFieldInfo fieldInfo,
		final CEnhancedBinder<?> binder) throws NoSuchMethodException, SecurityException,
		IllegalAccessException, InvocationTargetException {
		Component component = null;
		Check.notNull(fieldInfo, "Field");
		Check.notNull(binder, "Binder for field " + fieldInfo.getDisplayName());
		final Class<?> fieldType = fieldInfo.getFieldTypeClass();
		Check.notNull(fieldType, "Field type for field " + fieldInfo.getDisplayName());
		// Check if field should be rendered as ComboBox based on metadata
		final boolean hasDataProvider = (fieldInfo.getDataProviderBean() != null)
			&& !fieldInfo.getDataProviderBean().trim().isEmpty();

		if (hasDataProvider && (fieldType == String.class)) {
			component = createStringComboBox(fieldInfo, binder);
		}
		else if (hasDataProvider && fieldInfo.getJavaType().equals("Set")) {
			component = createComboBoxMultiSelect(fieldInfo, binder);
		}
		else if (hasDataProvider || CEntityDB.class.isAssignableFrom(fieldType)) {
			component = createComboBox(fieldInfo, binder);
		}
		else if ((fieldType == Boolean.class) || (fieldType == boolean.class)) {
			component = createCheckbox(fieldInfo, binder);
		}
		else if ((fieldType == String.class)
			&& (fieldInfo.getMaxLength() >= CEntityConstants.MAX_LENGTH_DESCRIPTION)) {
			component = createTextArea(fieldInfo, binder);
		}
		else if ((fieldType == String.class)
			&& (fieldInfo.getMaxLength() < CEntityConstants.MAX_LENGTH_DESCRIPTION)) {
			component = createTextField(fieldInfo, binder);
		}
		else if ((fieldType == Integer.class) || (fieldType == int.class)
			|| (fieldType == Long.class) || (fieldType == long.class)) {
			// Integer types
			component = createIntegerField(fieldInfo, binder);
		}
		else if (fieldType == BigDecimal.class) {
			component = createBigDecimalField(fieldInfo, binder);
		}
		else if ((fieldType == Double.class) || (fieldType == double.class)
			|| (fieldType == Float.class) || (fieldType == float.class)) {
			// Floating-point types
			component = createFloatingPointField(fieldInfo, binder);
		}
		else if (fieldType == LocalDate.class) {
			component = createDatePicker(fieldInfo, binder);
		}
		else if ((fieldType == LocalDateTime.class) || (fieldType == Instant.class)) {
			component = createDateTimePicker(fieldInfo, binder);
		}
		else if (fieldType.isEnum()) {
			component = createEnumComponent(fieldInfo, binder);
		}
		else {
			Check.condition(false, "Unsupported field type: " + fieldType.getSimpleName()
				+ " for field: " + fieldInfo.getDisplayName());
		}
		Check.notNull(component, "Component for field " + fieldInfo.getFieldName()
			+ " of type " + fieldType.getSimpleName());
		setRequiredIndicatorVisible(fieldInfo, component);
		// dont use helper text for Checkbox components setHelperText(meta, component);
		setComponentWidth(component, fieldInfo.getWidth());
		// setclass name for styling in format of form-field{ComponentType}
		component.setClassName("form-field-" + component.getClass().getSimpleName());
		// Create field
		return component;
	}

	private static DatePicker createDatePicker(final EntityFieldInfo fieldInfo,
		final CEnhancedBinder<?> binder) {
		final DatePicker datePicker = new DatePicker();
		CAuxillaries.setId(datePicker);
		safeBindComponentWithField(binder, datePicker, fieldInfo.getFieldName(),
			"DatePicker");
		return datePicker;
	}

	private static DateTimePicker createDateTimePicker(final EntityFieldInfo fieldInfo,
		final CEnhancedBinder<?> binder) {
		final DateTimePicker dateTimePicker = new DateTimePicker();
		CAuxillaries.setId(dateTimePicker);
		safeBindComponentWithField(binder, dateTimePicker, fieldInfo.getFieldName(),
			"DateTimePicker");
		return dateTimePicker;
	}

	@SuppressWarnings ("unchecked")
	public static <EntityClass> CEnhancedBinder<EntityClass>
		createEnhancedBinder(final Class<?> entityClass) {
		Check.notNull(entityClass, "Entity class for enhanced binder");
		return CBinderFactory.createEnhancedBinder((Class<EntityClass>) entityClass);
	}

	@SuppressWarnings ("unchecked")
	private static Component createEnumComponent(final EntityFieldInfo fieldInfo,
		final CEnhancedBinder<?> binder) {
		final Class<? extends Enum> enumType =
			(Class<? extends Enum>) fieldInfo.getFieldTypeClass();
		final Enum[] enumConstants = enumType.getEnumConstants();

		if (fieldInfo.isUseRadioButtons()) {
			final RadioButtonGroup<Enum> radioGroup = new RadioButtonGroup<>();
			radioGroup.setItems(enumConstants);
			radioGroup.setItemLabelGenerator(Enum::name);
			safeBindComponentWithField(binder, radioGroup, fieldInfo.getFieldName(),
				"RadioButtonGroup");
			return radioGroup;
		}
		else {
			final ComboBox<Enum> comboBox = new ComboBox<>();
			// Set ID for better test automation
			CAuxillaries.setId(comboBox);
			// Following coding guidelines: All selective ComboBoxes must be selection
			// only (user must not be able to type arbitrary text)
			comboBox.setAllowCustomValue(false);
			comboBox.setItems(enumConstants);
			comboBox.setItemLabelGenerator(Enum::name);
			safeBindComponentWithField(binder, comboBox, fieldInfo.getFieldName(),
				"ComboBox(Enum)");
			return comboBox;
		}
	}

	private static CHorizontalLayout createFieldLayout(final EntityFieldInfo fieldInfo,
		final Component component) {
		Check.notNull(fieldInfo, "MetaData for field layout");
		Check.notNull(component, "Component for field layout" + fieldInfo.getFieldName());
		final CHorizontalLayout horizontalLayout = CHorizontalLayout.forForm();
		final CDiv labelDiv = new CDiv(fieldInfo.getDisplayName());
		labelDiv.setClassName("form-field-label");

		if (fieldInfo.isRequired()) {
			labelDiv.getStyle().set("font-weight", "bold");
		}
		horizontalLayout.add(labelDiv, component);
		return horizontalLayout;
	}

	private static NumberField createFloatingPointField(final EntityFieldInfo fieldInfo,
		final CEnhancedBinder<?> binder) {
		Check.notNull(binder, "Binder for floating point field creation");
		final NumberField numberField = new NumberField();
		// Set ID for better test automation
		CAuxillaries.setId(numberField);
		// Set step for floating point fields
		numberField.setStep(0.01);

		// Set default value if specified
		if ((fieldInfo.getDefaultValue() != null)
			&& !fieldInfo.getDefaultValue().trim().isEmpty()) {
			final double defaultVal = Double.parseDouble(fieldInfo.getDefaultValue());
			numberField.setValue(defaultVal);
		}
		safeBindComponent(binder, numberField, fieldInfo.getFieldName(), "NumberField");
		return numberField;
	}

	private static NumberField createIntegerField(final EntityFieldInfo fieldInfo,
		final CEnhancedBinder<?> binder) {
		Check.notNull(binder, "Binder for integer field creation");
		final NumberField numberField = new NumberField();
		CAuxillaries.setId(numberField);
		numberField.setStep(1);

		// Set default value if specified
		if ((fieldInfo.getDefaultValue() != null)
			&& !fieldInfo.getDefaultValue().trim().isEmpty()) {
			final double defaultVal = Double.parseDouble(fieldInfo.getDefaultValue());
			numberField.setValue(defaultVal);
		}
		// Handle different integer types with proper conversion
		final Class<?> fieldType = fieldInfo.getFieldTypeClass();
		final String propertyName = fieldInfo.getFieldName();

		if ((fieldType == Integer.class) || (fieldType == int.class)) {
			binder.forField(numberField)
				.withConverter(value -> value != null ? value.intValue() : null,
					value -> value != null ? value.doubleValue() : null,
					"Invalid integer value")
				.bind(propertyName);
			LOGGER.debug(
				"Successfully bound NumberField with Integer converter for field '{}'",
				fieldInfo.getFieldName());
		}
		else if ((fieldType == Long.class) || (fieldType == long.class)) {
			binder.forField(numberField)
				.withConverter(value -> value != null ? value.longValue() : null,
					value -> value != null ? value.doubleValue() : null,
					"Invalid long value")
				.bind(propertyName);
			LOGGER.debug(
				"Successfully bound NumberField with Long converter for field '{}'",
				fieldInfo.getFieldName());
		}
		else {
			// Fallback for other number types (Double, etc.)
			binder.bind(numberField, propertyName);
		}
		return numberField;
	}

	private static ComboBox<String> createStringComboBox(final EntityFieldInfo fieldInfo,
		final CEnhancedBinder<?> binder) throws NoSuchMethodException, SecurityException,
		IllegalAccessException, InvocationTargetException {
		Check.notNull(fieldInfo, "Field for String ComboBox creation");
		Check.notNull(binder, "Binder for String ComboBox creation");
		final ComboBox<String> comboBox = new ComboBox<>();
		// Configure basic properties from metadata
		comboBox.setLabel(fieldInfo.getDisplayName());
		comboBox.setPlaceholder(fieldInfo.getPlaceholder());
		comboBox.setAllowCustomValue(fieldInfo.isAllowCustomValue());
		comboBox.setReadOnly(fieldInfo.isComboboxReadOnly() || fieldInfo.isReadOnly());

		// Set width if specified
		if (!fieldInfo.getWidth().trim().isEmpty()) {
			comboBox.setWidth(fieldInfo.getWidth());
		}
		// Resolve String data using data provider
		final List<String> items = resolveStringData(fieldInfo);
		comboBox.setItems(items);

		// Handle clearOnEmptyData configuration
		if (fieldInfo.isClearOnEmptyData() && items.isEmpty()) {
			comboBox.setValue(null);
		}
		// Handle default value
		final boolean hasDefaultValue = (fieldInfo.getDefaultValue() != null)
			&& !fieldInfo.getDefaultValue().trim().isEmpty();

		if (hasDefaultValue) {

			// For String ComboBox, try to match default value exactly
			if (items.contains(fieldInfo.getDefaultValue())) {
				comboBox.setValue(fieldInfo.getDefaultValue());
				LOGGER.debug("Set String ComboBox default value for field '{}': '{}'",
					fieldInfo.getFieldName(), fieldInfo.getDefaultValue());
			}
			else {
				LOGGER.warn("Default value '{}' not found in items for String field '{}'",
					fieldInfo.getDefaultValue(), fieldInfo.getFieldName());
			}
		}
		else if (fieldInfo.isAutoSelectFirst() && !items.isEmpty()) {
			// Auto-select first item if configured
			comboBox.setValue(items.get(0));
			LOGGER.debug("Auto-selected first string item for field '{}': '{}'",
				fieldInfo.getFieldName(), items.get(0));
		}
		// Bind to field
		safeBindComponentWithField(binder, comboBox, fieldInfo.getFieldName(),
			"String ComboBox");
		return comboBox;
	}

	private static TextArea createTextArea(final EntityFieldInfo fieldInfo,
		final CEnhancedBinder<?> binder) {
		Check.notNull(binder, "Binder for text area creation");
		final TextArea item = new TextArea();

		if (fieldInfo.getMaxLength() > 0) {
			item.setMaxLength(fieldInfo.getMaxLength());
		}
		item.setWidthFull();
		item.setMinHeight("100px");

		if ((fieldInfo.getDefaultValue() != null)
			&& !fieldInfo.getDefaultValue().trim().isEmpty()) {

			try {
				item.setValue(fieldInfo.getDefaultValue());
			} catch (final Exception e) {
				LOGGER.error("Failed to set default value '{}' for text area '{}': {}",
					fieldInfo.getDefaultValue(), fieldInfo.getFieldName(),
					e.getMessage());
			}
		}

		try {
			safeBindComponentWithField(binder, item, fieldInfo.getFieldName(),
				"TextArea");
		} catch (final Exception e) {
			LOGGER.error("Failed to bind text area for field '{}': {}",
				fieldInfo.getFieldName(), e.getMessage());
			return null;
		}
		return item;
	}

	private static TextField createTextField(final EntityFieldInfo fieldInfo,
		final CEnhancedBinder<?> binder) {
		Check.notNull(binder, "Binder for text field creation");
		final TextField item = new TextField();
		// Set ID for better test automation
		CAuxillaries.setId(item);
		item.setClassName("plain-look-textfield");

		if (fieldInfo.getMaxLength() > 0) {
			item.setMaxLength(fieldInfo.getMaxLength());
		}
		item.setWidthFull();

		if ((fieldInfo.getDefaultValue() != null)
			&& !fieldInfo.getDefaultValue().trim().isEmpty()) {

			try {
				item.setValue(fieldInfo.getDefaultValue());
			} catch (final Exception e) {
				LOGGER.error("Failed to set default value '{}' for text area '{}': {}",
					fieldInfo.getDefaultValue(), fieldInfo.getFieldName(),
					e.getMessage());
			}
		}

		try {
			safeBindComponentWithField(binder, item, fieldInfo.getFieldName(),
				"TextField");
		} catch (final Exception e) {
			LOGGER.error("Failed to bind text field for field '{}': {}",
				fieldInfo.getFieldName(), e.getMessage());
			return null;
		}
		return item;
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
	}

	private static List<Field> getSortedFilteredFieldsList(final List<Field> allFields) {
		return allFields.stream().filter(field -> {
			Check.notNull(field, "Field in sorted filtered fields list");
			return !Modifier.isStatic(field.getModifiers());
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

	private static <EntityClass> Component processField(
		final CEnhancedBinder<EntityClass> binder, final VerticalLayout formLayout,
		final Map<String, CHorizontalLayout> mapHorizontalLayouts, final Field field)
		throws NoSuchMethodException, SecurityException, IllegalAccessException,
		InvocationTargetException {
		Check.notNull(field, "field");
		final EntityFieldInfo fieldInfo = CEntityFieldService.createFieldInfo(field);
		Check.notNull(fieldInfo, "MetaData for field " + field.getName());
		final Component component = createComponentForField(fieldInfo, binder);
		Check.notNull(component, "Component for field " + field.getName()
			+ " with displayName " + fieldInfo.getFieldName());
		final CHorizontalLayout horizontalLayout =
			createFieldLayout(fieldInfo, component);
		Check.notNull(horizontalLayout, "HorizontalLayout for field " + field.getName()
			+ " with displayName " + fieldInfo.getDisplayName());
		formLayout.add(horizontalLayout);

		if (mapHorizontalLayouts != null) {
			mapHorizontalLayouts.put(field.getName(), horizontalLayout);
		}
		return component;
	}

	/**
	 * Recursively searches for ComboBox components and resets them to their first item.
	 */
	@SuppressWarnings ("unchecked")
	private static void resetComboBoxesRecursively(
		final com.vaadin.flow.component.HasComponents container) {
		container.getElement().getChildren().forEach(element -> {

			// Get the component from the element
			if (element.getComponent().isPresent()) {
				final com.vaadin.flow.component.Component component =
					element.getComponent().get();

				if (component instanceof ComboBox) {
					final ComboBox<Object> comboBox = (ComboBox<Object>) component;

					try {
						// Get the first item from the ComboBox data provider
						final java.util.Optional<Object> firstItem =
							comboBox.getDataProvider()
								.fetch(new com.vaadin.flow.data.provider.Query<>())
								.findFirst();

						if (firstItem.isPresent()) {
							comboBox.setValue(firstItem.get());
							LOGGER.debug("Reset ComboBox to first item: {}",
								firstItem.get());
						}
						else {
							LOGGER.debug("ComboBox has no items to reset to");
						}
					} catch (final Exception e) {
						LOGGER.warn("Error resetting ComboBox to first item: {}",
							e.getMessage());
					}
				}
				else if (component instanceof com.vaadin.flow.component.HasComponents) {
					// Recursively check child components
					resetComboBoxesRecursively(
						(com.vaadin.flow.component.HasComponents) component);
				}
			}
		});
	}

	/**
	 * Resets all ComboBox components in a container to their first available item. This
	 * method is useful for implementing "New" button behavior where ComboBoxes should
	 * default to their first option instead of being empty.
	 * @param container the container component to search for ComboBoxes
	 */
	public static void resetComboBoxesToFirstItem(
		final com.vaadin.flow.component.HasComponents container) {
		Check.notNull(container, "Container for resetting ComboBoxes to first item");
		resetComboBoxesRecursively(container);
	}

	private static List<String> resolveStringData(final EntityFieldInfo fieldInfo)
		throws NoSuchMethodException, SecurityException, IllegalAccessException,
		InvocationTargetException {
		Check.notNull(fieldInfo, "EntityFieldInfo for String data resolution");
		Check.notNull(applicationContext,
			"ApplicationContext for String data resolution");
		// Try to resolve data provider bean
		final String beanName = fieldInfo.getDataProviderBean();
		Check.notNull(beanName, "Data provider bean name for String field '"
			+ fieldInfo.getFieldName() + "'");
		Check.condition(!beanName.trim().isEmpty(),
			"Data provider bean name for String field '" + fieldInfo.getFieldName()
				+ "' must not be empty");

		if (beanName.equals("none")) {
			return List.of(); // No data provider configured, return empty list
		}
		Check.condition(applicationContext.containsBean(beanName),
			"Data provider bean '" + beanName + "' for String field '"
				+ fieldInfo.getFieldName() + "' must be present in Spring context");
		final Object serviceBean = applicationContext.getBean(beanName);
		LOGGER.debug("Retrieved data provider bean '{}' for String field '{}'", beanName,
			fieldInfo.getFieldName());
		// Determine method name to call
		final String methodName = fieldInfo.getDataProviderMethod();
		Check.notNull(methodName, "Data provider method name for String field '"
			+ fieldInfo.getFieldName() + "'");
		Check.condition(!methodName.trim().isEmpty(),
			"Data provider method name for String field '" + fieldInfo.getFieldName()
				+ "' must not be empty");

		// Try to call the method
		if ((fieldInfo.getDataProviderParamMethod() != null)
			&& (fieldInfo.getDataProviderParamMethod().trim().length() > 0)) {

			try {
				// call dataprovider param method, returning Object as result
				final String methodstr = fieldInfo.getDataProviderParamMethod();
				final Method method = serviceBean.getClass().getMethod(methodstr);
				Check.notNull(method, "Method '" + methodName
					+ "' on service bean for field '" + fieldInfo.getFieldName() + "'");
				final Object param = method.invoke(serviceBean);
				return callStringDataMethod(serviceBean, methodName,
					fieldInfo.getFieldName(), param);
			} catch (final NoSuchMethodException e) {
				LOGGER.error(
					"Data provider method '{}' not found on service bean for field '{}': {}",
					fieldInfo.getDataProviderParamMethod(), fieldInfo.getFieldName(),
					e.getMessage());
				throw e;
			}
		}
		return callStringDataMethod(serviceBean, methodName, fieldInfo.getFieldName());
	}

	/**
	 * Safely binds a component to a field, ensuring no incomplete bindings are left. This
	 * method prevents the "All bindings created with forField must be completed" error.
	 */
	private static void safeBindComponent(final CEnhancedBinder<?> binder,
		final HasValueAndElement<?, ?> component, final String fieldName,
		final String componentType) {
		Check.notNull(binder, "Binder for safe binding");
		Check.notNull(component, "Component for safe binding");
		Check.notNull(fieldName, "Field name for safe binding");

		try {
			binder.bind(component, fieldName);
		} catch (final Exception e) {
			LOGGER.error(
				"Failed to bind {} for field '{}': {} - this may cause incomplete bindings",
				componentType, fieldName, e.getMessage(), e);
			// Don't throw - just log the error to prevent form generation failure But
			// warn that this might cause incomplete bindings
			binder.removeBinding(fieldName); // Eğer CEnhancedBinder bunu destekliyorsa
		}
	}

	/**
	 * Safely binds a component to a field using the correct property name, ensuring no
	 * incomplete bindings are left.
	 */
	private static void safeBindComponentWithField(final CEnhancedBinder<?> binder,
		final HasValueAndElement<?, ?> component, final String propertyName,
		final String componentType) {

		if ((binder == null) || (component == null) || (propertyName == null)) {
			LOGGER.error(
				"Null parameters in safeBindComponentWithField - binder: {}, component: {}, field: {}",
				binder != null ? "present" : "null",
				component != null ? "present" : "null", propertyName);
			return;
		}
		safeBindComponent(binder, component, propertyName, componentType);
	}

	private static void setComponentWidth(final Component component, final String width) {

		if ((component == null) || (width == null)) {
			return;
		}

		if (component instanceof com.vaadin.flow.component.HasSize) {
			final com.vaadin.flow.component.HasSize hasSize =
				(com.vaadin.flow.component.HasSize) component;

			if ((width != null) && !width.trim().isEmpty()) {

				try {
					hasSize.setWidth(width);
				} catch (final Exception e) {
					LOGGER.warn("Failed to set component width '{}': {}", width,
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

	private static void setRequiredIndicatorVisible(final EntityFieldInfo fieldInfo,
		final Component field) {
		((HasValueAndElement<?, ?>) field).setReadOnly(fieldInfo.isReadOnly());
		((HasValueAndElement<?, ?>) field)
			.setRequiredIndicatorVisible(fieldInfo.isRequired());
	}

	final CVerticalLayout formLayout;

	final Map<String, Component> componentMap;

	final Map<String, CHorizontalLayout> horizontalLayoutMap;

	public CEntityFormBuilder() {
		this.componentMap = new HashMap<>();
		this.horizontalLayoutMap = new HashMap<>();
		this.formLayout = new CVerticalLayout(false, false, false);
	}

	public CEntityFormBuilder(final Class<?> entityClass,
		final CEnhancedBinder<EntityClass> binder, final List<String> entityFields)
		throws NoSuchMethodException, SecurityException, IllegalAccessException,
		InvocationTargetException {
		this.componentMap = new HashMap<>();
		this.horizontalLayoutMap = new HashMap<>();
		this.formLayout = new CVerticalLayout(false, false, false);
		CEntityFormBuilder.buildForm(entityClass, binder, entityFields, componentMap,
			horizontalLayoutMap, formLayout);
	}

	public CVerticalLayout build(final Class<?> entityClass,
		final CEnhancedBinder<EntityClass> binder, final List<String> entityFields)
		throws NoSuchMethodException, SecurityException, IllegalAccessException,
		InvocationTargetException {
		return CEntityFormBuilder.buildForm(entityClass, binder, entityFields,
			componentMap, horizontalLayoutMap, formLayout);
	}

	public Component getComponent(final String fieldName) {
		Check.notNull(fieldName, "Field name for component retrieval");
		final Component component = componentMap.get(fieldName);
		Check.notNull(component,
			"Component for field " + fieldName + " not found in form builder map");
		return component;
	}

	public CVerticalLayout getFormLayout() { return formLayout; }

	public CHorizontalLayout getHorizontalLayout(final String fieldName) {
		Check.notNull(fieldName, "Field name for horizontal layout retrieval");
		final CHorizontalLayout layout = horizontalLayoutMap.get(fieldName);
		Check.notNull(layout,
			"HorizontalLayout for field " + fieldName + " not found in form builder map");
		return layout;
	}

	/**
	 * Sets the application context and initializes the data provider resolver. This
	 * method is called automatically by Spring.
	 * @param context the Spring application context
	 */
	@Override
	public void setApplicationContext(final ApplicationContext context) {
		// Store the application context for String data provider resolution
		CEntityFormBuilder.applicationContext = context;

		try {
			CEntityFormBuilder.dataProviderResolver =
				context.getBean(CDataProviderResolver.class);
		} catch (final Exception e) {
			LOGGER.warn(
				"Failed to initialize CDataProviderResolver - annotation-based providers will not work: {}",
				e.getMessage());
			CEntityFormBuilder.dataProviderResolver = null;
		}
	}
}
