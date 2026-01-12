package io.flowset.control.view.bpmengine;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.entity.engine.EnvironmentType;
import io.jmix.core.Messages;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.FragmentRenderer;
import io.jmix.flowui.fragmentrenderer.RendererItemContainer;
import io.jmix.flowui.view.ViewComponent;
import org.springframework.beans.factory.annotation.Autowired;

@FragmentDescriptor("engine-environment-badge-fragment.xml")
@RendererItemContainer("bpmEngineDc")
public class EngineEnvironmentBadgeFragment extends FragmentRenderer<Span, BpmEngine> {

    @ViewComponent
    protected Span root;
    @Autowired
    protected Messages messages;

    protected boolean small = true;

    @Override
    public void setItem(BpmEngine item) {
        super.setItem(item);

        EnvironmentType environmentType = item.getEnvironmentType();
        if (environmentType != null) {
            root.setVisible(true);
            root.setText(messages.getMessage(environmentType));

            root.getElement().getThemeList().clear();
            root.getElement().getThemeList().add("badge pill engine-env-%s".formatted(environmentType.name().toLowerCase()));

            addIcon(environmentType);
            updateSmallTheme();
        } else {
            root.setVisible(false);
        }
    }

    public void setSmall(boolean small) {
        this.small = small;
        updateSmallTheme();
    }

    protected void addIcon(EnvironmentType environmentType) {
        if (environmentType == EnvironmentType.PROD) {
            Icon icon = VaadinIcon.BOLT.create();
            icon.getStyle().set("padding", "var(--lumo-space-xs)");
            root.addComponentAsFirst(icon);
        }
    }

    protected void updateSmallTheme() {
        if (small) {
            root.getElement().getThemeList().add("small");
        } else {
            root.getElement().getThemeList().remove("small");
        }
    }
}

