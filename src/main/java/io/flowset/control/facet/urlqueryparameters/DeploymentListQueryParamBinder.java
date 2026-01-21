package io.flowset.control.facet.urlqueryparameters;

import com.google.common.collect.ImmutableMap;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.QueryParameters;
import io.flowset.control.entity.filter.DeploymentFilter;
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.component.formlayout.JmixFormLayout;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.model.InstanceContainer;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.flowset.control.view.util.FilterQueryParamUtils.*;

public class DeploymentListQueryParamBinder extends AbstractFilterUrlQueryParamBinder {
    public static final String NAME_PARAM = "name";
    public static final String DEPLOYED_AFTER_PARAM = "deployedAfter";
    public static final String DEPLOYED_BEFORE_PARAM = "deployedBefore";

    protected final InstanceContainer<DeploymentFilter> filterDc;
    protected final Runnable loadDelegate;

    protected TypedTextField<String> nameField;
    protected TypedDateTimePicker<OffsetDateTime> deployedAfterField;
    protected TypedDateTimePicker<OffsetDateTime> deployedBeforeField;

    public DeploymentListQueryParamBinder(InstanceContainer<DeploymentFilter> filterDc,
                                          Runnable loadDelegate, JmixFormLayout formLayout) {
        super();
        this.filterDc = filterDc;
        this.loadDelegate = loadDelegate;

        this.nameField = (TypedTextField<String>) formLayout.getOwnComponent("nameField");
        this.deployedAfterField = (TypedDateTimePicker<OffsetDateTime>) formLayout.getOwnComponent("deploymentAfterField");
        this.deployedBeforeField = (TypedDateTimePicker<OffsetDateTime>) formLayout.getOwnComponent("deploymentBeforeField");

        addComponentValueChangeListener(nameField, NAME_PARAM, o -> (String) o);
        addComponentValueChangeListener(deployedAfterField, DEPLOYED_AFTER_PARAM, o -> convertOffsetDateTimeParamValue((OffsetDateTime) o));
        addComponentValueChangeListener(deployedBeforeField, DEPLOYED_BEFORE_PARAM, o -> convertOffsetDateTimeParamValue((OffsetDateTime) o));
    }

    @Override
    protected Map<String, List<String>> getEmptyParametersMap() {
        return ImmutableMap.of(
                NAME_PARAM, Collections.emptyList(),
                DEPLOYED_AFTER_PARAM, Collections.emptyList(),
                DEPLOYED_BEFORE_PARAM, Collections.emptyList());
    }

    @Override
    public Component getComponent() {
        return null;
    }

    @Override
    public void updateState(QueryParameters queryParameters) {
        DeploymentFilter deploymentFilter = this.filterDc.getItem();

        String nameParam = getStringParam(queryParameters, NAME_PARAM);
        deploymentFilter.setNameLike(nameParam);

        OffsetDateTime deployedAfter = getOffsetDateTimeParam(queryParameters, DEPLOYED_AFTER_PARAM);
        deploymentFilter.setDeploymentAfter(deployedAfter);

        OffsetDateTime deployedBefore = getOffsetDateTimeParam(queryParameters, DEPLOYED_BEFORE_PARAM);
        deploymentFilter.setDeploymentBefore(deployedBefore);

        this.loadDelegate.run();
    }
}
