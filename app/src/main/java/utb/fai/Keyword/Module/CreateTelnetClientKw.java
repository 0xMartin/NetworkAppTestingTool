package utb.fai.Keyword.Module;

import utb.fai.Core.Keyword;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.VariableProcessor;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;
import utb.fai.Exception.TestedAppFailedToRunException;
import utb.fai.Module.TelnetClient;

/**
 * Umoznuje definovat modul pro telnet klienta
 */
@NATTAnnotation.Keyword(name = "create_telnet_client")
public class CreateTelnetClientKw extends Keyword {

    protected String moduleName;
    protected String host;
    protected Long port;

    private TelnetClient module;

    @Override
    public boolean execute()
            throws InternalErrorException, TestedAppFailedToRunException, NonUniqueModuleNamesException {
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
    public void deleteAction() throws InternalErrorException {
        if(this.module != null) {
            this.module.terminateModule();
        }
    }

    @Override
    protected void keywordInit() throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("name", Keyword.ParameterValueType.STRING,
                true);
        moduleName = (String) val.getValue();

        // host (string) [neni vyzadovany]
        val = this.getParameterValue("host", Keyword.ParameterValueType.STRING,
                false);
        host = (String) val.getValue();

        // port (long) [je vyzadovany]
        val = this.getParameterValue("port", Keyword.ParameterValueType.LONG,
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
