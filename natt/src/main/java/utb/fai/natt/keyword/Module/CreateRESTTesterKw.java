package utb.fai.natt.keyword.Module;

import utb.fai.natt.spi.NATTKeyword;
import utb.fai.natt.spi.NATTKeyword.ParamValType;
import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.InvalidSyntaxInConfigurationException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

import utb.fai.natt.core.VariableProcessor;
import utb.fai.natt.module.RESTTester;

/**
 * Umoznuje definovat modul pro testovani REST API
 */
@NATTAnnotation.Keyword(
    name = "create_rest_tester",
    description = "Creates a module that launches an HTTP client for testing REST APIs.",
    parameters = { "name", "url", "request_type", "content_type" },
    types = { ParamValType.STRING, ParamValType.STRING, ParamValType.STRING, ParamValType.STRING },
    kwGroup = "NATT Module"
    )
public class CreateRESTTesterKw extends NATTKeyword {

    protected String moduleName;
    protected String url;
    protected String requestType;
    protected String contentType;

    private RESTTester module;

    @Override
    public boolean execute(INATTContext ctx)
            throws InternalErrorException, NonUniqueModuleNamesException {
        // zpracovani promennych v retezci
        this.moduleName = VariableProcessor.processVariables(this.moduleName);
        this.url = VariableProcessor.processVariables(this.url);
        this.requestType = VariableProcessor.processVariables(this.requestType);

        this.module = new RESTTester(this.moduleName, this.url, this.requestType, this.contentType);
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

        // url (string) [je vyzadovany]
        val = this.getParameterValue("url", NATTKeyword.ParamValType.STRING,
                true);
        url = (String) val.getValue();

        // request_type (String) [je vyzadovany]
        val = this.getParameterValue("request_type", NATTKeyword.ParamValType.STRING,
                true);
        requestType = (String) val.getValue();

        // content_type (String) [neni vyzadovany]
        val = this.getParameterValue("content_type", NATTKeyword.ParamValType.STRING,
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
