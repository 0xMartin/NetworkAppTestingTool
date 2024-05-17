package utb.fai.Keyword.AppControll;

import utb.fai.Core.ExternalProgramRunner;
import utb.fai.Core.Keyword;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.NATTContext;
import utb.fai.Core.VariableProcessor;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;
import utb.fai.Exception.TestedAppFailedToRunException;

/**
 * Umoznuje odeslat textovou zpravu na standartni stream spustene externi
 * testovane aplikace
 */
@NATTAnnotation.Keyword(name = "standard_stream_send")
public class StandardStreamSendKw extends Keyword {

    protected String message;

    private boolean status;

    @Override
    public boolean execute()
            throws InternalErrorException, TestedAppFailedToRunException, NonUniqueModuleNamesException {
        this.status = false;
         
        // zpracovani promennych v retezci
        this.message = VariableProcessor.processVariables(this.message);

        ExternalProgramRunner runner = (ExternalProgramRunner) NATTContext.instance()
                .getModule(ExternalProgramRunner.NAME);
        if (runner == null) {
            return false;
        }

        this.status = runner.sendMessageToExternalProgram(message, true);
        return this.status;
    }

    @Override
    protected void keywordInit() throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue(Keyword.DEFAULT_PARAMETER_NAME, Keyword.ParameterValueType.STRING,
                true);
        message = (String) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction() throws InternalErrorException {
    }

    @Override
    public String getDescription() {
        String message;
        if (this.status) {
            message = String.format(
                    "<font color=\"green\">Message was successfully sent on standard stream of external application. Message content:<br><b>%s</b></font>",
                    this.message);
        } else {
            message = String.format("<font color=\"red\">Failed to send message on standard stream of external application.</font>");
        }
        return super.getDescription() + "<br>" + message;
    }

}
