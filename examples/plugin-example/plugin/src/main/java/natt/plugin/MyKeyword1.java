package natt.plugin;

import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.NATTKeyword;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.InvalidSyntaxInConfigurationException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

/**
 * This is your keyword. You must register it in your plugin's main class that implements INATTPlugin.
 */
public class MyKeyword1 extends NATTKeyword {

    @Override
    public void deleteAction(INATTContext ctx) throws InternalErrorException {
        throw new UnsupportedOperationException("Unimplemented method 'deleteAction'");
    }

    @Override
    public boolean execute(INATTContext ctx) throws InternalErrorException, NonUniqueModuleNamesException {
        throw new UnsupportedOperationException("Unimplemented method 'execute'");
    }

    @Override
    public void keywordInit(INATTContext ctx) throws InvalidSyntaxInConfigurationException {
        throw new UnsupportedOperationException("Unimplemented method 'keywordInit'");
    }
    
}
