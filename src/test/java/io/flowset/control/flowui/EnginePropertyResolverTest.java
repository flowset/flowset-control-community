/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.flowui;

import io.flowset.control.annotation.EngineProperty;
import io.flowset.control.entity.engine.EngineFamily;
import io.flowset.control.entity.engine.EngineType;
import io.jmix.core.metamodel.model.MetaProperty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("integrationTest")
@DisplayName("Attribute support by engine in EnginePropertyResolver")
class EnginePropertyResolverTest {

    private final EnginePropertyResolver resolver = new EnginePropertyResolver();

    @Test
    @DisplayName("Attribute without annotation is supported")
    void givenNoAnnotation_whenIsSupported_thenTrueReturned() {
        // given
        MetaProperty metaProperty = getMetaProperty("notAnnotated");

        // when
        boolean supported = resolver.isSupported(metaProperty, EngineType.CAMUNDA_7);

        // then
        assertThat(supported).isTrue();
    }

    @Test
    @DisplayName("Attribute with empty annotation is supported")
    void givenEmptyAnnotation_whenIsSupported_thenTrueReturned() {
        // given
        MetaProperty metaProperty = getMetaProperty("empty");

        // when
        boolean supported = resolver.isSupported(metaProperty, EngineType.CAMUNDA_7);

        // then
        assertThat(supported).isTrue();
    }

    @Test
    @DisplayName("Attribute is supported for a supported engine type")
    void givenSupportedEngine_whenIsSupportedForThisEngine_thenTrueReturned() {
        // given
        MetaProperty metaProperty = getMetaProperty("supportedOnOperaton");

        // when
        boolean supported = resolver.isSupported(metaProperty, EngineType.OPERATON);

        // then
        assertThat(supported).isTrue();
    }

    @Test
    @DisplayName("Attribute is not supported for an engine type not in supported engines")
    void givenSupportedEngine_whenIsSupportedForOtherEngine_thenFalseReturned() {
        // given
        MetaProperty metaProperty = getMetaProperty("supportedOnOperaton");

        // when
        boolean supported = resolver.isSupported(metaProperty, EngineType.CAMUNDA_7);

        // then
        assertThat(supported).isFalse();
    }

    @Test
    @DisplayName("Attribute with supported engines is not supported without engine type")
    void givenSupportedEngine_whenIsSupportedWithoutEngine_thenFalseReturned() {
        // given
        MetaProperty metaProperty = getMetaProperty("supportedOnOperaton");

        // when
        boolean supported = resolver.isSupported(metaProperty, null);

        // then
        assertThat(supported).isFalse();
    }

    @Test
    @DisplayName("Attribute is not supported for an unsupported engine type")
    void givenUnsupportedEngine_whenIsSupportedForThisEngine_thenFalseReturned() {
        // given
        MetaProperty metaProperty = getMetaProperty("unsupportedOnOperaton");

        // when
        boolean supported = resolver.isSupported(metaProperty, EngineType.OPERATON);

        // then
        assertThat(supported).isFalse();
    }

    @Test
    @DisplayName("Attribute is supported for an engine type not in unsupported engines")
    void givenUnsupportedEngine_whenIsSupportedForOtherEngine_thenTrueReturned() {
        // given
        MetaProperty metaProperty = getMetaProperty("unsupportedOnOperaton");

        // when
        boolean supported = resolver.isSupported(metaProperty, EngineType.CAMUNDA_7);

        // then
        assertThat(supported).isTrue();
    }

    @Test
    @DisplayName("Attribute with unsupported engines is supported without engine type")
    void givenUnsupportedEngine_whenIsSupportedWithoutEngine_thenTrueReturned() {
        // given
        MetaProperty metaProperty = getMetaProperty("unsupportedOnOperaton");

        // when
        boolean supported = resolver.isSupported(metaProperty, null);

        // then
        assertThat(supported).isTrue();
    }

    @ParameterizedTest
    @EnumSource(EngineType.class)
    @DisplayName("Attribute is supported for every engine type of a supported family")
    void givenSupportedFamily_whenIsSupportedForEngineOfFamily_thenTrueReturned(EngineType engineType) {
        // given
        MetaProperty metaProperty = getMetaProperty("supportedOnCamunda7Family");

        // when
        boolean supported = resolver.isSupported(metaProperty, engineType);

        // then
        assertThat(supported).isTrue();
    }

    @Test
    @DisplayName("Attribute of the Camunda 7 family is supported without engine type")
    void givenSupportedFamily_whenIsSupportedWithoutEngine_thenTrueReturned() {
        // given
        MetaProperty metaProperty = getMetaProperty("supportedOnCamunda7Family");

        // when
        boolean supported = resolver.isSupported(metaProperty, null);

        // then
        assertThat(supported).isTrue();
    }

