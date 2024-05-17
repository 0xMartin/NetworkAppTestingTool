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
 * Umoznuje definovat trzeni ze ciselna hodnota definovane promnenne je mensi
 * jako hodnota zadaneho cisla
 */
@NATTAnnotation.Keyword(name = "assert_lower")
public class AssertLowerKw extends Keyword {

    private NATTLogger logger = new NATTLogger(AssertEqualsKw.class);

    protected String varName;
    protected Double value;
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

        String varValue = NATTContext.instance().getVariable(varName);
        if (varValue == null) {
            logger.warning(String.format("Assertion failed. Variable '%s' not found!", varName));
            return false;
        }

        this.finalStatus = NATTAssert.assertNumberCondition(varValue, value, "<");

        // normalizace na podle ocekavaneho vysledku
        this.finalStatus = (this.finalStatus == this.result);
        
        if (!this.finalStatus) {
            logger.warning(String.format(
                    "Assertion failed. %s was expected as the result. Condition: (Value of variable is '%s' is not lower than '%f')",
                    this.result ? "True" : "False", varValue, this.value));
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
                    "<font color=\"green\">Assertion succeeded. <b>%s</b> was expected as the result. Condition: (Value of variable is <b>'%s'</b> is not lower than <b>'%f'</b>)</font>",
                    this.result ? "True" : "False", data, this.value);
        } else {
            message = String.format(
                    "<font color=\"red\">Assertion failed. <b>%s</b> was expected as the result. Condition: (Value of variable is <b>'%s'</b> is not lower than <b>'%f'</b>)</font>",
                    this.result ? "True" : "False", data, this.value);
        }

        return super.getDescription() + "<br>" + message;
    }

}
