/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.processdefinition.detail;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.sys.Composite;
import lombok.Getter;

/**
 * Wrapper for the general-info panel fragment shown on the Process detail view.
 * Source component: {@link io.flowset.control.view.processdefinition.GeneralPanelFragment}
 */
@Getter
public class ProcessGeneralPanelFragment extends Composite<ProcessGeneralPanelFragment> {

    @TestComponent(path = "generalPanelInfoBtn")
    private Button infoBtn;

    @TestComponent(path = "generalPanelUpperPanel")
    private ProcessPropertiesPanelFragment propertiesPanel;

    @TestComponent(path = "generalPanelStartProcessBtn")
    private Button startProcessBtn;

    @TestComponent(path = "generalPanelSuspendBtn")
    private Button suspendBtn;

    @TestComponent(path = "generalPanelActivateBtn")
    private Button activateBtn;

    @TestComponent(path = "generalPanelDeleteBtn")
    private Button deleteBtn;

    @TestComponent(path = "generalPanelMigrateBtn")
    private Button migrateBtn;

    @TestComponent(path = "generalPanelRefreshBtn")
    private Button refreshBtn;

}
