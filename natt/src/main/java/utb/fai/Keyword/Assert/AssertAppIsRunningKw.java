package utb.fai.Keyword.Assert;

import utb.fai.Core.Keyword;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.NATTContext;
import utb.fai.Core.NATTLogger;
import utb.fai.Core.VariableProcessor;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;
import utb.fai.Module.ExternalProgramRunner;

/**
 * Umoznuje definovat trzeni ze je externi testovana aplikace spustena
 */
@NATTAnnotation.Keyword(name = "assert_app_is_running")
public class AssertAppIsRunningKw extends Keyword {

    private NATTLogger logger = new NATTLogger(AssertAppIsRunningKw.class);

    protected Boolean result;
    protected String moduleName;

    // finalni stav tvrzeni (bude pouzito pri informativni zprave v reportu)
    private boolean finalStatus;

    @Override
    public boolean execute()
            throws InternalErrorException, NonUniqueModuleNamesException {
        if (result == null) {
            this.result = true;
        }

        // zpracovani promennych v retezci
        this.moduleName = VariableProcessor.processVariables(this.moduleName);

        // zjisteni stavu behu aplikace
        ExternalProgramRunner runner = (ExternalProgramRunner) NATTContext.instance()
                .getModule(this.moduleName == null ? "default" : this.moduleName);
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
        ParameterValue val = this.getParameterValue(new String[]{"result", Keyword.DEFAULT_PARAMETER_NAME}, Keyword.ParameterValueType.BOOLEAN,
                true);
        result = (Boolean) val.getValue();

        // (string) [neni vyzadovany]
        val = this.getParameterValue("name", Keyword.ParameterValueType.STRING,
                false);
        moduleName = (String) val.getValue();

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
