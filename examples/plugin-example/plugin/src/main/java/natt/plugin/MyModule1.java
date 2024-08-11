package natt.plugin;

import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.NATTModule;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

/**
 * This is your module. Modules are used to send and receive messages. Is not needed to register it in your plugin's main class. 
 * Plugins are used by keywords. After creating an instance of a module, its reference is automatically inserted into the NATTContext
 * and you can access it from several different keywords using this method "ctx.getModule(name)".
 */
public class MyModule1 extends NATTModule {

    public MyModule1(String name, INATTContext ctx) throws NonUniqueModuleNamesException, InternalErrorException {
        super(name, ctx);
    }

    @Override
    public void runModule() throws InternalErrorException {
        
    }

    @Override
    public boolean sendMessage(String arg0) throws InternalErrorException {
    }

    @Override
    public boolean terminateModule() {
    }
    
}
