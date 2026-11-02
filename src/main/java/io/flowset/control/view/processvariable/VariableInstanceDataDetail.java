/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processvariable;


import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.entity.engine.EngineType;
import io.flowset.control.entity.filter.VariableFilter;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.entity.variable.CamundaVariableType;
import io.flowset.control.entity.variable.ProcessVariableType;
import io.flowset.control.entity.variable.VariableInstanceData;
import io.flowset.control.entity.variable.VariableValueInfo;
import io.flowset.control.service.engine.EngineService;
import io.flowset.control.service.variable.VariableService;
import io.flowset.control.service.variable.VariableUtils;
import io.flowset.control.view.util.VariableTypeFieldGenerator;
import io.jmix.core.*;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.component.formlayout.JmixFormLayout;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.upload.FileUploadField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.upload.event.FileUploadSucceededEvent;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.apache.commons.lang3.BooleanUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.function.Function;

import static io.flowset.control.util.ProcessVariableUtils.getAllVariableTypes;
import static io.flowset.control.util.ProcessVariableUtils.isBinaryType;
import static io.flowset.control.util.ProcessVariableUtils.resolveVariableType;

@SuppressWarnings("LombokSetterMayBeUsed")
@Route(value = "bpm/variableinstancedata", layout = DefaultMainViewParent.class)
@ViewController("bpm_VariableInstanceData.detail")
@ViewDescriptor("variable-instance-data-detail.xml")
@EditedEntityContainer("variableInstanceDc")
@PrimaryDetailView(VariableInstanceData.class)
@DialogMode(width = "38em", resizable = true)
public class VariableInstanceDataDetail extends StandardDetailView<VariableInstanceData> {

    protected static final Logger log = LoggerFactory.getLogger(VariableInstanceDataDetail.class);

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
    @Autowired
    protected VariableTypeFieldGenerator variableTypeFieldGenerator;
    @Autowired
    protected EngineService engineService;

    @ViewComponent
    protected MessageBundle messageBundle;
    @ViewComponent
    protected JmixComboBox<ProcessVariableType> typeComboBox;
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
    protected TypedTextField<String> scopeField;
    @ViewComponent
    protected JmixCheckbox localField;
    @ViewComponent
    protected VerticalLayout localBox;

    protected Component valueComponent;
    protected ProcessInstanceData processInstance;
    protected VariableInstanceData originalVariableInstanceData;
    protected Function<VariableInstanceData, String> scopeLabelProvider;
    protected String localExecutionId;


    protected boolean newVariable = false;
    protected boolean saveEnabled = false;

    protected boolean validationEnabled = false;
    protected boolean localFlagEnabled = false;
    protected EngineType selectedEngineType;


    public void setSaveEnabled(boolean saveVisible) {
        this.saveEnabled = saveVisible;
    }

    public void setNewVariable(boolean editable) {
        this.newVariable = editable;
    }

    public void setProcessInstance(ProcessInstanceData processInstance) {
        this.processInstance = processInstance;
    }

    public void setValidationEnabled(boolean validationEnabled) {
        this.validationEnabled = validationEnabled;
    }

    public void setLocalFlagEnabled(boolean localFlagEnabled) {
        this.localFlagEnabled = localFlagEnabled;
    }

    public void setLocalExecutionId(String localExecutionId) {
        this.localExecutionId = localExecutionId;
    }

    public void setScopeLabelProvider(Function<VariableInstanceData, String> scopeLabelProvider) {
        this.scopeLabelProvider = scopeLabelProvider;
    }


    @Subscribe
    public void onInit(InitEvent event) {
        addClassNames(LumoUtility.Padding.Top.XSMALL);
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        selectedEngineType = engineService.getSelectedEngineType();

        initTypeLookup();
        initActions();
        initValueComponent();
        initVisibleComponents();
    }

    protected void initVisibleComponents() {
        if (!newVariable) {
            nameField.setReadOnly(true);
        }

        if (!newVariable || localFlagEnabled) {
            localBox.setVisible(true);
            localField.setReadOnly(!newVariable);
        }

        updateScopeField();
    }

    @Subscribe("localField")
    public void onLocalFieldComponentValueChange(final ComponentValueChangeEvent<JmixCheckbox, Boolean> event) {
        if (newVariable) {
            updateExecutionIdValue(event.getValue());
        }
    }

    protected void updateScopeField() {
        // A new variable has no scope yet: its scope is defined by the "Local" flag being edited.
        if (newVariable) {
            scopeField.setVisible(false);
            return;
        }

        String scopeName = scopeLabelProvider != null ? scopeLabelProvider.apply(getEditedEntity()) : null;

        scopeField.setValue(scopeName != null ? scopeName : "");
        scopeField.setVisible(true);
    }

