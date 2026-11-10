/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.flowui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import io.flowset.control.annotation.EngineProperty;
import io.flowset.control.entity.engine.EngineType;
import io.flowset.control.service.engine.EngineService;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.flowui.data.EntityValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.binding.impl.FieldValueBinding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

/**
 * Hides a field bound to an attribute that the selected engine does not support, see {@link EngineProperty}.
 */
@Primary
@org.springframework.stereotype.Component("control_FieldValueBinding")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ControlFieldValueBinding<V> extends FieldValueBinding<V> {

    protected EngineService engineService;
    protected EnginePropertyResolver enginePropertyResolver;

    public ControlFieldValueBinding(ValueSource<V> valueSource, HasValue<?, V> component) {
        super(valueSource, component);
    }

    @Autowired
    public void setEngineService(EngineService engineService) {
        this.engineService = engineService;
    }

    @Autowired
    public void setEnginePropertyResolver(EnginePropertyResolver enginePropertyResolver) {
        this.enginePropertyResolver = enginePropertyResolver;
    }

    @Override
    public void bind() {
        super.bind();

        if (component instanceof Component field && valueSource instanceof EntityValueSource<?, ?> entitySource) {
            MetaProperty metaProperty = entitySource.getMetaPropertyPath().getMetaProperty();
            EngineType engineType = engineService.getSelectedEngineType();
            if (!enginePropertyResolver.isSupported(metaProperty, engineType)) {
                field.setVisible(false);
            }
        }
    }
}
