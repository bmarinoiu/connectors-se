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
 */
package org.talend.components.azure.common;

import org.talend.components.azure.service.AzureComponentServices;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.action.Suggestable;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;

import lombok.Data;

@Data
@GridLayout(value = { @GridLayout.Row({ "schemaColumnName", "entityPropertyName" }) }, names = GridLayout.FormType.ADVANCED)
// TODO replace with optionsorder instead of gridlayout when it would be fixed
// @OptionsOrder({ "schemaColumnName", "entityPropertyName" })
public class NameMapping {

    @Option
    @Documentation("The column name of the component schema between double quotation marks")
    @Suggestable(AzureComponentServices.COLUMN_NAMES)
    private String schemaColumnName;

    @Option
    @Documentation("The property name of the Azure table entity between double quotation marks")
    private String entityPropertyName;
}