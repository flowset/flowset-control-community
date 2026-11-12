/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.engine.detail;

import io.flowset.control.test_support.AbstractUiTest;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.engine.BpmEngineDetailView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Condition.text;
import static io.jmix.masquerade.Masquerade.$j;

@DisplayName("BPM engine detail view")
public class BpmEngineDetailViewUiTest extends AbstractUiTest {

    @Test
    @DisplayName("Base URL helper text stays after changing the engine type")
    void givenNewEngine_whenChangeType_thenBaseUrlHelperTextShown() {
        // given
        MainView mainView = loginAsAdmin();
        mainView.openBpmEngineListView().getCreateButton().click();

        BpmEngineDetailView detailView = $j(BpmEngineDetailView.class).exists();

        // when
        detailView.getTypeField().setValue("Operaton");

        // then
        detailView.getTypeField().shouldHave(text("Operaton"));
        detailView.getBaseUrlField().getDelegate()
                .$("[slot='helper']")
                .shouldHave(text("A base URL to the BPM engine REST API, e.g. for local engine"));
    }
}
