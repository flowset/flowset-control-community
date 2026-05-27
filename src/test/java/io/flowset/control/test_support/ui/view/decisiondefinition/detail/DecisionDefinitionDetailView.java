/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.decisiondefinition.detail;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.TabSheet;
import io.jmix.masquerade.sys.View;
import lombok.Getter;

import static io.jmix.masquerade.JConditions.SELECTED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

/**
 * Wrapper for the Decision detail view.
 * Source view: {@link io.flowset.control.view.decisiondefinition.DecisionDefinitionDetailView}
 */
@Getter
@TestView(id = "bpm_DecisionDefinition.detail")
public class DecisionDefinitionDetailView extends View<DecisionDefinitionDetailView> {

    @TestComponent(path = "generalPanel")
    private DecisionGeneralPanelFragment generalPanel;

    @TestComponent(path = "tabSheet")
    private TabSheet tabs;

    @TestComponent(path = "closeButton")
    private Button closeButton;

    public DecisionInstancesTabFragment openDecisionInstancesTab() {
        tabs.getTabById("decisionInstancesTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);

        return $j(DecisionInstancesTabFragment.class, "decisionInstancesFragment")
                .exists()
                .shouldBe(VISIBLE);
    }
}
