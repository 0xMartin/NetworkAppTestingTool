package utb.fai.Module;

import utb.fai.Core.NATTModule;
import utb.fai.Core.PortChecker;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.NonUniqueModuleNamesException;

import java.util.Properties;

import io.moquette.broker.Server;
import io.moquette.broker.config.MemoryConfig;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.NATTContext;
import utb.fai.Core.NATTLogger;

/**
 * Modul umoznuje spustit MQTT broker
 */
@NATTAnnotation.Module("mqtt-broker")
public class MQTTBroker extends NATTModule {

    protected NATTLogger logger = new NATTLogger(MQTTBroker.class);

    private final Server server;
    private int port;

    public MQTTBroker(String name, int port) throws NonUniqueModuleNamesException, InternalErrorException {
        super(name);
        if (port < 0) {
            throw new InternalErrorException("Invalid port number");
        }
        this.server = new Server();
        this.port = port;
    }

    @Override
    public void runModule() throws InternalErrorException {
        if (!PortChecker.isPortAvailable("localhost", this.port)) {
            throw new InternalErrorException(
                    String.format("Failed to start MQTT broker because port %d is already taken", this.port));
        }

        MemoryConfig memoryConfig = new MemoryConfig(new Properties());
        memoryConfig.setProperty("port", String.valueOf(this.port));
        memoryConfig.setProperty("host", "0.0.0.0");
        try {
            server.startServer(memoryConfig);
            logger.info(super.getNameForLogger() + "MQTT broker is running on port: " + server.getPort());
            super.setRunning(true);
        } catch (Exception e) {
            throw new InternalErrorException("Failed to start MQTT broker. Error message: " + e.getMessage());
        }
    }

    @Override
    public boolean terminateModule() {
        // odstraneni tohoto modulu z aktivnich modulu
        NATTContext.instance().getModules().remove(this);
        super.setRunning(false);
        this.server.stopServer();
        logger.info(String.format(super.getNameForLogger() + "MQTT broker [%s] terminated", this.getName()));
        return true;
    }

    @Override
    public boolean sendMessage(String message) throws InternalErrorException {
        return false;
    }

}
