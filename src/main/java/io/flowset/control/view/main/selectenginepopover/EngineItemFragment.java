package io.flowset.control.view.main.selectenginepopover;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.service.engine.EngineTimeService;
import io.flowset.control.view.bpmengine.EngineEnvironmentBadgeFragment;
import io.jmix.core.Messages;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.FragmentRenderer;
import io.jmix.flowui.fragmentrenderer.RendererItemContainer;
import io.jmix.flowui.view.ViewComponent;
import org.springframework.beans.factory.annotation.Autowired;

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
    private Span engineTime;

    @Override
    public void setItem(BpmEngine item) {
        super.setItem(item);

        String engineNameValue = "%s (%s)".formatted(item.getName(), messages.getMessage(item.getType()));
        engineName.setText(engineNameValue);

        String time = engineTimeService.getEngineTimeDefaultFormat(item.getId());
        if (time != null) {
            engineTime.setVisible(true);
            engineTime.setText(time);
        } else {
            engineTime.setVisible(false);
        }

        envField.setItem(item);
    }
}