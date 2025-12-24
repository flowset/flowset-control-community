package io.flowset.control.view.job.column.state;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.flowset.control.entity.job.JobData;
import io.flowset.control.entity.job.JobState;
import io.jmix.core.Messages;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.FragmentRenderer;
import io.jmix.flowui.fragmentrenderer.RendererItemContainer;
import io.jmix.flowui.view.ViewComponent;
import org.springframework.beans.factory.annotation.Autowired;

@FragmentDescriptor("job-state-column-fragment.xml")
@RendererItemContainer("jobDc")
public class JobStateColumnFragment extends FragmentRenderer<HorizontalLayout, JobData> {

    @ViewComponent
    protected Span stateBadge;
    @Autowired
    protected Messages messages;

    @Override
    public void setItem(JobData item) {
        super.setItem(item);

        JobState state = item.getState();
        String message = messages.getMessage(state);
        stateBadge.setText(message);

        if(state == JobState.SUSPENDED) {
            stateBadge.getElement().getThemeList().add("warning");
        } else {
            stateBadge.getElement().getThemeList().add("success");
        }
    }
}