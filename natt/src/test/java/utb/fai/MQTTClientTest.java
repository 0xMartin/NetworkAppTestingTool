package utb.fai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import utb.fai.Core.MessageBuffer.NATTMessage;
import utb.fai.Core.NATTContext;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.NonUniqueModuleNamesException;
import utb.fai.Module.MQTTBroker;
import utb.fai.Module.MQTTClientTester;

public class MQTTClientTest {

    @Test
    public void mqttCommunicationTest() throws NonUniqueModuleNamesException, InternalErrorException, InterruptedException {
        // vytvorit broker... pokud jiz na zarizeni nejaky bezi nebude vytvoren v ramci testu
        MQTTBroker broker = null;
        try {
            broker = new MQTTBroker("broker", 9999);
            broker.runModule();
        } catch (Exception ex) {
            ex.printStackTrace();
            broker = null;
        }

        MQTTClientTester clientTester1 = new MQTTClientTester("client1", "tcp://localhost:9999", List.of("topic-1"));
        clientTester1.runModule();
        MQTTClientTester clientTester2 = new MQTTClientTester("client2", "tcp://localhost:9999", List.of("topic-2"));
        clientTester2.runModule();

        assertTrue(clientTester1.isRunning());
        assertTrue(clientTester2.isRunning());

        clientTester1.sendMessage("topic-2:Message from client 1");
        clientTester1.sendMessage("topic:Wrong topic 1");
        clientTester1.sendMessage("topic:Wrong topic 2");
        clientTester2.sendMessage("topic-1:Message from client 2");

        TimeUnit.SECONDS.sleep(1);

        CopyOnWriteArrayList<NATTMessage> messages1 = NATTContext.instance().getMessageBuffer().getMessages("client1");
        assertEquals(1, messages1.size());
        assertEquals("Message from client 2", messages1.get(0).getMessage());

        CopyOnWriteArrayList<NATTMessage> messages2 = NATTContext.instance().getMessageBuffer().getMessages("client2");
        assertEquals(1, messages2.size());
        assertEquals("Message from client 1", messages2.get(0).getMessage());

        if(broker != null) {
            broker.terminateModule();
        }
    }

}
