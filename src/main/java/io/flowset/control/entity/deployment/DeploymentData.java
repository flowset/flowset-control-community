/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity.deployment;

import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.core.metamodel.annotation.PropertyDatatype;

import java.time.OffsetDateTime;

@JmixEntity
public class DeploymentData {

    @JmixId
    @JmixProperty(mandatory = true)
    protected String id;

    protected String deploymentId;

    protected String source;

    @InstanceName
    protected String name;

    @PropertyDatatype(value = "engineOffsetDateTime")
    protected OffsetDateTime deploymentTime;

    protected String tenantId;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public OffsetDateTime getDeploymentTime() {
        return deploymentTime;
    }

    public void setDeploymentTime(OffsetDateTime deploymentTime) {
        this.deploymentTime = deploymentTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}