    @ParameterizedTest
    @EnumSource(EngineType.class)
    @DisplayName("Attribute is not supported for every engine type of an unsupported family")
    void givenUnsupportedFamily_whenIsSupportedForEngineOfFamily_thenFalseReturned(EngineType engineType) {
        // given
        MetaProperty metaProperty = getMetaProperty("unsupportedOnCamunda7Family");

        // when
        boolean supported = resolver.isSupported(metaProperty, engineType);

        // then
        assertThat(supported).isFalse();
    }

    @Test
    @DisplayName("Attribute unsupported by the Camunda 7 family is not supported without engine type")
    void givenUnsupportedFamily_whenIsSupportedWithoutEngine_thenFalseReturned() {
        // given
        MetaProperty metaProperty = getMetaProperty("unsupportedOnCamunda7Family");

        // when
        boolean supported = resolver.isSupported(metaProperty, null);

        // then
        assertThat(supported).isFalse();
    }

    @Test
    @DisplayName("Supported engines win over unsupported engines")
    void givenSupportedAndUnsupportedEngine_whenIsSupported_thenSupportedEnginesUsed() {
        // given
        MetaProperty metaProperty = getMetaProperty("supportedAndUnsupportedOnOperaton");

        // when
        boolean supported = resolver.isSupported(metaProperty, EngineType.OPERATON);

        // then
        assertThat(supported).isTrue();
    }

    @Test
    @DisplayName("Supported engines win over supported families")
    void givenSupportedEngineAndFamily_whenIsSupported_thenSupportedEnginesUsed() {
        // given
        MetaProperty metaProperty = getMetaProperty("supportedOnOperatonAndCamunda7Family");

        // when
        boolean supported = resolver.isSupported(metaProperty, EngineType.CAMUNDA_7);

        // then
        assertThat(supported).isFalse();
    }

    @Test
    @DisplayName("Unsupported engines win over supported families")
    void givenUnsupportedEngineAndSupportedFamily_whenIsSupported_thenUnsupportedEnginesUsed() {
        // given
        MetaProperty metaProperty = getMetaProperty("unsupportedOnOperatonAndSupportedOnCamunda7Family");

        // when
        boolean supported = resolver.isSupported(metaProperty, EngineType.OPERATON);

        // then
        assertThat(supported).isFalse();
    }

    @Test
    @DisplayName("Supported families win over unsupported families")
    void givenSupportedAndUnsupportedFamily_whenIsSupported_thenSupportedFamiliesUsed() {
        // given
        MetaProperty metaProperty = getMetaProperty("supportedAndUnsupportedOnCamunda7Family");

        // when
        boolean supported = resolver.isSupported(metaProperty, EngineType.CAMUNDA_7);

        // then
        assertThat(supported).isTrue();
    }

    private MetaProperty getMetaProperty(String attributeName) {
        MetaProperty metaProperty = mock(MetaProperty.class);
        try {
            when(metaProperty.getAnnotatedElement()).thenReturn(AnnotatedAttributes.class.getDeclaredField(attributeName));
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException(e);
        }
        return metaProperty;
    }

    @SuppressWarnings("unused")
    private static class AnnotatedAttributes {

        private String notAnnotated;

        @EngineProperty
        private String empty;

        @EngineProperty(supportedEngines = EngineType.OPERATON)
        private String supportedOnOperaton;

        @EngineProperty(unsupportedEngines = EngineType.OPERATON)
        private String unsupportedOnOperaton;

        @EngineProperty(supportedFamilies = EngineFamily.CAMUNDA_7)
        private String supportedOnCamunda7Family;

        @EngineProperty(unsupportedFamilies = EngineFamily.CAMUNDA_7)
        private String unsupportedOnCamunda7Family;

        @EngineProperty(supportedEngines = EngineType.OPERATON, unsupportedEngines = EngineType.OPERATON)
        private String supportedAndUnsupportedOnOperaton;

        @EngineProperty(supportedEngines = EngineType.OPERATON, supportedFamilies = EngineFamily.CAMUNDA_7)
        private String supportedOnOperatonAndCamunda7Family;

        @EngineProperty(unsupportedEngines = EngineType.OPERATON, supportedFamilies = EngineFamily.CAMUNDA_7)
        private String unsupportedOnOperatonAndSupportedOnCamunda7Family;

        @EngineProperty(supportedFamilies = EngineFamily.CAMUNDA_7, unsupportedFamilies = EngineFamily.CAMUNDA_7)
        private String supportedAndUnsupportedOnCamunda7Family;
    }
}
