/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.flowui;

import io.flowset.control.annotation.EngineProperty;
import io.flowset.control.entity.engine.EngineType;
import io.flowset.control.service.engine.EngineService;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.component.delegate.GridDelegate;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.data.grid.DataGridItems;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Removes a column of an attribute that the selected engine does not support, the same way as a column of an
 * attribute denied by security, see {@link EngineProperty}.
 */
@Primary
@Component("control_GridDelegate")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ControlGridDelegate<E, ITEMS extends DataGridItems<E>> extends GridDelegate<E, ITEMS> {

    protected EngineService engineService;
    protected EnginePropertyResolver enginePropertyResolver;

    public ControlGridDelegate(DataGrid<E> component) {
        super(component);
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
    public boolean isPropertyEnabledBySecurity(MetaPropertyPath mpp) {
        boolean propertyEnabledBySecurity = super.isPropertyEnabledBySecurity(mpp);
        if (!mpp.isDirectProperty()) {
            return propertyEnabledBySecurity;
        }
        EngineType engineType = engineService.getSelectedEngineType();
        return propertyEnabledBySecurity && enginePropertyResolver.isSupported(mpp.getMetaProperty(), engineType);
    }
}
