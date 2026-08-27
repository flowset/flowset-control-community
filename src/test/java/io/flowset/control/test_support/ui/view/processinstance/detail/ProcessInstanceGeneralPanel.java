/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.processinstance.detail;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.sys.Composite;
import lombok.Getter;

/**
 * Wrapper for the general-info panel fragment shown on the Process instance detail view.
 * Source component: {@link io.flowset.control.view.processinstance.generalpanel.GeneralPanelFragment}
 */
@Getter
public class ProcessInstanceGeneralPanel extends Composite<ProcessInstanceGeneralPanel> {

    @TestComponent(path = "generalPanelInfoBtn")
    private Button infoBtn;

    @TestComponent(path = "generalPanelRefreshBtn")
    private Button refreshBtn;

    @TestComponent(path = "generalPanelActivateBtn")
    private Button activateBtn;

    @TestComponent(path = "generalPanelSuspendBtn")
    private Button suspendBtn;

    @TestComponent(path = "generalPanelMigrateBtn")
    private Button migrateBtn;

    @TestComponent(path = "generalPanelTerminateBtn")
    private Button terminateBtn;

    @TestComponent(path = "generalPanelUpperPanel")
    private ProcessInstancePropertiesPanel propertiesPanel;
}
