/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.main;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.Unknown;
import io.jmix.masquerade.sys.Composite;
import lombok.Getter;

/**
 * Wrapper for the User tasks card on the dashboard.
 * Source component: {@link io.flowset.control.view.dashboard.UserTaskStatisticsCardFragment}
 */
@Getter
public class UserTasksCard extends Composite<UserTasksCard> {
    @TestComponent(path = "userTasksCount")
    private Unknown userTasksCount;

    @TestComponent(path = "viewUserTaskBtn")
    private Button viewUserTaskBtn;
}
