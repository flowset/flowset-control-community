/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.resourcerolemodeldetail;

import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.router.Route;
import io.jmix.core.Messages;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.action.list.ReadAction;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionPropertyContainer;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.jmix.security.model.ResourcePolicyModel;
import io.jmix.security.model.ResourceRoleModel;
import io.jmix.security.model.RoleSourceType;
import io.jmix.securityflowui.view.resourcerole.ResourceRoleModelDetailView;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

@Route(value = "sec/resourcerolemodels/:code", layout = DefaultMainViewParent.class)
@ViewController(id = "sec_ResourceRoleModel.detail")
@ViewDescriptor(path = "control-resource-role-model-detail-view.xml")
public class ControlResourceRoleModelDetailView extends ResourceRoleModelDetailView {
    @ViewComponent
    protected MessageBundle messageBundle;
    @Autowired
    protected Fragments fragments;
    @Autowired
    protected Messages messages;

    @ViewComponent
    protected JmixTabSheet tabSheet;
    @ViewComponent
    protected VerticalLayout contentBox;
    @ViewComponent
    protected DataGrid<ResourceRoleModel> childRolesTable;
    @ViewComponent("resourcePoliciesTable.read")
    protected ReadAction<ResourcePolicyModel> readAction;

    @ViewComponent
    protected CollectionContainer<ResourceRoleModel> childRolesDc;
    @ViewComponent
    protected CollectionPropertyContainer<ResourcePolicyModel> resourcePoliciesDc;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        super.onBeforeShow(event);

        updateTabCaptions();
        updateReadOnlyRoleComponents();
    }

    @Subscribe(id = "resourcePoliciesDc", target = Target.DATA_CONTAINER)
    public void onResourcePoliciesDcCollectionChange(final CollectionContainer.CollectionChangeEvent<ResourcePolicyModel> event) {
        updateResourcePoliciesTabCaption();
    }

    @Subscribe(id = "roleModelDc", target = Target.DATA_CONTAINER)
    public void onRoleModelDcItemPropertyChange(final InstanceContainer.ItemPropertyChangeEvent<ResourceRoleModel> event) {
        if (event.getProperty().equals("childRoles")) {
            updateBaseRolesTabCaption();
        }
    }


    protected void updateReadOnlyRoleComponents() {
        boolean isDatabaseSource = isDatabaseRole();
        Collection<Action> childRolesActions = childRolesTable.getActions();
        for (Action action : childRolesActions) {
            action.setVisible(isDatabaseSource);
        }

        readAction.setVisible(!isDatabaseSource);
    }

    protected boolean isDatabaseRole() {
        return RoleSourceType.DATABASE.equals(getEditedEntity().getSource());
    }

    protected void updateTabCaptions() {
        updateResourcePoliciesTabCaption();
        updateBaseRolesTabCaption();
    }

    protected void updateBaseRolesTabCaption() {
        Tab baseRolesTab = tabSheet.getTabAt(1);
        baseRolesTab.setLabel("%s (%s)".formatted(
                messages.getMessage("io.jmix.security.model/ResourceRoleModel.childRoles"),
                childRolesDc.getItems().size()
        ));
        baseRolesTab.addComponentAsFirst(VaadinIcon.FILE_TREE_SMALL.create());
    }

    protected void updateResourcePoliciesTabCaption() {
        Tab resourcePoliciesTab = tabSheet.getTabAt(0);
        resourcePoliciesTab.setLabel("%s (%s)".formatted(
                messages.getMessage("io.jmix.security.model/ResourceRoleModel.resourcePolicies"),
                resourcePoliciesDc.getItems().size()
        ));
        resourcePoliciesTab.addComponentAsFirst(VaadinIcon.CLIPBOARD_CHECK.create());
    }
}