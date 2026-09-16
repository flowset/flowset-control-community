package io.flowset.control.view.processinstance.runtime.variable;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.flowset.control.entity.variable.VariableInstanceData;
import io.flowset.control.view.entitydetaillink.EntityDetailLinkFragment;
import io.flowset.control.view.processvariable.VariableInstanceDataDetail;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.RendererItemContainer;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@FragmentDescriptor("variable-name-column-fragment.xml")
@RendererItemContainer("variableDc")
public class VariableNameColumnFragment extends EntityDetailLinkFragment<HorizontalLayout, VariableInstanceData> {

    @ViewComponent
    protected JmixButton nameBtn;

    @Override
    public void setItem(VariableInstanceData item) {
        super.setItem(item);

        nameBtn.setText(item.getName());
    }

    @Subscribe(id = "nameBtn", subject = "clickListener")
    public void onNameBtnClick(final ClickEvent<JmixButton> event) {
        dialogWindows.detail(getCurrentView(), VariableInstanceData.class)
                .editEntity(item)
                .withViewClass(VariableInstanceDataDetail.class).withViewConfigurer(view -> {
                    view.setSaveEnabled(true);
                    view.setValidationEnabled(true);
                }).withAfterCloseListener(afterCloseEvent -> {
                    if (afterCloseEvent.closedWith(StandardOutcome.SAVE) && afterSaveHandler != null) {
                        afterSaveHandler.run();
                    }
                }).build().open();
    }
}