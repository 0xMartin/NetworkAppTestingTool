package utb.fai.natt.module;

import java.util.*;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import utb.fai.natt.spi.NATTModule;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

import utb.fai.natt.core.NATTContext;
import utb.fai.natt.spi.NATTLogger;

/**
 * Tento modul obsahuje implementaci mqtt klienta. Umoznuje prijimat a odesilat
 * zpravy.
 * 
 * Prijate zpravy jsou do message bufferu ukladany v textove podobne neupravene
 * tak jak prichazeji od komunikujici protistrany. Tag je vzdy nastaven na
 * hodnotu topicu, ze ktereho zprava prisla
 */
@NATTAnnotation.Module("mqtt-client")
public class MQTTClientTester extends NATTModule {

    protected NATTLogger logger = new NATTLogger(MQTTClientTester.class);

    private String brokerURL;
    private List<String> subscribedTopics;
    private MqttClient mqttClient;

    /**
     * Vytvori instacni MQTT klienta
     * 
     * @param name             Nazev modulu
     * @param brokerURL        URL adresa MQTT brokeru (Muze byt null: defaultni
     *                         hodnota: tcp://localhost:1883)
     * @param subscribedTopics List odebiranych topicu. Muze byt null
     * @throws NonUniqueModuleNamesException
     */
    public MQTTClientTester(String name, String brokerURL, List<String> subscribedTopics)
            throws NonUniqueModuleNamesException, InternalErrorException {
        super(name, NATTContext.instance());
        this.brokerURL = brokerURL;
        this.subscribedTopics = subscribedTopics;
    }

    @Override
    public void runModule() throws InternalErrorException {
        try {
            // vytvoreni klienta
            this.mqttClient = new MqttClient(brokerURL == null ? "tcp://localhost:1883" : brokerURL,
                    MqttClient.generateClientId());

            // pripojeni
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            this.mqttClient.connect(options);

            // nastaveni callbacku
            this.mqttClient.setCallback(new MQTTCallback(this));

            // subscribed topics
            if (subscribedTopics != null) {
                for (String topic : subscribedTopics) {
                    this.mqttClient.subscribe(topic);
                }
            }

            logger.info(super.getNameForLogger() + "MQTT Client module is running.");
            super.setRunning(true);

        } catch (MqttException e) {
            logger.warning(super.getNameForLogger() + "Failed to initialize MQTT client: " + e.getMessage());
        }
    }

    @Override
    public boolean terminateModule() {
        // odstraneni tohoto modulu z aktivnich modulu
        NATTContext.instance().getModules().remove(this);
        super.setRunning(false);

        if (this.mqttClient.isConnected()) {
            try {
                this.mqttClient.disconnect();
            } catch (MqttException e) {
                logger.warning(super.getNameForLogger() + "Failed to disconnect MQTT client: " + e.getMessage());
            }
        }

        try {
            this.mqttClient.close();
        } catch (MqttException e) {
            logger.warning(super.getNameForLogger() + "Failed to terminate MQTT client: " + e.getMessage());
            return false;
        }

        logger.info(super.getNameForLogger() + String.format("MQTT client [%s] terminated", this.getName()));

        return true;
    }

    @Override
    public boolean sendMessage(String message) throws InternalErrorException {
        if (message == null) {
            return false;
        }
        if (message.isEmpty()) {
            return false;
        }

        String[] parts = message.split(":", 2);
        if (parts.length != 2) {
            throw new InternalErrorException("Invalid message format. It should be '(topic):(message)'.");
        }

        String topic = parts[0];
        String content = parts[1];

        try {
            this.mqttClient.publish(topic, content.getBytes(), 0, false);
        } catch (MqttException e) {
            logger.warning(super.getNameForLogger() + "Failed to send MQTT message: " + e.getMessage());
            return false;
        }

        logger.info(super.getNameForLogger()
                + String.format("Message send on topic [%s] with content: %s", topic, content));
        return true;
    }

    private static class MQTTCallback implements org.eclipse.paho.client.mqttv3.MqttCallback {

        private final MQTTClientTester virtualClient;

        public MQTTCallback(MQTTClientTester virtualClient) {
            this.virtualClient = virtualClient;
        }

        @Override
        public void connectionLost(Throwable cause) {
            virtualClient.logger.warning("Connection to MQTT broker lost");
        }

        @Override
        public void messageArrived(String topic, org.eclipse.paho.client.mqttv3.MqttMessage message) throws Exception {
            NATTContext.instance().getMessageBuffer().addMessage(virtualClient.getName(), topic,
                    new String(message.getPayload()));
            virtualClient.notifyMessageListeners(topic, new String(message.getPayload()));
        }

        @Override
        public void deliveryComplete(org.eclipse.paho.client.mqttv3.IMqttDeliveryToken token) {
        }
    }

}
