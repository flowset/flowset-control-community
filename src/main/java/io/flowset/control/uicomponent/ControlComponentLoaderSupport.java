package io.flowset.control.uicomponent;

import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.support.ComponentLoaderSupport;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * An extended implementation of {@link ComponentLoaderSupport} that supports loading a duration value in milliseconds.
 */
@Component("control_ComponentLoaderSupport")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Primary
public class ControlComponentLoaderSupport extends ComponentLoaderSupport {
    public ControlComponentLoaderSupport(ComponentLoader.Context context) {
        super(context);
    }

    @Override
    public Optional<Duration> loadDuration(Element element, String attributeName) {
       return loaderSupport.loadString(element, attributeName)
                .map(stepString -> {
                    Duration step;

                    if (stepString.endsWith("h")) {
                        step = Duration.ofHours(Long.parseLong(StringUtils.chop(stepString)));
                    } else if (stepString.endsWith("m")) {
                        step = Duration.ofMinutes(Long.parseLong(StringUtils.chop(stepString)));
                    } else if (stepString.endsWith("ms")) { // not supported by Jmix
                        step = Duration.ofMillis(Long.parseLong(StringUtils.substring(stepString, 0, -2)));
                    } else if (stepString.endsWith("s")) {
                        step = Duration.ofSeconds(Long.parseLong(StringUtils.chop(stepString)));
                    } else {
                        step = Duration.ofMinutes(Long.parseLong(stepString));
                    }

                    return step;
                });
    }
}