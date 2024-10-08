package utb.fai.natt.module;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import utb.fai.natt.spi.NATTModule;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

import utb.fai.natt.core.NATTContext;
import utb.fai.natt.spi.NATTLogger;
import utb.fai.natt.core.ProcessManager;

/**
 * Trida urcena pro spousteni externich aplikaci. Umoznuje spusteni, ukonceni,
 * odeslani a prijimani zprav na standartni stream teto aplikace.
 */
@NATTAnnotation.Module("external-program-runner")
public class ExternalProgramRunner extends NATTModule {

    private NATTLogger logger = new NATTLogger(ExternalProgramRunner.class);

    protected String command;

    private Process currentProcess;
    private long currentPID;
    private OutputStream outputStream;

    /**
     * Vytvori instanci modulu ExternalProgramRunner
     * 
     * @param name    - Nazev modulu
     * @param command - Prikaz, ktery se ma vykonat za ucelem spusteni externi
     *                aplikace
     * @throws NonUniqueModuleNamesException
     * @throws InternalErrorException
     */
    public ExternalProgramRunner(String name, String command)
            throws NonUniqueModuleNamesException, InternalErrorException {
        super(name, NATTContext.instance());
        this.command = command;
        this.currentProcess = null;
        super.setRunning(true);
    }

