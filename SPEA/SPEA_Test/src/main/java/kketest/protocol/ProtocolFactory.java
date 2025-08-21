package kketest.protocol;

public class ProtocolFactory {

    public static Sender createSender(String protocol, String host, int port, String format, int qosLevel) throws Exception {
        if ("http".equalsIgnoreCase(protocol)) {
            return new HttpSender(host, port, format);
        } else if ("coap".equalsIgnoreCase(protocol)) {
            return new CoapSender(host, port, format);
        } else if ("mqtt".equalsIgnoreCase(protocol)) {
            // Bei MQTT ist der "host" immer der Broker.
            return new MqttSender(host, port, format, qosLevel);
        } else if ("tcp".equalsIgnoreCase(protocol)) { 
            return new TcpSender(host, port, format);
        } else if ("udp".equalsIgnoreCase(protocol)) { 
            return new UdpSender(host, port, format);
        }
        throw new IllegalArgumentException("Unknown protocol for sender: " + protocol);
    }

    public static Receiver createReceiver(String protocol, int port, String format, int qosLevel) {
        if ("http".equalsIgnoreCase(protocol)) {
            return new HttpReceiver(port, format);
        } else if ("coap".equalsIgnoreCase(protocol)) {
            return new CoapReceiver(port, format);
        } else if ("mqtt".equalsIgnoreCase(protocol)) {
            return new MqttReceiver(port, format, qosLevel);
        } else if ("tcp".equalsIgnoreCase(protocol)) { 
            return new TcpReceiver(port, format);
        } else if ("udp".equalsIgnoreCase(protocol)) { 
            return new UdpReceiver(port, format);
        }
        throw new IllegalArgumentException("Unknown protocol for receiver: " + protocol);
    }
}