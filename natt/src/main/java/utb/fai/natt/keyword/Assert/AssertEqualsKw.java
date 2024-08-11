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
 * Umoznuje definovat trzeni ze ciselna hodnota definovane promnenne splnuje
 * podminku rovnosti se zadanym cislem
 */
@NATTAnnotation.Keyword(
    name = "assert_equals",
    description = "Checks if a variable is equal to the specified value. It's possible to set a certain tolerance range.",
    parameters = {"var_name", "value", "tolerance", "result"},
    types = {ParamValType.STRING, ParamValType.DOUBLE, ParamValType.DOUBLE, ParamValType.BOOLEAN}
    )
public class AssertEqualsKw extends NATTKeyword {

    private NATTLogger logger = new NATTLogger(AssertEqualsKw.class);

    protected String varName;
    protected Double value;
    protected Double tolerance;
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
        if(tolerance == null) {
            tolerance = 0.0;
        }

        String varValue = NATTContext.instance().getVariable(varName);
        if (varValue == null) {
            logger.warning(String.format("Assertion failed. Variable '%s' not found!", varName));
            return false;
        }

        this.finalStatus = NATTAssert.assertNumberEqualWithTolerance(varValue, value, tolerance);

        // normalizace na podle ocekavaneho vysledku
        this.finalStatus = (this.finalStatus == this.result);

        if (!this.finalStatus) {
            logger.warning(String.format(
                    "Assertion failed. %s was expected as the result. Condition: (Expected value '%f' must be same as the value of the variable '%s')",
                    this.result ? "True" : "False", this.value, varValue));
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

        // tolerance (double) [neni vyzadovany]
        val = this.getParameterValue("tolerance", NATTKeyword.ParamValType.DOUBLE,
                false);
        tolerance = (Double) val.getValue();

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
                    "<font color=\"green\">Assertion succeeded. <b>%s</b> was expected as the result. Condition: (Expected value <b>'%f'</b> must be same as the value of the variable <b>'%s'</b>)</font>",
                    this.result ? "True" : "False", this.value, data);
        } else {
            message = String.format(
                    "<font color=\"red\">Assertion failed. <b>%s</b> was expected as the result. Condition: (Expected value <b>'%f'</b> must be same as the value of the variable <b>'%s'</b>)</font>",
                    this.result ? "True" : "False", this.value, data);
        }

        return super.getDescription() + "<br>" + message;
    }

}
