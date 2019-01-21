package org.talend.components.activemq.service;

import org.junit.jupiter.api.Test;
import org.talend.components.activemq.configuration.BasicConfiguration;
import org.talend.components.activemq.datastore.ActiveMQDataStore;
import org.talend.sdk.component.api.record.Schema;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.healthcheck.HealthCheckStatus;

import org.talend.sdk.component.junit5.WithComponents;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.talend.components.activemq.MessageConst.MESSAGE_CONTENT;
import static org.talend.components.activemq.testutils.ActiveMQTestConstants.WRONG_HOST;
import static org.talend.components.activemq.testutils.ActiveMQTestConstants.WRONG_PORT;

@WithComponents("org.talend.components.activemq")
class ActionServiceTest {

    @Service
    private ActionService actionService;

    @Test
    public void testJMSNoConnection() {
        ActiveMQDataStore dataStore = new ActiveMQDataStore();
        dataStore.setHost(WRONG_HOST);
        dataStore.setPort(WRONG_PORT);
        dataStore.setSSL(true);
        HealthCheckStatus status = actionService.validateBasicDatastore(dataStore);

        assertEquals(HealthCheckStatus.Status.KO, status.getStatus());
        assertTrue(status.getComment().contains("Could not connect to broker"));
    }

    @Test
    public void testGuessSchema() {
        Schema schema = actionService.guessSchema(new BasicConfiguration());
        assertNotNull(schema, "Guess Schema should not be null");
        Optional<Schema.Entry> optional = schema.getEntries().stream().findFirst();
        assertTrue(optional.isPresent(), "Guess Schema Entry was not set");
        assertEquals(MESSAGE_CONTENT, optional.get().getName());
        assertEquals(Schema.Type.STRING, optional.get().getType());
    }

}
