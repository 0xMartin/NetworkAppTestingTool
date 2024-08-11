package utb.fai.natt.keyword.Module;

import java.util.ArrayList;
import java.util.List;

import utb.fai.natt.spi.NATTKeyword;
import utb.fai.natt.spi.NATTKeyword.ParamValType;
import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.InvalidSyntaxInConfigurationException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

import utb.fai.natt.core.VariableProcessor;
import utb.fai.natt.module.MQTTClientTester;

/**
 * Umoznuje definovat modul testovaciho MQTT klienta
 */
@NATTAnnotation.Keyword(
    name = "create_mqtt_client",
    description = "Creates a module that launches a virtual MQTT client.",
    parameters = { "name", "topics", "broker_url" },
    types = { ParamValType.STRING, ParamValType.LIST, ParamValType.STRING },
    kwGroup = "NATT Module"
    )
public class CreateMQTTClientKw extends NATTKeyword {

    protected String moduleName;
    protected List<String> topics;
    protected String brokerURL;

    private MQTTClientTester module;

    @Override
    public boolean execute(INATTContext ctx)
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
    public void deleteAction(INATTContext ctx) throws InternalErrorException {
        if (this.module != null) {
            this.module.terminateModule();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void init(INATTContext ctx) throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("name", NATTKeyword.ParamValType.STRING,
                true);
        moduleName = (String) val.getValue();

        // topics (list<string) [neni vyzadovany]
        val = this.getParameterValue("topics", NATTKeyword.ParamValType.LIST,
                false);
        topics = (List<String>) val.getValue();

        // broker_url (list<string) [neni vyzadovany]
        val = this.getParameterValue("broker_url", NATTKeyword.ParamValType.STRING,
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
