package utb.fai.natt.spi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.*;

/**
 * NATTLogger is a utility class for logging messages to Sytem.out and file.
 */
public class NATTLogger {

    // Target class name
    private String className;

    /**
     * Constructor for NATTLogger.
     * 
     * @param clazz The class for which the logger is created.
     */
    public NATTLogger(Class<?> clazz) {
        this.className = clazz.getSimpleName();
    }

    /**
     * Logs an info message.
     * 
     * @param message The message to be logged.
     */
    public void info(String message) {
        log("INFO", message);
        notifyListeners("INFO", message);
    }

    /**
     * Logs a warning message.
     * 
     * @param message The message to be logged.
     */
    public void warning(String message) {
        log("WARNING", message);
        notifyListeners("WARNING", message);
    }

    /**
     * Logs an error message.
     * 
     * @param message The message to be logged.
     */
    public void error(String message) {
        log("ERROR", message);
        notifyListeners("ERROR", message);
    }

    /**
     * Logs a message with the specified level.
     * 
     * @param level   The level of the message (INFO, WARNING, ERROR).
     * @param message The message to be logged.
     */
    private void log(String level, String message) {
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedTime = currentTime.format(timeFormatter);
        String logMessage = "[" + formattedTime + "][" + className + "][" + level + "] " + message;

        System.out.println(logMessage);

        LogFileWriter.getInstance().writeLog(logMessage);
    }

    /**
     * Singleton class for writing log messages to a file.
     */
    public static class LogFileWriter {
        private static LogFileWriter instance;
        private BufferedWriter logFileWriter;

        private LogFileWriter() {
            initializeLogFile();
        }

        /**
         * Returns the singleton instance of LogFileWriter.
         * 
         * @return The singleton instance of LogFileWriter.
         */
        public static synchronized LogFileWriter getInstance() {
            if (instance == null) {
                instance = new LogFileWriter();
            }
            return instance;
        }

        /**
         * Initializes the log file.
         */
        private void initializeLogFile() {
            try {
                // Create directory if it doesn't exist
                File logDirectory = new File("./natt-logs");
                if (!logDirectory.exists()) {
                    logDirectory.mkdirs();
                }

                // Create log file
                LocalDateTime currentTime = LocalDateTime.now();
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
                String formattedDate = currentTime.format(dateFormatter);
                String logFileName = logDirectory.getPath() + "/natt-log-" + formattedDate + ".txt";

                logFileWriter = new BufferedWriter(new FileWriter(logFileName, true));
            } catch (IOException e) {
                System.err.println("Failed to initialize log file: " + e.getMessage());
            }
        }

        /**
         * Writes a log message to the log file.
         * 
         * @param message The message to be written to the log file.
         */
        public synchronized void writeLog(String message) {
            try {
                if (logFileWriter != null) {
                    logFileWriter.write(message);
                    logFileWriter.newLine();
                    logFileWriter.flush();
                }
            } catch (IOException e) {
                System.err.println("Failed to write to log file: " + e.getMessage());
            }
        }

        /**
         * Closes the log file.
         */
        public void close() {
            try {
                if (logFileWriter != null) {
                    logFileWriter.close();
                }
            } catch (IOException e) {
                System.err.println("Failed to close log file: " + e.getMessage());
            }
        }
    }

    /**
     * Helper function to notify all listeners registered with the
     * LogCallbackHandler.
     * 
     * @param level   Level of the log message.
     * @param message Content of the log message.
     */
    private void notifyListeners(String level, String message) {
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedTime = currentTime.format(timeFormatter);
        LogCallbackHandler.getInstance().notifyListeners(formattedTime, level, className, message);
    }

    /**
     * Interface for log callback.
     */
    public interface LogCallback {
        void onComplete(String time, String level, String className, String result);
    }

    /**
     * Singleton class for log callback.
     */
    public static class LogCallbackHandler {

        private static LogCallbackHandler instance;
        private List<LogCallback> listeners;

        private LogCallbackHandler() {
            listeners = new ArrayList<LogCallback>();
        }

        /**
         * Returns the singleton instance of LogCallbackHandler.
         * 
         * @return The singleton instance of LogCallbackHandler.
         */
        public static synchronized LogCallbackHandler getInstance() {
            if (instance == null) {
                instance = new LogCallbackHandler();
            }
            return instance;
        }

        public void registerCallback(LogCallback listener) {
            listeners.add(listener);
        }

        protected void notifyListeners(String time, String level, String className, String message) {
            for (LogCallback listener : listeners) {
                listener.onComplete(time, level, className, message);
            }
        }

    }

}