/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.deploymentupload;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.Checkbox;
import io.flowset.control.test_support.ui.component.BpmnViewerFragment;
import io.jmix.masquerade.component.DataGrid;
import io.jmix.masquerade.component.FileUploadField;
import io.jmix.masquerade.component.TextField;
import io.jmix.masquerade.sys.View;
import lombok.Getter;

/**
 * Wrapper for the Upload deployment view.
 * Source view: {@link io.flowset.control.view.deploymentupload.DeploymentUploadView}
 */
@Getter
@TestView(id = "bpm_DeploymentUploadView")
public class DeploymentUploadView extends View<DeploymentUploadView> {

    public static final int CHECKBOX_COLUMN_INDEX = 0;
    public static final int FILE_NAME_COLUMN_INDEX = 1;
    public static final int TYPE_COLUMN_INDEX = 2;
    public static final int KEYS_COLUMN_INDEX = 3;
    public static final int STATUS_COLUMN_INDEX = 4;

    @TestComponent(path = "deploymentNameField")
    private TextField deploymentNameField;

    @TestComponent(path = "skipUnchangedField")
    private Checkbox skipUnchangedField;

    @TestComponent(path = "removeFileBtn")
    private Button removeFileBtn;

    @TestComponent(path = "filesUpload")
    private FileUploadField filesUpload;

    @TestComponent(path = "filesDataGrid")
    private DataGrid filesDataGrid;

    @TestComponent(path = "viewerFragmentViewerVBox")
    private BpmnViewerFragment bpmnViewerFragment;

    @TestComponent(path = "okBtn")
    private Button okBtn;

    @TestComponent(path = "cancelBtn")
    private Button cancelBtn;
}
