package io.flowset.control.view.main;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.view.main.selectenginepopover.SelectEnginePopoverContentFragment;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.flowset.control.entity.EngineConnectionCheckResult;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.entity.engine.EngineType;
import io.flowset.control.view.bpmengine.EngineEnvironmentBadgeFragment;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.vaadin.addons.componentfactory.spinner.Spinner;

@FragmentDescriptor("engine-status-badge-fragment.xml")
public class EngineStatusBadgeFragment extends Fragment<HorizontalLayout> {
    @Autowired
    protected Fragments fragments;
    @Autowired
    protected DataManager dataManager;

    @ViewComponent
    protected Spinner statusSpinner;

    @ViewComponent
    protected InstanceContainer<EngineConnectionCheckResult> engineConnectionStatusDc;
    @ViewComponent

    protected InstanceContainer<BpmEngine> selectedEngineDc;

    @ViewComponent
    protected HorizontalLayout engineStateBadge;

    @ViewComponent
    protected Div connectionStatusText;

    @ViewComponent
    protected EngineEnvironmentBadgeFragment envBadge;
    @ViewComponent
    protected Span successStatusBadge;
    @ViewComponent
    protected Span warningStatusBadge;

    @ViewComponent
    protected MessageBundle messageBundle;

    @ViewComponent
    protected JmixButton viewEngineConfigBtn;

    @ViewComponent
    protected Icon warningStatusIcon;

    @Autowired
    protected Messages messages;

    @Autowired
    protected DialogWindows dialogWindows;

    protected Popover popover;

    @Subscribe
    protected void onAttachEvent(final AttachEvent event) {
        initConnectionStatusComponents();
        if (popover == null) {
            initEnginePopover();
        }
        addButtonClickHandlers();
    }

    public void initConnectionStatusComponents() {
        statusSpinner.setVisible(true);
        statusSpinner.setLoading(true);

        successStatusBadge.setVisible(false);
        warningStatusIcon.setVisible(false);

        if (selectedEngineDc.getItemOrNull() != null) {
            addSelectedEngineData(null);
        } else {
            setNoSelectedEngineStatus();
        }
    }

    public void updateConnectionStatusComponents() {
        statusSpinner.setLoading(false);

        EngineConnectionCheckResult item = engineConnectionStatusDc.getItem();
        if (BooleanUtils.isTrue(item.getSuccess())) {
            setSuccessfulConnectionStatus(item.getVersion());
        } else {
            setFailedConnectionStatus();
        }
    }

    public void setFailedConnectionStatus() {
        engineStateBadge.removeClassNames(LumoUtility.TextColor.WARNING, LumoUtility.AlignItems.CENTER);
        engineStateBadge.addClassNames(LumoUtility.AlignItems.BASELINE);

        statusSpinner.setLoading(false);
        addSelectedEngineData(null);

        successStatusBadge.setVisible(false);
        warningStatusIcon.setVisible(false);
        warningStatusBadge.setVisible(true);
    }

    public void setNoSelectedEngineStatus() {
        connectionStatusText.removeAll();
        connectionStatusText.setText(messageBundle.getMessage("noSelectedEngine"));

        connectionStatusText.getElement().getThemeList().clear();
        connectionStatusText.getElement().getThemeList().add("warning");
        engineStateBadge.addClassNames(LumoUtility.TextColor.WARNING, LumoUtility.AlignItems.CENTER);

        successStatusBadge.setVisible(false);
        warningStatusIcon.setVisible(true);
        warningStatusBadge.setVisible(false);
        statusSpinner.setLoading(false);
        envBadge.setVisible(false);
    }

    @Subscribe(id = "viewEngineConfigBtn", subject = "clickListener")
    public void onViewEngineConfigBtnClick(final ClickEvent<JmixButton> event) {
        if (popover != null) {
            if (popover.isOpened()) {
                popover.close();
            } else {
                popover.open();
            }
        }
    }

    protected void setSuccessfulConnectionStatus(String version) {
        addSelectedEngineData(version);

        successStatusBadge.setVisible(true);
        warningStatusIcon.setVisible(false);
        warningStatusBadge.setVisible(false);
    }

    protected void initEnginePopover() {
        popover = new Popover();
        popover.addClassNames("select-engine-popover");

        popover.setTarget(getContent());
        popover.setPosition(PopoverPosition.BOTTOM);

        popover.addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                updatePopoverContent();
            }
        });

        updatePopoverContent();
    }

    protected void updatePopoverContent() {
        popover.removeAll();

        SelectEnginePopoverContentFragment popoverContent = fragments.create(this, SelectEnginePopoverContentFragment.class);
        popoverContent.setSelectedEngineDc(selectedEngineDc);
        popoverContent.init();
        popoverContent.setPopover(popover);

        popover.add(popoverContent);
    }

    protected void addSelectedEngineData(@Nullable String version) {
        connectionStatusText.removeAll();

        engineStateBadge.removeClassNames(LumoUtility.TextColor.WARNING, LumoUtility.AlignItems.CENTER);
        engineStateBadge.addClassNames(LumoUtility.AlignItems.BASELINE);

        BpmEngine engine = selectedEngineDc.getItem();

        envBadge.setItem(engine);

        Span engineName = new Span(engine.getName());
        engineName.setMaxWidth("12em");
        engineName.addClassNames(LumoUtility.TextOverflow.ELLIPSIS,
                LumoUtility.Overflow.HIDDEN, LumoUtility.Whitespace.NOWRAP);
        connectionStatusText.add(engineName);

        String engineTypeAndVersion;
        if (version != null) {
            String generalTypeName = messages.getMessage(EngineType.class, "EngineType.%s.general".formatted(engine.getType().name()));
            engineTypeAndVersion = "(%s %s)".formatted(generalTypeName, version);
        } else {
            engineTypeAndVersion = "(%s)".formatted(messages.getMessage(engine.getType()));
        }
        connectionStatusText.setTitle("%s %s".formatted(engine.getBaseUrl(), engineTypeAndVersion));
    }

    protected void addButtonClickHandlers() {
        addNoPropagationClickListener(viewEngineConfigBtn);
    }

    protected void addNoPropagationClickListener(Component component) {
        component.getElement().addEventListener("click", ignore -> {
        }).addEventData("event.stopPropagation()");
    }
}