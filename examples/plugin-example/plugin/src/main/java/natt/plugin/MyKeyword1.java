package natt.plugin;

import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.NATTKeyword;
import utb.fai.natt.spi.NATTKeyword.ParamValType;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.InvalidSyntaxInConfigurationException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

/**
 * This is your keyword. You must register it in your plugin's main class that
 * implements INATTPlugin.
 * 
 * Format of this example keyword in yaml test configuration:
 * 
 * my_keyword_1: "module-1"
 * 
 * or
 * 
 * my_keyword_1:
 * name: "module-1"
 * 
 * This keyword create custom module only.
 */
@NATTAnnotation.Keyword(
    name = "my_keyword_1",
    description = "This is my first keyword.",
    parameters = { "name" },
    types = { ParamValType.STRING },
    kwGroup = PluginMain.NAME
    )
public class MyKeyword1 extends NATTKeyword {

    protected String moduleName;

    private MyModule1 module;

    @Override
    public void init(INATTContext ctx) throws InvalidSyntaxInConfigurationException {
        // load keyword parameter value. name of parameter is "name" or
        // DEFAULT_PARAMETER_NAME (DEFAULT_PARAMETER_NAME = in yaml is no need to
        // specify parameter name, like this my_keyword_1: "module-1")
        ParameterValue val = this.getParameterValue(new String[] { NATTKeyword.DEFAULT_PARAMETER_NAME, "name" },
            ParamValType.STRING, true);
        if (val != null) {
            moduleName = val.getValue().toString();
        }
    }

    @Override
    public boolean execute(INATTContext ctx) throws InternalErrorException, NonUniqueModuleNamesException {
        // create module
        this.module = new MyModule1(moduleName, ctx);
        return true;
    }

    @Override
    public void deleteAction(INATTContext ctx) throws InternalErrorException {
        // terminate module
        if (this.module != null) {
            this.module.terminateModule();
        }
    }

}
