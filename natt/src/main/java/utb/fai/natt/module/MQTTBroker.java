package utb.fai.natt.module;

import utb.fai.natt.spi.NATTModule;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

import utb.fai.natt.core.NATTContext;
import utb.fai.natt.spi.NATTLogger;
import utb.fai.natt.core.PortChecker;

import java.util.Properties;

import io.moquette.broker.Server;
import io.moquette.broker.config.MemoryConfig;

/**
 * Modul umoznuje spustit MQTT broker
 */
public class MQTTBroker extends NATTModule {

    protected NATTLogger logger = new NATTLogger(MQTTBroker.class);

    private final Server server;
    private int port;

    public MQTTBroker(String name, int port) throws NonUniqueModuleNamesException, InternalErrorException {
        super(name, NATTContext.instance());
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
