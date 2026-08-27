package io.flowset.control.view.dashboard;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
import io.jmix.chartsflowui.component.Chart;
import io.jmix.chartsflowui.kit.component.model.legend.AbstractLegend;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.flowset.control.entity.ProcessExecutionGraphEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@FragmentDescriptor("recent-activity-card-fragment.xml")
public class RecentActivityCardFragment extends DashboardCard<VerticalLayout> {
    protected static final Logger log = LoggerFactory.getLogger(RecentActivityCardFragment.class);

    @ViewComponent
    protected CollectionContainer<ProcessExecutionGraphEntry> processExecutionsDc;
    @ViewComponent
    protected Chart processExecutionGraph;
    @ViewComponent
    protected VerticalLayout loadingBox;
    @ViewComponent
    protected FlexLayout chartContainer;

    @Subscribe
    protected void onAttachEvent(final AttachEvent event) {
        initChart(List.of());
    }

    public void refresh(List<ProcessExecutionGraphEntry> weeklyStatistics) {
        updateComponents(false);

        initChart(weeklyStatistics);
    }

    protected void initChart(List<ProcessExecutionGraphEntry> weeklyStatistics) {
        processExecutionsDc.setItems(weeklyStatistics);

        AbstractLegend<?> legend = processExecutionGraph.getLegend();
        if (legend == null) {
            return;
        }

        legend.setFormatterFunction(generateLegendJsFormatter(weeklyStatistics));
    }

    protected void updateComponents(boolean loading) {
        loadingBox.setVisible(loading);
        chartContainer.getStyle().setDisplay(loading ? Style.Display.NONE : Style.Display.FLEX);
    }

    protected String generateLegendJsFormatter(List<ProcessExecutionGraphEntry> weeklyStatistics) {
        long startedTasksCount = weeklyStatistics.stream().mapToLong(ProcessExecutionGraphEntry::getStartedInstancesCount).sum();
        long completedTasksCount = weeklyStatistics.stream().mapToLong(ProcessExecutionGraphEntry::getCompletedInstancesCount).sum();

        String startTasksMsg = messageBundle.getMessage("startedInstancesLine.title");
        String startedTasksName = messageBundle.formatMessage("startedInstancesLine.countTitle", formattedCount(startedTasksCount));

        String completedTasksMsg = messageBundle.getMessage("completedInstancesLine.title");
        String completedTasksName = messageBundle.formatMessage("completedInstancesLine.titleCount", formattedCount(completedTasksCount));

        String formatterFunction = """
                function (name) {
                    if(name === '%s') return '%s';
                    if(name === '%s') return '%s';
                    return name;
                  }
                """;
        return String.format(formatterFunction, startTasksMsg, startedTasksName, completedTasksMsg, completedTasksName);
    }
}