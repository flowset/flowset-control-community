/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.bpmengine;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.core.EntityStates;
import io.jmix.core.SaveContext;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.radiobuttongroup.JmixRadioButtonGroup;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.view.*;
import io.flowset.control.action.TestEngineConnectionAction;
import io.flowset.control.entity.engine.AuthType;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.entity.engine.EngineType;
import io.flowset.control.service.engine.EngineService;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;

import java.util.Set;

@Route(value = "bpm/engines/:id", layout = DefaultMainViewParent.class)
@ViewController(id = "BpmEngine.detail")
@ViewDescriptor(path = "bpm-engine-detail-view.xml")
@EditedEntityContainer("bpmEngineDc")
public class BpmEngineDetailView extends StandardDetailView<BpmEngine> {

    protected static final Logger log = LoggerFactory.getLogger(BpmEngineDetailView.class);

    @Autowired
    protected Fragments fragments;
    @Autowired
    protected EngineService engineService;
    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected BuildProperties buildProperties;

    @ViewComponent
    protected MessageBundle messageBundle;
    @ViewComponent
    protected Div authBox;
    @ViewComponent
    protected JmixRadioButtonGroup<AuthType> authTypeGroup;
    @ViewComponent
    protected JmixCheckbox defaultField;
    @ViewComponent
    protected TestEngineConnectionAction testConnectionAction;
    @ViewComponent
    protected TypedTextField<String> baseUrlField;

    @Subscribe
    public void onInitEntity(final InitEntityEvent<BpmEngine> event) {
        BpmEngine entity = event.getEntity();
        entity.setType(EngineType.CAMUNDA_7);

        boolean engineExists = engineService.engineExists();
        if (!engineExists) {
            entity.setIsDefault(true);
        }
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        BpmEngine engine = getEditedEntity();
        testConnectionAction.setEngine(engine);
        initAuthBox(engine.getAuthEnabled());

        if (BooleanUtils.isTrue(engine.getIsDefault()) && !entityStates.isNew(engine)) {
            defaultField.setEnabled(false);
        }

        String buildType = buildProperties.get("buildType");
        String hostExample = StringUtils.equals(buildType, "docker") ? "http://host.docker.internal:8080/engine-rest"
                : "http://localhost:8080/engine-rest";

        baseUrlField.setHelperText(messageBundle.formatMessage("baseUrlField.helperText", hostExample));
    }

    @Override
    public String getPageTitle() {
        BpmEngine engine = getEditedEntityOrNull();
        if (engine == null) {
            return messageBundle.getMessage("bpmEngineDetailView.title");
        }
        return entityStates.isNew(engine) ? messageBundle.getMessage("newBpmEngineDetailView.title") :
                messageBundle.formatMessage("existingBpmEngineDetailView.title", engine.getName());
    }

    @Install(target = Target.DATA_CONTEXT)
    protected Set<Object> saveDelegate(final SaveContext saveContext) {
        BpmEngine engineToSave = saveContext.getEntitiesToSave().get(BpmEngine.class, getEditedEntity().getId());
        Set<Object> entities = engineService.saveEngine(engineToSave);

        return entities;
    }


    @Subscribe("authEnabledField")
    public void onAuthEnabledFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<JmixCheckbox, Boolean> event) {
        Boolean isEnabled = event.getValue();
        initAuthBox(isEnabled);
    }

    protected void initAuthBox(Boolean isEnabled) {
        authBox.setEnabled(isEnabled);
        authTypeGroup.setEnabled(isEnabled);
        if (BooleanUtils.isNotTrue(isEnabled)) {
            getEditedEntity().setAuthType(null);

            getEditedEntity().setBasicAuthUsername(null);
            getEditedEntity().setBasicAuthPassword(null);

            getEditedEntity().setHttpHeaderName(null);
            getEditedEntity().setHttpHeaderValue(null);
        }
    }

    @Subscribe("authTypeGroup")
    public void onAuthTypeGroupComponentValueChange(final AbstractField.ComponentValueChangeEvent<JmixRadioButtonGroup<AuthType>, AuthType> event) {
        AuthType type = event.getValue();

        Fragment<VerticalLayout> authFragment = null;

        if (type != null) {
            switch (type) {
                case BASIC:
                    authFragment = fragments.create(this, BasicAuthFragment.class);
                    break;
                case HTTP_HEADER:
                    authFragment = fragments.create(this, HttpHeaderAuthFragment.class);
                    break;
                default:
                    authFragment = null;
                    break;
            }
        }

        authBox.removeAll();
        if (authFragment != null) {
            authBox.add(authFragment);
        }
    }

    @Subscribe("baseUrlField")
    public void onBaseUrlFieldTypedValueChange(final SupportsTypedValue.TypedValueChangeEvent<TypedTextField<String>, String> event) {
        if (event.isFromClient()) {
            String trimmedValue = StringUtils.trim(event.getValue());
            getEditedEntity().setBaseUrl(trimmedValue);
        }
    }
}