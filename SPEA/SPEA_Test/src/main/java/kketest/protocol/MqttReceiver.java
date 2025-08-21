package kketest.protocol;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import kketest.model.DataObject;
import kketest.serializer.Serializer;
import kketest.serializer.SerializerFactory;
import kketest.utility.MessErgebnis;

public class MqttReceiver implements Receiver {
    private static final String TOPIC = "kke/test/data";
    private static final String BROKER_IP = "192.168.100.20";

    private final String brokerHost;
    private final int port;
    private final String format;
    private final int qosLevel;

    public MqttReceiver(int port, String format, int qosLevel) {
        this.brokerHost = BROKER_IP;
        this.port = port;
        this.format = format;
        this.qosLevel = qosLevel; // Speichere den übergebenen QoS-Level
    }

    @Override
    public void listen() throws Exception {
        final Mqtt5BlockingClient client = Mqtt5Client.builder()
                .serverHost(this.brokerHost)
                .serverPort(this.port)
                .identifier("KKE_Receiver")
                .buildBlocking();

        client.connect();
        System.out.println("MQTT Receiver connected to broker " + this.brokerHost);

        // Der Haupt-Thread wird blockiert, bis der Callback ihn aufweckt
        final Object lock = new Object();

        client.toAsync().subscribeWith()
            .topicFilter(TOPIC)
            .qos(MqttQos.fromCode(this.qosLevel))
            .callback(publish -> {
                try {
                    byte[] requestBytes = publish.getPayloadAsBytes();
                    System.out.println("RECEIVED_PAYLOAD_SIZE_bytes=" + requestBytes.length);
                    
                    Serializer serializer = SerializerFactory.getSerializer("json");
                    MessErgebnis<DataObject> desErgebnis = serializer.deserialize(requestBytes, DataObject.class);
                    System.out.println("DESERIALIZATION_TIME_ns=" + desErgebnis.zeitInNs);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // Wecke den Haupt-Thread auf, damit er den Prozess beenden kann
                    synchronized(lock) {
                        lock.notify();
                    }
                }
            })
            .send();
            
        System.out.println("MQTT Receiver subscribed to topic '" + TOPIC + "' with QoS " + this.qosLevel + ". Waiting for message...");
        
        // Blockiere, bis die Nachricht im Callback verarbeitet wurde
        synchronized(lock) {
            lock.wait();
        }

        client.disconnect();
        System.out.println("MQTT Receiver finished.");
    }

    @Override public void stop() {}
}