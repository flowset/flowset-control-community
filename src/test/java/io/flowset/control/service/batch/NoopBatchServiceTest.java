/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.batch;

import io.flowset.control.entity.batch.BatchData;
import io.flowset.control.entity.batch.BatchStatisticsData;
import io.flowset.control.test_support.AbstractIntegrationTest;
import io.flowset.control.test_support.AuthenticatedAsAdmin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
public class NoopBatchServiceTest extends AbstractIntegrationTest {

    @Autowired
    BatchService batchService;

    @Test
    @DisplayName("Empty list of batch statistics returned when no engine exists")
    void givenNoEngine_whenFindAllBatchStatistics_thenEmptyListReturned() {
        //when
        List<BatchStatisticsData> statistics = batchService.findAllBatchStatistics(new BatchLoadContext());

        //then
        assertThat(statistics).isEmpty();
    }

    @Test
    @DisplayName("Empty list of historic batches returned when no engine exists")
    void givenNoEngine_whenFindAllHistoricBatches_thenEmptyListReturned() {
        //when
        List<BatchData> batches = batchService.findAllHistoricBatches(new BatchLoadContext());

        //then
        assertThat(batches).isEmpty();
    }

    @Test
    @DisplayName("Zero returned as batch statistics count when no engine exists")
    void givenNoEngine_whenGetBatchStatisticsCount_thenZeroReturned() {
        //when
        long count = batchService.getBatchStatisticsCount(null);

        //then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Zero returned as historic batches count when no engine exists")
    void givenNoEngine_whenGetHistoricBatchCount_thenZeroReturned() {
        //when
        long count = batchService.getHistoricBatchCount(null);

        //then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Null returned for batch statistics when no engine exists")
    void givenNoEngine_whenFindBatchStatisticsById_thenNullReturned() {
        //when
        BatchStatisticsData statistics = batchService.findBatchStatisticsById("batchId");

        //then
        assertThat(statistics).isNull();
    }

    @Test
    @DisplayName("Null returned for batch when no engine exists")
    void givenNoEngine_whenGetById_thenNullReturned() {
        //when
        BatchData batch = batchService.getById("batchId");

        //then
        assertThat(batch).isNull();
    }

    @Test
    @DisplayName("Null returned for historic batch when no engine exists")
    void givenNoEngine_whenFindHistoricBatchById_thenNullReturned() {
        //when
        BatchData batch = batchService.findHistoricBatchById("batchId");

        //then
        assertThat(batch).isNull();
    }
}
