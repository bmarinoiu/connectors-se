package org.talend.components.activemq.testutils;

public class ActiveMQTestConstants {

    public static final String LOCALHOST = "localhost";

    public static final String WRONG_HOST = "host";

    public static final String WRONG_PORT = "1234";

    public static final String PORT = System.getProperty("activemq.port", "61617");

    public static final String DESTINATION = "test";

    public static final String TEST_MESSAGE = "hello world";

    public static final String TEST_MESSAGE2 = "test message";

    public static final boolean DURABLE_SUBSCRIPTION = false;

    public static final String CLIENT_ID = "testClientId";

    public static final String SUBSCRIBER_NAME = "test";

    public static final int TIMEOUT = 1;

    public static final int NO_MESSAGES = 0;

    public static final int TEN_MESSAGES = 10;

}
