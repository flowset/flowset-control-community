/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.resourcerolemodellist;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import io.jmix.securityflowui.view.resourcerole.ResourceRoleModelListView;

@Route(value = "sec/resourcerolemodels", layout = DefaultMainViewParent.class)
@ViewController(id = "sec_ResourceRoleModel.list")
@ViewDescriptor(path = "control-resource-role-model-list-view.xml")
public class ControlResourceRoleModelListView extends ResourceRoleModelListView {
}