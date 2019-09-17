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
package org.talend.components.slack.service;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.talend.components.slack.connection.SlackConnection;
import org.talend.components.slack.dataset.SlackDataset;
import org.talend.components.slack.input.SlackInputConfiguration;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.record.Schema;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.configuration.Configuration;
import org.talend.sdk.component.api.service.http.Response;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;

import javax.json.*;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.talend.components.slack.SlackApiConstants.*;

@Accessors
@Slf4j
@Service
public class SlackService {

    @Getter
    @Service
    protected I18nMessage i18n;

    @Getter
    @Service
    protected RecordBuilderFactory recordBuilder;

    @Getter
    @Service
    private JsonBuilderFactory jsonFactory;

    @Getter
    @Service
    private JsonReaderFactory jsonReader;

    @Getter
    @Service
    private JsonWriterFactory jsonWriter;

    @Getter
    @Service
    protected MessagesClient messagesClient;

    public void initClients(SlackConnection connection) {
        messagesClient.base(connection.getEndpoint());
    }

    /**
     * Retrieve and set an access token for using API
     */
    public String retrieveAccessToken(@Configuration("configuration") final SlackDataset dataset) {
        initClients(dataset.getConnection());
        String token = " Bearer " + dataset.getConnection().getToken();
        log.debug("[retrieveAccessToken] [{}] :.", token);
        return token;
    }


    private Schema getMessagesSchema() {
        return recordBuilder.newSchemaBuilder(Schema.Type.RECORD)
                .withEntry(recordBuilder.newEntryBuilder().withName(ATTR_TEXT).withType(Schema.Type.STRING).build())
                .withEntry(recordBuilder.newEntryBuilder().withName(ATTR_TIMESTAMP).withType(Schema.Type.STRING).build())
                .withEntry(recordBuilder.newEntryBuilder().withName(ATTR_USERNAME).withType(Schema.Type.STRING).build()).build();
    }

    protected JsonArray parseResultFromResponse(Response<JsonObject> response) {
        if (response.status() == 200 && response.body() != null && response.body().getJsonArray(ATTR_RESULT) != null) {
            return response.body().getJsonArray(ATTR_RESULT);
        }
        log.error("[parseResultFromResponse] Error: [{}] headers:{}; body: {}.", response.status(), response.headers(),
                response.body());
        throw new IllegalArgumentException(i18n.invalidOperation());
    }

    public Schema getEntitySchema(final SlackInputConfiguration configuration) {
        Schema s = null;
        Schema.Builder b = recordBuilder.newSchemaBuilder(Schema.Type.RECORD);

        Schema messagesSchema = getMessagesSchema();

        return messagesSchema;
    }

    public JsonObject toJson(final Record record) {
        String recordStr = record.toString().replaceAll("AvroRecord\\{delegate=(.*)\\}$", "$1");
        JsonReader reader = jsonReader.createReader(new StringReader(recordStr));
        Throwable throwable = null;
        JsonObject json;
        try {
            json = reader.readObject();
        } catch (Throwable throwable1) {
            throwable = throwable1;
            throw throwable1;
        } finally {
            if (reader != null) {
                if (throwable != null) {
                    try {
                        reader.close();
                    } catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                } else {
                    reader.close();
                }
            }
        }
        return json;
    }

    private boolean hasJsonValue(JsonValue value) {
        if (value == null) {
            return false;
        }
        if (value.getValueType().equals(JsonValue.ValueType.NULL)) {
            return false;
        }
        return true;
    }

    public Record convertToRecord(final JsonObject json, final Map<String, Schema.Entry> schema) {
        Record.Builder b = getRecordBuilder().newRecordBuilder();
        log.debug("[convertToRecord] json {} VS schema {}", json.entrySet().size(), schema.keySet().size());
        for (Schema.Entry entry : schema.values()) {
            String key = entry.getName();
            JsonValue val = json.get(key);
            switch (entry.getType()) {
            case ARRAY:
                String ary = "";
                if (val != null) {
                    json.getJsonArray(key).stream().map(JsonValue::toString).collect(joining(","));
                    // not in a sub array
                    if (!ary.contains("{")) {
                        ary = ary.replaceAll("\"", "").replaceAll("(\\[|\\])", "");
                    }
                }
                b.withString(key, ary);
                break;
            case RECORD:
            case BYTES:
            case STRING:
                if (hasJsonValue(val)) {
                    switch (val.getValueType()) {
                    case ARRAY:
                        b.withString(key, json.getJsonArray(key).stream().map(JsonValue::toString).collect(joining(",")));
                        break;
                    case OBJECT:
                        b.withString(key, String.valueOf(json.getJsonObject(key).toString()));
                        break;
                    case STRING:
                        b.withString(key, json.getString(key));
                        break;
                    case NUMBER:
                        b.withString(key, String.valueOf(json.getJsonNumber(key)));
                        break;
                    case TRUE:
                    case FALSE:
                        b.withString(key, String.valueOf(json.getBoolean(key)));
                        break;
                    case NULL:
                        b.withString(key, null);
                        break;
                    }
                } else {
                    b.withString(key, null);
                }
                break;
            case INT:
                b.withInt(key, hasJsonValue(val) ? json.getInt(key) : 0);
                break;
            case LONG:
                b.withLong(key, hasJsonValue(val) ? json.getJsonNumber(key).longValue() : 0);
                break;
            case FLOAT:
            case DOUBLE:
                b.withDouble(key, hasJsonValue(val) ? json.getJsonNumber(key).doubleValue() : 0);
                break;
            case BOOLEAN:
                b.withBoolean(key, hasJsonValue(val) ? json.getBoolean(key) : null);
                break;
            case DATETIME:
                try {
                    b.withDateTime(key,
                            hasJsonValue(val) ? new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(json.getString(key))
                                    : null);
                } catch (ParseException e1) {
                    log.error("[convertToRecord] Date parsing error: {}.", e1.getMessage());
                }
                break;
            }
        }
        Record record = b.build();
        log.debug("[convertToRecord] returning : {}.", record);
        return record;
    }

}
