/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.engine.auth;

import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.entity.engine.EngineAuthState;
import io.flowset.control.entity.engine.EngineType;
import io.flowset.control.test_support.AbstractIntegrationTest;
import io.flowset.control.test_support.AuthenticatedAsAdmin;
import io.jmix.core.DataManager;
import io.jmix.core.querycondition.PropertyCondition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(AuthenticatedAsAdmin.class)
@TestPropertySource(properties = {
        "flowset.control.engine.oauth2.lock-enabled=true",
        "flowset.control.engine.oauth2.max-retries=2"
})
public class EngineAuthStateServiceLockEnabledTest extends AbstractIntegrationTest {

    @Autowired
    DataManager dataManager;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    EngineAuthStateService engineAuthStateService;

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM control_engine_auth_state");
        jdbcTemplate.execute("DELETE FROM control_bpm_engine");
    }

    @Test
    @DisplayName("isLocked returns false when auth state does not exist")
    void testGivenNoAuthState_whenIsLocked_thenFalse() {
        //given
        BpmEngine engine = createEngine();

        //when
        boolean actualLocked = engineAuthStateService.isLocked(engine.getId());

        //then
        assertThat(actualLocked).isFalse();
    }

    @Test
    @DisplayName("findByEngineId returns null when auth state does not exist")
    void testGivenNoAuthState_whenFindByEngineId_thenNull() {
        //given
        BpmEngine engine = createEngine();

        //when
        EngineAuthState actualState = engineAuthStateService.findByEngineId(engine.getId());

        //then
        assertThat(actualState).isNull();
    }

    @Test
    @DisplayName("findByEngineId returns state when auth state exists")
    void testGivenExistingAuthState_whenFindByEngineId_thenStateReturned() {
        //given
        BpmEngine engine = createEngine();
        createEngineAuthState(engine, 1, false);

        //when
        EngineAuthState actualState = engineAuthStateService.findByEngineId(engine.getId());

        //then
        assertThat(actualState).isNotNull();
        assertThat(actualState.getEngine()).isEqualTo(engine);
        assertThat(actualState.getAccessTokenRetries()).isEqualTo(1);
        assertThat(actualState.getIsLocked()).isFalse();
        assertThat(actualState.getLockDate()).isNull();
    }

    @Test
    @DisplayName("registerAccessTokenFailure creates unlocked state with one retry on first failure")
    void testGivenNoAuthState_whenRegisterAccessTokenFailure_thenStateCreatedWithSingleRetryAndUnlocked() {
        //given
        BpmEngine engine = createEngine();

        //when
        engineAuthStateService.registerAccessTokenFailure(engine);

        //then
        Optional<EngineAuthState> actualStateOptional = loadEngineAuthState(engine.getId());
        assertThat(actualStateOptional)
                .isPresent()
                .get()
                .satisfies(state -> {
                    assertThat(state.getEngine()).isEqualTo(engine);
                    assertThat(state.getAccessTokenRetries()).isOne();
                    assertThat(state.getIsLocked()).isFalse();
                    assertThat(state.getLockDate()).isNull();
                });
    }

    @Test
    @DisplayName("registerAccessTokenFailure locks state when retries reach configured max")
    void testGivenSingleFailureState_whenRegisterAccessTokenFailure_thenStateBecomesLockedOnMaxRetries() {
        //given
        BpmEngine engine = createEngine();
        createEngineAuthState(engine, 1, false);

        //when
        engineAuthStateService.registerAccessTokenFailure(engine);

        //then
        Optional<EngineAuthState> actualStateOptional = loadEngineAuthState(engine.getId());
        assertThat(actualStateOptional)
                .isPresent()
                .get()
                .satisfies(state -> {
                    assertThat(state.getAccessTokenRetries()).isEqualTo(2);
                    assertThat(state.getIsLocked()).isTrue();
                    assertThat(state.getLockDate()).isNotNull();
                });
    }

    @Test
    @DisplayName("registerAccessTokenFailure does not change retries when state is already locked")
    void testGivenLockedState_whenRegisterAccessTokenFailure_thenStateNotChanged() {
        //given
        BpmEngine engine = createEngine();
        createEngineAuthState(engine, 2, true);
        OffsetDateTime expectedLockDate = loadEngineAuthState(engine.getId())
                .map(EngineAuthState::getLockDate)
                .orElseThrow();

        //when
        engineAuthStateService.registerAccessTokenFailure(engine);

        //then
        Optional<EngineAuthState> actualStateOptional = loadEngineAuthState(engine.getId());
        assertThat(actualStateOptional)
                .isPresent()
                .get()
                .satisfies(state -> {
                    assertThat(state.getAccessTokenRetries()).isEqualTo(2);
                    assertThat(state.getIsLocked()).isTrue();
                    assertThat(state.getLockDate()).isEqualTo(expectedLockDate);
                });
    }

    @Test
    @DisplayName("unlock removes existing state")
    void testGivenExistingState_whenUnlock_thenStateRemoved() {
        //given
        BpmEngine engine = createEngine();
        createEngineAuthState(engine, 2, true);

        //when
        engineAuthStateService.unlock(engine.getId());

        //then
        Optional<EngineAuthState> actualStateOptional = loadEngineAuthState(engine.getId());
        assertThat(actualStateOptional).isEmpty();
    }

    @Test
    @DisplayName("unlock does nothing when state does not exist")
    void testGivenNoAuthState_whenUnlock_thenStateStillAbsent() {
        //given
        BpmEngine engine = createEngine();

        //when
        engineAuthStateService.unlock(engine.getId());

        //then
        Optional<EngineAuthState> actualStateOptional = loadEngineAuthState(engine.getId());
        assertThat(actualStateOptional).isEmpty();
    }

    BpmEngine createEngine() {
        BpmEngine engine = dataManager.create(BpmEngine.class);
        engine.setName("auth-state-" + UUID.randomUUID().toString().substring(0, 8));
        engine.setType(EngineType.CAMUNDA_7);
        engine.setBaseUrl("http://localhost:18080/engine-rest/" + UUID.randomUUID());
        engine.setAuthEnabled(true);
        return dataManager.save(engine);
    }

    Optional<EngineAuthState> loadEngineAuthState(UUID engineId) {
        return dataManager.load(EngineAuthState.class)
                .condition(PropertyCondition.equal("engine.id", engineId))
                .optional();
    }

    void createEngineAuthState(BpmEngine engine, Integer retries, Boolean isLocked) {
        EngineAuthState state = dataManager.create(EngineAuthState.class);
        state.setEngine(engine);
        state.setAccessTokenRetries(retries);
        state.setIsLocked(isLocked);
        state.setLockDate(Boolean.TRUE.equals(isLocked) ? OffsetDateTime.now() : null);
        dataManager.save(state);
    }
}
