/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processvariable;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.jmix.core.*;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.component.formlayout.JmixFormLayout;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.upload.FileUploadField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.upload.event.FileUploadSucceededEvent;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.flowset.control.entity.filter.VariableFilter;
import io.flowset.control.entity.variable.CamundaVariableType;
import io.flowset.control.entity.variable.VariableInstanceData;
import io.flowset.control.entity.variable.VariableValueInfo;
import io.flowset.control.service.variable.VariableService;
import io.flowset.control.service.variable.VariableUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

@Route(value = "bpm/variableinstancedata", layout = DefaultMainViewParent.class)
@ViewController("bpm_VariableInstanceData.detail")
@ViewDescriptor("variable-instance-data-detail.xml")
@EditedEntityContainer("variableInstanceDc")
@PrimaryDetailView(VariableInstanceData.class)
@DialogMode(width = "38em", resizable = true)
public class VariableInstanceDataDetail extends StandardDetailView<VariableInstanceData> {

    protected static final Logger log = LoggerFactory.getLogger(VariableInstanceDataDetail.class);

    @Autowired
    protected DatatypeRegistry datatypeRegistry;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected Actions actions;
    @Autowired
    protected VariableService variableService;
    @Autowired
    private CoreProperties coreProperties;

    @ViewComponent
    protected MessageBundle messageBundle;
    @ViewComponent
    protected JmixComboBox<CamundaVariableType> typeComboBox;
    @ViewComponent
    protected TypedTextField<String> nameField;
    @ViewComponent
    protected InstanceContainer<VariableInstanceData> variableInstanceDc;
    @ViewComponent
    protected JmixFormLayout form;
    @ViewComponent
    protected TypedTextField<String> objectTypeInfoField;
    @ViewComponent
    protected JmixFormLayout valueForm;
    @ViewComponent
    protected TypedTextField<String> serializationDataFormatField;
    @ViewComponent
    protected JmixButton saveBtn;
    @ViewComponent
    protected JmixButton okBtn;
    @ViewComponent
    protected TypedTextField<String> activityInstanceIdField;

    protected Component valueComponent;
    protected String processInstanceId;
    protected VariableInstanceData originalVariableInstanceData;

    protected boolean newVariable = false;
    protected boolean saveEnabled = false;

    protected boolean validationEnabled = false;

    @SuppressWarnings("LombokSetterMayBeUsed")
    public void setSaveEnabled(boolean saveVisible) {
        this.saveEnabled = saveVisible;
    }

    @SuppressWarnings("LombokSetterMayBeUsed")
    public void setNewVariable(boolean editable) {
        this.newVariable = editable;
    }

    @SuppressWarnings("LombokSetterMayBeUsed")
    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    @SuppressWarnings("LombokSetterMayBeUsed")
    public void setValidationEnabled(boolean validationEnabled) {
        this.validationEnabled = validationEnabled;
    }

    @Subscribe
    public void onInit(InitEvent event) {
        addClassNames(LumoUtility.Padding.Top.XSMALL);
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        initTypeLookup();
        initActions();
        initValueComponent();
        initVisibleComponents();
    }

    protected void initVisibleComponents() {
        if (!newVariable) {
            activityInstanceIdField.setVisible(true);
            nameField.setReadOnly(true);
        }
    }

