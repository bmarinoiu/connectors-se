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

package org.talend.components.azure.runtime.output;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.talend.components.azure.common.exception.BlobRuntimeException;
import org.talend.components.azure.common.service.AzureComponentServices;
import org.talend.components.azure.output.BlobOutputConfiguration;
import org.talend.components.azure.runtime.converters.AvroConverter;
import org.talend.components.azure.service.AzureBlobComponentServices;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.record.Schema;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

public class AvroBlobFileWriter extends BlobFileWriter {

    private BlobOutputConfiguration config;

    private Schema recordSchema;

    private AvroConverter converter;

    public AvroBlobFileWriter(BlobOutputConfiguration config, AzureBlobComponentServices connectionServices) throws Exception {
        super(config, connectionServices);
        this.config = config;
        converter = AvroConverter.of();
    }

    @Override
    public void generateFile() throws URISyntaxException, StorageException {
        String fileName = config.getDataset().getDirectory() + "/" + config.getBlobNameTemplate() + System.currentTimeMillis()
                + ".avro";

        CloudBlockBlob blob = getContainer().getBlockBlobReference(fileName);
        if (blob.exists(null, null, AzureComponentServices.getTalendOperationContext())) {
            generateFile();
            return;
        }

        setCurrentItem(blob);
    }

    @Override
    public void writeRecord(Record record) {
        super.writeRecord(record);

        if (recordSchema == null) {
            recordSchema = record.getSchema();
        }
    }

    @Override
    public void newBatch() {
        super.newBatch();

        try {
            generateFile();
        } catch (Exception e) {
            throw new BlobRuntimeException(e);
        }

    }

    @Override
    public void flush() throws StorageException, IOException {
        if (getBatch().isEmpty()) {
            return;
        }

        byte[] batchBytes = appendFirstBatch();
        getCurrentItem().uploadFromByteArray(batchBytes, 0, batchBytes.length);
    }

    private byte[] appendFirstBatch() throws IOException {

        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>();
        DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(datumWriter);
        dataFileWriter.create(converter.inferAvroSchema(recordSchema), byteBuffer);
        for (Record record : getBatch()) {
            dataFileWriter.append(converter.fromRecord(record));
        }
        dataFileWriter.flush();
        return byteBuffer.toByteArray();
    }

}
