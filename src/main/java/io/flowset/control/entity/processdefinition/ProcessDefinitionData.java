/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity.processdefinition;

import io.flowset.control.annotation.EngineProperty;
import io.flowset.control.entity.engine.EngineFamily;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

@JmixEntity(name = "bpm_ProcessDefinitionData")
public class ProcessDefinitionData {

    @JmixId
    @JmixProperty(mandatory = true)
    protected String id;

    protected String processDefinitionId;

    @EngineProperty(supportedFamilies = EngineFamily.CAMUNDA_7)
    protected String versionTag;

    protected String name;

    protected String key;

    protected String category;

    protected String deploymentId;

    protected Boolean suspended;

    @EngineProperty(supportedFamilies = EngineFamily.CAMUNDA_7)
    protected Integer historyTimeToLive;

    @EngineProperty(supportedFamilies = EngineFamily.CAMUNDA_7)
    protected Boolean startableInTaskList;

    protected String resourceName;

    protected String diagramResourceName;

    protected String tenantId;

    protected String description;

    protected Integer version;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public String getVersionTag() {
        return versionTag;
    }

    public void setVersionTag(String versionTag) {
        this.versionTag = versionTag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public Boolean getSuspended() {
        return suspended;
    }

    public void setSuspended(Boolean suspended) {
        this.suspended = suspended;
    }

    public Integer getHistoryTimeToLive() {
        return historyTimeToLive;
    }

    public void setHistoryTimeToLive(Integer historyTimeToLive) {
        this.historyTimeToLive = historyTimeToLive;
    }

    public Boolean getStartableInTaskList() {
        return startableInTaskList;
    }

    public void setStartableInTaskList(Boolean startableInTaskList) {
        this.startableInTaskList = startableInTaskList;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getDiagramResourceName() {
        return diagramResourceName;
    }

    public void setDiagramResourceName(String diagramResourceName) {
        this.diagramResourceName = diagramResourceName;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @InstanceName
    @DependsOnProperties({"key", "version"})
    public String getInstanceName() {
        return key + " " + version;
    }

}
