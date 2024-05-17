package utb.fai.Keyword.Module;

import utb.fai.Core.Keyword;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.VariableProcessor;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;
import utb.fai.Exception.TestedAppFailedToRunException;
import utb.fai.Module.SOAPTester;

/**
 * Umoznuje definovat modul pro testovani SOAP weboce sluzby
 */
@NATTAnnotation.Keyword(name = "create_soap_tester")
public class CreateSOAPTesterKw extends Keyword {

    protected String moduleName;
    protected String url;

    private SOAPTester module;

    @Override
    public boolean execute()
            throws InternalErrorException, TestedAppFailedToRunException, NonUniqueModuleNamesException {
        // zpracovani promennych v retezci
        this.moduleName = VariableProcessor.processVariables(this.moduleName);
        this.url = VariableProcessor.processVariables(this.url);

        this.module = new SOAPTester(this.moduleName, this.url);
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

        // url (string) [je vyzadovany]
        val = this.getParameterValue("url", Keyword.ParameterValueType.STRING,
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
