package org.talend.components.activemq.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.talend.components.activemq.ActiveMQTestExtention;
import org.talend.components.activemq.datastore.ActiveMQDataStore;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.healthcheck.HealthCheckStatus;
import org.talend.sdk.component.junit5.WithComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.talend.components.activemq.testutils.ActiveMQTestConstants.*;

@WithComponents("org.talend.components.activemq")
@ExtendWith(ActiveMQTestExtention.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActionServiceTestIT {

    @Service
    private ActionService actionService;

    private ActiveMQTestExtention.TestContext testContext;

    @BeforeAll
    private void init(ActiveMQTestExtention.TestContext testContext) {
        this.testContext = testContext;
    }

    @Test
    public void testJMSSuccessfulConnection() {
        HealthCheckStatus status = actionService.validateBasicDatastore(testContext.getDataStore());

        assertEquals(HealthCheckStatus.Status.OK, status.getStatus());
    }

    @Test
    public void testJMSNotSuccessfulConnection() {
        ActiveMQDataStore dataStore = new ActiveMQDataStore();
        dataStore.setHost(WRONG_HOST);
        dataStore.setPort(PORT);
        dataStore.setSSL(true);
        HealthCheckStatus status = actionService.validateBasicDatastore(dataStore);

        assertEquals(HealthCheckStatus.Status.KO, status.getStatus());
    }
}
