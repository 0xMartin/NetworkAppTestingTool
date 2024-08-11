package utb.fai.natt.module;

import javax.mail.*;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

import java.io.IOException;

import utb.fai.natt.spi.NATTModule;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

import utb.fai.natt.core.NATTContext;
import utb.fai.natt.spi.NATTLogger;
import utb.fai.natt.core.PortChecker;

/**
 * Modul obsahuje implementaci virtualniho email serveru. Umoznuje prijimat
 * emaily a tak je odesilat.
 * 
 * Prijate emaily jsou do message bufferu ukladany v textove podobne neupravene
 * tak jak prichazeji od odeslitele. Tag je vzdy nastaven na hodnotu "subject"
 * emailu a obsah zpravy odpovida obsahu samotneho emailu.
 */
@NATTAnnotation.Module("email-server")
public class SMTPEmailServer extends NATTModule {

    protected NATTLogger logger = new NATTLogger(SMTPEmailServer.class);

    private GreenMail smtpServer;
    private int port;
    private volatile boolean isRunning;

    /**
     * Vytvori instanci virtualniho smtp email serveru
     * 
     * @param name Nazev modulu
     * @param port Port, na kterem bude email server naslouchat
     * @throws NonUniqueModuleNamesException
     */
    public SMTPEmailServer(String name, int port) throws NonUniqueModuleNamesException, InternalErrorException {
        super(name, NATTContext.instance());
        if (port < 0) {
            throw new InternalErrorException("Invalid port number");
        }
        this.port = port;
        this.smtpServer = new GreenMail(new ServerSetup(port, null, "smtp"));
        this.isRunning = false;
    }

    @Override
    public void runModule() throws InternalErrorException {
        if (!PortChecker.isPortAvailable("localhost", this.port)) {
            throw new InternalErrorException(
                    String.format("Failed to start SMTP email server because port %d is already taken", this.port));
        }

        smtpServer.start();
        logger.info(super.getNameForLogger() + "SMTP Email Server is listening on port: " + this.port);
        this.isRunning = true;

        // vlakno pro zpracovani prichozich emailu
        Thread emailHandlerThread = new Thread(() -> {
            try {
                while (isRunning) {
                    // zpracuje vsechny prijate emaily
                    Message[] messages = smtpServer.getReceivedMessages();
                    for (Message message : messages) {
                        try {
                            processEmail(message);
                        } catch (IOException e) {
                            logger.warning(super.getNameForLogger() + "Failed to process email: " + e.getMessage());
                        }
                    }
                    // odstrani prijate emaily z pameti
                    if (messages.length > 0) {
                        try {
                            smtpServer.purgeEmailFromAllMailboxes();
                        } catch (FolderException e) {
                            logger.warning(super.getNameForLogger() + "Failed to remove emails from mail box");
                        }
                    }
                    // uspany vlakna na urcity interval ... pokud dojde email je probuzeno okamzite
                    smtpServer.waitForIncomingEmail(5000, 1);
                }
            } catch (MessagingException e) {
                logger.info(super.getNameForLogger() + "SMTP Email Server closed. Message: " + e.getMessage());
            } catch (Exception e) {
                logger.warning(super.getNameForLogger() + "Unhandled exception occurred in email handler thread: "
                        + e.getMessage());
            } finally {
                logger.info(super.getNameForLogger() + "Email handler thread terminated.");
            }
        });
        emailHandlerThread.start();
        super.setRunning(true);
    }

    @Override
    public boolean terminateModule() {
        NATTContext.instance().getModules().remove(this);
        this.isRunning = false;
        super.setRunning(false);
        smtpServer.stop();
        logger.info(super.getNameForLogger() + String.format("SMTP email server [%s] terminated", this.getName()));
        return true;
    }

    @Override
    public boolean sendMessage(String message) throws InternalErrorException {
        return false;
    }

    private void processEmail(Message message) throws MessagingException, IOException {
        String subject = message.getSubject();
        String content = message.getContent().toString();
        String finalContent = content.replaceAll("\\s+$", "");
        NATTContext.instance().getMessageBuffer().addMessage(this.getName(), subject, finalContent);
        super.notifyMessageListeners(subject, finalContent);
    }

}