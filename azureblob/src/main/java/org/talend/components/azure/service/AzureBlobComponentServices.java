package org.talend.components.azure.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.talend.components.azure.datastore.AzureConnection;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.completion.SuggestionValues;
import org.talend.sdk.component.api.service.completion.Suggestions;
import org.talend.sdk.component.api.service.healthcheck.HealthCheck;
import org.talend.sdk.component.api.service.healthcheck.HealthCheckStatus;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;

@Service
public class AzureBlobComponentServices {

    /**
     * The name of HealthCheck service
     */
    public static final String TEST_CONNECTION = "testConnection";

    public static final String GET_CONTAINER_NAMES = "getContainers";

    @Service
    AzureBlobConnectionServices connectionService;

    @Service
    private MessageService i18nService;

    @HealthCheck(TEST_CONNECTION)
    public HealthCheckStatus testConnection(@Option AzureConnection azureConnection) {
        final int maxContainers = 1;
        try {
            CloudStorageAccount cloudStorageAccount = connectionService.createStorageAccount(azureConnection);
            CloudBlobClient blobClient = connectionService.createCloudBlobClient(cloudStorageAccount,
                    AzureBlobConnectionServices.DEFAULT_RETRY_POLICY);
            // will throw an exception if not authorized or account not exist
            blobClient.listContainersSegmented(null, null, maxContainers, null, null,
                    AzureBlobConnectionServices.getTalendOperationContext());
        } catch (Exception e) {
            String errorMessage = (StringUtils.isNotEmpty(e.getMessage()) || (e.getCause() == null)) ? e.getMessage()
                    : e.getCause().toString();
            return new HealthCheckStatus(HealthCheckStatus.Status.KO, i18nService.connectionError() + ": " + errorMessage);
        }
        return new HealthCheckStatus(HealthCheckStatus.Status.OK, i18nService.connected());
    }

    @Suggestions(GET_CONTAINER_NAMES)
    public SuggestionValues getContainerNames(@Option AzureConnection azureConnection) {
        List<SuggestionValues.Item> containerNames = new ArrayList<>();
        try {
            CloudStorageAccount storageAccount = connectionService.createStorageAccount(azureConnection);
            final OperationContext operationContext = AzureBlobConnectionServices.getTalendOperationContext();
            for (CloudBlobContainer container : connectionService
                    .createCloudBlobClient(storageAccount, AzureBlobConnectionServices.DEFAULT_RETRY_POLICY)
                    .listContainers(null, null, null, operationContext)) {
                containerNames.add(new SuggestionValues.Item(container.getName(), container.getName()));
            }

        } catch (Exception e) {
            throw new RuntimeException(i18nService.errorRetrieveContainers(), e);
        }

        return new SuggestionValues(true, containerNames);
    }
}