    @Subscribe(id = "variableInstanceDc", target = Target.DATA_CONTAINER)
    public void onVariableInstanceDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<VariableInstanceData> event) {
        if ("type".equals(event.getProperty())) {
            event.getItem().setValue(null);
            initValueComponent();
        }
    }

    @Subscribe
    public void onValidation(final ValidationEvent event) {
        VariableInstanceData variableInstanceData = getEditedEntity();

        if (!validationEnabled) {
            return;
        }

        if (newVariable || !StringUtils.equals(variableInstanceData.getName(), originalVariableInstanceData.getName())) {
            VariableFilter variableFilter = metadata.create(VariableFilter.class);
            variableFilter.setProcessInstanceId(processInstanceId);
            variableFilter.setVariableName(variableInstanceData.getName());

            long runtimeVariablesCount = variableService.getRuntimeVariablesCount(variableFilter);
            if (runtimeVariablesCount > 0) {
                event.getErrors().add(nameField, messageBundle.getMessage("variableNameAlreadyExistsError"));
                nameField.setInvalid(true);
            }
        }
    }

    @Install(to = "variableInstanceDl", target = Target.DATA_LOADER)
    private VariableInstanceData variableInstanceDlLoadDelegate(final LoadContext<VariableInstanceData> loadContext) {
        VariableInstanceData variableInstanceData = variableService.findRuntimeVariableById((String) loadContext.getId());
        originalVariableInstanceData = variableInstanceData;
        return variableInstanceData;
    }

    @Install(target = Target.DATA_CONTEXT)
    private Set<VariableInstanceData> saveDelegate(final SaveContext saveContext) {
        VariableInstanceData variableInstanceData = variableInstanceDc.getItem();

        if (saveEnabled) {
            String type = variableInstanceData.getType();
            if (CamundaVariableType.FILE.getId().equals(type) || CamundaVariableType.BYTES.getId().equals(type)) {
                variableService.updateVariableBinary(variableInstanceData, (File) variableInstanceData.getValue());
            } else {
                variableService.updateVariableLocal(variableInstanceData);
            }
        }

        return Set.of(getEditedEntity());
    }

    protected void initTypeLookup() {
        typeComboBox.setItems(CamundaVariableType.class);
        typeComboBox.setItemLabelGenerator(CamundaVariableType::getId);
        typeComboBox.setRequired(true);
        typeComboBox.addValueChangeListener(event -> {
            getEditedEntity().setType(event.getValue().getId());
            updateComponentsVisibility(event.getValue());
        });

        String type = getEditedEntity().getType();
        if (type != null) {
            CamundaVariableType camundaVariableType = CamundaVariableType.fromId(type);
            typeComboBox.setValue(camundaVariableType);
            updateComponentsVisibility(camundaVariableType);
        }
    }

    protected void initValueComponent() {
        valueForm.removeAll();
        VariableInstanceData variableInstance = variableInstanceDc.getItem();
        if (variableInstance.getType() != null) {
            valueComponent = createComponent(variableInstance);
            setupValueComponent(valueComponent);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void setupValueComponent(Component valueComponent) {
        VariableInstanceData variableInstance = variableInstanceDc.getItem();
        ((HasSize) valueComponent).setWidthFull();

        if (valueComponent instanceof HasValue<?, ?>) {
            Object value = variableInstance.getValue();
            if (value != null) {
                if (valueComponent instanceof TypedTextField typedTextField) {
                    typedTextField.setTypedValue(value);
                } else if (valueComponent instanceof TypedDateTimePicker typedDateTimePicker) {
                    typedDateTimePicker.setTypedValue((Comparable) VariableUtils.parseDateValue(value));
                } else {
                    ((HasValue) valueComponent).setValue(value);
                }
            }
        }
        valueForm.addFormItem(valueComponent, messageBundle.getMessage("variableInstanceDataEdit.value"));
    }

    protected void initActions() {
        saveBtn.setVisible(saveEnabled);
        okBtn.setVisible(!saveEnabled);
    }

    protected Component createComponent(VariableInstanceData variableInstance) {
        String typeName = variableInstance.getType();
        CamundaVariableType type = CamundaVariableType.fromId(typeName);

        if (type == null) {
            return createTextField(String.class);
        }

        return switch (type) {
            case LONG -> createTextField(Long.class);
            case SHORT -> createTextField(Short.class);
            case INTEGER -> createIntegerField();
            case DOUBLE -> createNumberField();
            case DATE -> createDateField(Date.class);
            case BOOLEAN -> createBooleanField();
            case BYTES, FILE -> createFileField();
            case OBJECT -> createObjectField();
            default -> createTextField(String.class);
        };
    }

    protected TextArea createObjectField() {
        JmixTextArea textArea = uiComponents.create(JmixTextArea.class);
        textArea.setValueChangeMode(ValueChangeMode.ON_BLUR);
        textArea.addClassName("resizable-vertical");
        textArea.setMinHeight("9,5em");
        textArea.addValueChangeListener(event -> {
            String value = event.getValue();
            getEditedEntity().setValue(value);
        });

        return textArea;
    }

    protected FileUploadField createFileField() {
        VariableInstanceData variableInstanceData = getEditedEntity();

        FileUploadField fileUploadField = uiComponents.create(FileUploadField.class);
        fileUploadField.setDropAllowed(true);
        fileUploadField.setFileNameVisible(true);
        fileUploadField.setClearButtonVisible(true);
        fileUploadField.setRequired(true);

        VariableValueInfo variableValueInfo = variableInstanceData.getValueInfo();
        if (variableValueInfo != null) {
            fileUploadField.setFileName(variableValueInfo.getFilename());
        }

        fileUploadField.addFileUploadSucceededListener(this::handleFileUpload);

        byte[] binaryValue = loadBinaryValue(variableInstanceData);
        fileUploadField.setValue(binaryValue);

        return fileUploadField;
    }

    protected void handleFileUpload(FileUploadSucceededEvent<FileUploadField> event) {
        MemoryBuffer memoryBuffer = event.getReceiver();
        VariableValueInfo valueInfo = getEditedEntity().getValueInfo();
        valueInfo.setFilename(memoryBuffer.getFileName());
        valueInfo.setMimeType(memoryBuffer.getFileData().getMimeType());

        OutputStream outputBuffer = memoryBuffer.getFileData().getOutputBuffer();
        ByteArrayOutputStream fileBytes = ((ByteArrayOutputStream) outputBuffer);

        String tempDir = coreProperties.getTempDir();

        File dir = new File(tempDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION,
                    "Cannot create temp directory: " + dir.getAbsolutePath());
        }

        File outputFile = new File(tempDir, memoryBuffer.getFileName());
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            fileBytes.writeTo(outputStream);
            getEditedEntity().setValue(outputFile);
        } catch (IOException e) {
            throw new RuntimeException("Error storing uploaded file", e);
        }
    }

    protected byte[] loadBinaryValue(VariableInstanceData variableInstanceData) {
        String type = variableInstanceData.getType();
        if (!newVariable && (type.equals(CamundaVariableType.FILE.getId()) || type.equals(CamundaVariableType.BYTES.getId()))) {
            Resource resource = variableService.getVariableInstanceBinary(variableInstanceData.getVariableInstanceId());
            try {
                return resource.getContentAsByteArray();
            } catch (IOException e) {
                throw new RuntimeException("Error loading binary value", e);
            }
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Select<Boolean> createBooleanField() {
        JmixSelect<Boolean> jmixSelect = uiComponents.create(JmixSelect.class);
        jmixSelect.setEmptySelectionAllowed(true);
        jmixSelect.setItems(Boolean.FALSE, Boolean.TRUE);
        jmixSelect.addValueChangeListener(event -> {
            getEditedEntity().setValue(event.getValue());
        });
        return jmixSelect;
    }


    protected NumberField createNumberField() {
        NumberField numberField = uiComponents.create(NumberField.class);
        numberField.setValueChangeMode(ValueChangeMode.ON_BLUR);
        numberField.addValueChangeListener(event -> {
            getEditedEntity().setValue(event.getValue());
        });
        return numberField;
    }


    protected IntegerField createIntegerField() {
        IntegerField integerField = uiComponents.create(IntegerField.class);
        integerField.setValueChangeMode(ValueChangeMode.ON_BLUR);
        integerField.addValueChangeListener(event -> {
            getEditedEntity().setValue(event.getValue());
        });
        return integerField;
    }


    @SuppressWarnings("unchecked")
    protected <V extends Comparable<?>> TypedDateTimePicker<V> createDateField(Class<V> clazz) {
        TypedDateTimePicker<V> component = uiComponents.create(TypedDateTimePicker.class);
        component.setDatatype(datatypeRegistry.get(clazz));

        if (getEditedEntity().getValue() == null) {
            component.setValue(LocalDateTime.now());
        }

        component.addTypedValueChangeListener(event -> {
            V value = event.getValue();
            getEditedEntity().setValue(value);
        });
        return component;
    }

    @SuppressWarnings("unchecked")
    protected <V> TypedTextField<V> createTextField(Class<V> clazz) {
        TypedTextField<V> component = uiComponents.create(TypedTextField.class);
        component.setDatatype(datatypeRegistry.get(clazz));
        component.setValueChangeMode(ValueChangeMode.ON_BLUR);
        component.addTypedValueChangeListener(event -> {
            V value = event.getValue();
            getEditedEntity().setValue(value);
        });

        return component;
    }

    protected void updateComponentsVisibility(CamundaVariableType camundaVariableType) {
        boolean isObjectType = camundaVariableType == CamundaVariableType.OBJECT;
        objectTypeInfoField.setVisible(isObjectType);
        serializationDataFormatField.setVisible(isObjectType);

        boolean nullType = camundaVariableType == CamundaVariableType.NULL;
        valueForm.setVisible(!nullType);
    }
}