package utb.fai.natt.keyword.Assert;

import utb.fai.natt.spi.NATTKeyword;
import utb.fai.natt.spi.NATTModule;
import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.InvalidSyntaxInConfigurationException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

import utb.fai.natt.core.NATTContext;
import utb.fai.natt.spi.NATTLogger;
import utb.fai.natt.core.VariableProcessor;

/**
 * Umoznuje definovat trzeni ze konktretni modul je aktivni
 */
@NATTAnnotation.Keyword(name = "assert_module_is_running")
public class AssertModuleIsRunningKw extends NATTKeyword {

    private NATTLogger logger = new NATTLogger(AssertAppIsRunningKw.class);

    protected String moduleName;
    protected Boolean result;

    // finalni stav tvrzeni (bude pouzito pri informativni zprave v reportu)
    private boolean finalStatus;

    @Override
    public boolean execute(INATTContext ctx)
            throws InternalErrorException, NonUniqueModuleNamesException {
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
    public void keywordInit(INATTContext ctx) throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // module_name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("module_name", NATTKeyword.ParameterValueType.STRING,
                true);
        moduleName = (String) val.getValue();

        // result (boolean) [neni vyzadovany]
        val = this.getParameterValue("result", NATTKeyword.ParameterValueType.BOOLEAN,
                false);
        result = (Boolean) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction(INATTContext ctx) throws InternalErrorException {
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
