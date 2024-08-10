package utb.fai.natt;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;
import utb.fai.natt.module.TelnetClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TelnetClientTest {

    private static final String HOST = "localhost";
    private static final int PORT = 5555;

    private ServerSocket serverSocket;
    private TelnetClient telnetClient;

    @Before
    public void setUp() throws IOException, InternalErrorException, NonUniqueModuleNamesException {
        serverSocket = new ServerSocket(PORT);

        telnetClient = new TelnetClient("TestClient", HOST, PORT);
        telnetClient.runModule();
    }

    @After
    public void tearDown() throws IOException, InternalErrorException {
        telnetClient.terminateModule();
        
        if (serverSocket != null) {
            serverSocket.close();
        }
    }

    @Test
    public void testConnectionEstablishment() {
        Thread serverThread = new Thread(() -> {
            try {
                Socket clientSocket = serverSocket.accept();
                assertNotNull(clientSocket);
            } catch (IOException e) {
                fail("Server failed to accept connection: " + e.getMessage());
            }
        });
        serverThread.start();

        assertTrue(telnetClient.isConnected());
    }

    @Test
    public void testSendMessage() throws InternalErrorException {
        Thread serverThread = new Thread(() -> {
            try {
                Socket clientSocket = serverSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String receivedMessage = reader.readLine();
                assertEquals("Test message", receivedMessage);
            } catch (IOException e) {
                fail("Server failed to accept connection: " + e.getMessage());
            }
        });
        serverThread.start();

        telnetClient.sendMessage("Test message");
    }

    @Test
    public void testCleanup() throws InternalErrorException {
        assertTrue(telnetClient.isConnected());
        telnetClient.terminateModule();
        assertFalse(telnetClient.isConnected());
    }
}
