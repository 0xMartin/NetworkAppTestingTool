package utb.fai.natt.core;

import java.util.ArrayList;
import java.util.List;

/**
 * This is simple terminal utility class for hosting a simple server for
 * purpose of interactive testing. This utility use servers defined in module
 * package.
 */
public class ServerHostUtility {

    public ServerHostUtility() {

    }

    /**
     * Host a server for interactive testing.
     * 
     * @param option Option of server to host
     */
    public void hostServer(String option) {

    }

    /**
     * Get the list of available servers that can be hosted.
     * 
     * @return List of options
     */
    public List<String> getHostOptions() {
        List<String> opts = new ArrayList<String>();
        opts.add("email-server");
        opts.add("mqtt-broker");
        opts.add("telnet-server");
        opts.add("telnet-server-echo");
        opts.add("telnet-server-broadcast");
        opts.add("http-server");
        return opts;
    }

    /**
     * Terminate the server.
     */
    public void terminateServer() {
    }

}
