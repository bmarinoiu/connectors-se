package org.talend.components.jms.service;

import org.junit.jupiter.api.Test;
import org.talend.components.jms.datastore.JmsDataStore;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.healthcheck.HealthCheckStatus;
import org.talend.sdk.component.junit5.WithComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WithComponents("org.talend.components.jms")
class ActionServiceTestIT {

    public static String JMS_PROVIDER = "ACTIVEMQ";

    public static final String URL = "tcp://localhost:61616";

    @Service
    private ActionService actionService;

    @Test
    public void testJMSSuccessfulConnection() {
        JmsDataStore dataStore = new JmsDataStore();
        dataStore.setModuleList(JMS_PROVIDER);
        dataStore.setUrl(URL);
        HealthCheckStatus status = actionService.validateBasicDatastore(dataStore);

        assertEquals(HealthCheckStatus.Status.OK, status.getStatus());
    }

}