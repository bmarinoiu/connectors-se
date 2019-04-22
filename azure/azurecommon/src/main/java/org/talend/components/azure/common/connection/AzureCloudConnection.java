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
package org.talend.components.azure.common.connection;

import java.io.Serializable;

import org.talend.components.azure.common.service.AzureComponentServices;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.action.Checkable;
import org.talend.sdk.component.api.configuration.condition.ActiveIf;
import org.talend.sdk.component.api.configuration.type.DataStore;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;

import lombok.Data;

@GridLayout({ @GridLayout.Row("accountConnection"), @GridLayout.Row("useAzureSharedSignature"),
        @GridLayout.Row("signatureConnection") })
@Data
@DataStore
@Checkable(AzureComponentServices.TEST_CONNECTION)
public class AzureCloudConnection implements Serializable {

    @Option
    @Documentation("Name of the storage account you need to access. "
            + "A storage account name can be found in the Storage accounts dashboard of the Microsoft Azure Storage system to be used. "
            + "Ensure that the administrator of the system has granted you the appropriate access permissions to this storage account.")
    @ActiveIf(target = "useAzureSharedSignature", value = "false")
    private AzureStorageConnectionAccount accountConnection;

    @Option
    @Documentation("Use a shared access signature (SAS) to access the storage resources without need for the account key. "
            + "For more information, see Using Shared Access Signatures (SAS): "
            + "https://docs.microsoft.com/en-us/azure/storage/storage-dotnet-shared-access-signature-part-1")
    private boolean useAzureSharedSignature;

    @Option
    @Documentation("Enter your account SAS URL between double quotation marks.\n"
            + "You can get the SAS URL for each allowed service on Microsoft Azure portal after generating SAS.\n"
            + "The SAS URL format is https://<$storagename>.<$service>.core.windows.net/<$sastoken>, "
            + "where <$storagename> is the storage account name, "
            + "<$service> is the allowed service name (blob, file, queue or table)," + "and <$sastoken> is the SAS token value."
            + "For more information, see Azure documentation.\n" + "Note that the SAS has valid period, "
            + "you can set the start time at which the SAS becomes valid and the expiry time after which the SAS is no longer valid when generating it, "
            + "and you need to make sure your SAS is still valid when running your Job.")
    @ActiveIf(target = "useAzureSharedSignature", value = "true")
    private AzureStorageConnectionSignature signatureConnection;
}