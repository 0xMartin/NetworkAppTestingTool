package utb.fai.natt.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NATTLogger {
    private String className;

    public NATTLogger(Class<?> clazz) {
        this.className = clazz.getSimpleName();
    }

    public void info(String message) {
        log("INFO", message);
    }

    public void warning(String message) {
        log("WARNING", message);
    }

    public void error(String message) {
        log("ERROR", message);
    }

    private void log(String level, String message) {
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedTime = currentTime.format(timeFormatter);
        String logMessage = "[" + formattedTime + "][" + className + "][" + level + "] " + message;

        System.out.println(logMessage);

        LogFileWriter.getInstance().writeLog(logMessage);
    }

    public static class LogFileWriter {
        private static LogFileWriter instance;
        private BufferedWriter logFileWriter;

        private LogFileWriter() {
            initializeLogFile();
        }

        public static synchronized LogFileWriter getInstance() {
            if (instance == null) {
                instance = new LogFileWriter();
            }
            return instance;
        }

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
}