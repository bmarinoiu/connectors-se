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
package org.talend.components.netsuite.service;

import org.talend.components.netsuite.dataset.NetSuiteDataSet;
import org.talend.components.netsuite.datastore.NetSuiteDataStore;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.record.Schema;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.completion.SuggestionValues;
import org.talend.sdk.component.api.service.completion.Suggestions;
import org.talend.sdk.component.api.service.healthcheck.HealthCheck;
import org.talend.sdk.component.api.service.healthcheck.HealthCheckStatus;
import org.talend.sdk.component.api.service.healthcheck.HealthCheckStatus.Status;
import org.talend.sdk.component.api.service.schema.DiscoverSchema;

@Service
public class UIActionService {

    public static final String HEALTH_CHECK = "connection.healthcheck";

    public static final String GUESS_SCHEMA = "guessSchema";

    public static final String LOAD_RECORD_TYPES = "loadRecordTypes";

    public static final String LOAD_FIELDS = "loadFields";

    public static final String LOAD_OPERATORS = "loadOperators";

    @Service
    private NetSuiteService service;

    @Service
    private Messages i18n;

    @HealthCheck(HEALTH_CHECK)
    public HealthCheckStatus validateConnection(@Option final NetSuiteDataStore dataStore) {
        try {
            this.service.connect(dataStore);
            return new HealthCheckStatus(Status.OK, i18n.healthCheckOk());
        } catch (Exception e) {
            return new HealthCheckStatus(Status.KO, i18n.healthCheckFailed(e.getMessage()));
        }
    }

    @DiscoverSchema(GUESS_SCHEMA)
    public Schema guessSchema(@Option final NetSuiteDataSet dataSet) {
        return service.getSchema(dataSet, null);
    }

    @Suggestions(LOAD_RECORD_TYPES)
    public SuggestionValues loadRecordTypes(@Option final NetSuiteDataStore dataStore) {
        return new SuggestionValues(true, service.getRecordTypes(dataStore));
    }

    @Suggestions(LOAD_FIELDS)
    public SuggestionValues loadFields(@Option final NetSuiteDataSet dataSet) {
        return new SuggestionValues(true, service.getSearchTypes(dataSet));
    }

    @Suggestions(LOAD_OPERATORS)
    public SuggestionValues loadOperators(@Option("dataStore") final NetSuiteDataSet dataSet, String field) {
        return new SuggestionValues(false, service.getSearchFieldOperators(dataSet, field));
    }
}
