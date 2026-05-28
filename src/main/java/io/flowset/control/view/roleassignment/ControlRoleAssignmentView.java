/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.roleassignment;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import io.jmix.securityflowui.view.roleassignment.RoleAssignmentView;

@Route(value = "sec/roleassignment/:username", layout = DefaultMainViewParent.class)
@ViewController(id = "roleAssignmentView")
@ViewDescriptor(path = "control-role-assignment-view.xml")
public class ControlRoleAssignmentView extends RoleAssignmentView {
}