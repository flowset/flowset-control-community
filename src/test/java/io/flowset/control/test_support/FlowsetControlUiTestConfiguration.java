/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Profile;

import static io.flowset.control.test_support.Constants.UI_TEST_PROFILE;

/**
 * Configuration for UI tests.
 */
@Profile(UI_TEST_PROFILE)
@TestConfiguration
@ConfigurationPropertiesScan
public class FlowsetControlUiTestConfiguration {

}
