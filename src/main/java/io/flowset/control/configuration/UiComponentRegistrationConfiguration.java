/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.configuration;

import io.flowset.control.uicomponent.grid.ControlDataGridLoader;
import io.flowset.control.uicomponent.menu.ControlListMenu;
import io.flowset.control.uicomponent.menu.ControlListMenuLoader;
import io.flowset.control.uicomponent.spinner.SpinnerLoader;
import io.flowset.control.uicomponent.treedatagrid.NoClickTreeDataGridLoader;
import io.flowset.control.uicomponent.treedatagrid.NoClickTreeGrid;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.sys.registration.ComponentRegistration;
import io.jmix.flowui.sys.registration.ComponentRegistrationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.vaadin.addons.componentfactory.spinner.Spinner;

@Configuration
public class UiComponentRegistrationConfiguration {

    @Bean
    public ComponentRegistration noClickTreeDataGrid() {
        return ComponentRegistrationBuilder.create(NoClickTreeGrid.class)
                .withComponentLoader("noClickTreeDataGrid", NoClickTreeDataGridLoader.class)
                .build();
    }

    @Bean
    public ComponentRegistration spinner() {
        return ComponentRegistrationBuilder.create(Spinner.class)
                .withComponentLoader("spinner", SpinnerLoader.class)
                .build();
    }

    @Bean
    public ComponentRegistration controlListMenu() {
        return ComponentRegistrationBuilder.create(ControlListMenu.class)
                .withComponentLoader("listMenu", ControlListMenuLoader.class)
                .build();
    }

    @Bean
    public ComponentRegistration controlDataGrid() {
        return ComponentRegistrationBuilder
                .create(DataGrid.class)
                .withComponentLoader("dataGrid", ControlDataGridLoader.class)
                .build();
    }
}
