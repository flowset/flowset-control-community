package io.flowset.control.aop;

import io.flowset.control.service.analytics.AmplitudeEventType;
import io.flowset.control.service.analytics.AnalyticsService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class AnalyticsAspect {

    private final AnalyticsService analyticsService;

    public AnalyticsAspect(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @Before("execution(* io.flowset.control.uicomponent.viewer.handler.CallActivityOverlayClickHandler.handleProcessNavigation(..))")
    private void calledProcessNavigation() {
        analyticsService.logEvent(AmplitudeEventType.CONTROL_OPEN_CALLED_PROCESS_VIEW);
    }

    @Before("execution(* io.flowset.control.uicomponent.viewer.handler.CallActivityOverlayClickHandler.handleInstancesNavigation(..))")
    private void calledProcessInstancesNavigation() {
        analyticsService.logEvent(AmplitudeEventType.CONTROL_OPEN_CALLED_INSTANCES_VIEW);
    }

    @Before("execution (* io.flowset.control.service.engine.impl.EngineServiceImpl.setSelectedEngine(..))")
    private void selectEngine() {
        analyticsService.logEvent(AmplitudeEventType.CONTROL_SELECT_ENGINE);
    }

    @Before("execution(* io.flowset.control.uicomponent.viewer.handler.BusinessRuleTaskOverlayClickHandler.handleDecisionNavigation(..))")
    private void dmnNavigation() {
        analyticsService.logEvent(AmplitudeEventType.CONTROL_NAVIGATE_TO_DECISION_TABLE);
    }
}
