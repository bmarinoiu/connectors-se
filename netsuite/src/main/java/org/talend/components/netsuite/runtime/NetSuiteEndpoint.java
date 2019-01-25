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
package org.talend.components.netsuite.runtime;

import org.apache.commons.lang3.StringUtils;
import org.talend.components.netsuite.datastore.NetSuiteDataStore;
import org.talend.components.netsuite.datastore.NetSuiteDataStore.LoginType;
import org.talend.components.netsuite.runtime.client.MetaDataSource;
import org.talend.components.netsuite.runtime.client.NetSuiteClientFactory;
import org.talend.components.netsuite.runtime.client.NetSuiteClientService;
import org.talend.components.netsuite.runtime.client.NetSuiteCredentials;
import org.talend.components.netsuite.runtime.client.NetSuiteException;
import org.talend.components.netsuite.runtime.client.NetSuiteVersion;
import org.talend.components.netsuite.runtime.client.NsTokenPassport;
import org.talend.components.netsuite.service.Messages;
import org.talend.sdk.component.api.service.Service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Represents NetSuite Web Service endpoint.
 */

public class NetSuiteEndpoint {

    public static final String CONNECTION = "NetSuite_Connection";

    /** Creates instance of NetSuite client. */
    private NetSuiteClientFactory<?> clientFactory;

    /** Connection configuration for this endpoint. */
    private ConnectionConfig connectionConfig;

    /** NetSuite client. */
    private NetSuiteClientService<?> clientService;

    @Service
    private static Messages i18n;

    /**
     * Creates new instance using given client factory and connection configuration.
     *
     * @param clientFactory client factory
     * @param connectionConfig connection configuration
     */
    public NetSuiteEndpoint(NetSuiteClientFactory<?> clientFactory, ConnectionConfig connectionConfig) {
        this.clientFactory = clientFactory;
        this.connectionConfig = connectionConfig;
    }

    /**
     * Create connection configuration for given connection properties.
     *
     * @param properties connection properties
     * @return connection configuration
     * @throws NetSuiteException if connection configuration not valid
     */
    public static ConnectionConfig createConnectionConfig(NetSuiteDataStore properties) throws NetSuiteException {
        if (StringUtils.isEmpty(properties.getEndpoint())) {
            throw new NetSuiteException(new NetSuiteErrorCode(NetSuiteErrorCode.CLIENT_ERROR), i18n.endpointUrlRequired());
        }
        if (StringUtils.isEmpty(properties.getAccount())) {
            throw new NetSuiteException(new NetSuiteErrorCode(NetSuiteErrorCode.CLIENT_ERROR), i18n.accountRequired());
        }

        NetSuiteCredentials credentials = null;
        NsTokenPassport tokenPassport = null;
        if (properties.getLoginType() == LoginType.BASIC) {

            if (StringUtils.isEmpty(properties.getEmail())) {
                throw new NetSuiteException(new NetSuiteErrorCode(NetSuiteErrorCode.CLIENT_ERROR), i18n.emailRequired());
            }
            if (StringUtils.isEmpty(properties.getPassword())) {
                throw new NetSuiteException(new NetSuiteErrorCode(NetSuiteErrorCode.CLIENT_ERROR), i18n.passwordRequired());
            }

            if (properties.getRole() == 0) {
                throw new NetSuiteException(new NetSuiteErrorCode(NetSuiteErrorCode.CLIENT_ERROR), i18n.roleRequired());
            }

            credentials = new NetSuiteCredentials(properties.getEmail(), properties.getPassword(), properties.getAccount(),
                    String.valueOf(properties.getRole()), properties.getApplicationId());
        } else {
            if (StringUtils.isEmpty(properties.getConsumerKey())) {
                throw new NetSuiteException(new NetSuiteErrorCode(NetSuiteErrorCode.CLIENT_ERROR), i18n.consumerKeyRequired());
            }
            if (StringUtils.isEmpty(properties.getConsumerSecret())) {
                throw new NetSuiteException(new NetSuiteErrorCode(NetSuiteErrorCode.CLIENT_ERROR), i18n.consumerSecretRequired());
            }
            if (StringUtils.isEmpty(properties.getTokenId())) {
                throw new NetSuiteException(new NetSuiteErrorCode(NetSuiteErrorCode.CLIENT_ERROR), i18n.tokenIdRequired());
            }
            if (StringUtils.isEmpty(properties.getTokenSecret())) {
                throw new NetSuiteException(new NetSuiteErrorCode(NetSuiteErrorCode.CLIENT_ERROR), i18n.tokenSecretRequired());
            }
            tokenPassport = new NsTokenPassport(properties.getAccount(), properties.getConsumerKey(),
                    properties.getConsumerSecret(), properties.getTokenId(), properties.getTokenSecret());
        }

        NetSuiteVersion endpointApiVersion = NetSuiteVersion.detectVersion(properties.getEndpoint());
        NetSuiteVersion apiVersion = NetSuiteVersion.parseVersion(properties.getApiVersion());

        if (!endpointApiVersion.isSameMajor(apiVersion)) {
            throw new NetSuiteException(new NetSuiteErrorCode(NetSuiteErrorCode.CLIENT_ERROR),
                    i18n.endpointUrlApiVersionMismatch(properties.getEndpoint(), properties.getApiVersion().getVersion()));
        }

        ConnectionConfig connectionConfig = new ConnectionConfig(properties.getEndpoint(), apiVersion.getMajor(), credentials,
                tokenPassport, properties.isEnableCustomization());
        return connectionConfig;
    }

    /**
     * Connect to NetSuite remote endpoint.
     *
     * @return NetSuite client
     * @throws NetSuiteException if an error occurs during connecting
     */
    public NetSuiteClientService<?> connect() throws NetSuiteException {
        return connect(connectionConfig);
    }

    /**
     * Return NetSuite client.
     *
     * <p>
     * If endpoint is not yet connected then the method creates client and
     * connects ({@link #connect()}) to NetSuite.
     *
     * @return client
     * @throws NetSuiteException if an error occurs during connecting
     */
    public NetSuiteClientService<?> getClientService() throws NetSuiteException {
        return clientService == null ? connect() : clientService;
    }

    /**
     * Creates new NetSuite client and connects to NetSuite remote endpoint.
     *
     * @param connectionConfig connection configuration
     * @return client
     * @throws NetSuiteException if an error occurs during connecting
     */
    private NetSuiteClientService<?> connect(ConnectionConfig connectionConfig) throws NetSuiteException {

        NetSuiteClientService<?> clientService = clientFactory.createClient();
        clientService.setEndpointUrl(connectionConfig.getEndpointUrl());
        clientService.setCredentials(connectionConfig.getCredentials());
        clientService.setTokenPassport(connectionConfig.getTokenPassport());
        MetaDataSource metaDataSource = clientService.getMetaDataSource();
        metaDataSource.setCustomizationEnabled(connectionConfig.isCustomizationEnabled());

        clientService.login();

        return clientService;
    }

    /**
     * Holds configuration for connecting to NetSuite.
     */
    @Data
    @EqualsAndHashCode
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConnectionConfig {

        private String endpointUrl;

        private NetSuiteVersion apiVersion;

        private NetSuiteCredentials credentials;

        private NsTokenPassport tokenPassport;

        private boolean customizationEnabled;
    }
}