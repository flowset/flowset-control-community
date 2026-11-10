/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.processdefinition;

import com.codeborne.selenide.SelenideElement;
import io.flowset.control.test_support.ui.component.BpmnViewerFragment;
import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.FileUploadField;
import io.jmix.masquerade.sys.View;
import lombok.Getter;
import org.openqa.selenium.support.FindBy;

/**
 * Wrapper for the process deployment view.
 * Source component: {@link io.flowset.control.view.newprocessdeployment.NewProcessDeploymentView}
 */
@Getter
@TestView(id = "bpm_NewProcessDeploymentView")
public class NewProcessDeploymentView extends View<NewProcessDeploymentView> {

    @TestComponent(path = "okBtn")
    private Button okBtn;

    @TestComponent(path = "bpmnXmlUploadField")
    private FileUploadField bpmnXmlUploadField;

    @FindBy(css = "[data-testid='processIdLabel']")
    private SelenideElement processIdLabel;

    @TestComponent(path = "viewerFragmentViewerVBox")
    private BpmnViewerFragment bpmnViewerFragment;
}
