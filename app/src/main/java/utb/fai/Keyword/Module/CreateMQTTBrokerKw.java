package utb.fai.Keyword.Module;

import utb.fai.Core.Keyword;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.VariableProcessor;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;
import utb.fai.Exception.TestedAppFailedToRunException;
import utb.fai.Module.MQTTBroker;

/**
 * Umoznuje definovat modul pro spusteni lokalnitho MQTT brokeru
 */
@NATTAnnotation.Keyword(name = "create_mqtt_broker")
public class CreateMQTTBrokerKw extends Keyword {

    protected String moduleName;
    protected Long port;

    private MQTTBroker module;

    @Override
    public boolean execute()
            throws InternalErrorException, TestedAppFailedToRunException, NonUniqueModuleNamesException {
        // zpracovani promennych v retezci
        this.moduleName = VariableProcessor.processVariables(this.moduleName);

        if (this.port == null) {
            this.port = 1883L;
        }
        this.module = new MQTTBroker(this.moduleName, this.port.intValue());
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

        // port (long) [je vyzadovany]
        val = this.getParameterValue("port", Keyword.ParameterValueType.LONG,
                true);
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
