/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.talend.components.couchbase.output;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.talend.components.couchbase.CouchbaseUtilTest;
import org.talend.components.couchbase.TestData;
import org.talend.components.couchbase.dataset.CouchbaseDataSet;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.record.Schema;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;
import org.talend.sdk.component.junit.BaseComponentsHandler;
import org.talend.sdk.component.junit5.Injected;
import org.talend.sdk.component.junit5.WithComponents;
import org.talend.sdk.component.runtime.manager.chain.Job;
import org.talend.sdk.component.runtime.record.SchemaImpl;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.talend.sdk.component.junit.SimpleFactory.configurationByExample;

@Slf4j
@WithComponents("org.talend.components.couchbase")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Testing of CouchbaseOutput component")
public class CouchbaseOutputTest extends CouchbaseUtilTest {

    @Injected
    private BaseComponentsHandler componentsHandler;

    @Service
    private RecordBuilderFactory recordBuilderFactory;

    private final String SIMPLE_OUTPUT_TEST_ID = "simpleOutputTest_";

    private List<JsonDocument> retrieveDataFromDatabase() {
        Bucket bucket = couchbaseCluster.openBucket(BUCKET_NAME, BUCKET_PASSWORD);
        JsonDocument doc1 = bucket.get(SIMPLE_OUTPUT_TEST_ID + "1");
        JsonDocument doc2 = bucket.get(SIMPLE_OUTPUT_TEST_ID + "2");
        List<JsonDocument> resultList = new ArrayList<>();
        resultList.add(doc1);
        resultList.add(doc2);
        bucket.close();
        return resultList;
    }

    private void executeJob() {
        final String outputConfig = configurationByExample().forInstance(getOutputConfiguration()).configured().toQueryString();
        Job.components().component("Couchbase_Output", "Couchbase://Output?" + outputConfig)
                .component("emitter", "test://emitter").connections().from("emitter").to("Couchbase_Output").build().run();
    }

    @Test
    @DisplayName("Check fields from retrieved data")
    void simpleOutputTest() {
        log.info("test simpleOutputTest started");
        List<Record> records = createRecords();
        componentsHandler.setInputData(records);
        executeJob();

        List<JsonDocument> resultList = retrieveDataFromDatabase();
        TestData testData = new TestData();

        assertEquals(new Integer(testData.getColIntMin()), resultList.get(0).content().getInt("t_int_min"));
        assertEquals(new Integer(testData.getColIntMax()), resultList.get(0).content().getInt("t_int_max"));
        assertEquals(new Long(testData.getColLongMin()), resultList.get(0).content().getLong("t_long_min"));
        assertEquals(new Long(testData.getColLongMax()), resultList.get(0).content().getLong("t_long_max"));
        assertEquals(testData.getColFloatMin(), resultList.get(0).content().getNumber("t_float_min").floatValue());
        assertEquals(testData.getColFloatMax(), resultList.get(0).content().getNumber("t_float_max").floatValue());
        assertEquals(testData.getColDoubleMin(), resultList.get(0).content().getDouble("t_double_min"));
        assertEquals(testData.getColDoubleMax(), resultList.get(0).content().getDouble("t_double_max"));
        assertEquals(testData.isColBoolean(), resultList.get(0).content().getBoolean("t_boolean"));
        assertEquals(testData.getColDateTime().toString(), resultList.get(0).content().getString("t_datetime"));
        Assertions.assertArrayEquals(testData.getColList().toArray(),
                resultList.get(0).content().getArray("t_array").toList().toArray());

        assertEquals(2, resultList.size());
        log.info("test simpleOutputTest finished");
    }

    private List<Record> createRecords() {
        List<Record> records = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            records.add(createRecord(SIMPLE_OUTPUT_TEST_ID + i));
        }
        return records;
    }

    private Record createRecord(String id) {
        TestData testData = new TestData();

        final Schema.Entry.Builder entryBuilder = recordBuilderFactory.newEntryBuilder();
        SchemaImpl arrayInnerSchema = new SchemaImpl();
        arrayInnerSchema.setType(Schema.Type.STRING);

        Record record = recordBuilderFactory.newRecordBuilder()
                .withString(entryBuilder.withName("t_string").withType(Schema.Type.STRING).build(), id)
                .withInt(entryBuilder.withName("t_int_min").withType(Schema.Type.INT).build(), testData.getColIntMin())
                .withInt(entryBuilder.withName("t_int_max").withType(Schema.Type.INT).build(), testData.getColIntMax())
                .withLong(entryBuilder.withName("t_long_min").withType(Schema.Type.LONG).build(), testData.getColLongMin())
                .withLong(entryBuilder.withName("t_long_max").withType(Schema.Type.LONG).build(), testData.getColLongMax())
                .withFloat(entryBuilder.withName("t_float_min").withType(Schema.Type.FLOAT).build(), testData.getColFloatMin())
                .withFloat(entryBuilder.withName("t_float_max").withType(Schema.Type.FLOAT).build(), testData.getColFloatMax())
                .withDouble(entryBuilder.withName("t_double_min").withType(Schema.Type.DOUBLE).build(),
                        testData.getColDoubleMin())
                .withDouble(entryBuilder.withName("t_double_max").withType(Schema.Type.DOUBLE).build(),
                        testData.getColDoubleMax())
                .withBoolean(entryBuilder.withName("t_boolean").withType(Schema.Type.BOOLEAN).build(), testData.isColBoolean())
                .withDateTime(entryBuilder.withName("t_datetime").withType(Schema.Type.DATETIME).build(),
                        testData.getColDateTime())
                .withArray(
                        entryBuilder.withName("t_array").withType(Schema.Type.ARRAY).withElementSchema(arrayInnerSchema).build(),
                        testData.getColList())
                .build();
        return record;
    }

    private CouchbaseOutputConfiguration getOutputConfiguration() {
        CouchbaseDataSet couchbaseDataSet = new CouchbaseDataSet();
        couchbaseDataSet.setBucket(BUCKET_NAME);
        couchbaseDataSet.setDatastore(couchbaseDataStore);

        CouchbaseOutputConfiguration configuration = new CouchbaseOutputConfiguration();
        configuration.setIdFieldName("t_string");
        configuration.setDataSet(couchbaseDataSet);
        return configuration;
    }
}