// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.activemq.service;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.talend.components.activemq.configuration.MessageType;
import org.talend.components.activemq.datastore.ActiveMQDataStore;
import org.talend.sdk.component.api.service.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ActiveMQService {

    @Service
    private I18nMessage i18n;

    public Destination getDestination(Session session, String destination, MessageType messageType) throws JMSException {
        return (MessageType.QUEUE == messageType) ? session.createQueue(destination) : session.createTopic(destination);
    }

    public ConnectionFactory createConnectionFactory(ActiveMQDataStore dataStore) {
        log.info(dataStore.getUrl());
        return new ActiveMQConnectionFactory(dataStore.getUrl());
    }

    public Session getSession(Connection connection, Boolean transacted) throws JMSException {
        int acknowledge = transacted ? Session.SESSION_TRANSACTED : Session.AUTO_ACKNOWLEDGE;
        return connection.createSession(transacted, acknowledge);
    }

    public Connection getConnection(ConnectionFactory connectionFactory, ActiveMQDataStore dataStore) throws JMSException {
        return dataStore.isUserIdentity() ? connectionFactory.createConnection(dataStore.getUserName(), dataStore.getPassword())
                : connectionFactory.createConnection();
    }

    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException e) {
                log.warn(i18n.warnConnectionCantBeClosed(), e);
            }
        }
    }

    public void commit(Session session) {
        try {
            if (session.getTransacted()) {
                session.commit();
            }
        } catch (JMSException e) {

            try {
                session.rollback();
            } catch (JMSException e1) {
                log.error("Can't rollback", e);
            }
        }
    }

}
