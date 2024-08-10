package utb.fai.natt;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import utb.fai.natt.spi.INATTMessage;
import utb.fai.natt.spi.INATTMessage.SearchType;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;
import utb.fai.natt.core.NATTContext;
import utb.fai.natt.module.TelnetClient;
import utb.fai.natt.module.TelnetServer;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TelnetServerTest {

    private TelnetServer telnetServer;

    @Before
    public void setUp() throws InternalErrorException, NonUniqueModuleNamesException {
        telnetServer = new TelnetServer("server-1", 9999);
        telnetServer.runModule();
    }

    @After
    public void tearDown() throws InternalErrorException {
        telnetServer.terminateModule();
    }

    @Test
    public void testClientConnection() throws IOException, InternalErrorException {
        Socket socket = new Socket("localhost", 9999);
        assertTrue(socket.isConnected());
        socket.close();
    }

    @Test
    public void testClientMessageProcessingMultipleClients()
            throws IOException, InternalErrorException, InterruptedException, NonUniqueModuleNamesException {

        TelnetClient client1 = new TelnetClient("TestClient1", "localhost", 9999);
        client1.runModule();
        assertTrue(client1.isConnected());
        // 2x odeslana zprava od 1. klienta
        client1.sendMessage("Test message from client 1");
        client1.sendMessage("Test message from client 1");

        TelnetClient client2 = new TelnetClient("TestClient2", "localhost", 9999);
        client2.runModule();
        assertTrue(client2.isConnected());
        // 3x odeslana zprava od 2. klienta
        client2.sendMessage("Test message from client 2");
        client2.sendMessage("Test message from client 2");
        client2.sendMessage("Test message from client 2");

        TimeUnit.SECONDS.sleep(1);

        LinkedList<INATTMessage> receivedMessages = NATTContext.instance().getMessageBuffer().searchMessages("server-1",
                "client-1", "", SearchType.NONE, false);
        assertEquals(2, receivedMessages.size());
        assertEquals("Test message from client 1", receivedMessages.getFirst().getMessage());

        receivedMessages = NATTContext.instance().getMessageBuffer().searchMessages("server-1",
                "client-2", "", SearchType.NONE, false);
        assertEquals(3, receivedMessages.size());
        assertEquals("Test message from client 2", receivedMessages.getFirst().getMessage());

        client1.terminateModule();
        client2.terminateModule();
    }
}
