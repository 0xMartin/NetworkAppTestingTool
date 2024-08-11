package utb.fai.natt.module;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import utb.fai.natt.spi.NATTModule;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

import utb.fai.natt.core.NATTContext;
import utb.fai.natt.spi.NATTLogger;

/**
 * Modul obsahuje implementaci telnet klienta. Umoznuje prijimat prichozi zpravy
 * a take zpravy odesilat.
 * 
 * Prijate zpravy jsou do message bufferu ukladany v textove podobne neupravene
 * tak jak prichazeji od komunikujici protistrany. Tag je vzdy prazdny ""
 */
public class TelnetClient extends NATTModule {

    protected NATTLogger logger = new NATTLogger(TelnetClient.class);

    private String host;
    private int port;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    /**
     * Vytvori instanci telnet clienta
     * 
     * @param name Nazev modulu
     * @param host Adresa hosta, se kterym bude telnet client komunikovat. Muze byt
     *             null, defaultni hodnota je "localhost"
     * @param port Port, na kterem se bude komunikovat
     * @throws NonUniqueModuleNamesException
     */
    public TelnetClient(String name, String host, int port)
            throws NonUniqueModuleNamesException, InternalErrorException {
        super(name, NATTContext.instance());

        if (port < 0) {
            throw new InternalErrorException("Invalid port number");
        }

        this.host = host == null ? "localhost" : host;
        this.port = port;
    }

    @Override
    public void runModule() throws InternalErrorException {
        try {
            // navaze spojeni s hostem
            socket = new Socket(host, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            logger.info(super.getNameForLogger() + String.format(
                    "Telnet client connected to host with the address: '%s' on port '%d'", host, port));

            // zacne naslouchat prichozim zpravam
            Thread messageListener = new Thread(() -> {
                try {
                    String message;
                    while ((message = reader.readLine()) != null) {
                        // prijatou zpravu vlozi do bufferu
                        NATTContext.instance().getMessageBuffer().addMessage(this.getName(), "", message);
                        // notifikace o prijate zprave
                        super.notifyMessageListeners("", message);
                    }
                } catch (IOException e) {
                    logger.warning(super.getNameForLogger() + String.format("Connection closed"));
                }
                super.setRunning(false);
            });
            messageListener.start();
            super.setRunning(true);
        } catch (IOException e) {
            logger.warning(super.getNameForLogger() + String.format(
                    "Failed to establish a connection with the host '%s' on port '%d'", host, port));
        }
    }

    @Override
    public boolean terminateModule() {
        // odstraneni tohoto modulu z aktivnich modulu
        NATTContext.instance().getModules().remove(this);
        super.setRunning(false);

        try {
            if (socket != null)
                socket.close();
            if (reader != null)
                reader.close();
            if (writer != null)
                writer.close();
        } catch (IOException e) {
            logger.warning(
                    super.getNameForLogger() + "Failed to termite telnet client. Error message: " + e.getMessage());
            return false;
        }

        logger.info(super.getNameForLogger() + String.format("Telnet client [%s] terminated", this.getName()));

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

        if (writer != null) {
            writer.println(message);
            logger.info(super.getNameForLogger() + "Message send: " + message);
        } else {
            logger.warning(super.getNameForLogger() + "Writer not initialized");
            return false;
        }

        return true;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

}
