package io.flowset.control.service.decisioninstance;

import io.flowset.control.entity.decisioninstance.HistoricDecisionInstanceShortData;
import io.flowset.control.entity.filter.DecisionInstanceFilter;
import io.flowset.control.security.SecuredEntityLoad;
import org.springframework.lang.Nullable;

import java.util.List;

public interface DecisionInstanceService {
    /**
     * Loads decision instances from the engine history using the specified context.
     *
     * @param loadContext a context to load decision instances
     * @return found historic decision instances
     * @see DecisionInstanceLoadContext
     */
    @SecuredEntityLoad(entityClass = HistoricDecisionInstanceShortData.class)
    List<HistoricDecisionInstanceShortData> findAllHistoryDecisionInstances(DecisionInstanceLoadContext loadContext);

    /**
     * Loads a decision instance with the specified identifier.
     *
     * @param decisionInstanceId a decision instance identifier
     * @return found decision instance or null if not found
     */
    @Nullable
    @SecuredEntityLoad(entityClass = HistoricDecisionInstanceShortData.class)
    HistoricDecisionInstanceShortData getById(String decisionInstanceId);

    /**
     * Loads from engine history the total count of decision instances that match the specified filter.
     *
     * @param filter a decision filter instance
     * @return count of decision instances
     */
    @SecuredEntityLoad(entityClass = HistoricDecisionInstanceShortData.class)
    long getHistoryDecisionInstancesCount(@Nullable DecisionInstanceFilter filter);

    /**
     * Loads the total count of instances of the decision definition version with the specified identifier.
     *
     * @param decisionDefinitionId a decision definition identifier
     * @return count of instances
     */
    @SecuredEntityLoad(entityClass = HistoricDecisionInstanceShortData.class)
    long getCountByDecisionDefinitionId(String decisionDefinitionId);

    /**
     * Loads the count of running instances of the decision definition with the specified key.
     *
     * @param decisionDefinitionKey a process key
     * @return count of instances
     */
    @SecuredEntityLoad(entityClass = HistoricDecisionInstanceShortData.class)
    long getCountByDecisionDefinitionKey(String decisionDefinitionKey);
}
