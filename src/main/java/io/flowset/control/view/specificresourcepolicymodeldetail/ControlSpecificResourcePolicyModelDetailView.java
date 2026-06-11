/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.specificresourcepolicymodeldetail;

import com.vaadin.flow.component.AbstractField;
import io.flowset.control.entity.UserTaskData;
import io.flowset.control.entity.decisiondefinition.DecisionDefinitionData;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.entity.incident.IncidentData;
import io.flowset.control.entity.job.JobData;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.jmix.core.EntityStates;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import io.jmix.securityflowui.view.resourcepolicy.SpecificResourcePolicyModelDetailView;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

@ViewController(id = "sec_SpecificResourcePolicyModel.detail")
@ViewDescriptor(path = "control-specific-resource-policy-model-detail-view.xml")
public class ControlSpecificResourcePolicyModelDetailView extends SpecificResourcePolicyModelDetailView {
    @Autowired
    protected EntityStates entityStates;
    @Autowired
    private Metadata metadata;

    @Subscribe("resourceField")
    public void onResourceFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<JmixComboBox<String>, String> event) {
        if (event.isFromClient() && entityStates.isNew(getEditedEntity())) {
            String specificPolicy = event.getValue();
            String policyGroup = resolvePolicyGroup(specificPolicy);
            getEditedEntity().setPolicyGroup(policyGroup);
        }
    }

    @Nullable
    private String resolvePolicyGroup(@Nullable String specificPolicy) {
        if (specificPolicy == null) {
            return null;
        }
        String[] policyParts = StringUtils.split(specificPolicy, ".");
        if (policyParts.length == 2) {
            Class<?> entityClass = null;
            String specificPermissionGroup = policyParts[0];
            switch (specificPermissionGroup) {
                case "processDefinition" -> entityClass = ProcessDefinitionData.class;
                case "userTask" -> entityClass = UserTaskData.class;
                case "job" -> entityClass = JobData.class;
                case "incident" -> entityClass = IncidentData.class;
                case "engine" -> entityClass = BpmEngine.class;
                case "processInstance" -> entityClass = ProcessInstanceData.class;
                case "decisionDefinition" -> entityClass = DecisionDefinitionData.class;
                default -> {}
            }

            if (entityClass != null) {
                MetaClass metaClass = metadata.findClass(entityClass);
                return metaClass != null ? metaClass.getName() : null;
            }
        }
        return null;
    }

}