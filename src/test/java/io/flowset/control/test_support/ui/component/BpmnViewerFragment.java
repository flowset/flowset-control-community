/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.component;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebElementCondition;
import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.sys.Composite;
import lombok.Getter;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.shadowCss;
import static org.openqa.selenium.By.cssSelector;

/**
 * Wrapper for the BPMN diagram viewer fragment used in process and deployment detail views.
 * Source component: {@link io.flowset.uikit.fragment.bpmnviewer.BpmnViewerFragment}.
 */
@Getter
public class BpmnViewerFragment extends Composite<BpmnViewerFragment> {
    public static final String BPMN_VIEWER_TAG = "flowset-bpmn-viewer";
    public static final String ACTIVITY_STATISTICS_OVERLAY_CLASS = "djs-overlay-activity-statistics";
    public static final String CALLED_PROCESS_OVERLAY_CLASS = "djs-overlay-called-process";
    public static final String CALLED_PROCESS_INSTANCE_OVERLAY_CLASS = "djs-overlay-called-process-instance";
    public static final String DECISION_OVERLAY_CLASS = "djs-overlay-decision";
    public static final String DECISION_INSTANCE_OVERLAY_CLASS = "djs-overlay-decision-instance";
    public static final String INCIDENT_COUNT_OVERLAY_CLASS = "djs-overlay-incident-count";

    public static final WebElementCondition ACTIVE_ELEMENT = cssClass("activity-hover");
    public static final WebElementCondition SELECTED_ELEMENT = cssClass("primary-color-activity");

    public static final By DIAGRAM_SVG_VIEWPORT_BY = shadowCss(".viewport", BPMN_VIEWER_TAG);
    public static final By RUNNING_INSTANCES_OVERLAY = cssSelector(".running-instances-overlay");
    public static final By INCIDENT_COUNT_OVERLAY = cssSelector(".incident-overlay");

    @TestComponent(path = "viewerFragmentViewerContainer")
    private SelenideElement bpmnViewerContainer;

    @TestComponent(path = {"viewerFragmentActionsLeftBox", "viewerFragmentShowStatisticsBtn"})
    private Button viewActivityStatisticsButton;

    @TestComponent(path = {"viewerFragmentActionsLeftBox", "viewerFragmentShowDocumentationBtn"})
    private Button viewDocumentationButton;

    @TestComponent(path = "viewerFragmentZoomResetBtn")
    private Button resetZoomButton;

    @TestComponent(path = "viewerFragmentZoomInBtn")
    private Button zoomInButton;

    @TestComponent(path = "viewerFragmentZoomOutBtn")
    private Button zoomOutButton;

    /**
     * Returns all overlays for the given element id.
     *
     * @param elementId the id of the diagram element from BPMN XML
     * @return the collection of overlays
     */
    public ElementsCollection getOverlaysByElementId(String elementId) {
        return bpmnViewerContainer.$$(shadowCss(
                ".djs-overlays[data-container-id=\"" + elementId + "\"] .djs-overlay",
                BPMN_VIEWER_TAG));
    }

    /**
     * Returns the diagram element by its id.
     *
     * @param elementId the id of the diagram element from BPMN XML
     * @return the diagram element
     */
    public SelenideElement getProcessDiagramElementById(String elementId) {
        return bpmnViewerContainer.$(shadowCss("[data-element-id=\"" + elementId + "\"]",
                        BPMN_VIEWER_TAG))
                .shouldBe(exist)
                .shouldBe(visible);
    }

    /**
     * Returns the activity statistics overlay for the given element id.
     *
     * @param elementId the id of the diagram element from BPMN XML
     * @return the activity statistics overlay
     */
    public SelenideElement getActivityStatisticsOverlay(String elementId) {
        return getOverlayByElementId(elementId, ACTIVITY_STATISTICS_OVERLAY_CLASS);
    }

    /**
     * Returns the overlay for the called process or called process instance.
     *
     * @param elementId the id of the diagram element from BPMN XML
     * @return the overlay
     */
    public SelenideElement getCalledProcessOverlay(String elementId) {
        return getOverlayByElementId(elementId, CALLED_PROCESS_OVERLAY_CLASS);
    }

    /**
     * Returns the overlay for the called process instance.
     *
     * @param elementId the id of the diagram element from BPMN XML
     * @return the overlay
     */
    public SelenideElement getCalledProcessInstanceOverlay(String elementId) {
        return getOverlayByElementId(elementId, CALLED_PROCESS_INSTANCE_OVERLAY_CLASS);
    }

    /**
     * Returns the running instances overlay for the given element id.
     *
     * @param elementId the id of the diagram element from BPMN XML
     * @return the running instances overlay
     */
    public SelenideElement getRunningInstancesOverlay(String elementId) {
        return getActivityStatisticsOverlay(elementId).$(RUNNING_INSTANCES_OVERLAY);
    }

    /**
     * Returns the overlay for the decision or decision instance.
     *
     * @param elementId the id of the diagram element from BPMN XML
     * @return the diagram element overlay
     */
    public SelenideElement getDecisionOverlay(String elementId) {
        return getOverlayByElementId(elementId, DECISION_OVERLAY_CLASS);
    }

    /**
     * Returns the overlay for the decision instance.
     *
     * @param elementId the id of the diagram element from BPMN XML
     * @return the diagram element overlay
     */
    public SelenideElement getDecisionInstanceOverlay(String elementId) {
        return getOverlayByElementId(elementId, DECISION_INSTANCE_OVERLAY_CLASS);
    }

    /**
     * Returns the incident count overlay for the given element id.
     *
     * @param elementId the id of the diagram element from BPMN XML
     * @return the incident count overlay
     */
    public SelenideElement getIncidentCountOverlay(String elementId) {
        return getOverlayByElementId(elementId, INCIDENT_COUNT_OVERLAY_CLASS);
    }

    /**
     * Returns the overlay by element id and overlay class.
     *
     * @param elementId    the id of the diagram element from BPMN XML
     * @param overlayClass the class of the overlay
     * @return the overlay element
     */
    protected SelenideElement getOverlayByElementId(String elementId, String overlayClass) {
        return bpmnViewerContainer.$(shadowCss(
                ".djs-overlays[data-container-id=\"" + elementId + "\"] .djs-overlay." + overlayClass,
                BPMN_VIEWER_TAG));
    }
}
