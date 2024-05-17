package utb.fai.Keyword.Assert;

import utb.fai.Core.ExternalProgramRunner;
import utb.fai.Core.Keyword;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.NATTContext;
import utb.fai.Core.NATTLogger;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;
import utb.fai.Exception.TestedAppFailedToRunException;

/**
 * Umoznuje definovat trzeni ze je externi testovana aplikace spustena
 */
@NATTAnnotation.Keyword(name = "assert_app_is_running")
public class AssertAppIsRunningKw extends Keyword {

    private NATTLogger logger = new NATTLogger(AssertAppIsRunningKw.class);

    protected Boolean result;

    // finalni stav tvrzeni (bude pouzito pri informativni zprave v reportu)
    private boolean finalStatus;

    @Override
    public boolean execute()
            throws InternalErrorException, TestedAppFailedToRunException, NonUniqueModuleNamesException {
        if (result == null) {
            this.result = true;
        }

        ExternalProgramRunner runner = (ExternalProgramRunner) NATTContext.instance()
                .getModule(ExternalProgramRunner.NAME);
        if (runner == null) {
            logger.warning("Assertion failed. External program runner not found!");
            return false;
        }

        this.finalStatus = runner.isProcessRunning();

        // normalizace na podle ocekavaneho vysledku
        this.finalStatus = (this.finalStatus == this.result);

        if (!this.finalStatus) {
            logger.warning(String.format(
                    "Assertion failed. %s was expected as the result. Condition: (External tested application is running)",
                    this.result ? "True" : "False"));
        }

        return this.finalStatus;
    }

    @Override
    protected void keywordInit() throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // (boolean) [je vyzadovany]
        ParameterValue val = this.getParameterValue(Keyword.DEFAULT_PARAMETER_NAME, Keyword.ParameterValueType.BOOLEAN,
                true);
        result = (Boolean) val.getValue();

        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction() throws InternalErrorException {
    }

    @Override
    public String getDescription() {
        String message;
        if (this.finalStatus) {
            message = String.format(
                    "<font color=\"green\">Assertion succeeded. <b>%s</b> was expected as the result. Condition: (External application is running)</font>",
                    this.result ? "True" : "False");
        } else {
            message = String.format(
                    "<font color=\"red\">Assertion failed. <b>%s</b> was expected as the result. Condition: (External application is not running)</font>",
                    this.result ? "True" : "False");
        }
        return super.getDescription() + "<br>" + message;
    }

}
