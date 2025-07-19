package tech.derbent.abstracts.annotations;

import java.lang.reflect.Field;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;

public class CEntityFormBuilder {

	protected static final String LabelMinWidth_210PX = "210px";

	public static <EntityClass> Div buildForm(final Class<?> entityClass, final BeanValidationBinder<EntityClass> binder) {
		if (entityClass == null) {
			throw new IllegalArgumentException("Entity class cannot be null");
		}
		if (binder == null) {
			throw new IllegalArgumentException("Binder cannot be null");
		}
		final Div panel = new Div();
		panel.setClassName("editor-layout");
		final FormLayout formLayout = new FormLayout();
		for (final Field field : entityClass.getDeclaredFields()) {
			final MetaData meta = field.getAnnotation(MetaData.class);
			if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
				continue;
			}
			if ((meta == null) || meta.hidden()) {
				continue; // Skip fields without MetaData or hidden fields
			}
			Component component = null;
			if ((field.getType() == Boolean.class) || (field.getType() == boolean.class)) {
				component = createCheckbox(field, meta, binder);
			}
			else if ((field.getType() == String.class) || (field.getType() == char.class)) {
				component = createTextField(field, meta, binder);
			}
			else {
				// Handle other field types as needed
				throw new UnsupportedOperationException("Unsupported field type: " + field.getType());
			}
			final HorizontalLayout horizantalLayout = new HorizontalLayout();
			horizantalLayout.setPadding(false);
			horizantalLayout.setSpacing(false);
			horizantalLayout.setMargin(false);
			horizantalLayout.setJustifyContentMode(JustifyContentMode.START);
			horizantalLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
			final Div div = new Div(meta.displayName());
			div.setMinWidth(LabelMinWidth_210PX);
			horizantalLayout.add(div);
			horizantalLayout.add(component);
			formLayout.add(horizantalLayout);
			// formLayout.add(txtField);
		}
		panel.add(formLayout);
		return panel;
	}

	private static Checkbox createCheckbox(final Field field, final MetaData meta, final BeanValidationBinder<?> binder) {
		final Checkbox checkbox = new Checkbox();
		// no label for checkbox, checkbox.setLabel(meta.displayName());
		checkbox.setRequiredIndicatorVisible(meta.required());
		checkbox.setReadOnly(meta.readOnly());
		if (!meta.description().isEmpty()) {
			checkbox.setHelperText(meta.description());
		}
		binder.bind(checkbox, field.getName());
		return checkbox;
	}

	private static TextField createTextField(final Field field, final MetaData meta, final BeanValidationBinder<?> binder) {
		final TextField textField = new TextField();
		// no label for text field, textField.setLabel(meta.displayName());
		textField.setRequiredIndicatorVisible(meta.required());
		textField.setReadOnly(meta.readOnly());
		if (!meta.description().isEmpty()) {
			textField.setHelperText(meta.description());
		}
		if (!meta.defaultValue().isEmpty()) {
			textField.setValue(meta.defaultValue());
		}
		// txtField.setId(field.getName());
		textField.setClassName("form-field-text");
		textField.setWidthFull();
		binder.bind(textField, field.getName());
		return textField;
	}
}
