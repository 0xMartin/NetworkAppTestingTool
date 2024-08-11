package utb.fai.natt.keyword.AppControll;

import utb.fai.natt.spi.NATTKeyword;
import utb.fai.natt.spi.NATTKeyword.ParamValType;
import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.InvalidSyntaxInConfigurationException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

import utb.fai.natt.core.NATTContext;
import utb.fai.natt.core.VariableProcessor;
import utb.fai.natt.module.ExternalProgramRunner;

/**
 * Umoznuje odeslat textovou zpravu na standartni stream spustene externi
 * testovane aplikace
 */
@NATTAnnotation.Keyword(
    name = "standard_stream_send",
    description = "Sends a message to the running application via standard streaming.",
    parameters = {"message", "name"},
    types = {ParamValType.STRING, ParamValType.STRING}
)
public class StandardStreamSendKw extends NATTKeyword {

    protected String message;
    protected String moduleName;

    private boolean status;

    @Override
    public boolean execute(INATTContext ctx)
            throws InternalErrorException, NonUniqueModuleNamesException {
        this.status = false;

        // zpracovani promennych v retezci
        this.message = VariableProcessor.processVariables(this.message);
        this.moduleName = VariableProcessor.processVariables(this.moduleName);

        ExternalProgramRunner runner = (ExternalProgramRunner) NATTContext.instance()
                .getModule(moduleName == null ? "default": moduleName);
        if (runner == null) {
            return false;
        }

        this.status = runner.sendMessageToExternalProgram(message, true);
        return this.status;
    }

    @Override
    public void init(INATTContext ctx) throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue(new String[] { "message", NATTKeyword.DEFAULT_PARAMETER_NAME },
                NATTKeyword.ParamValType.STRING,
                true);
        message = (String) val.getValue();

        // (string) [neni vyzadovany]
        val = this.getParameterValue("name", NATTKeyword.ParamValType.STRING,
                false);
        moduleName = (String) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction(INATTContext ctx) throws InternalErrorException {
    }

    @Override
    public String getDescription() {
        String message;
        if (this.status) {
            message = String.format(
                    "<font color=\"green\">Message was successfully sent on standard stream of external application. Message content: <b>'%s'</b></font>",
                    this.message);
        } else {
            message = String.format(
                    "<font color=\"red\">Failed to send message on standard stream of external application.</font>");
        }
        return super.getDescription() + "<br>" + message;
    }

}
