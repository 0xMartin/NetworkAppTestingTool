package utb.fai.natt.core;

import java.net.Socket;

public class PortChecker {

    /**
     * Zjisti, zda je port dostupny na zadanem hostiteli.
     * @param host Adresa hostitele (napr. "localhost" nebo IP adresa).
     * @param port Cislo portu, ktery chceme zkontrolovat.
     * @return true, pokud je port dostupny (neni obsazen), jinak false.
     */
    public static boolean isPortAvailable(String host, int port) {
        try (Socket ignored = new Socket(host, port)) {
            // Pokud se podari pripojit, port je obsazen.
            return false; 
        } catch (Exception ignored) {
            // Pokud se pripojeni nezdaří, port je dostupny.
            return true; 
        }
    }

}