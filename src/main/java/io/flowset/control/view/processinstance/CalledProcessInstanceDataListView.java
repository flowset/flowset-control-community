/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processinstance;

import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;
import io.flowset.control.view.util.ComponentHelper;
import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.core.Messages;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.Install;
import io.jmix.flowui.view.LookupComponent;
import io.jmix.flowui.view.StandardListView;
import io.jmix.flowui.view.Supply;
import io.jmix.flowui.view.Target;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import io.flowset.control.entity.filter.ProcessInstanceFilter;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.service.processinstance.ProcessInstanceLoadContext;
import io.flowset.control.service.processinstance.ProcessInstanceService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "called-process-instances", layout = DefaultMainViewParent.class)
@ViewController(id = "bpm_CalledProcessInstanceData.list")
@ViewDescriptor(path = "called-process-instance-data-list-view.xml")
@LookupComponent("processInstancesDataGrid")
@DialogMode(minWidth = "65em", width = "70%")
public class CalledProcessInstanceDataListView extends StandardListView<ProcessInstanceData> {

    @Autowired
    protected ProcessInstanceService processInstanceService;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected Messages messages;
    @Autowired
    protected ComponentHelper componentHelper;

    protected List<String> processInstanceIds;

    public void setProcessInstanceIds(List<String> processInstanceIds) {
        this.processInstanceIds = processInstanceIds;
    }

    @Install(to = "processInstancesDl", target = Target.DATA_LOADER)
    protected List<ProcessInstanceData> processInstancesDlLoadDelegate(LoadContext<ProcessInstanceData> loadContext) {
        if (CollectionUtils.isEmpty(processInstanceIds)) {
            return List.of();
        }
        ProcessInstanceFilter filter = dataManager.create(ProcessInstanceFilter.class);
        filter.setProcessInstanceIds(processInstanceIds);

        ProcessInstanceLoadContext context = new ProcessInstanceLoadContext()
                .setFirstResult(0)
                .setMaxResults(processInstanceIds.size())
                .setFilter(filter);

        return processInstanceService.findAllHistoricInstances(context);
    }

    @Supply(to = "processInstancesDataGrid.processDefinitionId", subject = "renderer")
    protected Renderer<ProcessInstanceData> processInstancesDataGridProcessDefinitionIdRenderer() {
        return new TextRenderer<>(item -> item.getProcessDefinitionVersion() == null ? item.getProcessDefinitionId() :
                componentHelper.getProcessLabel(item.getProcessDefinitionKey(), item.getProcessDefinitionVersion()));
    }
}
