package io.flowset.control.uicomponent.grid;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.view.util.ComponentHelper;
import io.jmix.flowui.xml.layout.loader.component.DataGridLoader;

/**
 * Extends the functionality of DataGridLoader by adding a default empty state component when no specific
 * empty state component or text is provided.
 */
public class ControlDataGridLoader extends DataGridLoader {
    public static final String GRID_EMPTY_CONTENT_DEFAULT_ID = "emptyStateBox";

    protected ComponentHelper componentHelper;

    @Override
    protected void loadEmptyStateComponent() {
        super.loadEmptyStateComponent();

        if (resultComponent.getEmptyStateComponent() == null && resultComponent.getEmptyStateText() == null) {
            Component emptyStateComponent = createDefaultEmptyStateComponent();

            resultComponent.setEmptyStateComponent(emptyStateComponent);
        }
    }

    protected Component createDefaultEmptyStateComponent() {
        VerticalLayout emptyStateBox = new VerticalLayout();
        emptyStateBox.setId(GRID_EMPTY_CONTENT_DEFAULT_ID);
        emptyStateBox.setHeightFull();
        emptyStateBox.setWidthFull();
        emptyStateBox.addClassNames(LumoUtility.Gap.SMALL);
        emptyStateBox.setAlignItems(FlexComponent.Alignment.CENTER);
        emptyStateBox.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        getComponentHelper().addNoDataGridStateComponents(emptyStateBox);

        return emptyStateBox;
    }

    protected ComponentHelper getComponentHelper() {
        if (componentHelper == null) {
            componentHelper = applicationContext.getBean(ComponentHelper.class);
        }
        return componentHelper;
    }

}
