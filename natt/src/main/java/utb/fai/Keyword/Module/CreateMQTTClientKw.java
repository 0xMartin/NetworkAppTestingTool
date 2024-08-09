package utb.fai.Keyword.Module;

import java.util.ArrayList;
import java.util.List;

import utb.fai.Core.Keyword;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.VariableProcessor;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;
import utb.fai.Module.MQTTClientTester;

/**
 * Umoznuje definovat modul testovaciho MQTT klienta
 */
@NATTAnnotation.Keyword(name = "create_mqtt_client")
public class CreateMQTTClientKw extends Keyword {

    protected String moduleName;
    protected List<String> topics;
    protected String brokerURL;

    private MQTTClientTester module;

    @Override
    public boolean execute()
            throws InternalErrorException, NonUniqueModuleNamesException {
        // zpracovani promennych v retezci
        this.moduleName = VariableProcessor.processVariables(this.moduleName);
        this.brokerURL = VariableProcessor.processVariables(this.brokerURL);
        List<String> topicsUpdated = new ArrayList<String>();
        for (String topic : this.topics) {
            topicsUpdated.add(VariableProcessor.processVariables(topic));
        }

        this.module = new MQTTClientTester(this.moduleName, this.brokerURL, topicsUpdated);
        this.module.runModule();
        
        return this.module.isRunning();
    }

    @Override
    public void deleteAction() throws InternalErrorException {
        if(this.module != null) {
            this.module.terminateModule();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void keywordInit() throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("name", Keyword.ParameterValueType.STRING,
                true);
        moduleName = (String) val.getValue();

        // topics (list<string) [neni vyzadovany]
        val = this.getParameterValue("topics", Keyword.ParameterValueType.LIST,
                false);
        topics = (List<String>) val.getValue();

        // broker_url (list<string) [neni vyzadovany]
        val = this.getParameterValue("broker_url", Keyword.ParameterValueType.STRING,
                false);
        brokerURL = (String) val.getValue();
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
