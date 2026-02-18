package io.flowset.control.service.decisiondefinition;

import io.flowset.control.entity.decisiondefinition.DecisionDefinitionData;
import io.flowset.control.entity.filter.DecisionDefinitionFilter;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Provides methods to manage decision definitions and their versions in the BPM engine.
 */
public interface DecisionDefinitionService {

    /**
     * Loads latest versions of the decision definitions deployed in the BPM engine.
     *
     * @return a list of deployed decision definitions
     */
    List<DecisionDefinitionData> findLatestVersions();

    /**
     * Loads decision definition versions from the engine using the specified context.
     *
     * @param context a context to load decision definitions
     * @return a list of deployed decision definition versions
     */
    List<DecisionDefinitionData> findAll(DecisionDefinitionLoadContext context);

    /**
     * Loads from engine the total count of decision definition versions that match the specified filter.
     *
     * @param filter a decision definition filter
     * @return count of deployed decision definitions
     */
    long getCount(@Nullable DecisionDefinitionFilter filter);

    /**
     * Loads decision definition versions from the engine with the specified decision key.
     *
     * @param decisionDefinitionKey a decision key
     * @return a list of deployed decision definition versions
     */
    List<DecisionDefinitionData> findAllByKey(String decisionDefinitionKey);

    /**
     * Loads a decision definition with the specified identifier.
     *
     * @param decisionDefinitionId a decision definition identifier
     * @return found decision definition or null if not found
     */
    @Nullable
    DecisionDefinitionData getById(String decisionDefinitionId);

    /**
     * Loads a DMN XML of the decision definition with the specified identifier.
     *
     * @param decisionDefinitionId a decision definition identifier
     * @return a decision definition content in the DMN XML format
     */
    String getDmnXml(String decisionDefinitionId);

    /**
     * Loads decision definitions with the specified identifiers.
     *
     * @param ids a list of decision definition ids
     * @return found decision definitions
     */
    List<DecisionDefinitionData> findAllByIds(Collection<String> ids);
}

