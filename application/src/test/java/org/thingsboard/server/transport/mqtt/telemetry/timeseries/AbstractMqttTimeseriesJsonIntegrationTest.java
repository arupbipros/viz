package org.thingsboard.server.transport.mqtt.telemetry.timeseries;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.thingsboard.server.common.data.TransportPayloadType;
import org.thingsboard.server.transport.mqtt.MqttTestCallback;
import org.thingsboard.server.transport.mqtt.MqttTestClient;
import org.thingsboard.server.transport.mqtt.MqttTestConfigProperties;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Slf4j
public abstract class AbstractMqttTimeseriesJsonIntegrationTest extends AbstractMqttTimeseriesIntegrationTest {

    private static final String POST_DATA_TELEMETRY_TOPIC = "data/telemetry";

    @Before
    @Override
    public void beforeTest() throws Exception {
        //do nothing, processBeforeTest will be invoked in particular test methods with different parameters
    }

    @Test
    public void testPushTelemetry() throws Exception {
        MqttTestConfigProperties configProperties = MqttTestConfigProperties.builder()
                .deviceName("Test Post Telemetry device json payload")
                .transportPayloadType(TransportPayloadType.JSON)
                .telemetryTopicFilter(POST_DATA_TELEMETRY_TOPIC)
                .build();
        processBeforeTest(configProperties);
        List<String> expectedKeys = Arrays.asList("key1", "key2", "key3", "key4", "key5");
        processJsonPayloadTelemetryTest(POST_DATA_TELEMETRY_TOPIC, expectedKeys, PAYLOAD_VALUES_STR.getBytes(), false);
    }

    @Test
    public void testPushTelemetryWithTs() throws Exception {
        MqttTestConfigProperties configProperties = MqttTestConfigProperties.builder()
                .deviceName("Test Post Telemetry device json payload")
                .transportPayloadType(TransportPayloadType.JSON)
                .telemetryTopicFilter(POST_DATA_TELEMETRY_TOPIC)
                .build();
        processBeforeTest(configProperties);
        String payloadStr = "{\"ts\": 10000, \"values\": " + PAYLOAD_VALUES_STR + "}";
        List<String> expectedKeys = Arrays.asList("key1", "key2", "key3", "key4", "key5");
        processJsonPayloadTelemetryTest(POST_DATA_TELEMETRY_TOPIC, expectedKeys, payloadStr.getBytes(), true);
    }

    @Test
    public void testPushTelemetryOnShortTopic() throws Exception {
        MqttTestConfigProperties configProperties = MqttTestConfigProperties.builder()
                .deviceName("Test Post Telemetry device json payload")
                .transportPayloadType(TransportPayloadType.JSON)
                .telemetryTopicFilter(POST_DATA_TELEMETRY_TOPIC)
                .build();
        processBeforeTest(configProperties);
        super.testPushTelemetryOnShortTopic();
    }

    @Test
    public void testPushTelemetryOnShortJsonTopic() throws Exception {
        MqttTestConfigProperties configProperties = MqttTestConfigProperties.builder()
                .deviceName("Test Post Telemetry device json payload")
                .transportPayloadType(TransportPayloadType.JSON)
                .telemetryTopicFilter(POST_DATA_TELEMETRY_TOPIC)
                .build();
        processBeforeTest(configProperties);
        super.testPushTelemetryOnShortJsonTopic();
    }

    @Test
    public void testPushTelemetryGateway() throws Exception {
        MqttTestConfigProperties configProperties = MqttTestConfigProperties.builder()
                .deviceName("Test Post Telemetry device json payload")
                .gatewayName("Test Post Telemetry gateway json payload")
                .transportPayloadType(TransportPayloadType.JSON)
                .telemetryTopicFilter(POST_DATA_TELEMETRY_TOPIC)
                .build();
        processBeforeTest(configProperties);
        super.testPushTelemetryGateway();
    }

    @Test
    public void testGatewayConnect() throws Exception {
        MqttTestConfigProperties configProperties = MqttTestConfigProperties.builder()
                .deviceName("Test Post Telemetry device json payload")
                .gatewayName("Test Post Telemetry gateway json payload")
                .transportPayloadType(TransportPayloadType.JSON)
                .telemetryTopicFilter(POST_DATA_TELEMETRY_TOPIC)
                .build();
        processBeforeTest(configProperties);
        super.testGatewayConnect();
    }

