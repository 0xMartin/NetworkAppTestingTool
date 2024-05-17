package utb.fai.Keyword.Module;

import utb.fai.Core.Keyword;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.VariableProcessor;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;
import utb.fai.Exception.TestedAppFailedToRunException;
import utb.fai.Module.RESTTester;

/**
 * Umoznuje definovat modul pro testovani REST API
 */
@NATTAnnotation.Keyword(name = "create_rest_tester")
public class CreateRESTTesterKw extends Keyword {

    protected String moduleName;
    protected String url;
    protected String requestType;
    protected String contentType;

    private RESTTester module;

    @Override
    public boolean execute()
            throws InternalErrorException, TestedAppFailedToRunException, NonUniqueModuleNamesException {
        // zpracovani promennych v retezci
        this.moduleName = VariableProcessor.processVariables(this.moduleName);
        this.url = VariableProcessor.processVariables(this.url);
        this.requestType = VariableProcessor.processVariables(this.requestType);

        this.module = new RESTTester(this.moduleName, this.url, this.requestType, this.contentType);
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

        // request_type (String) [je vyzadovany]
        val = this.getParameterValue("request_type", Keyword.ParameterValueType.STRING,
                true);
        requestType = (String) val.getValue();

        // content_type (String) [neni vyzadovany]
        val = this.getParameterValue("content_type", Keyword.ParameterValueType.STRING,
                false);
        contentType = (String) val.getValue();
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
