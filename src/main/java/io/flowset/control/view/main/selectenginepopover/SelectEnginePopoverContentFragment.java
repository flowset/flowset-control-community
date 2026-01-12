package io.flowset.control.view.main.selectenginepopover;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.service.engine.EngineUiService;
import io.flowset.control.view.bpmengine.BpmEngineDetailView;
import io.flowset.control.view.engineconnectionsettings.EngineConnectionSettingsView;
import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.core.Sort;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.component.listbox.JmixListBox;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.Install;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.Target;
import io.jmix.flowui.view.ViewComponent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.util.List;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@FragmentDescriptor("select-engine-popover-content-fragment.xml")
public class SelectEnginePopoverContentFragment extends Fragment<VerticalLayout> {
    @Autowired
    protected EngineUiService engineUiService;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected DialogWindows dialogWindows;

    @ViewComponent
    protected CollectionContainer<BpmEngine> enginesDc;
    @ViewComponent
    protected CollectionLoader<BpmEngine> enginesDl;

    @ViewComponent
    protected TypedTextField<String> searchField;
    @ViewComponent
    protected VerticalLayout emptyEnginesBox;
    @ViewComponent
    protected JmixListBox<BpmEngine> engineListBox;

    protected Popover popover;
    protected InstanceContainer<BpmEngine> selectedEngineDc;

    public void setPopover(Popover popover) {
        this.popover = popover;
    }

    public void setSelectedEngineDc(InstanceContainer<BpmEngine> selectedEngineDc) {
        this.selectedEngineDc = selectedEngineDc;
    }

    public void init() {
        refreshValues();
        setSelectedEngine(selectedEngineDc.getItemOrNull());
    }

    protected void refreshValues() {
        enginesDl.load();
        List<Component> list = engineListBox.getChildren().filter(component -> component instanceof Hr).toList();
        engineListBox.remove(list);

        List<BpmEngine> engines = enginesDc.getItems();
        engines.forEach(bpmEngine -> {
            engineListBox.addComponents(bpmEngine, new Hr());
        });
        engineListBox.setVisible(!engines.isEmpty());
        emptyEnginesBox.setVisible(engines.isEmpty());
    }

    protected void setSelectedEngine(@Nullable BpmEngine engine) {
        if (engine != null) {
            boolean containsEngine = enginesDc.containsItem(engine);
            if (containsEngine) {
                engineListBox.setValue(engine);
            } else {
                engineListBox.setValue(null);
            }
        } else {
            engineListBox.setValue(null);
        }
    }

    @Subscribe(id = "createBpmEnginBtn", subject = "clickListener")
    public void onCreateBpmEnginBtnClick(final ClickEvent<JmixButton> event) {
        popover.close();
        dialogWindows.detail(getCurrentView(), BpmEngine.class)
                .newEntity()
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                        BpmEngineDetailView detailView = (BpmEngineDetailView) closeEvent.getView();
                        BpmEngine createdEngine = detailView.getEditedEntity();
                        engineUiService.selectEngine(createdEngine);
                    }
                })
                .open();
    }

    @Subscribe("engineListBox")
    public void onEngineListBoxComponentValueChange(final AbstractField.ComponentValueChangeEvent<JmixListBox<BpmEngine>, BpmEngine> event) {
        if (event.isFromClient()) {
            engineUiService.selectEngine(event.getValue());
            popover.close();
        }
    }

    @Subscribe(id = "advancedModeBtn", subject = "clickListener")
    public void onAdvancedModeBtnClick(final ClickEvent<JmixButton> event) {
        popover.close();
        dialogWindows.view(getCurrentView(), EngineConnectionSettingsView.class)
                .open();
    }

    @Subscribe(id = "closeBtn", subject = "clickListener")
    public void onCloseBtnClick(final ClickEvent<JmixButton> event) {
        popover.close();
    }

    @Subscribe("searchField")
    public void onSearchFieldTypedValueChange(final SupportsTypedValue.TypedValueChangeEvent<TypedTextField<String>, String> event) {
        if (event.isFromClient()) {
            init();
        }
    }

    @Install(to = "enginesDl", target = Target.DATA_LOADER)
    protected List<BpmEngine> enginesDlLoadDelegate(final LoadContext<BpmEngine> loadContext) {
        String searchString = searchField.getTypedValue();
        Condition engineCondition;
        if (StringUtils.isBlank(searchString)) {
            engineCondition = LogicalCondition.and();
        } else {
            engineCondition = LogicalCondition.or(PropertyCondition.contains("name", searchString),
                    PropertyCondition.contains("baseUrl", searchString));
        }

        return dataManager.load(BpmEngine.class)
                .condition(engineCondition)
                .sort(Sort.by(Sort.Direction.ASC, "name"))
                .list();
    }
}