package utb.fai.Core;

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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedTime = currentTime.format(formatter);
        System.out.println("[" + formattedTime + "][" + className + "][" + level + "] " + message);
    }

}
