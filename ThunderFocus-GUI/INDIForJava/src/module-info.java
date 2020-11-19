module INDIForJava {
    requires java.xml;
    requires slf4j.api;
    requires xstream;
    requires commons.codec;
    requires javax.websocket.api;
    requires tyrus.client;
    requires jandex;
    requires tyrus.server;
    exports org.indilib.i4j.properties;
    exports org.indilib.i4j.driver;
    exports org.indilib.i4j.driver.focuser;
    exports org.indilib.i4j.driver.connection;
    exports org.indilib.i4j.driver.util;
    exports org.indilib.i4j.server;
    exports org.indilib.i4j.server.api;
    exports org.indilib.i4j.protocol;
    exports org.indilib.i4j.protocol.api;
    exports org.indilib.i4j;
}