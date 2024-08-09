package utb.fai.Core;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Zajistuje pri opetovnem spousteni nastroje ukonceni spoustenych procesu,
 * ktere se v dusledku pripadne chyby, nebo necekaneho ukoneceni nastroje
 * nepodarilo ukoncit spravne
 */
public class ProcessManager {

    public static String DEFAULT_FILE = "pid-list.txt";

    private static NATTLogger logger = new NATTLogger(ProcessManager.class);

    /**
     * Precte seznam PID procesu ulozenych v souoru
     * 
     * @param filePath Cesta k souboru
     * @return list PID
     * @throws IOException
     */
    public static List<String> readPIDs(String filePath) throws IOException {
        List<String> pids = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                pids.add(line.trim());
            }
        }
        return pids;
    }

    /**
     * Ukonci vsechny procesy jejihz PID jsou v souboru
     * 
     * @param filePath Cesta k souboru
     */
    public static void killProcessesAndCleanUp(String filePath) {
        List<String> pids;
        try {
            pids = readPIDs(filePath);
        } catch (IOException e) {
            ProcessManager.logger.warning("Failed to read PIDs: " + e.getMessage());
            return;
        }

        for (String pid : pids) {
            try {
                ProcessBuilder pb;
                if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                    pb = new ProcessBuilder("taskkill", "/PID", pid, "/F");
                } else {
                    pb = new ProcessBuilder("kill", "-9", pid);
                }
                Process process = pb.start();
                int exitValue = process.waitFor();
                if (exitValue == 0) {
                    ProcessManager.logger.info("Process PID " + pid + " has been terminated.");
                    removePID(filePath, pid);
                } else {
                    ProcessManager.logger.warning("Failed to terminate process PID " + pid);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Prida PID na konec souboru
     * 
     * @param filePath Cesta k souboru
     * @param newPID   PID ktery ma byt pridan
     * @throws IOException
     */
    public static synchronized void addPID(String filePath, String newPID) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(newPID);
            writer.newLine();
            ProcessManager.logger.info(String.format("New PID (%s) added to file", newPID));
        }
    }

    /**
     * Odstrani PID ze souboru
     * 
     * @param filePath  Cesta k souboru
     * @param targetPID PID ktery ma byt odebran
     * @throws IOException
     */
    public static synchronized void removePID(String filePath, String targetPID) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        List<String> updatedLines = lines.stream().filter(line -> !line.trim().equals(targetPID))
                .collect(Collectors.toList());
        Files.write(Paths.get(filePath), updatedLines);
        ProcessManager.logger.info(String.format("PID (%s) removed from file", targetPID));
    }

}
