/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.camunda7;

import io.flowset.control.entity.engine.EngineType;
import io.flowset.control.test_support.AbstractUiTest;
import io.flowset.control.test_support.EnabledOnEngine;

@EnabledOnEngine({EngineType.CAMUNDA_7, EngineType.OPERATON})
public class AbstractCamunda7UiTest extends AbstractUiTest {
}
