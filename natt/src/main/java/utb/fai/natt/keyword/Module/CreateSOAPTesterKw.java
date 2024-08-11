package utb.fai.natt.keyword.Module;

import utb.fai.natt.spi.NATTKeyword;
import utb.fai.natt.spi.NATTKeyword.ParamValType;
import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.InvalidSyntaxInConfigurationException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

import utb.fai.natt.core.VariableProcessor;
import utb.fai.natt.module.SOAPTester;

/**
 * Umoznuje definovat modul pro testovani SOAP weboce sluzby
 */
@NATTAnnotation.Keyword(
    name = "create_soap_tester",
    description = "Creates a module for testing SOAP services.",
    parameters = { "name", "url" },
    types = { ParamValType.STRING, ParamValType.STRING }
    )
public class CreateSOAPTesterKw extends NATTKeyword {

    protected String moduleName;
    protected String url;

    private SOAPTester module;

    @Override
    public boolean execute(INATTContext ctx)
            throws InternalErrorException, NonUniqueModuleNamesException {
        // zpracovani promennych v retezci
        this.moduleName = VariableProcessor.processVariables(this.moduleName);
        this.url = VariableProcessor.processVariables(this.url);

        this.module = new SOAPTester(this.moduleName, this.url);
        this.module.runModule();

        return this.module.isRunning();
    }

    @Override
    public void deleteAction(INATTContext ctx) throws InternalErrorException {
        if(this.module != null) {
            this.module.terminateModule();
        }
    }

    @Override
    public void keywordInit(INATTContext ctx) throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("name", NATTKeyword.ParamValType.STRING,
                true);
        moduleName = (String) val.getValue();

        // url (string) [je vyzadovany]
        val = this.getParameterValue("url", NATTKeyword.ParamValType.STRING,
                true);
        url = (String) val.getValue();
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