    @Test
    public void testPushTelemetryWithMalformedPayloadAndSendAckOnErrorEnabled() throws Exception {
        MqttTestConfigProperties configProperties = MqttTestConfigProperties.builder()
                .deviceName("Test Post Telemetry device json payload")
                .transportPayloadType(TransportPayloadType.JSON)
                .telemetryTopicFilter(POST_DATA_TELEMETRY_TOPIC)
                .sendAckOnValidationException(true)
                .build();
        processBeforeTest(configProperties);
        MqttTestClient client = new MqttTestClient();
        client.connectAndWait(accessToken);
        MqttTestCallback callback = new MqttTestCallback();
        client.setCallback(callback);
        client.publish(POST_DATA_TELEMETRY_TOPIC, MALFORMED_JSON_PAYLOAD.getBytes());
        callback.getDeliveryLatch().await(3, TimeUnit.SECONDS);
        assertTrue(callback.isPubAckReceived());
        client.disconnect();
    }

    @Test
    public void testPushTelemetryWithMalformedPayloadAndSendAckOnErrorDisabled() throws Exception {
        MqttTestConfigProperties configProperties = MqttTestConfigProperties.builder()
                .deviceName("Test Post Telemetry device json payload")
                .transportPayloadType(TransportPayloadType.JSON)
                .telemetryTopicFilter(POST_DATA_TELEMETRY_TOPIC)
                .build();
        processBeforeTest(configProperties);
        MqttTestClient client = new MqttTestClient();
        client.connectAndWait(accessToken);
        MqttTestCallback callback = new MqttTestCallback();
        client.setCallback(callback);
        client.publish(POST_DATA_TELEMETRY_TOPIC, MALFORMED_JSON_PAYLOAD.getBytes());
        callback.getDeliveryLatch().await(3, TimeUnit.SECONDS);
        assertFalse(callback.isPubAckReceived());
    }

    @Test
    public void testPushTelemetryGatewayWithMalformedPayloadAndSendAckOnErrorEnabled() throws Exception {
        MqttTestConfigProperties configProperties = MqttTestConfigProperties.builder()
                .deviceName("Test Post Telemetry device json payload")
                .gatewayName("Test Post Telemetry gateway json payload")
                .transportPayloadType(TransportPayloadType.JSON)
                .telemetryTopicFilter(POST_DATA_TELEMETRY_TOPIC)
                .sendAckOnValidationException(true)
                .build();
        processBeforeTest(configProperties);
        MqttTestClient client = new MqttTestClient();
        client.connectAndWait(gatewayAccessToken);
        MqttTestCallback callback = new MqttTestCallback();
        client.setCallback(callback);
        client.publish(POST_DATA_TELEMETRY_TOPIC, MALFORMED_JSON_PAYLOAD.getBytes());
        callback.getDeliveryLatch().await(3, TimeUnit.SECONDS);
        assertTrue(callback.isPubAckReceived());
        client.disconnect();
    }

    @Test
    public void testPushTelemetryGatewayWithMalformedPayloadAndSendAckOnErrorDisabled() throws Exception {
        MqttTestConfigProperties configProperties = MqttTestConfigProperties.builder()
                .deviceName("Test Post Telemetry device json payload")
                .gatewayName("Test Post Telemetry gateway json payload")
                .transportPayloadType(TransportPayloadType.JSON)
                .telemetryTopicFilter(POST_DATA_TELEMETRY_TOPIC)
                .build();
        processBeforeTest(configProperties);
        MqttTestClient client = new MqttTestClient();
        client.connectAndWait(gatewayAccessToken);
        MqttTestCallback callback = new MqttTestCallback();
        client.setCallback(callback);
        client.publish(POST_DATA_TELEMETRY_TOPIC, MALFORMED_JSON_PAYLOAD.getBytes());
        callback.getDeliveryLatch().await(3, TimeUnit.SECONDS);
        assertFalse(callback.isPubAckReceived());
    }

}
