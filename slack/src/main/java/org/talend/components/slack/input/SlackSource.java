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
package org.talend.components.slack.input;

import lombok.extern.slf4j.Slf4j;
import org.talend.components.slack.SlackApiConstants;
import org.talend.components.slack.dataset.SlackDataset;
import org.talend.components.slack.service.MessagesClient;
import org.talend.components.slack.service.SlackService;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.input.Producer;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.record.Schema;
import org.talend.sdk.component.api.service.http.Response;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.json.*;

import org.talend.components.slack.service.I18nMessage;
import org.talend.components.slack.SlackRuntimeException;

import static org.talend.components.slack.SlackApiConstants.ATTR_CODE;
import static org.talend.components.slack.SlackApiConstants.ATTR_MESSAGE;

@Slf4j
public class SlackSource implements Serializable {

    protected final SlackService slackService;

    protected final I18nMessage i18n;

    protected final JsonBuilderFactory jsonFactory;

    protected final JsonReaderFactory jsonReader;

    protected final JsonWriterFactory jsonWriter;

    protected transient String nextPageToken;

    protected transient String accessToken;

    private SlackDataset dataset;

    protected final SlackInputConfiguration configuration;

    protected Map<String, Schema.Entry> schema;

    protected Iterator<JsonValue> resultIterator;

    private final MessagesClient messagesClient;

    public SlackSource(@Option("configuration") final SlackInputConfiguration configuration, final SlackService service) {
        this.configuration = configuration;
        this.dataset = configuration.getDataset();
        this.i18n = service.getI18n();
        this.jsonFactory = service.getJsonFactory();
        this.jsonReader = service.getJsonReader();
        this.jsonWriter = service.getJsonWriter();
        this.slackService = service;

        this.messagesClient = service.getMessagesClient();
    }

    @PostConstruct
    public void init() {
        accessToken = slackService.retrieveAccessToken(dataset);
        schema = buildSchemaMap(slackService.getEntitySchema(configuration));
        processBatch();
    }

    @Producer
    public Record next() {
        JsonValue next = null;
        log.info("Slack Source next");
        if (resultIterator == null) {
            return null;
        }
        boolean hasNext = resultIterator.hasNext();
        if (hasNext) {
            next = resultIterator.next();
        }
        return next == null ? null : slackService.convertToRecord(next.asJsonObject(), schema);
    }

    private Map<String, Schema.Entry> buildSchemaMap(final Schema entitySchema) {
        log.debug("[buildSchemaMap] {}", entitySchema);
        Map<String, Schema.Entry> s = new HashMap<>();
        if (entitySchema != null) {
            for (Schema.Entry entry : entitySchema.getEntries()) {
                s.put(entry.getName(), entry);
            }
        }
        return s;
    }

    /**
     * Convert Slack Errors array to a single String (generally for Exception throwing).
     *
     * @param errors
     * @return flattened string
     */
    public String getErrors(JsonArray errors) {
        StringBuffer error = new StringBuffer();
        for (JsonObject json : errors.getValuesAs(JsonObject.class)) {
            error.append(String.format("[%s] %s", json.getString(ATTR_CODE), json.getString(ATTR_MESSAGE)));
        }

        return error.toString();
    }

    /**
     * Handle a typical Marketo response's payload to API call.
     *
     * @param response the http response
     * @return Marketo API result
     */
    public JsonObject handleResponse(final Response<JsonObject> response) {
        log.debug("[handleResponse] [{}] body: {}.", response.status(), response.body());
        if (response.status() == SlackApiConstants.HTTP_STATUS_OK) {
            return response.body();
        }
        throw new SlackRuntimeException(response.error(String.class));
    }

    public void processBatch() {
        JsonObject result = getMessages();
        log.info("Slack process batch");
        JsonArray requestResult = result.getJsonArray("messages");
        if (requestResult != null) {
            resultIterator = requestResult.iterator();
        }
    }

    /**
     * Returns a list of activities from after a datetime given by the nextPageToken parameter. Also allows for
     * filtering by lead
     * static list membership, or by a list of up to 30 lead ids.
     *
     * @return
     */
    private JsonObject getMessages() {
        return handleResponse(messagesClient.getMessages(SlackApiConstants.HEADER_CONTENT_TYPE_APPLICATION_X_WWW_FORM_URLENCODED,
                this.slackService.retrieveAccessToken(this.dataset), this.dataset.getChannel(), ""));
    }

}
