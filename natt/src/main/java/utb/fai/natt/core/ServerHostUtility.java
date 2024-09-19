package utb.fai.natt.core;

import java.util.ArrayList;
import java.util.List;

import utb.fai.natt.module.MQTTBroker;
import utb.fai.natt.module.SMTPEmailServer;
import utb.fai.natt.module.TelnetServer;
import utb.fai.natt.spi.IMessageListener;
import utb.fai.natt.spi.NATTModule;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

/**
 * This is simple terminal utility class for hosting a simple server for
 * purpose of interactive testing. This utility use servers defined in module
 * package.
 */
public class ServerHostUtility {

    protected NATTModule module;

    public ServerHostUtility() {
        this.module = null;
    }

    /**
     * Host a server for interactive testing.
     * 
     * @param option Option of server to host
     * 
     * @return True if server was started, false otherwise
     */
    public boolean hostServer(String option) {
        try {
            switch (option) {
                case "email-server":
                    this.module = new SMTPEmailServer("email-server", 9999);
                    break;
                case "mqtt-broker":
                    this.module = new MQTTBroker("mqtt-broker", 9999);
                    break;
                case "telnet-server":
                    this.module = new TelnetServer("telnet-server", 9999);
                    break;
                case "telnet-server-echo":
                    this.module = new TelnetServer("telnet-server-echo", 9999);
                    this.module.addMessageListener(new IMessageListener() {
                        @Override
                        public void onMessageReceived(String sender, String tag, String message) {
                            if (sender.equals("telnet-server-echo")) {
                                try {
                                    // odesle zpravu klientovi ktery ji na server poslal (tag obsahuje id klienta
                                    // pripojeneho k serveru)
                                    ((TelnetServer) module).sendSingleMessage(message, tag);
                                } catch (InternalErrorException e) {
                                }
                            }
                        }
                    });
                    break;
                case "telnet-server-broadcast":
                    this.module = new TelnetServer("telnet-server-broadcast", 9999);
                    this.module.addMessageListener(new IMessageListener() {
                        @Override
                        public void onMessageReceived(String sender, String tag, String message) {
                            if (sender.equals("telnet-server-broadcast")) {
                                try {
                                    // odesle prijatou zpravu vsem klientu pripojenym k serveru
                                    module.sendMessage(message);
                                } catch (InternalErrorException e) {
                                }
                            }
                        }
                    });
                    break;
                default:
                    System.out.println("Unknown server option.");
                    return false;
            }
            if (module != null) {
                this.module.runModule();
            }
        } catch (NonUniqueModuleNamesException | InternalErrorException e) {
            e.printStackTrace();
            return false;
        }
        return true;
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
        return opts;
    }

    /**
     * Terminate the server.
     */
    public void terminateServer() {
        if (this.module != null) {
            this.module.terminateModule();
        }
    }

}
