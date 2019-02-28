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
package org.talend.components.jdbc.configuration;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.talend.components.jdbc.dataset.TableNameDataset;
import org.talend.components.jdbc.service.I18nMessage;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.action.Suggestable;
import org.talend.sdk.component.api.configuration.condition.ActiveIf;
import org.talend.sdk.component.api.configuration.condition.ActiveIfs;
import org.talend.sdk.component.api.configuration.constraint.Required;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.talend.components.jdbc.service.UIActionService.ACTION_SUGGESTION_ACTION_ON_DATA;
import static org.talend.components.jdbc.service.UIActionService.ACTION_SUGGESTION_TABLE_COLUMNS_NAMES;
import static org.talend.sdk.component.api.configuration.condition.ActiveIf.EvaluationStrategy.CONTAINS;
import static org.talend.sdk.component.api.configuration.condition.ActiveIfs.Operator.AND;
import static org.talend.sdk.component.api.configuration.condition.ActiveIfs.Operator.OR;

@Data
@GridLayout(value = { @GridLayout.Row("dataset"), @GridLayout.Row({ "actionOnData" }), @GridLayout.Row("createTableIfNotExists"),
        @GridLayout.Row("varcharLength"), @GridLayout.Row("keys"), @GridLayout.Row("sortKeys"),
        @GridLayout.Row("distributionStrategy"), @GridLayout.Row("distributionKeys"), @GridLayout.Row("ignoreUpdate") })
@GridLayout(names = GridLayout.FormType.ADVANCED, value = { @GridLayout.Row("dataset"),
        @GridLayout.Row("rewriteBatchedStatements") })
@Documentation("Those properties define an output data set for the JDBC output component")
public class OutputConfig implements Serializable {

    @Option
    @Required
    @Documentation("Dataset configuration")
    private TableNameDataset dataset;

    @Option
    @Required
    @Suggestable(value = ACTION_SUGGESTION_ACTION_ON_DATA, parameters = { "../dataset" })
    @Documentation("The action on data to be performed")
    private String actionOnData;

    @Option
    @Required
    @ActiveIf(target = "actionOnData", value = { "INSERT", "UPSERT", "BULK_LOAD" })
    @Documentation("Create table if don't exists")
    private boolean createTableIfNotExists = false;

    @Option
    @Required
    @ActiveIf(target = "../createTableIfNotExists", value = { "true" })
    @Documentation("The length of varchar types. This value will be used to create varchar columns in this table."
            + "\n-1 means that the max supported length of the targeted database will be used.")
    private int varcharLength = -1;

    @Option
    @ActiveIf(target = "../createTableIfNotExists", value = { "true" })
    @Suggestable(value = ACTION_SUGGESTION_TABLE_COLUMNS_NAMES, parameters = { "dataset" })
    @Documentation("List of columns to be used as keys for this operation")
    private List<String> keys = new ArrayList<>();

    @Option
    @ActiveIfs(operator = AND, value = { @ActiveIf(target = "../dataset.connection.dbType", value = { "Redshift" }),
            @ActiveIf(target = "../createTableIfNotExists", value = { "true" }) })
    @Suggestable(value = ACTION_SUGGESTION_TABLE_COLUMNS_NAMES, parameters = { "../dataset" })
    @Documentation("List of columns to be used as sort keys for redshift")
    private List<String> sortKeys = new ArrayList<>();

    @Option
    @ActiveIfs(operator = AND, value = { @ActiveIf(target = "../dataset.connection.dbType", value = { "Redshift" }),
            @ActiveIf(target = "../createTableIfNotExists", value = { "true" }) })
    @Documentation("Define the distribution strategy of Redshift table")
    private DistributionStrategy distributionStrategy = DistributionStrategy.KEYS;

    @Option
    @ActiveIfs(operator = AND, value = { @ActiveIf(target = "../dataset.connection.dbType", value = { "Redshift" }),
            @ActiveIf(target = "../distributionStrategy", value = { "KEYS" }),
            @ActiveIf(target = "../createTableIfNotExists", value = { "true" }) })
    @Suggestable(value = ACTION_SUGGESTION_TABLE_COLUMNS_NAMES, parameters = { "../dataset" })
    @Documentation("List of columns to be used as distribution keys for redshift")
    private List<String> distributionKeys = new ArrayList<>();

    @Option
    @Suggestable(value = ACTION_SUGGESTION_TABLE_COLUMNS_NAMES, parameters = { "../dataset" })
    @ActiveIf(target = "../actionOnData", value = { "UPDATE", "UPSERT" })
    @Documentation("List of columns to be ignored from update")
    private List<String> ignoreUpdate = new ArrayList<>();

    @Option
    @ActiveIfs(operator = OR, value = { @ActiveIf(target = "../dataset.connection.dbType", value = { "MySQL" }),
            @ActiveIf(target = "../dataset.connection.handler", evaluationStrategy = CONTAINS, value = { "MySQL" }) })
    @Documentation("Rewrite batched statements, to execute one statement per batch combining values in the sql query")
    private boolean rewriteBatchedStatements = true;

    public ActionOnData getActionOnData() {
        if (actionOnData == null || actionOnData.isEmpty()) {
            throw new IllegalArgumentException("label on data is required");
        }

        return ActionOnData.valueOf(actionOnData);
    }

    @RequiredArgsConstructor
    public enum ActionOnData {
        BULK_LOAD(I18nMessage::actionOnDataBulkLoad),
        INSERT(I18nMessage::actionOnDataInsert),
        UPDATE(I18nMessage::actionOnDataUpdate),
        DELETE(I18nMessage::actionOnDataDelete),
        UPSERT(I18nMessage::actionOnDataUpsert);

        private final Function<I18nMessage, String> labelExtractor;

        public String label(final I18nMessage messages) {
            return labelExtractor.apply(messages);
        }
    }
}
