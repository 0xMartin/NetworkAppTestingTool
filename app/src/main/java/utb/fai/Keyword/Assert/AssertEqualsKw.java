package utb.fai.Keyword.Assert;

import utb.fai.Core.Keyword;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.NATTAssert;
import utb.fai.Core.NATTContext;
import utb.fai.Core.NATTLogger;
import utb.fai.Core.VariableProcessor;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;
import utb.fai.Exception.TestedAppFailedToRunException;

/**
 * Umoznuje definovat trzeni ze ciselna hodnota definovane promnenne splnuje
 * podminku rovnosti se zadanym cislem
 */
@NATTAnnotation.Keyword(name = "assert_equals")
public class AssertEqualsKw extends Keyword {

    private NATTLogger logger = new NATTLogger(AssertEqualsKw.class);

    protected String varName;
    protected Double value;
    protected Double tolerance;
    protected Boolean result;

    // finalni stav tvrzeni (bude pouzito pri informativni zprave v reportu)
    private boolean finalStatus;

    @Override
    public boolean execute()
            throws InternalErrorException, TestedAppFailedToRunException, NonUniqueModuleNamesException {
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
    protected void keywordInit() throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // var_name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("var_name", Keyword.ParameterValueType.STRING,
                true);
        varName = (String) val.getValue();

        // value (double) [je vyzadovany]
        val = this.getParameterValue("value", Keyword.ParameterValueType.DOUBLE,
                true);
        value = (Double) val.getValue();

        // tolerance (double) [neni vyzadovany]
        val = this.getParameterValue("tolerance", Keyword.ParameterValueType.DOUBLE,
                false);
        tolerance = (Double) val.getValue();

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
