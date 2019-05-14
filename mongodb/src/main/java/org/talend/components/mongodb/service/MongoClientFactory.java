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

package org.talend.components.mongodb.service;

import com.mongodb.*;
import org.talend.components.mongodb.datastore.MongoDBDatastore;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class MongoClientFactory {

    protected final MongoDBDatastore datastore;

    protected final MongoClientOptions.Builder clientOptions;

    public MongoClientFactory(MongoDBDatastore datastore, MongoClientOptions.Builder clientOptions) {
        this.datastore = datastore;
        this.clientOptions = clientOptions;
    }

    public static MongoClientFactory getInstance(MongoDBDatastore datastore, MongoClientOptions.Builder clientOptions,
            I18nMessage i18nMessage) {
        if (datastore.isUseConnectionString()) {
            return new ConnectionStringClientFactory(datastore, clientOptions);
        }
        if (datastore.isAuthentication()) {
            switch (datastore.getMongoAuthentication().getAuthenticationMechanism()) {
            case NEGOTIATE_MEC:
                return new NegotiateAuthMongoClientBuilder(datastore, clientOptions);
            case PLAIN_MEC:
                return new PlainAuthMongoClientFactory(datastore, clientOptions);
            case SCRAMSHA1_MEC:
                return new ScramSha1AuthMongoClientBuilder(datastore, clientOptions);
            case KERBEROS_MEC:
                return new KerberosAuthMongoClientFactory(datastore, clientOptions);
            default:
                throw new IllegalArgumentException(i18nMessage
                        .authMechanismNotSupported(datastore.getMongoAuthentication().getAuthenticationMechanism().name()));
            }
        }
        return new DefaultMongoClientFactory(datastore, clientOptions);
    }

    protected abstract MongoClient createClient();

    protected List<ServerAddress> getServerAddresses() {
        List<ServerAddress> addressesList;
        if (datastore.isUseReplicaSetAddress()) {
            addressesList = datastore.getReplicaAddresses().stream().map(a -> new ServerAddress(a.getAddress(), a.getPort()))
                    .collect(Collectors.toList());
        } else {
            addressesList = Collections.singletonList(
                    new ServerAddress(datastore.getServerAddress().getAddress(), datastore.getServerAddress().getPort()));
        }
        return addressesList;
    }

    public static class ConnectionStringClientFactory extends MongoClientFactory {

        public ConnectionStringClientFactory(MongoDBDatastore datastore, MongoClientOptions.Builder clientOptions) {
            super(datastore, clientOptions);
        }

        @Override
        protected MongoClient createClient() {
            return new MongoClient(new MongoClientURI(datastore.getConnectionString(), clientOptions));
        }

    }

    public static class DefaultMongoClientFactory extends MongoClientFactory {

        public DefaultMongoClientFactory(MongoDBDatastore datastore, MongoClientOptions.Builder clientOptions) {
            super(datastore, clientOptions);
        }

        @Override
        protected MongoClient createClient() {
            return new MongoClient(getServerAddresses(), clientOptions.build());
        }
    }

    public abstract static class AuthenticationMongoClientFactory extends MongoClientFactory {

        public AuthenticationMongoClientFactory(MongoDBDatastore datastore, MongoClientOptions.Builder clientOptions) {
            super(datastore, clientOptions);
        }

        @Override
        protected MongoClient createClient() {
            return new MongoClient(getServerAddresses(), getMongoCredentials(), clientOptions.build());
        }

        protected abstract MongoCredential getMongoCredentials();

    }

    public static class PlainAuthMongoClientFactory extends AuthenticationMongoClientFactory {

        public PlainAuthMongoClientFactory(MongoDBDatastore datastore, MongoClientOptions.Builder clientOptions) {
            super(datastore, clientOptions);
        }

        @Override
        protected MongoCredential getMongoCredentials() {
            return MongoCredential.createPlainCredential(
                    datastore.getMongoAuthentication().getUserPassConfiguration().getUsername(), datastore.getDatabase(),
                    datastore.getMongoAuthentication().getUserPassConfiguration().getPassword().toCharArray());
        }
    }

    public static class KerberosAuthMongoClientFactory extends AuthenticationMongoClientFactory {

        public KerberosAuthMongoClientFactory(MongoDBDatastore datastore, MongoClientOptions.Builder clientOptions) {
            super(datastore, clientOptions);
        }

        @Override
        protected MongoCredential getMongoCredentials() {
            System.setProperty("java.security.krb5.realm", datastore.getMongoAuthentication().getKerberosCreds().getRealm());
            System.setProperty("java.security.krb5.kdc", datastore.getMongoAuthentication().getKerberosCreds().getKdcServer());
            System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
            return MongoCredential
                    .createGSSAPICredential(datastore.getMongoAuthentication().getKerberosCreds().getUserPrincipal());
        }

    }

    protected abstract static class WithAuthDatabaseMongoClientFactory extends AuthenticationMongoClientFactory {

        public WithAuthDatabaseMongoClientFactory(MongoDBDatastore datastore, MongoClientOptions.Builder clientOptions) {
            super(datastore, clientOptions);
        }

        protected String getAuthDatabase() {
            if (datastore.getMongoAuthentication().getAuthDatabaseConfig().isSetAuthenticationDatabase()) {
                return datastore.getMongoAuthentication().getAuthDatabaseConfig().getAuthenticationDatabase();
            }
            return datastore.getDatabase();
        }

    }

    public static class ScramSha1AuthMongoClientBuilder extends WithAuthDatabaseMongoClientFactory {

        public ScramSha1AuthMongoClientBuilder(MongoDBDatastore datastore, MongoClientOptions.Builder clientOptions) {
            super(datastore, clientOptions);
        }

        @Override
        protected MongoCredential getMongoCredentials() {
            return MongoCredential.createScramSha1Credential(
                    datastore.getMongoAuthentication().getUserPassConfiguration().getUsername(), getAuthDatabase(),
                    datastore.getMongoAuthentication().getUserPassConfiguration().getPassword().toCharArray());
        }

    }

    public static class NegotiateAuthMongoClientBuilder extends WithAuthDatabaseMongoClientFactory {

        public NegotiateAuthMongoClientBuilder(MongoDBDatastore datastore, MongoClientOptions.Builder clientOptions) {
            super(datastore, clientOptions);
        }

        @Override
        protected MongoCredential getMongoCredentials() {
            return MongoCredential.createCredential(datastore.getMongoAuthentication().getUserPassConfiguration().getUsername(),
                    getAuthDatabase(), datastore.getMongoAuthentication().getUserPassConfiguration().getPassword().toCharArray());
        }

    }

}
