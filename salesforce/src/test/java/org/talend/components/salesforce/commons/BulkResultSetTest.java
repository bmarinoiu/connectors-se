/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 */

package org.talend.components.salesforce.commons;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

import org.junit.Assert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

/**
 *
 */
class BulkResultSetTest {

    @Test
    @DisplayName("Test resultset")
    void testResultSet() throws IOException {

        final int recordCount = 100;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CsvWriter csvWriter = new CsvWriter(new BufferedOutputStream(out), ',', StandardCharsets.UTF_8);

        for (int i = 0; i < recordCount; i++) {
            csvWriter.writeRecord(new String[] { "fieldValueA" + i, "fieldValueB" + i, "fieldValueC" + i });
        }
        csvWriter.close();

        CsvReader csvReader = new CsvReader(new BufferedInputStream(new ByteArrayInputStream(out.toByteArray())), ',',
                StandardCharsets.UTF_8);

        BulkResultSet resultSet = new BulkResultSet(csvReader, Arrays.asList("fieldA", "fieldB", "fieldC"));

        int count = 0;
        Map<String, String> result;
        while ((result = resultSet.next()) != null) {
            assertEquals("fieldValueA" + count, result.get("fieldA"));
            assertEquals("fieldValueB" + count, result.get("fieldB"));
            assertEquals("fieldValueC" + count, result.get("fieldC"));

            count++;
        }

        assertEquals(recordCount, count);
    }

    @Test
    @DisplayName("Test resultset")
    void testSafetySwitchTrueFailure() {
        try {
            prepareSafetySwitchTest(true, 100_001);
        } catch (Exception ioe) {
            Assert.assertTrue(ioe.getCause().getMessage().startsWith("Maximum column length of 100,000 exceeded"));
        }
    }

    @Test
    @DisplayName("Test safetySwitch false success")
    void testSafetySwitchFalseSuccess() throws IOException {
        final int columnLength = 200_000;
        Assert.assertEquals(columnLength, prepareSafetySwitchTest(false, columnLength));
    }

    private int prepareSafetySwitchTest(boolean safetySwitchParameter, int columnLength) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CsvWriter csvWriter = new CsvWriter(new BufferedOutputStream(out), ',', StandardCharsets.UTF_8);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columnLength; i++) {
            sb.append("a");
        }
        String[] data = new String[] { "fieldValueA", "fieldValueB", sb.toString() };
        csvWriter.writeRecord(data);
        csvWriter.close();

        CsvReader csvReader = new CsvReader(new BufferedInputStream(new ByteArrayInputStream(out.toByteArray())), ',',
                StandardCharsets.UTF_8);
        csvReader.setSafetySwitch(safetySwitchParameter);
        BulkResultSet resultSet = new BulkResultSet(csvReader, Arrays.asList("fieldA", "fieldB", "fieldC"));
        Map<String, String> result = resultSet.next();
        return result.get("fieldC").length();
    }

    @Test
    @DisplayName("Test resultset IOException")
    void testResultSetIOError() throws IOException {
        assertThrows(IllegalStateException.class, () -> {
            InputStream in = new InputStream() {

                @Override
                public int read() throws IOException {
                    throw new IOException("I/O ERROR");
                }
            };

            CsvReader csvReader = new CsvReader(in, ',', StandardCharsets.UTF_8);

            BulkResultSet resultSet = new BulkResultSet(csvReader, Arrays.asList("fieldA", "fieldB", "fieldC"));

            while (resultSet.next() != null) {
            }
        });
    }
}
