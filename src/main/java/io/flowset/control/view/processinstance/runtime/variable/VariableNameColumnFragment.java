package io.flowset.control.view.processinstance.runtime.variable;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.entity.variable.VariableInstanceData;
import io.flowset.control.view.entitydetaillink.EntityDetailLinkFragment;
import io.flowset.control.view.processvariable.VariableInstanceDataDetail;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.RendererItemContainer;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;

import java.util.function.Function;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@FragmentDescriptor("variable-name-column-fragment.xml")
@RendererItemContainer("variableDc")
public class VariableNameColumnFragment extends EntityDetailLinkFragment<HorizontalLayout, VariableInstanceData> {

    @ViewComponent
    protected JmixButton nameBtn;

    protected ProcessInstanceData processInstance;
    protected Function<VariableInstanceData, String> scopeLabelProvider;
    protected boolean saveEnabled = false;

    @SuppressWarnings("LombokSetterMayBeUsed")
    public void setProcessInstance(ProcessInstanceData processInstance) {
        this.processInstance = processInstance;
    }

    @SuppressWarnings("LombokSetterMayBeUsed")
    public void setSaveEnabled(boolean saveEnabled) {
        this.saveEnabled = saveEnabled;
    }

    @SuppressWarnings("LombokSetterMayBeUsed")
    public void setScopeLabelProvider(Function<VariableInstanceData, String> scopeLabelProvider) {
        this.scopeLabelProvider = scopeLabelProvider;
    }

    @Override
    public void setItem(VariableInstanceData item) {
        super.setItem(item);

        nameBtn.setText(item.getName());
    }

    @Subscribe(id = "nameBtn", subject = "clickListener")
    public void onNameBtnClick(final ClickEvent<JmixButton> event) {
        dialogWindows.detail(getCurrentView(), VariableInstanceData.class)
                .editEntity(item)
                .withViewClass(VariableInstanceDataDetail.class)
                .withViewConfigurer(view -> {
                    view.setSaveEnabled(saveEnabled);
                    view.setValidationEnabled(true);
                    view.setProcessInstance(processInstance);
                    view.setScopeLabelProvider(scopeLabelProvider);
                })
                .withAfterCloseListener(afterCloseEvent -> {
                    if (afterCloseEvent.closedWith(StandardOutcome.SAVE)
                            && afterSaveHandler != null) {
                        afterSaveHandler.run();
                    }
                })
                .open();
    }
}