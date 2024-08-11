package utb.fai.natt.module;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import utb.fai.natt.spi.NATTModule;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

import utb.fai.natt.core.NATTContext;
import utb.fai.natt.spi.NATTLogger;
import utb.fai.natt.core.PortChecker;

/**
 * Modul obsahuje implementaci telnet serveru. Umoznuje pripojeni libovolneho
 * mnozstvi klientu a umoznuje prijimaz zpravy od vsech. Server nijak
 * nepreposila komunikaci ostatnim klientum, jen je mozne libovolnou zpravu
 * odeslat vsem.
 * 
 * Prijate zpravy jsou do message bufferu ukladany v textove podobne neupravene
 * tak jak prichazeji od klientu. Tag je vzdy nastaven nasledujicim zpusobem:
 * "client-#" kde # odpovide cislu klient. Prvni pripojeni klient ma 1, druhy ma
 * 2 a tak dale
 */
public class TelnetServer extends NATTModule {

    private NATTLogger logger = new NATTLogger(TelnetServer.class);

    private int port;

    private ServerSocket serverSocket;
    private List<ClientHandler> clients = new ArrayList<>();

    /**
     * Vytvori instanci telnet serveru
     * 
     * @param name Nazev modulu
     * @param port Port, na kterem se bude komunikovat
     * @throws NonUniqueModuleNamesException
     */
    public TelnetServer(String name, int port) throws NonUniqueModuleNamesException, InternalErrorException {
        super(name, NATTContext.instance());

        if (port < 0) {
            throw new InternalErrorException("Invalid port number");
        }

        this.port = port;
    }

    @Override
    public void runModule() throws InternalErrorException {
        if (!PortChecker.isPortAvailable("localhost", this.port)) {
            throw new InternalErrorException(
                    String.format("Failed to start Telnet server because port %d is already taken", this.port));
        }

        try {
            // vytvori server soket pro novazovani prichozich spojeni
            serverSocket = new ServerSocket(this.port);
            logger.info(super.getNameForLogger() + "Server socket is listening on port " + this.port);

            // navazovani spojeni s klienty spusti v novem vlakne
            Thread acceptClientsThread = new Thread(() -> {
                try {
                    long id = 1;
                    while (true) {
                        Socket clientSocket = serverSocket.accept();
                        ClientHandler clientHandler = new ClientHandler(clientSocket, "client-" + id, this);
                        id++;
                        clients.add(clientHandler);
                        clientHandler.start();
                    }
                } catch (IOException e) {
                    logger.info(super.getNameForLogger() + "Server socket closed");
                }
                super.setRunning(false);
            });
            acceptClientsThread.start();
            super.setRunning(true);
        } catch (IOException e) {
            throw new InternalErrorException("Failed to start Telnet server: " + e.getMessage());
        }
    }

    @Override
    public boolean terminateModule() {
        // odstraneni tohoto modulu z aktivnich modulu
        NATTContext.instance().getModules().remove(this);
        super.setRunning(false);
        try {
            serverSocket.close();
        } catch (IOException e) {
            logger.warning(super.getNameForLogger() + "Failed to terminate Telnet server: " + e.getMessage());
            return false;
        }

        logger.info(super.getNameForLogger() + String.format("Telnet server [%s] terminated", this.getName()));

        return true;
    }

    @Override
    public boolean sendMessage(String message) throws InternalErrorException {
        try {
            // odesle zpravu vsem pripojenym klientum
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
            logger.info(super.getNameForLogger() + "Message send: " + message);
        } catch (IOException e) {
            logger.warning(super.getNameForLogger() + "Failed to send message to clients: " + e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Handler pro zpracovani prichozi a odchozi komunikace od klientu
     */
    private class ClientHandler extends Thread {
        private String id;
        private TelnetServer serverInstance;

        private Socket clientSocket;
        private BufferedReader reader;
        private PrintWriter writer;

        public ClientHandler(Socket clientSocket, String id, TelnetServer serverInstance) {
            this.clientSocket = clientSocket;
            this.id = id;
            this.serverInstance = serverInstance;
            try {
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                writer = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (IOException e) {
                logger.warning(getNameForLogger() + "Failed to create telnet client handler: " + e.getMessage());
            }
        }

        @Override
        public void run() {
            try {
                String inputLine;
                while ((inputLine = reader.readLine()) != null) {
                    // prijatou zpravu vlozi do bufferu
                    NATTContext.instance().getMessageBuffer().addMessage(this.serverInstance.getName(), this.id,
                            inputLine);
                    // notifikace o prijate zprave
                    this.serverInstance.notifyMessageListeners(this.id, inputLine);
                }
            } catch (IOException e) {
                logger.info(getNameForLogger() + "Client disconnected");
            } finally {
                close();
            }
        }

        public void close() {
            try {
                if (reader != null)
                    reader.close();
                if (writer != null)
                    writer.close();
                if (clientSocket != null)
                    clientSocket.close();
                clients.remove(this);
            } catch (IOException e) {
                logger.warning(getNameForLogger() + "Failed to close: " + e.getMessage());
            }
            logger.info(getNameForLogger() + "Client disconnected");
        }

        public void sendMessage(String message) throws IOException {
            if (writer != null) {
                writer.println(message);
            }
        }

    }
}
