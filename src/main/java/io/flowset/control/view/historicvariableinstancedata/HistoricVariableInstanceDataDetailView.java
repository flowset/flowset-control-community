/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.historicvariableinstancedata;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.entity.variable.CamundaVariableType;
import io.flowset.control.entity.variable.HistoricVariableInstanceData;
import io.flowset.control.service.variable.VariableService;
import io.flowset.control.service.variable.VariableUtils;
import io.jmix.core.LoadContext;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.component.formlayout.JmixFormLayout;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

@Route(value = "historicVariableInstanceDatas/:id", layout = DefaultMainViewParent.class)
@ViewController("HistoricVariableInstanceData.detail")
@ViewDescriptor("historic-variable-instance-data-detail-view.xml")
@EditedEntityContainer("historicVariableInstanceDataDc")
@DialogMode(minWidth = "30em", maxWidth = "60em")
public class HistoricVariableInstanceDataDetailView extends StandardDetailView<HistoricVariableInstanceData> {

    @Autowired
    protected VariableService variableService;
    @ViewComponent
    protected JmixFormLayout form;
    @ViewComponent
    protected InstanceContainer<HistoricVariableInstanceData> historicVariableInstanceDataDc;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected DatatypeRegistry datatypeRegistry;
    @ViewComponent
    protected MessageBundle messageBundle;
    @ViewComponent
    protected TypedTextField<String> fileNameField;
    @ViewComponent
    protected TypedTextField<String> mimeTypeField;
    @ViewComponent
    protected TypedTextField<String> encodingField;
    @ViewComponent
    protected TypedTextField<String> objectTypeInfoField;
    @ViewComponent
    protected TypedTextField<String> serializationDataFormatField;

    @Subscribe
    public void onInit(final InitEvent event) {
        addClassNames(LumoUtility.Padding.Top.XSMALL);
        form.getOwnComponents().forEach(component -> component.addClassNames(LumoUtility.Padding.Top.SMALL));
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        HistoricVariableInstanceData variableInstance = variableService.findHistoricVariableById(getEditedEntity().getHistoricVariableInstanceId());
        historicVariableInstanceDataDc.setItem(variableInstance);
        if (variableInstance.getType() != null) {
            initValueComponent();

            CamundaVariableType camundaVariableType = CamundaVariableType.fromId(variableInstance.getType());
            if (camundaVariableType == CamundaVariableType.FILE) {
                fileNameField.setVisible(true);
                mimeTypeField.setVisible(true);
                encodingField.setVisible(true);
            } else if (camundaVariableType == CamundaVariableType.OBJECT) {
                objectTypeInfoField.setVisible(true);
                serializationDataFormatField.setVisible(true);
            }
        }
    }

    protected void initValueComponent() {
        Component valueComponent = createComponent();
        if (valueComponent instanceof HasSize hasSize) {
            hasSize.setWidthFull();
        }
        if (valueComponent instanceof HasLabel hasLabel) {
            hasLabel.setLabel(messageBundle.getMessage("historicVariableInstanceData.value"));
        }

        form.addComponentAtIndex(2, valueComponent);
    }

    protected Component createComponent() {
        String typeName = getEditedEntity().getType();
        CamundaVariableType type = CamundaVariableType.fromId(typeName);

        if (type == null) {
            return createTextField(String.class);
        }

        return switch (type) {
            case LONG -> createTextField(Long.class);
            case SHORT -> createTextField(Short.class);
            case INTEGER -> createIntegerField();
            case DOUBLE -> createNumberField();
            case DATE -> createDateField();
            default -> createTextField(String.class);
        };
    }

    protected NumberField createNumberField() {
        NumberField numberField = uiComponents.create(NumberField.class);
        numberField.setReadOnly(true);
        numberField.setValue((Double) getEditedEntity().getValue());
        return numberField;
    }


    protected IntegerField createIntegerField() {
        IntegerField integerField = uiComponents.create(IntegerField.class);
        integerField.setReadOnly(true);
        integerField.setValue((Integer) getEditedEntity().getValue());
        return integerField;
    }

    @SuppressWarnings("unchecked")
    protected <V> TypedTextField<V> createTextField(Class<V> clazz) {
        TypedTextField<V> component = uiComponents.create(TypedTextField.class);
        component.setDatatype(datatypeRegistry.get(clazz));
        component.setReadOnly(true);
        if (getEditedEntity().getValue() != null) {
            if (getEditedEntity().getValue().getClass().isAssignableFrom(clazz)) {
                component.setTypedValue((V) getEditedEntity().getValue());
            } else {
                component.setValue(getEditedEntity().getValue().toString());
            }
        }

        return component;
    }

    @SuppressWarnings("unchecked")
    protected TypedDateTimePicker<Date> createDateField() {
        TypedDateTimePicker<Date> component = uiComponents.create(TypedDateTimePicker.class);
        component.setDatatype(datatypeRegistry.get(Date.class));
        component.setReadOnly(true);
        component.setTypedValue((Date) VariableUtils.parseDateValue(getEditedEntity().getValue()));
        return component;
    }

    @Install(to = "historicVariableInstanceDataDl", target = Target.DATA_LOADER)
    protected HistoricVariableInstanceData historicVariableInstanceDataDlLoadDelegate(final LoadContext<HistoricVariableInstanceData> loadContext) {
        Object id = loadContext.getId();
        if (id != null) {
            return variableService.findHistoricVariableById(id.toString());
        }
        return null;
    }

}
