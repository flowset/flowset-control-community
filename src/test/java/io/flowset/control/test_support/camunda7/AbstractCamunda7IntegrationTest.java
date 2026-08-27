/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.camunda7;

import io.flowset.control.entity.engine.EngineType;
import io.flowset.control.test_support.AbstractIntegrationTest;
import io.flowset.control.test_support.EnabledOnEngine;

/**
 * A base class for integration test related to Camunda 7 and compatible with Camunda 7 engines.
 */
@EnabledOnEngine({EngineType.CAMUNDA_7, EngineType.OPERATON})
public class AbstractCamunda7IntegrationTest extends AbstractIntegrationTest {
}
