/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.deployment.impl;

import feign.FeignException;
import feign.utils.ExceptionUtils;
import io.jmix.core.Sort;
import io.flowset.control.entity.deployment.DeploymentData;
import io.flowset.control.entity.deployment.DeploymentResource;
import io.flowset.control.entity.filter.DeploymentFilter;
import io.flowset.control.exception.EngineNotSelectedException;
import io.flowset.control.mapper.DeploymentMapper;
import io.flowset.control.mapper.DeploymentResourceMapper;
import io.flowset.control.service.deployment.DeploymentContext;
import io.flowset.control.service.deployment.DeploymentLoadContext;
import io.flowset.control.service.deployment.DeploymentService;
import io.flowset.control.service.engine.EngineTenantProvider;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentQuery;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.community.rest.client.api.DeploymentApiClient;
import org.camunda.community.rest.client.model.DeploymentDto;
import org.camunda.community.rest.client.model.DeploymentResourceDto;
import org.camunda.community.rest.impl.RemoteRepositoryService;
import org.camunda.community.rest.impl.builder.DelegatingDeploymentBuilder;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.net.ConnectException;
import java.util.List;
import java.util.Optional;

import static io.flowset.control.util.QueryUtils.*;

@Service("control_DeploymentService")
@Slf4j
public class DeploymentServiceImpl implements DeploymentService {
    public static final String FLOWSET_CONTROL_SOURCE = "Flowset Control";

    protected final RemoteRepositoryService remoteRepositoryService;
    protected final DeploymentMapper deploymentMapper;
    protected final DeploymentResourceMapper deploymentResourceMapper;
    protected final DeploymentApiClient deploymentApiClient;
    protected final EngineTenantProvider engineTenantProvider;

    public DeploymentServiceImpl(RemoteRepositoryService remoteRepositoryService,
                                 DeploymentMapper deploymentMapper,
                                 DeploymentResourceMapper deploymentResourceMapper,
                                 DeploymentApiClient deploymentApiClient, EngineTenantProvider engineTenantProvider) {
        this.remoteRepositoryService = remoteRepositoryService;
        this.deploymentMapper = deploymentMapper;
        this.deploymentResourceMapper = deploymentResourceMapper;
        this.deploymentApiClient = deploymentApiClient;
        this.engineTenantProvider = engineTenantProvider;
    }

    @Override
    public DeploymentWithDefinitions createDeployment(DeploymentContext context) {
        DelegatingDeploymentBuilder deployment = remoteRepositoryService.createDeployment();

        String tenant = engineTenantProvider.getCurrentUserTenantId();
        return deployment
                .tenantId(tenant)
                .source(FLOWSET_CONTROL_SOURCE)
                .addInputStream(context.getResourceName(), context.getResourceContent())
                .deployWithResult();
    }

    @Override
    @Nullable
    public DeploymentData findById(String deploymentId) {
        try {
            ResponseEntity<DeploymentDto> response = deploymentApiClient.getDeployment(deploymentId);
            if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
                return deploymentMapper.fromDto(response.getBody());
            }
        } catch (Exception e) {
            if (e instanceof FeignException feignException && feignException.status() == 404) {
                log.error("Unable to find deployment by id {}", deploymentId, e);
                return null;
            }
            throw e;
        }

        return null;
    }

    @Override
    public List<DeploymentData> findAll(DeploymentLoadContext context) {
        try {
            DeploymentQuery deploymentQuery = createDeploymentQuery(context.getFilter(), context.getSort());

            List<Deployment> deployments;
            if (context.getFirstResult() != null && context.getMaxResults() != null) {
                deployments = deploymentQuery.listPage(context.getFirstResult(), context.getMaxResults());
            } else {
                deployments = deploymentQuery.list();
            }

            return deployments
                    .stream()
                    .map(deploymentMapper::fromProcessDefinitionModel)
                    .toList();
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause instanceof EngineNotSelectedException) {
                log.warn("Unable to load deployments because BPM engine not selected");
                return List.of();
            }
            if (rootCause instanceof ConnectException) {
                log.error("Unable to load deployments because of connection error: ", e);
                return List.of();
            }
            throw e;
        }
    }

    @Override
    @Nullable
    public List<DeploymentResource> getDeploymentResources(String deploymentId) {
        try {

            ResponseEntity<List<DeploymentResourceDto>> response =
                    deploymentApiClient.getDeploymentResources(deploymentId);
            if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
                List<DeploymentResourceDto> deploymentResources =
                        Optional.ofNullable(response.getBody()).orElse(List.of());

                return deploymentResources
                        .stream()
                        .map(deploymentResourceMapper::fromDto)
                        .toList();
            }
        } catch (Exception e) {
            if (e instanceof FeignException feignException && feignException.status() == 404) {
                log.error("Unable to find deployment with id {}", deploymentId, e);
                return null;
            }
            throw e;
        }

        return null;
    }

    @Override
    @Nullable
    public Resource getDeploymentResourceData(String deploymentId, String resourceId) {
        try {
            ResponseEntity<Resource> response = deploymentApiClient.getDeploymentResourceData(deploymentId, resourceId);
            if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
                return response.getBody();
            }

        } catch (Exception e) {
            if (e instanceof FeignException feignException && feignException.status() == 404) {
                log.error("Unable to find deployment resource with id {} for deployment with id {} because of {}",
                        resourceId, deploymentId, e.getMessage());
                return null;
            }
            throw e;
        }

        return null;
    }

    @Override
    public void deleteById(String deploymentId, boolean deleteAllRelatedInstances, boolean skipCustomListeners,
                           boolean skipIoMappings) {
        remoteRepositoryService.deleteDeployment(deploymentId, deleteAllRelatedInstances, skipCustomListeners,
                skipIoMappings);
    }

    protected DeploymentQuery createDeploymentQuery(@Nullable DeploymentFilter filter, @Nullable Sort sort) {
        DeploymentQuery deploymentQuery = remoteRepositoryService.createDeploymentQuery();

        addIfNotNull(engineTenantProvider.getCurrentUserTenantId(), deploymentQuery::tenantIdIn);
        addDeploymentFilters(deploymentQuery, filter);
        addDeploymentSort(deploymentQuery, sort);
        return deploymentQuery;
    }
}
