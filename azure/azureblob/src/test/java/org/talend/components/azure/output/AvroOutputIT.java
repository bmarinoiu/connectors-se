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

package org.talend.components.azure.output;

import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.talend.components.azure.BaseIT;
import org.talend.components.azure.common.FileFormat;
import org.talend.components.azure.dataset.AzureBlobDataset;
import org.talend.components.azure.source.BlobInputProperties;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;
import org.talend.sdk.component.junit5.WithComponents;
import org.talend.sdk.component.runtime.manager.chain.Job;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import static org.talend.sdk.component.junit.SimpleFactory.configurationByExample;

@WithComponents("org.talend.components.azure")
class AvroOutputIT extends BaseIT {

    private BlobOutputConfiguration blobOutputProperties;

    private final String testStringValue = "test";

    private final boolean testBooleanValue = true;

    private final long testLongValue = 0L;

    private final int testIntValue = 1;

    private final double testDoubleValue = 2.0;

    private final ZonedDateTime testDateValue = ZonedDateTime.now();

    private final byte[] bytes = new byte[] { 1, 2, 3 };

    @BeforeEach
    void initDataset() {
        AzureBlobDataset dataset = new AzureBlobDataset();
        dataset.setConnection(dataStore);
        dataset.setFileFormat(FileFormat.AVRO);

        dataset.setContainerName(containerName);
        blobOutputProperties = new BlobOutputConfiguration();
        blobOutputProperties.setDataset(dataset);
    }

    @Test
    void testOutput() throws URISyntaxException, StorageException {
        final int recordSize = 5;
        blobOutputProperties.getDataset().setDirectory("avroDir");
        blobOutputProperties.setBlobNameTemplate("testFile");

        Record testRecord = componentsHandler.findService(RecordBuilderFactory.class).newRecordBuilder()
                .withString("stringValue", testStringValue).withBoolean("booleanValue", testBooleanValue)
                .withLong("longValue", testLongValue).withInt("intValue", testIntValue).withDouble("doubleValue", testDoubleValue)
                .withDateTime("dateValue", testDateValue).withBytes("byteArray", bytes).build();

        List<Record> testRecords = new ArrayList<>();
        for (int i = 0; i < recordSize; i++) {
            testRecords.add(testRecord);
        }
        componentsHandler.setInputData(testRecords);

        String outputConfig = configurationByExample().forInstance(blobOutputProperties).configured().toQueryString();
        outputConfig += "&$configuration.$maxBatchSize=" + recordSize;
        Job.components().component("inputFlow", "test://emitter").component("outputComponent", "Azure://Output?" + outputConfig)
                .connections().from("inputFlow").to("outputComponent").build().run();

        CloudBlobContainer container = storageAccount.createCloudBlobClient().getContainerReference(containerName);

        Assert.assertTrue("No files were created in test container",
                container.listBlobs(blobOutputProperties.getDataset().getDirectory() + "/", false).iterator().hasNext());

        BlobInputProperties inputProperties = new BlobInputProperties();
        inputProperties.setDataset(blobOutputProperties.getDataset());

        String inputConfig = configurationByExample().forInstance(inputProperties).configured().toQueryString();
        Job.components().component("azureInput", "Azure://Input?" + inputConfig).component("collector", "test://collector")
                .connections().from("azureInput").to("collector").build().run();
        List<Record> records = componentsHandler.getCollectedData(Record.class);

        Assert.assertEquals(recordSize, records.size());
        Record firstRecord = records.get(0);
        Assert.assertEquals(testRecord.getString("stringValue"), firstRecord.getString("stringValue"));
        Assert.assertEquals(testRecord.getBoolean("booleanValue"), firstRecord.getBoolean("booleanValue"));
        Assert.assertEquals(testRecord.getLong("longValue"), firstRecord.getLong("longValue"));
        Assert.assertEquals(testRecord.getInt("intValue"), firstRecord.getInt("intValue"));
        Assert.assertEquals(testRecord.getDouble("doubleValue"), firstRecord.getDouble("doubleValue"), 0.01);
        Assert.assertEquals(testRecord.getDateTime("dateValue"), firstRecord.getDateTime("dateValue"));
        Assert.assertArrayEquals(testRecord.getBytes("byteArray"), firstRecord.getBytes("byteArray"));
    }

    @Test
    public void testBatchSizeIsGreaterThanRowSize() throws URISyntaxException, StorageException {
        final int recordSize = 5;
        blobOutputProperties.getDataset().setDirectory("avroDir");
        blobOutputProperties.setBlobNameTemplate("testFile");

        Record testRecord = componentsHandler.findService(RecordBuilderFactory.class).newRecordBuilder()
                .withString("stringValue", testStringValue).withBoolean("booleanValue", testBooleanValue)
                .withLong("longValue", testLongValue).withInt("intValue", testIntValue).withDouble("doubleValue", testDoubleValue)
                .withDateTime("dateValue", testDateValue).withBytes("byteArray", bytes).build();

        List<Record> testRecords = new ArrayList<>();
        for (int i = 0; i < recordSize; i++) {
            testRecords.add(testRecord);
        }
        componentsHandler.setInputData(testRecords);

        String outputConfig = configurationByExample().forInstance(blobOutputProperties).configured().toQueryString();
        outputConfig += "&$configuration.$maxBatchSize=" + (recordSize * 100);
        Job.components().component("inputFlow", "test://emitter").component("outputComponent", "Azure://Output?" + outputConfig)
                .connections().from("inputFlow").to("outputComponent").build().run();

        CloudBlobContainer container = storageAccount.createCloudBlobClient().getContainerReference(containerName);

        Assert.assertTrue("No files were created in test container",
                container.listBlobs(blobOutputProperties.getDataset().getDirectory() + "/", false).iterator().hasNext());
    }
}
