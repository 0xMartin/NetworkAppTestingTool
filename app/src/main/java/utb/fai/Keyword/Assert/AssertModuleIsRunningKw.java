package utb.fai.Keyword.Assert;

import utb.fai.Core.Keyword;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.NATTContext;
import utb.fai.Core.NATTLogger;
import utb.fai.Core.NATTModule;
import utb.fai.Core.VariableProcessor;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;
import utb.fai.Exception.TestedAppFailedToRunException;

/**
 * Umoznuje definovat trzeni ze konktretni modul je aktivni
 */
@NATTAnnotation.Keyword(name = "assert_module_is_running")
public class AssertModuleIsRunningKw extends Keyword {

    private NATTLogger logger = new NATTLogger(AssertAppIsRunningKw.class);

    protected String moduleName;
    protected Boolean result;

    // finalni stav tvrzeni (bude pouzito pri informativni zprave v reportu)
    private boolean finalStatus;

    @Override
    public boolean execute()
            throws InternalErrorException, TestedAppFailedToRunException, NonUniqueModuleNamesException {
        // zpracovani promennych v retezci
        this.moduleName = VariableProcessor.processVariables(this.moduleName);

        if (result == null) {
            this.result = true;
        }


        NATTModule module = NATTContext.instance().getModule(this.moduleName);
        if (module == null) {
            logger.warning(String.format("Assertion failed. Module '%s' not found!", this.moduleName));
            this.finalStatus = false;
        } else {
            this.finalStatus = module.isRunning();
        }

        // normalizace na podle ocekavaneho vysledku
        this.finalStatus = (this.finalStatus == this.result);

        if (!this.finalStatus) {
            logger.warning(String.format(
                    "Assertion failed. %s was expected as the result. Condition: (Module with name '%s' is running)",
                    this.result ? "True" : "False", this.moduleName));
        }

        return this.finalStatus;
    }

    @Override
    protected void keywordInit() throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // module_name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("module_name", Keyword.ParameterValueType.STRING,
                true);
        moduleName = (String) val.getValue();

        // result (boolean) [neni vyzadovany]
        val = this.getParameterValue("result", Keyword.ParameterValueType.BOOLEAN,
                false);
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
                    "<font color=\"green\">Assertion succeeded. <b>%s</b> was expected as the result. Condition: (Module with name <b>'%s'</b> is running)</font>",
                    this.result ? "True" : "False", this.moduleName);
        } else {
            message = String.format(
                    "<font color=\"red\">Assertion failed. <b>%s</b> was expected as the result. Condition: (Module with name <b>'%s'</b> is running)</font>",
                    this.result ? "True" : "False", this.moduleName);
        }
        return super.getDescription() + "<br>" + message;
    }

}
