/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.util;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.value.ValueChangeMode;
import io.flowset.control.entity.variable.CamundaVariableType;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.upload.FileUploadField;
import io.jmix.flowui.kit.component.upload.event.FileUploadSucceededEvent;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Component("control_VariableTypeFieldGenerator")
@AllArgsConstructor
public class VariableTypeFieldGenerator {

    protected final UiComponents uiComponents;
    protected final DatatypeRegistry datatypeRegistry;


    /**
     * Creates a field that edits a value of the given primitive Camunda variable type.
     *
     * @param type          the variable type, {@code null} if the type is unknown: a text field is created then
     * @param valueConsumer the consumer of the entered value
     * @param valueProvider the provider of the current value of the variable, used for the default of a date field
     * @return the created field
     * @throws IllegalArgumentException if the type is not primitive
     */
    public com.vaadin.flow.component.Component createPrimitiveCamundaField(@Nullable CamundaVariableType type,
                                                                           Consumer<Object> valueConsumer, Supplier<Object> valueProvider) {
        if (type == null) {
            return createTextField(String.class, valueConsumer);
        }

        if (!type.isPrimitive()) {
            throw new IllegalArgumentException("Unsupported variable type: " + type);
        }

        return switch (type) {
            case LONG -> createTextField(Long.class, valueConsumer);
            case SHORT -> createTextField(Short.class, valueConsumer);
            case INTEGER -> createIntegerField(valueConsumer);
            case DOUBLE -> createNumberField(valueConsumer);
            case DATE -> createDateTimeField(Date.class, valueConsumer, valueProvider);
            case BOOLEAN -> createBooleanField(valueConsumer);
            default -> createTextField(String.class, valueConsumer);
        };
    }

    /**
     * Creates a required upload field for a binary variable value.
     *
     * @param fileName          the name of the current file, {@code null} if there is none
     * @param initialValue      the current binary content, {@code null} if there is none
     * @param fileUploadHandler the handler of an uploaded file
     * @return the created field
     */
    public FileUploadField createFileFieldField(@Nullable String fileName, byte @Nullable [] initialValue,
                                                ComponentEventListener<FileUploadSucceededEvent<FileUploadField, byte[]>> fileUploadHandler) {
        FileUploadField fileUploadField = uiComponents.create(FileUploadField.class);
        if (fileName != null) {
            fileUploadField.setFileName(fileName);
        }
        fileUploadField.setDropAllowed(true);
        fileUploadField.setFileNameVisible(true);
        fileUploadField.setClearButtonVisible(true);
        fileUploadField.setRequired(true);

        fileUploadField.addFileUploadSucceededListener(fileUploadHandler);

        fileUploadField.setValue(initialValue);
        return fileUploadField;
    }

    /**
     * Creates a vertically resizable text area for a serialized object value.
     *
     * @param valueConsumer the consumer of the entered value
     * @return the created field
     */
    public TextArea createObjectField(Consumer<Object> valueConsumer) {
        JmixTextArea textArea = uiComponents.create(JmixTextArea.class);
        textArea.setValueChangeMode(ValueChangeMode.ON_BLUR);
        textArea.addClassName("resizable-vertical");
        textArea.setMinHeight("9.5em");
        textArea.addValueChangeListener(event -> {
            String value = event.getValue();
            valueConsumer.accept(value);
        });

        return textArea;
    }

    protected NumberField createNumberField(Consumer<Object> valueConsumer) {
        NumberField numberField = uiComponents.create(NumberField.class);
        numberField.setValueChangeMode(ValueChangeMode.ON_BLUR);
        numberField.addValueChangeListener(event -> {
            valueConsumer.accept(event.getValue());
        });
        return numberField;
    }


    protected IntegerField createIntegerField(Consumer<Object> valueConsumer) {
        IntegerField integerField = uiComponents.create(IntegerField.class);
        integerField.setValueChangeMode(ValueChangeMode.ON_BLUR);
        integerField.addValueChangeListener(event -> {
            valueConsumer.accept(event.getValue());
        });
        return integerField;
    }


    /**
     * Creates a date-time field for values of the given Java type. Without a current value the field shows the
     * current time, truncated to minutes, and passes it to the consumer, so that the value matches what is shown.
     *
     * @param clazz         the Java type of the value
     * @param valueConsumer the consumer of the entered value
     * @param valueProvider the provider of the current value of the variable
     * @param <V>           the type of the value
     * @return the created field
     */
    @SuppressWarnings("unchecked")
    protected <V extends Comparable<?>> TypedDateTimePicker<V> createDateTimeField(Class<V> clazz, Consumer<Object> valueConsumer,
                                                                                   Supplier<Object> valueProvider) {
        TypedDateTimePicker<V> component = uiComponents.create(TypedDateTimePicker.class);
        component.setDatatype(datatypeRegistry.get(clazz));

        component.addTypedValueChangeListener(event -> {
            V value = event.getValue();
            valueConsumer.accept(value);
        });

        Object sourceValue = valueProvider.get();
        if (sourceValue == null) {
            component.setValue(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
            valueConsumer.accept(component.getTypedValue());
        }
        return component;
    }

    @SuppressWarnings("unchecked")
    protected <V> TypedTextField<V> createTextField(Class<V> clazz, Consumer<Object> valueConsumer) {
        TypedTextField<V> component = uiComponents.create(TypedTextField.class);
        component.setDatatype(datatypeRegistry.get(clazz));
        component.setValueChangeMode(ValueChangeMode.ON_BLUR);
        component.addTypedValueChangeListener(event -> {
            V value = event.getValue();
            valueConsumer.accept(value);
        });

        return component;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Select<Boolean> createBooleanField(Consumer<Object> valueConsumer) {
        JmixSelect<Boolean> jmixSelect = uiComponents.create(JmixSelect.class);
        jmixSelect.setEmptySelectionAllowed(true);
        jmixSelect.setItems(Boolean.FALSE, Boolean.TRUE);
        jmixSelect.addValueChangeListener(event -> {
            valueConsumer.accept(event.getValue());
        });
        return jmixSelect;
    }

}
