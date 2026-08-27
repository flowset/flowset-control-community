/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.decisioninstance;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.sys.View;
import lombok.Getter;

/**
 * Wrapper for the historic decision instance detail view opened as a route view.
 * Source view: {@link io.flowset.control.view.decisioninstance.DecisionInstanceDetailView}
 */
@Getter
@TestView(id = "bpm_DecisionInstance.detail")
public class DecisionInstanceDetailView extends View<DecisionInstanceDetailView> {

    @TestComponent(path = "closeButton")
    private Button closeButton;

    @TestComponent(path = "infoBtn")
    private Button infoBtn;

    @TestComponent(path = "copyDecisionInstanceId")
    private Button copyDecisionInstanceIdBtn;

    @TestComponent(path = "openDecisionDefinitionEditorBtn")
    private Button openDecisionDefinitionEditorBtn;

    @TestComponent(path = "openProcessInstanceEditorBtn")
    private Button openProcessInstanceEditorBtn;

    @TestComponent(path = "openProcessDefinitionEditorBtn")
    private Button openProcessDefinitionEditorBtn;
}
