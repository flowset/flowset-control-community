package io.flowset.control.view.main.selectenginepopover;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.property.UiProperties;
import io.flowset.control.service.engine.EngineTimeService;
import io.flowset.control.view.bpmengine.EngineEnvironmentBadgeFragment;
import io.jmix.core.Messages;
import io.jmix.flowui.asynctask.UiAsyncTasks;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.FragmentRenderer;
import io.jmix.flowui.fragmentrenderer.RendererItemContainer;
import io.jmix.flowui.view.ViewComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

@Slf4j
@FragmentDescriptor("engine-item-fragment.xml")
@RendererItemContainer("bpmEngineDc")
public class EngineItemFragment extends FragmentRenderer<HorizontalLayout, BpmEngine> {

    @ViewComponent
    protected EngineEnvironmentBadgeFragment envField;
    @Autowired
    protected Messages messages;
    @Autowired
    protected EngineTimeService engineTimeService;
    @ViewComponent
    protected Span engineName;
    @ViewComponent
    protected Span engineTime;

    @Autowired
    private UiProperties uiProperties;

    @Autowired
    private UiAsyncTasks uiAsyncTasks;

    @Override
    public void setItem(BpmEngine item) {
        super.setItem(item);

        String engineNameValue = "%s (%s)".formatted(item.getName(), messages.getMessage(item.getType()));
        engineName.setText(engineNameValue);

        uiAsyncTasks.supplierConfigurer(() -> engineTimeService.getEngineTimeDefaultFormat(item.getId()))
                .withTimeout(uiProperties.getEngineTimeLoadTimeoutSec(), TimeUnit.SECONDS)
                .withResultHandler(time -> {
                    if (time != null) {
                        engineTime.setVisible(true);
                        engineTime.setText(time);
                    } else {
                        engineTime.setVisible(false);
                    }
                })
                .withExceptionHandler(throwable -> {
                    log.error("Error occurs on engine fragment engine time loading", throwable);
                    engineTime.setVisible(false);
                })
                .supplyAsync();

        envField.setItem(item);
    }
}