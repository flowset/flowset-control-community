package io.flowset.control.facet.urlqueryparameters;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.QueryParameters;
import io.flowset.control.entity.filter.DecisionDefinitionFilter;
import io.jmix.flowui.component.formlayout.JmixFormLayout;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.model.InstanceContainer;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.flowset.control.view.util.FilterQueryParamUtils.getBooleanParam;
import static io.flowset.control.view.util.FilterQueryParamUtils.getStringParam;

public class DecisionDefinitionListQueryParamBinder extends AbstractFilterUrlQueryParamBinder {
    public static final String NAME_PARAM = "name";
    public static final String KEY_PARAM = "key";
    public static final String LATEST_VERSION_ONLY_PARAM = "latestVersionOnly";

    protected final InstanceContainer<DecisionDefinitionFilter> filterDc;
    protected final Runnable loadDelegate;

    public DecisionDefinitionListQueryParamBinder(InstanceContainer<DecisionDefinitionFilter> filterDc, Runnable loadDelegate,
                                                  JmixFormLayout filterForm) {
        super();
        this.filterDc = filterDc;
        this.loadDelegate = loadDelegate;

        addValueChangeListeners(filterForm);
    }

    @Override
    public Component getComponent() {
        return null;
    }

    @Override
    protected Map<String, List<String>> getEmptyParametersMap() {
        return Map.of(NAME_PARAM, Collections.emptyList(),
                KEY_PARAM, Collections.emptyList(),
                LATEST_VERSION_ONLY_PARAM, Collections.emptyList());
    }

    @Override
    public void updateState(QueryParameters queryParameters) {
        DecisionDefinitionFilter definitionFilter = this.filterDc.getItem();

        String nameParam = getStringParam(queryParameters, NAME_PARAM);
        definitionFilter.setNameLike(nameParam);

        String keyParam = getStringParam(queryParameters, KEY_PARAM);
        definitionFilter.setKeyLike(keyParam);

        if (Strings.isNullOrEmpty(nameParam)) {
            Boolean latestVersionParam = getBooleanParam(queryParameters, LATEST_VERSION_ONLY_PARAM);
            definitionFilter.setLatestVersionOnly(latestVersionParam != null ? latestVersionParam : true);
        } else {
            definitionFilter.setLatestVersionOnly(false);
        }

        this.loadDelegate.run();
    }

    protected void addValueChangeListeners(JmixFormLayout filterForm) {
        TypedTextField<String> nameField = (TypedTextField<String>) filterForm.getOwnComponent("nameField");
        nameField.addTypedValueChangeListener(e -> {
            if (e.isFromClient()) {
                Map<String, String> params = new HashMap<>();
                params.put(NAME_PARAM, e.getValue());

                boolean nameEmpty = Strings.isNullOrEmpty(e.getValue());
                if (!nameEmpty) {
                    params.put(LATEST_VERSION_ONLY_PARAM, null);
                }

                updateQueryParams(params);
            }
        });

        Component keyField = filterForm.getOwnComponent("keyField");
        addComponentValueChangeListener(keyField, KEY_PARAM, o -> (String) o);

        Component latestVersionOnlyCb = filterForm.getComponent("lastVersionOnlyCb");
        addComponentValueChangeListener(latestVersionOnlyCb, LATEST_VERSION_ONLY_PARAM, o -> Boolean.toString((Boolean) o));
    }
}