    @Override
    public void runModule() throws InternalErrorException {
        if (this.currentProcess != null) {
            logger.warning(super.getNameForLogger() + "Failed to run. External app is currently running!");
            return;
        }

        logger.info(super.getNameForLogger() + "Run external app with command: " + command);

        try {
            currentProcess = Runtime.getRuntime().exec(ExternalProgramRunner.commandStringToCommandList(command));
            if (currentProcess == null) {
                throw new InternalErrorException("Failed to create process for this command '" + command + "'");
            }
            this.outputStream = currentProcess.getOutputStream();
            InputStream inputStream = currentProcess.getInputStream();

            if (inputStream == null) {
                currentProcess.destroy();
                throw new InternalErrorException("Failed to create input stream for this command '" + command + "'");
            }
            if (this.outputStream == null) {
                currentProcess.destroy();
                throw new InternalErrorException("Failed to create output stream for this command '" + command + "'");
            }

            ProgramOutputHandler outputHandler = new ProgramOutputHandler(this.currentProcess, inputStream,
                    this.getName());
            outputHandler.start();

            // prida pid procesu do souboru pro priadne pozdejsi ukonceni (pokud by doslo k
            // necekanemu ukonceni nastroje)
            try {
                this.currentPID = currentProcess.pid();
                ProcessManager.addPID(ProcessManager.DEFAULT_FILE, String.valueOf(this.currentPID));
            } catch (Exception ex) {
            }

        } catch (IOException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }

    @Override
    public boolean terminateModule() {
        if (currentProcess != null) {
            logger.info(super.getNameForLogger() + "Termiting external application");
            currentProcess.destroy();
            currentProcess = null;

            try {
                outputStream.close();
            } catch (IOException e) {
                logger.warning(super.getNameForLogger() + "Failed to close output stream!");
            }

            try {
                ProcessManager.removePID(ProcessManager.DEFAULT_FILE, String.valueOf(this.currentPID));
            } catch (Exception ex) {
                logger.warning(super.getNameForLogger() + "Failed to remove PID from file!");
            }
        }

        return this.getContext().removeActiveModule(this.getName());
    }

    @Override
    public boolean sendMessage(String message) throws InternalErrorException {
        return this.sendMessageToExternalProgram(message, true);
    }

    /**
     * Prevede prikaz zapsany v retezci na pole argumenut. Napriklad: "java -jar
     * app.jar 11" rozzdeli na [java, jar, app.jar, 11]
     * 
     * @param commandString Prikaz zapsany v retezci
     * @return Pole prikazu
     */
    public static String[] commandStringToCommandList(String commandString) {
        if (commandString == null) {
            String[] empty = new String[0];
            return empty;
        }
        if (commandString.isEmpty()) {
            String[] empty = new String[0];
            return empty;
        }

        String[] parts = commandString.split("(?=(?:[^']*'[^']*')*[^']*$)\\s+");

        List<String> commandList = new ArrayList<>();

        for (String part : parts) {
            if (part.startsWith("'") && part.endsWith("'")) {
                part = part.substring(1, part.length() - 1);
            }
            commandList.add(part);
        }

        String[] command = commandList.toArray(new String[0]);
        return command;
    }

    /**
     * Odesle zpravu na System.in spustene aplikace
     * 
     * @param message Obsah odesilane zpravy
     * @param endLine Pokud je true bude za konec zpravy vlozen symbol ukonceni
     *                radku
     * 
     * @return True v pripade uspesneho odeslani zpravy
     */
    public boolean sendMessageToExternalProgram(String message, boolean endLine) {
        if (currentProcess != null) {
            logger.info(super.getNameForLogger()
                    + "Sending message on standard stream. Message content: " + message);
            try {

                outputStream.write(message.getBytes());
                if (endLine) {
                    outputStream.write('\n');
                }
                outputStream.flush();

                return true;

            } catch (IOException e) {
                logger.warning(super.getNameForLogger() + "Failed to send data. Stream is closed!");
            }
        } else {
            logger.warning(super.getNameForLogger() + "Failed to send data. External app is not running now!");
        }

        return false;
    }

    /**
     * Overi zda je proces spusteny
     * 
     * @return True v pripade ze proces je stale sputeny
     */
    public boolean isProcessRunning() {
        if (this.currentProcess == null) {
            return false;
        }
        try {
            this.currentProcess.exitValue();
            return false; // Proces byl ukoncen
        } catch (IllegalThreadStateException e) {
            return true; // proces je stale aktivni
        }
    }

    /**
     * Trida pro zpracovani prichozich zprav od externe spustene aplikace. Zpravy
     * jsou prijimany pres standartni stream.
     */
    private class ProgramOutputHandler extends Thread {

        private Process targetProcess;
        private BufferedReader reader;
        private String msgBufferName;

        public ProgramOutputHandler(Process targetProcess, InputStream inputStream, String msgBufferName) {
            this.targetProcess = targetProcess;
            this.reader = new BufferedReader(new InputStreamReader(inputStream));
            this.msgBufferName = msgBufferName;
        }

        @Override
        public void run() {
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    // prijatou zpravu vlozi na buffer a zalogguje
                    NATTContext.instance().getMessageBuffer().addMessage(msgBufferName, "", line);
                    notifyMessageListeners("", line);
                }
            } catch (IOException e) {
                logger.info(getNameForLogger() + "External application termited");
            }

            // odstraneni PID z listu jelikoz aplikace jiz byla ukoncena
            try {
                ProcessManager.removePID(ProcessManager.DEFAULT_FILE, String.valueOf(currentPID));
            } catch (Exception ex) {
            }

            // pokusi se ziskat exit kod aplikace a vypsat pripadne chyby
            int exitCode = 0;
            try {
                exitCode = this.targetProcess.waitFor();
                if (exitCode != 0) {
                    logger.warning(getNameForLogger() + "External application ends with error: " + exitCode);
                    BufferedReader errorReader = new BufferedReader(
                            new InputStreamReader(targetProcess.getErrorStream()));
                    String errorLine;
                    StringBuilder errorMessage = new StringBuilder();
                    try {
                        while ((errorLine = errorReader.readLine()) != null) {
                            errorMessage.append(errorLine).append("\n");
                        }
                    } catch (IOException e) {
                    }
                    if (!errorMessage.isEmpty()) {
                        logger.warning(getNameForLogger() + "Error message from external application: "
                                + errorMessage.toString());
                    }
                } else {
                    logger.info(getNameForLogger() + "External application ends with exit code 0");
                }
            } catch (InterruptedException e) {
                logger.warning(getNameForLogger() + "Failed to get exit code of external application. Message: "
                        + e.getMessage());
            }
            currentProcess = null;
        }

    }

}
