/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processinstance;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * A root component for the tab content that lazily loads its content when it is attached to the UI.
 */
public class LazyTabContent extends Div implements ApplicationContextAware, InitializingBean {
    protected ApplicationContext applicationContext;

    protected SerializableSupplier<? extends Component> supplier;

    public LazyTabContent(SerializableSupplier<? extends Component> supplier) {
        addClassNames(LumoUtility.Width.FULL, LumoUtility.Height.FULL);
        initComponent(supplier);
        this.supplier = supplier;
    }

    protected void initComponent(SerializableSupplier<? extends Component> supplier) {
        addAttachListener(event -> {
            if (getElement().getChildCount() == 0) {
                add(supplier.get());
            }
        });
    }

    public void init() {
        if (getElement().getChildCount() == 0) {
            add(this.supplier.get());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
