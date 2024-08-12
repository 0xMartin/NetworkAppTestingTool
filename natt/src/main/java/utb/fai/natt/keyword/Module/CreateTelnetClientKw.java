package utb.fai.natt.keyword.Module;

import utb.fai.natt.spi.NATTKeyword;
import utb.fai.natt.spi.NATTKeyword.ParamValType;
import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.InvalidSyntaxInConfigurationException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;
import utb.fai.natt.core.VariableProcessor;
import utb.fai.natt.module.TelnetClient;

/**
 * Umoznuje definovat modul pro telnet klienta
 */
@NATTAnnotation.Keyword(
    name = "create_telnet_client",
    description = "Creates a module that launches a new virtual Telnet client.",
    parameters = { "name", "host", "port" },
    types = { ParamValType.STRING, ParamValType.STRING, ParamValType.LONG },
    kwGroup = "NATT Module"
    )
public class CreateTelnetClientKw extends NATTKeyword {

    protected String moduleName;
    protected String host;
    protected Long port;

    private TelnetClient module;

    @Override
    public boolean execute(INATTContext ctx)
            throws InternalErrorException, NonUniqueModuleNamesException {
        // zpracovani promennych v retezci
        this.moduleName = VariableProcessor.processVariables(this.moduleName);
        this.host = VariableProcessor.processVariables(this.host);

        if (this.port == null) {
            this.port = 23L;
        }
        this.module = new TelnetClient(this.moduleName, this.host, this.port.intValue());
        this.module.runModule();

        return this.module.isRunning();
    }

    @Override
    public void deleteAction(INATTContext ctx) throws InternalErrorException {
        if (this.module != null) {
            this.module.terminateModule();
        }
    }

    @Override
    public void init(INATTContext ctx) throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("name", NATTKeyword.ParamValType.STRING,
                true);
        moduleName = (String) val.getValue();

        // host (string) [neni vyzadovany]
        val = this.getParameterValue("host", NATTKeyword.ParamValType.STRING,
                false);
        host = (String) val.getValue();

        // port (long) [je vyzadovany]
        val = this.getParameterValue("port", NATTKeyword.ParamValType.LONG,
                false);
        port = (Long) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public String getDescription() {
        String message;
        if (this.module == null) {
            message = String.format("<font color=\"red\">Failed to start module with name '%s'.</font>",
                    this.moduleName);
        }
        if (this.module.isRunning()) {
            message = String.format("<font color=\"green\">The module with name '%s' is running.</font>",
                    this.moduleName);
        } else {
            message = String.format("<font color=\"red\">Failed to start module with name '%s'.</font>",
                    this.moduleName);
        }
        return super.getDescription() + "<br>" + message;
    }

}
