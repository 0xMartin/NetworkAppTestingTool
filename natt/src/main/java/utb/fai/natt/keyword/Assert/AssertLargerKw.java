package utb.fai.natt.keyword.Assert;

import utb.fai.natt.spi.NATTKeyword;
import utb.fai.natt.spi.NATTKeyword.ParamValType;
import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.spi.NATTAssert;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.InvalidSyntaxInConfigurationException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

import utb.fai.natt.core.NATTContext;
import utb.fai.natt.spi.NATTLogger;
import utb.fai.natt.core.VariableProcessor;

/**
 * Umoznuje definovat trzeni ze ciselna hodnota definovane promnenne je vetsi
 * jako hodnota zadaneho cisla
 */
@NATTAnnotation.Keyword(
    name = "assert_larger",
    description = "Checks if a numeric variable is larger than the specified value.",
    parameters = {"var_name", "value", "result"},
    types = {ParamValType.STRING, ParamValType.DOUBLE, ParamValType.BOOLEAN},
    kwGroup = "NATT Assert"
    )
public class AssertLargerKw extends NATTKeyword {

    private NATTLogger logger = new NATTLogger(AssertEqualsKw.class);

    protected String varName;
    protected Double value;
    protected Boolean result;

    // finalni stav tvrzeni (bude pouzito pri informativni zprave v reportu)
    private boolean finalStatus;

    @Override
    public boolean execute(INATTContext ctx)
            throws InternalErrorException, NonUniqueModuleNamesException {
        this.finalStatus = false;

        // zpracovani promennych v retezci
        this.varName = VariableProcessor.processVariables(this.varName);

        if (result == null) {
            this.result = true;
        }

        String varValue = ctx.getVariable(varName);
        if (varValue == null) {
            logger.warning(String.format("Assertion failed. Variable '%s' not found!", varName));
            return false;
        }

        this.finalStatus = NATTAssert.assertNumberCondition(varValue, value, ">");

        // normalizace na podle ocekavaneho vysledku
        this.finalStatus = (this.finalStatus == this.result);
        
        if (!this.finalStatus) {
            logger.warning(String.format(
                    "Assertion failed. %s was expected as the result. Condition: (Value of variable is '%s' is larger than '%f')",
                    this.result ? "True" : "False", varValue, this.value));
        }

        return this.finalStatus;
    }

    @Override
    public void init(INATTContext ctx) throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // var_name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("var_name", NATTKeyword.ParamValType.STRING,
                true);
        varName = (String) val.getValue();

        // value (double) [je vyzadovany]
        val = this.getParameterValue("value", NATTKeyword.ParamValType.DOUBLE,
                true);
        value = (Double) val.getValue();

        // result (boolean) [neni vyzadovany]
        val = this.getParameterValue("result", NATTKeyword.ParamValType.BOOLEAN,
                false);
        result = (Boolean) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction(INATTContext ctx) throws InternalErrorException {
    }

    @Override
    public String getDescription() {
        String varValue = NATTContext.instance().getVariable(varName);
        if (varValue == null) {
            varValue = "";
        }
        String data = varValue.replaceAll("<", "&lt;").replaceAll(">", "&gt;");

        String message;
        if (this.finalStatus) {
            message = String.format(
                    "<font color=\"green\">Assertion succeeded. <b>%s</b> was expected as the result. Condition: (Value of variable is <b>'%s'</b> is larger than <b>'%f'</b>)</font>",
                    this.result ? "True" : "False", data, this.value);
        } else {
            message = String.format(
                    "<font color=\"red\">Assertion failed. <b>%s</b> was expected as the result. Condition: (Value of variable is <b>'%s'</b> is larger than <b>'%f'</b>)</font>",
                    this.result ? "True" : "False", data, this.value);
        }

        return super.getDescription() + "<br>" + message;
    }

}
