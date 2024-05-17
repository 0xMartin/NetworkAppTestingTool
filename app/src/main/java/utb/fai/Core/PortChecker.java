package utb.fai.Core;

import java.net.Socket;

public class PortChecker {

    public static boolean isPortAvailable(String host, int port) {
        try (Socket ignored = new Socket(host, port)) {
            return false; // Port je obsazen
        } catch (Exception ignored) {
            return true; // Port je dostupny
        }
    }

}
