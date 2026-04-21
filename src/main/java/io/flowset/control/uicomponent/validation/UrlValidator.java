/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.uicomponent.validation;

import io.flowset.control.util.UrlUtils;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.flowui.component.validation.AbstractValidator;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * A validator class that checks if a given string value is a valid URL.
 */
@Component("control_UrlValidator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UrlValidator extends AbstractValidator<String> implements InitializingBean {

    @Override
    public void accept(@Nullable String value) {
        if (!UrlUtils.isValidUrl(value)) {
            String message = getMessage();
            this.defaultMessage = messages.getMessage("validation.constraints.invalidUrl");

            fireValidationException(message == null ? defaultMessage : message,
                    ParamsMap.of("value", value));
        }
    }
}
