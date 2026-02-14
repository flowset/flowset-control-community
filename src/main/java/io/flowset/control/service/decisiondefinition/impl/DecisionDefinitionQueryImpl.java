/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.decisiondefinition.impl;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinitionQuery;
import org.camunda.community.rest.client.api.DecisionDefinitionApiClient;
import org.camunda.community.rest.client.model.CountResultDto;
import org.camunda.community.rest.client.model.DecisionDefinitionDto;
import org.camunda.community.rest.impl.query.BaseQuery;
import org.camunda.community.rest.impl.query.QueryOrderingProperty;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

@Slf4j
public class DecisionDefinitionQueryImpl extends BaseQuery<DecisionDefinitionQuery, DecisionDefinition>
        implements DecisionDefinitionQuery {

    protected final DecisionDefinitionApiClient decisionDefinitionApiClient;

    protected String decisionDefinitionId;
    protected String decisionDefinitionIdIn;
    protected String name;
    protected String nameLike;
    protected String deploymentId;
    protected OffsetDateTime deployedAfter;
    protected OffsetDateTime deployedAt;
    protected String key;
    protected String keyLike;
    protected String category;
    protected String categoryLike;
    protected Integer version;
    protected Boolean latestVersion;
    protected String resourceName;
    protected String resourceNameLike;
    protected String decisionRequirementsDefinitionId;
    protected String decisionRequirementsDefinitionKey;
    protected Boolean withoutDecisionRequirementsDefinition;
    protected String tenantIdIn;
    protected Boolean withoutTenantId;
    protected Boolean includeDecisionDefinitionsWithoutTenantId;
    protected String versionTag;
    protected String versionTagLike;

    public DecisionDefinitionQueryImpl(DecisionDefinitionApiClient decisionDefinitionApiClient) {
        this.decisionDefinitionApiClient = decisionDefinitionApiClient;
    }

    public void setWithoutTenantId(Boolean withoutTenantId) {
        this.withoutTenantId = withoutTenantId;
    }

    @Override
    public long count() {
        ResponseEntity<CountResultDto> response = decisionDefinitionApiClient.getDecisionDefinitionsCount(
                decisionDefinitionId, decisionDefinitionIdIn, name, nameLike, deploymentId, deployedAfter, deployedAt,
                key, keyLike, category, categoryLike, version, latestVersion, resourceName, resourceNameLike,
                decisionRequirementsDefinitionId, decisionRequirementsDefinitionKey,
                withoutDecisionRequirementsDefinition, tenantIdIn, withoutTenantId,
                includeDecisionDefinitionsWithoutTenantId, versionTag, versionTagLike);
        CountResultDto countResultDto = response.getBody();
        if (response.getStatusCode().is2xxSuccessful() && countResultDto != null) {
            return countResultDto.getCount();
        }
        log.error("Error on loading decisions count, status code {}", response.getStatusCode());
        return -1;
    }

    @Override
    public List<DecisionDefinition> listPage(int firstResult, int maxResult) {
        QueryOrderingProperty queryOrderingProperty = sortProperty();
        String orderBy = queryOrderingProperty != null ? queryOrderingProperty.getProperty() : null;
        String orderDirection = queryOrderingProperty != null && queryOrderingProperty.getDirection() != null
                ? queryOrderingProperty.getDirection().name().toLowerCase()
                : null;
        ResponseEntity<List<DecisionDefinitionDto>> response = decisionDefinitionApiClient.getDecisionDefinitions(
                orderBy, orderDirection, firstResult, maxResult,
                decisionDefinitionId, decisionDefinitionIdIn, name, nameLike, deploymentId, deployedAfter, deployedAt,
                key, keyLike, category, categoryLike, version, latestVersion, resourceName, resourceNameLike,
                decisionRequirementsDefinitionId, decisionRequirementsDefinitionKey,
                withoutDecisionRequirementsDefinition, tenantIdIn, withoutTenantId,
                includeDecisionDefinitionsWithoutTenantId, versionTag, versionTagLike);
        List<DecisionDefinitionDto> decisionDefinitionDtoList = response.getBody();
        if (response.getStatusCode().is2xxSuccessful() && decisionDefinitionDtoList != null) {
            return decisionDefinitionDtoList
                    .stream()
                    .map(e -> (DecisionDefinition) new DecisionDefinitionImpl(
                            e.getId(), e.getCategory(), e.getName(), e.getKey(), e.getVersion(), e.getResource(),
                            e.getDeploymentId(), null, e.getTenantId(), e.getHistoryTimeToLive(),
                            e.getDecisionRequirementsDefinitionId(), e.getDecisionRequirementsDefinitionKey(),
                            e.getVersionTag()))
                    .toList();
        }
        log.error("Error on loading decisions, status code {}", response.getStatusCode());
        return List.of();
    }

    @Override
    public DecisionDefinitionQuery decisionDefinitionId(String decisionDefinitionId) {
        this.decisionDefinitionId = decisionDefinitionId;
        return this;
    }

    @Override
    public DecisionDefinitionQuery decisionDefinitionIdIn(String... strings) {
        this.decisionDefinitionIdIn = String.join(",", strings);
        return null;
    }

    @Override
    public DecisionDefinitionQuery decisionDefinitionCategory(String category) {
        this.category = category;
        return this;
    }

    @Override
    public DecisionDefinitionQuery decisionDefinitionCategoryLike(String categoryLike) {
        this.categoryLike = categoryLike;
        return this;
    }

    @Override
    public DecisionDefinitionQuery decisionDefinitionName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public DecisionDefinitionQuery decisionDefinitionKey(String key) {
        this.key = key;
        return this;
    }

    @Override
    public DecisionDefinitionQuery decisionDefinitionKeyLike(String keyLike) {
        this.keyLike = keyLike;
        return this;
    }

    @Override
    public DecisionDefinitionQuery decisionDefinitionNameLike(String nameLike) {
        this.nameLike = nameLike;
        return this;
    }

    @Override
    public DecisionDefinitionQuery deploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    @Override
    public DecisionDefinitionQuery deployedAfter(Date date) {
        this.deployedAfter = date.toInstant().atOffset(ZoneOffset.UTC);
        return this;
    }

    @Override
    public DecisionDefinitionQuery deployedAt(Date date) {
        this.deployedAt = date.toInstant().atOffset(ZoneOffset.UTC);
        return this;
    }

    @Override
    public DecisionDefinitionQuery decisionDefinitionVersion(Integer version) {
        this.version = version;
        return this;
    }

    @Override
    public DecisionDefinitionQuery latestVersion() {
        this.latestVersion = true;
        return this;
    }

    @Override
    public DecisionDefinitionQuery decisionDefinitionResourceName(String resourceName) {
        this.resourceName = resourceName;
        return this;
    }

    @Override
    public DecisionDefinitionQuery decisionDefinitionResourceNameLike(String resourceNameLike) {
        this.resourceNameLike = resourceNameLike;
        return this;
    }

    @Override
    public DecisionDefinitionQuery decisionRequirementsDefinitionId(String decisionRequirementsDefinitionId) {
        this.decisionRequirementsDefinitionId = decisionRequirementsDefinitionId;
        return this;
    }

    @Override
    public DecisionDefinitionQuery decisionRequirementsDefinitionKey(String decisionRequirementsDefinitionKey) {
        this.decisionRequirementsDefinitionKey = decisionRequirementsDefinitionKey;
        return this;
    }

    @Override
    public DecisionDefinitionQuery withoutDecisionRequirementsDefinition() {
        this.withoutDecisionRequirementsDefinition = true;
        return this;
    }

    @Override
    public DecisionDefinitionQuery includeDecisionDefinitionsWithoutTenantId() {
        this.includeDecisionDefinitionsWithoutTenantId = true;
        return this;
    }

    @Override
    public DecisionDefinitionQuery versionTag(String versionTag) {
        this.versionTag = versionTag;
        return this;
    }

    @Override
    public DecisionDefinitionQuery versionTagLike(String versionTagLike) {
        this.versionTagLike = versionTagLike;
        return this;
    }

    @Override
    public DecisionDefinitionQuery orderByDecisionDefinitionCategory() {
        super.orderBy("category");
        return this;
    }

    @Override
    public DecisionDefinitionQuery orderByDecisionDefinitionKey() {
        super.orderBy("key");
        return this;
    }

    @Override
    public DecisionDefinitionQuery orderByDecisionDefinitionId() {
        super.orderBy("id");
        return this;
    }

    @Override
    public DecisionDefinitionQuery orderByDecisionDefinitionVersion() {
        super.orderBy("version");
        return this;
    }

    @Override
    public DecisionDefinitionQuery orderByDecisionDefinitionName() {
        super.orderBy("name");
        return this;
    }

    @Override
    public DecisionDefinitionQuery orderByDeploymentId() {
        super.orderBy("deploymentId");
        return this;
    }

    @Override
    public DecisionDefinitionQuery orderByDeploymentTime() {
        super.orderBy("deployTime");
        return this;
    }

    @Override
    public DecisionDefinitionQuery orderByDecisionRequirementsDefinitionKey() {
        super.orderBy("decisionRequirementsDefinitionKey");
        return this;
    }

    @Override
    public DecisionDefinitionQuery orderByVersionTag() {
        super.orderBy("versionTag");
        return this;
    }

    @Getter
    static class DecisionDefinitionImpl implements DecisionDefinition {
        public DecisionDefinitionImpl(String id, String category, String name, String key, int version,
                                      String resourceName, String deploymentId, String diagramResourceName,
                                      String tenantId, Integer historyTimeToLive,
                                      String decisionRequirementsDefinitionId, String decisionRequirementsDefinitionKey,
                                      String versionTag) {
            this.id = id;
            this.category = category;
            this.name = name;
            this.key = key;
            this.version = version;
            this.resourceName = resourceName;
            this.deploymentId = deploymentId;
            this.diagramResourceName = diagramResourceName;
            this.tenantId = tenantId;
            this.historyTimeToLive = historyTimeToLive;
            this.decisionRequirementsDefinitionId = decisionRequirementsDefinitionId;
            this.decisionRequirementsDefinitionKey = decisionRequirementsDefinitionKey;
            this.versionTag = versionTag;
        }

        String id;
        String category;
        String name;
        String key;
        int version;
        String resourceName;
        String deploymentId;
        String diagramResourceName;
        String tenantId;
        Integer historyTimeToLive;
        String decisionRequirementsDefinitionId;
        String decisionRequirementsDefinitionKey;
        String versionTag;
    }
}