    protected void updateExecutionIdValue(@Nullable Boolean local) {
        if (BooleanUtils.isTrue(local)) {
            getEditedEntity().setExecutionId(localExecutionId);
        } else {
            String processInstanceId = processInstance != null ? processInstance.getInstanceId() : null;
            getEditedEntity().setExecutionId(processInstanceId);
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
        // Name and scope of an existing variable are read-only, so a duplicate can appear
        // only when a new variable is created.
        if (!validationEnabled || !newVariable) {
            return;
        }

        VariableInstanceData variableInstanceData = getEditedEntity();

        VariableFilter variableFilter = metadata.create(VariableFilter.class);
        variableFilter.setExecutionId(variableInstanceData.getExecutionId());
        variableFilter.setVariableName(variableInstanceData.getName());

        long runtimeVariablesCount = variableService.getRuntimeVariablesCount(variableFilter);
        if (runtimeVariablesCount > 0) {
            event.getErrors().add(nameField, messageBundle.getMessage("variableNameAlreadyExistsError"));
            nameField.setInvalid(true);
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
            if (isBinaryType(selectedEngineType, type)) {
                // The entity value contains a File only after a new upload (see handleFileUpload).
                // If the variable is reopened and saved without changes, there is nothing to send to the engine.
                if (variableInstanceData.getValue() instanceof File file) {
                    variableService.updateVariableBinary(variableInstanceData, file);
                } else {
                    log.debug("Binary variable '{}' has no newly uploaded file, skipping binary update",
                            variableInstanceData.getName());
                }
            } else {
                variableService.updateVariableLocal(variableInstanceData);
            }
        }

        return Set.of(getEditedEntity());
    }

    protected void initTypeLookup() {
        typeComboBox.setItems(getAllVariableTypes(selectedEngineType));
        typeComboBox.setItemLabelGenerator(ProcessVariableType::getId);
        typeComboBox.setRequired(true);
        typeComboBox.addValueChangeListener(event -> {
            getEditedEntity().setType(event.getValue().getId());
            updateComponentsVisibility(event.getValue());
        });

        String type = getEditedEntity().getType();
        if (type != null) {
            ProcessVariableType variableType = resolveVariableType(selectedEngineType, type);
            if (variableType != null) {
                typeComboBox.setValue(variableType);
                updateComponentsVisibility(variableType);
            }
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
        valueComponent.setId("variableValueField");
        valueForm.addFormItem(valueComponent, messageBundle.getMessage("variableInstanceDataEdit.value"));
    }

    protected void initActions() {
        saveBtn.setVisible(saveEnabled);
        okBtn.setVisible(!saveEnabled);
    }

    protected Component createComponent(VariableInstanceData variableInstance) {
        String typeName = variableInstance.getType();

        if (selectedEngineType == null || selectedEngineType.isCamunda7Compatible()) {
            CamundaVariableType type = CamundaVariableType.fromId(typeName);
            if (type == null || type.isPrimitive()) {
                return variableTypeFieldGenerator.createPrimitiveCamundaField(type, o -> getEditedEntity().setValue(o),
                        () -> getEditedEntity().getValue());
            }
            return createCamundaComponent(type);
        }

        throw new IllegalStateException("Unsupported engine type: " + selectedEngineType);
    }

    protected Component createCamundaComponent(CamundaVariableType type) {
        return switch (type) {
            case BYTES, FILE -> createFileField();
            default -> variableTypeFieldGenerator.createObjectField(value -> getEditedEntity().setValue(value));
        };
    }

    protected FileUploadField createFileField() {
        VariableInstanceData variableInstanceData = getEditedEntity();

        VariableValueInfo variableValueInfo = variableInstanceData.getValueInfo();
        String fileName = variableValueInfo != null ? variableValueInfo.getFilename() : null;

        return variableTypeFieldGenerator.createFileFieldField(fileName, loadBinaryValue(variableInstanceData),
                this::handleFileUpload);
    }

    protected void handleFileUpload(FileUploadSucceededEvent<FileUploadField, byte[]> event) {
        VariableInstanceData variableInstanceData = getEditedEntity();
        VariableValueInfo valueInfo = variableInstanceData.getValueInfo();
        if (valueInfo == null) {
            valueInfo = metadata.create(VariableValueInfo.class);
            variableInstanceData.setValueInfo(valueInfo);
        }
        valueInfo.setFilename(event.getFileName());
        valueInfo.setMimeType(event.getMimeType());

        String tempDir = coreProperties.getTempDir();

        File dir = new File(tempDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION, "Cannot create temp directory: " + dir.getAbsolutePath());
        }

        File outputFile = new File(tempDir, event.getFileName());
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(event.getData());
            getEditedEntity().setValue(outputFile);
        } catch (IOException e) {
            throw new RuntimeException("Error storing uploaded file", e);
        }
    }

    protected byte[] loadBinaryValue(VariableInstanceData variableInstanceData) {
        String type = variableInstanceData.getType();
        if (!newVariable && isBinaryType(selectedEngineType, type)) {
            Resource resource = variableService.getVariableInstanceBinary(variableInstanceData.getVariableInstanceId());
            if (resource == null) {
                return null;
            }
            try {
                return resource.getContentAsByteArray();
            } catch (IOException e) {
                throw new RuntimeException("Error loading binary value", e);
            }
        }
        return null;
    }

    protected <V extends ProcessVariableType> void updateComponentsVisibility(V type) {
        if (type instanceof CamundaVariableType camundaVariableType) {
            boolean isObjectType = camundaVariableType == CamundaVariableType.OBJECT;
            objectTypeInfoField.setVisible(isObjectType);
            serializationDataFormatField.setVisible(isObjectType);

            boolean nullType = camundaVariableType == CamundaVariableType.NULL;
            valueForm.setVisible(!nullType);
        }
    }
}