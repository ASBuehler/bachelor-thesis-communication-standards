package kketest.protocol;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import kketest.utility.MessErgebnis;

public class MqttSender implements Sender {
    private static final String TOPIC = "kke/test/data";
    private static final String BROKER_IP = "192.168.100.20";

    private final String brokerHost;
    private final int port;
    private final int qosLevel;

    public MqttSender(String host, int port, String format, int qosLevel) {
        this.brokerHost = BROKER_IP;
        this.port = port;
        this.qosLevel = qosLevel;
    }

    @Override
    public MessErgebnis<Void> send(byte[] data) throws Exception {
        final Mqtt5BlockingClient client = Mqtt5Client.builder()
                .serverHost(this.brokerHost)
                .serverPort(this.port)
                .buildBlocking();
        
        client.connect();

        long startTime = System.nanoTime();
        client.publishWith()
            .topic(TOPIC)
            .qos(MqttQos.fromCode(this.qosLevel)) // Setzt den QoS-Level
            .payload(data)
            .send();
        long endTime = System.nanoTime();

        System.out.println("MQTT_MESSAGE_PUBLISHED=true_QOS=" + this.qosLevel);

        client.disconnect();
        return new MessErgebnis<>(null, endTime - startTime);
    }
}