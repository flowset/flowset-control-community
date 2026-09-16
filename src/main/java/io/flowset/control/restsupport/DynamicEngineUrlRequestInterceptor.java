package io.flowset.control.restsupport;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.exception.EngineNotSelectedException;
import io.flowset.control.service.engine.auth.EngineAuthenticator;
import io.flowset.control.service.engine.EngineService;

public class DynamicEngineUrlRequestInterceptor implements RequestInterceptor {
    protected final EngineService engineService;
    protected final EngineAuthenticator engineAuthenticator;

    public DynamicEngineUrlRequestInterceptor(EngineService engineService,
                                              EngineAuthenticator engineAuthenticator) {
        this.engineService = engineService;
        this.engineAuthenticator = engineAuthenticator;
    }

    @Override
    public void apply(RequestTemplate template) {
        BpmEngine selectedEngine = engineService.getSelectedEngine();
        if (selectedEngine == null) {
            throw new EngineNotSelectedException("BPM engine not selected");
        }
        template.target(selectedEngine.getBaseUrl());
        engineAuthenticator.applyAuthentication(selectedEngine, template);
    }
}
